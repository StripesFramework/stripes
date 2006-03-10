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
import net.sourceforge.stripes.action.SessionScope;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.exception.StripesServletException;
import net.sourceforge.stripes.util.Log;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
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

    /** Key used to store the default handler in the Map of handler methods. */
    private static final String DEFAULT_HANDLER_KEY = "__default_handler";

    /** Log instance for use within in this class. */
    private Log log = Log.getInstance(AnnotatedClassActionResolver.class);

    /** Handle to the configuration. */
    private Configuration configuration;

    /** Map of form names to Class objects representing subclasses of ActionBean. */
    private Map<String,Class<? extends ActionBean>> formBeans =
            new HashMap<String,Class<? extends ActionBean>>();

    /**
     * Map used to resolve the methods handling events within form beans. Maps the class
     * representing a subclass of ActionBean to a Map of event names to Method objects.
     */
    private Map<Class<? extends ActionBean>,Map<String,Method>> eventMappings =
        new HashMap<Class<? extends ActionBean>,Map<String,Method>>();

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
            String binding = getUrlBinding(clazz);

            // Only process the class if it's properly annotated
            if (binding != null) {
                this.formBeans.put(binding, clazz);

                // Construct the mapping of event->method for the class
                Map<String, Method> classMappings = new HashMap<String, Method>();
                processMethods(clazz, classMappings);

                // Put the event->method mapping for the class into the set of mappings
                this.eventMappings.put(clazz, classMappings);
            }
        }

        log.debug("Mappings initialized: ", this.eventMappings);
    }

    /**
     * Takes a class that implements ActionBean and returns the URL binding of that class.
     * The default implementation retrieves the UrlBinding annotations and returns its
     * value. Subclasses could do more complex things like parse the class and package names
     * and construct a "default" binding when one is not specified.
     *
     * @param clazz a class that implements ActionBean
     * @return the UrlBinding or null if none can be determined
     */
    public String getUrlBinding(Class<? extends ActionBean> clazz) {
        UrlBinding binding = clazz.getAnnotation(UrlBinding.class);
        if (binding != null) {
            return binding.value();
        }
        else {
            return null;
        }
    }

    /**
     * Helper method that examines a class, starting at it's highest super class and
     * working it's way down again, to find method annotations and ensure that child
     * class annotations take precedence.
     */
    protected void processMethods(Class clazz, Map<String,Method> classMappings) {
        // Do the super class first if there is one
        Class superclass = clazz.getSuperclass();
        if (superclass != null) {
            processMethods(superclass, classMappings);
        }

        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if ( Modifier.isPublic(method.getModifiers()) ) {
                String eventName = getHandledEvent(method);
                DefaultHandler defaultMapping = method.getAnnotation(DefaultHandler.class);
                if (eventName != null) {
                    classMappings.put(eventName, method);
                }
                if (defaultMapping != null) {
                    // Makes sure we catch the default handler
                    classMappings.put(DEFAULT_HANDLER_KEY, method);
                }
            }
        }
    }

    /**
     * Responsible for determining the name of the event handled by this method, if indeed
     * it handles one at all.  By default looks for the HandlesEvent annotations and returns
     * it's value if present.
     *
     * @param handler a method that might or might not be a handler method
     * @return the name of the event handled, or null
     */
    public String getHandledEvent(Method handler) {
        HandlesEvent mapping = handler.getAnnotation(HandlesEvent.class);
        if (mapping != null) {
            return mapping.value();
        }
        else {
            return null;
        }
    }

    /**
     * Fetches the Class representing the type of ActionBean that has been bound to
     * the URL supplied.  Will not cause any ActionBean to be instantiated. If no
     * ActionBean has been bound to the URL supplied will return null.
     *
     * @param urlBinding the url to find the bound bean type
     * @return the class object for the type of action bean bound to the url, or
     *         null if no bean is bound to that url
     */
    public Class<? extends ActionBean> getActionBeanType(String urlBinding) {
        return this.formBeans.get(urlBinding);
    }

    /**
     * Gets the logical name of the ActionBean that should handle the request.  Implemented to look
     * up the name of the form based on the name assigned to the form in the form tag, and
     * encoded in a hidden field.
     *
     * @param context the ActionBeanContext for the current request
     * @return the name of the form to be used for this request
     */
    public ActionBean getActionBean(ActionBeanContext context) throws StripesServletException {
        // Defensively construct the URL that was used to hit the dispatcher
        HttpServletRequest request = context.getRequest();
        String servletPath = request.getServletPath();
        String pathInfo    = request.getPathInfo();
        String boundUrl = (servletPath == null ? "" : servletPath) +
                            (pathInfo == null ? "" : pathInfo);

        request.setAttribute(RESOLVED_ACTION, boundUrl);
        return getActionBean(context, boundUrl);
    }

    /**
     * Returns the ActionBean class that is bound to the UrlBinding supplied.
     *
     * @param urlBinding the URL to which the ActionBean has been bound
     * @param context the current ActionBeanContext
     * @return a Class<ActionBean> for the ActionBean requested
     * @throws StripesServletException if the UrlBinding does not match an ActionBean binding
     */
    public ActionBean getActionBean(ActionBeanContext context, String urlBinding)
            throws StripesServletException {

        Class<? extends ActionBean> beanClass = this.formBeans.get(urlBinding);
        ActionBean bean;

        if (beanClass == null) {
            StripesServletException sse = new StripesServletException(
                    "Could not locate an ActionBean that is bound to the URL [" + urlBinding +
                            "]. Commons reasons for this include mis-matched URLs and forgetting " +
                            "to implement ActionBean in your class. Registered ActionBeans are: " +
                            this.formBeans);

            log.error(sse);
            throw sse;
        }

        try {
            if (beanClass.isAnnotationPresent(SessionScope.class)) {
                HttpServletRequest request = context.getRequest();
                bean = (ActionBean) request.getSession().getAttribute(urlBinding);

                if (bean == null) {
                    bean = makeNewActionBean(beanClass, context);
                    request.getSession().setAttribute(urlBinding, bean);
                }
            }
            else {
                bean = makeNewActionBean(beanClass, context);
            }

            return bean;
        }
        catch (Exception e) {
            StripesServletException sse = new StripesServletException(
                "Could not create instance of ActionBean type [" + beanClass.getName() + "].", e);
            log.error(sse);
            throw sse;
        }
    }

    /**
     * Helper method to construct and return a new ActionBean instance. Called whenever a new
     * instance needs to be manufactured.  Provides a convenient point for subclasses to add
     * specific behaviour during action bean creation.
     *
     * @param type the type of ActionBean to create
     * @param context the current ActionBeanContext
     * @return the new ActionBean instance
     * @throws Exception if anything goes wrong!
     */
    protected ActionBean makeNewActionBean(Class<? extends ActionBean> type, ActionBeanContext context)
        throws Exception {

        return type.newInstance();
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
    public String getEventName(Class<? extends ActionBean> bean, ActionBeanContext context) {
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
    public Method getHandler(Class<? extends ActionBean> bean, String eventName)
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
     * there is more than one then the method marked with @DefaultHandler will be returned.
     *
     * @param bean the ActionBean type bound to the request
     * @return Method object that should handle the request
     * @throws StripesServletException if no default handler could be located
     */
    public Method getDefaultHandler(Class<? extends ActionBean> bean) throws StripesServletException {
        Map<String,Method> handlers = this.eventMappings.get(bean);

        if (handlers.size() == 1) {
            return handlers.values().iterator().next();
        }
        else {
            Method handler = handlers.get(DEFAULT_HANDLER_KEY);
            if (handler != null) return handler;
        }

        // If we get this far, there is no sensible default!  Kaboom!
        throw new StripesServletException("No default handler could be found for ActionBean of " +
            "type: " + bean.getName());
    }
}
