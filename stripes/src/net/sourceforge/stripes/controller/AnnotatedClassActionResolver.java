/* Copyright (C) 2005 Tim Fennell
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the license with this software. If not,
 * it can be found online at http://www.fsf.org/licensing/licenses/lgpl.html
 */
package net.sourceforge.stripes.controller;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.exception.StripesServletException;
import net.sourceforge.stripes.util.Log;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p>Uses Annotations on classes to identify the ActionBean that corresponds to the current
 * request.  ActionBeans are annotated with an @UrlBinding annotation, which denotes the
 * web application relative URL that the ActionBean should respond to.
 * that methods are capable of handling.</p>
 *
 * <p>Individual methods on ActionBean classes are expected to be annotated with @HandlesEvent
 * annotations, and potentially a @DefaultHandler annotation.  Using these annotations the
 * Resolver will determine which method should be executed for the current request.</p>
 *
 * @see net.sourceforge.stripes.action.UrlBinding
 * @author Tim Fennell
 */
public class AnnotatedClassActionResolver implements ActionResolver {
    /**
     * Configuration key used to lookup a comma-separated list of patterns that are used to
     * restrict the set of URLs in the classpath that are searched for ActionBean classes.
     */
    private static final String URL_FILTERS = "ActionResolver.UrlFilters";

    /**
     * Configuration key used to lookup a comma-separated list of patterns that are used to
     * restrict the packages that will be scanned for ActionBean classes.
     */
    private static final String PACKAGE_FILTERS = "ActionResolver.PackageFilters";

    /** Log instance for use within in this class. */
    private Log log = Log.getInstance(AnnotatedClassActionResolver.class);

    /** Handle to the configuration. */
    private Configuration configuration;

    /** Map of form names to Class objects representing subclasses of ActionBean. */
    private Map<String,Class<ActionBean>> formBeans = new HashMap<String,Class<ActionBean>>();

    /**
     * Map used to resolve the methods handling events within form beans. Maps the class
     * representing a subclass of ActionBean to a Map of event names to Method objects.
     */
    private Map<Class<ActionBean>,Map<String,Method>> eventMappings =
        new HashMap<Class<ActionBean>,Map<String,Method>>();


    /**
     * Scans the classpath of the current classloader (not including parents) to find implementations
     * of the ActionBean interface.  Examines annotations on the classes found to determine what
     * forms and events they map to, and stores this information in a pair of maps for fast
     * access during request processing.
     */
    public void init(Configuration configuration) {
        this.configuration = configuration;

        log.warn("this.configuration: " + this.configuration);
        log.warn("this.configuration.bootstrap: " + this.configuration.getBootstrapPropertyResolver());

        // Set up the ActionClassCache
        Set<String> urlFilters = new HashSet<String>();
        Set<String> packageFilters = new HashSet<String>();

        String temp = configuration.getBootstrapPropertyResolver().getProperty(URL_FILTERS);
        if (temp != null) {
            urlFilters.addAll(Arrays.asList( temp.split(",")));
        }

        temp = configuration.getBootstrapPropertyResolver().getProperty(PACKAGE_FILTERS);
        if (temp != null) {
            packageFilters.addAll(Arrays.asList( temp.split(",")));
        }

        ActionClassCache.init(urlFilters, packageFilters);

        // Use the actionResolver util to find all ActionBean implementations in the classpath
        Set<Class<ActionBean>> beans = ActionClassCache.getInstance().getActionBeanClasses();

        // Process each ActionBean
        for (Class<ActionBean> clazz : beans) {
            UrlBinding binding = clazz.getAnnotation(UrlBinding.class);

            // Only process the class if it's properly annotated
            if (binding != null) {
                this.formBeans.put(binding.value(), clazz);

                // Construct the mapping of event->method for the class
                Map<String, Method> classMappings = new HashMap<String, Method>();
                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    HandlesEvent mapping = method.getAnnotation(HandlesEvent.class);
                    DefaultHandler defaultMapping = method.getAnnotation(DefaultHandler.class);
                    if (mapping != null) {
                        classMappings.put(mapping.value(), method);
                    }
                    else if (defaultMapping != null) {
                        // Makes sure we catch the default handler
                        classMappings.put("__default_handler", method);
                    }
                }

                // Put the event->method mapping for the class into the set of mappings
                this.eventMappings.put(clazz, classMappings);
            }
        }

        log.debug("Mappings initialized: ", this.eventMappings);
    }

    /**
     * Gets the logical name of the ActionBean that should handle the request.  Implemented to look
     * up the name of the form based on the name assigned to the form in the form tag, and
     * encoded in a hidden field.
     *
     * @param context the ActionBeanContext for the current request
     * @return the name of the form to be used for this request
     */
    public Class<ActionBean> getActionBean(ActionBeanContext context) throws StripesServletException {
        // Defensively construct the URL that was used to hit the dispatcher
        HttpServletRequest request = context.getRequest();
        String servletPath = request.getServletPath();
        String pathInfo    = request.getPathInfo();
        String boundUrl = (servletPath == null ? "" : servletPath) +
                            (pathInfo == null ? "" : pathInfo);

        Class<ActionBean> beanClass = this.formBeans.get(boundUrl);

        if (beanClass == null) {
            StripesServletException sse = new StripesServletException(
                    "Could not locate an ActionBean that is bound to the URL [" + boundUrl +
                    "]. Full request URL was [" + context.getRequest().getRequestURL() +
                    "]. Commons reasons for this include mis-matched URLs and forgetting " +
                    "to implement ActionBean in your class. Registered ActionBeans are: " +
                    this.formBeans);

            log.debug(sse);
            throw sse;
        }

        request.setAttribute(RESOLVED_ACTION, boundUrl);
        return beanClass;
    }

    /**
     * Returns the ActionBean class that is bound to the UrlBinding supplied.
     *
     * @param urlBinding the URL to which the ActionBean has been bound
     * @return a Class<ActionBean> for the ActionBean requested
     * @throws StripesServletException if the UrlBinding does not match an ActionBean binding
     */
    public Class<ActionBean> getActionBean(String urlBinding) throws StripesServletException {
        Class<ActionBean> beanClass = this.formBeans.get(urlBinding);

        if (beanClass == null) {
            StripesServletException sse = new StripesServletException(
                    "Could not locate an ActionBean that is bound to the URL [" + urlBinding +
                            "]. Commons reasons for this include mis-matched URLs and forgetting " +
                            "to implement ActionBean in your class. Registered ActionBeans are: " +
                            this.formBeans);

            log.debug(sse);
            throw sse;
        }

        return beanClass;
    }


    /**
     * Searched for a parameter in the request whose name matches one of the named events handled
     * by the ActionBean.  For example, if the ActionBean can handle events foo and bar, this
     * method will scan the request for foo=somevalue and bar=somevalue.  If it find a request
     * paremeter with a matching name it will return that name.  If there are multiple matching
     * names, the result of this method cannot be guaranteed.
     *
     * @param bean the ActionBean type bound to the request
     * @param context the ActionBeanContect for the current request
     * @return String the name of the event submitted, or null if none can be found
     */
    public String getEventName(Class<ActionBean> bean, ActionBeanContext context) {
        Map<String,Method> mappings = this.eventMappings.get(bean);
        Map parameterMap = context.getRequest().getParameterMap();

        for (String event : mappings.keySet()) {
            if (parameterMap.containsKey(event) || parameterMap.containsKey(event + ".x")) {
                return event;
            }
        }

        return null;
    }


    /**
     * Uses the Maps constructed earlier to locate the Method which can handle the event.
     *
     * @param bean the subclass of ActionBean that is bound to the request.
     * @param eventName the name of the event being handled
     * @return a Method object representing the handling method.
     * @throws StripesServletException thrown when no method handles the named event.
     */
    public Method getHandler(Class<ActionBean> bean, String eventName)
        throws StripesServletException {
        Map<String,Method> mappings = this.eventMappings.get(bean);
        Method handler = mappings.get(eventName);

        // If we could not find a handler then we should blow up quickly
        if (handler == null) {
            throw new StripesServletException(
                    "Could not find handler method for event name [" + eventName + "] on class [" +
                    bean.getName() + "].  Known handler mappings are: " + mappings);
        }

        return handler;
    }

    /**
     * Returns the Method that is the default handler for events in the ActionBean class supplied.
     * If only one handler method is defined in the class, that is assumed to be the default. If
     * there is more than one, the handlers are examined in turn for the annotation DefaultHandler
     * and the first such annotated Method is returned.
     *
     * @param bean the ActionBean type bound to the request
     * @return Method object that should handle the request
     * @throws StripesServletException if no default handler could be located
     */
    public Method getDefaultHandler(Class<ActionBean> bean) throws StripesServletException {
        Collection<Method> handlers = this.eventMappings.get(bean).values();
        if (handlers.size() == 1) {
            return handlers.iterator().next();
        }
        else {
            for (Method handler : handlers) {
                if (handler.getAnnotation(DefaultHandler.class) != null) {
                    return handler;
                }
            }
        }

        // If we got this far there is no default handler
        throw new StripesServletException("No default handler could be found for ActionBean of " +
            "type: " + bean.getName());
    }
}
