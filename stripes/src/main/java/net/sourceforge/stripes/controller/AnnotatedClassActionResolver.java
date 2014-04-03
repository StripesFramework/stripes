/* Copyright 2005-2006 Tim Fennell
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sourceforge.stripes.controller;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.SessionScope;
import net.sourceforge.stripes.config.BootstrapPropertyResolver;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.config.DontAutoLoad;
import net.sourceforge.stripes.exception.ActionBeanNotFoundException;
import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.exception.StripesServletException;
import net.sourceforge.stripes.util.HttpUtil;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.util.ResolverUtil;
import net.sourceforge.stripes.util.StringUtil;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>Uses Annotations on classes to identify the ActionBean that corresponds to the current
 * request.  ActionBeans are annotated with an {@code @UrlBinding} annotation, which denotes the
 * web application relative URL that the ActionBean should respond to.</p>
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
     * Configuration key used to lookup a comma-separated list of package names. The
     * packages (and their sub-packages) will be scanned for implementations of
     * ActionBean.
     * @since Stripes 1.5 
     */
    public static final String PACKAGES = "ActionResolver.Packages";

    /** Key used to store the default handler in the Map of handler methods. */
    private static final String DEFAULT_HANDLER_KEY = "__default_handler";

    /** Log instance for use within in this class. */
    private static final Log log = Log.getInstance(AnnotatedClassActionResolver.class);

    /** Handle to the configuration. */
    private Configuration configuration;

    /** Parses {@link UrlBinding} values and maps request URLs to {@link ActionBean}s. */
    private UrlBindingFactory urlBindingFactory = new UrlBindingFactory();

    /**
     * Map used to resolve the methods handling events within form beans. Maps the class
     * representing a subclass of ActionBean to a Map of event names to Method objects.
     */
    private Map<Class<? extends ActionBean>,Map<String,Method>> eventMappings =
        new HashMap<Class<? extends ActionBean>,Map<String,Method>>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Map<String, Method> get(Object key) {
                Map<String, Method> value = super.get(key);
                if (value == null)
                    return Collections.emptyMap();
                else
                    return value;
            }
    };

    /**
     * Scans the classpath of the current classloader (not including parents) to find implementations
     * of the ActionBean interface.  Examines annotations on the classes found to determine what
     * forms and events they map to, and stores this information in a pair of maps for fast
     * access during request processing.
     */
    public void init(Configuration configuration) throws Exception {
        this.configuration = configuration;

        // Process each ActionBean
        for (Class<? extends ActionBean> clazz : findClasses()) {
            addActionBean(clazz);
        }
    }

    /** Get the {@link UrlBindingFactory} that is being used by this action resolver. */
    public UrlBindingFactory getUrlBindingFactory() {
        return urlBindingFactory;
    }

    /**
     * Adds an ActionBean class to the set that this resolver can resolve. Identifies
     * the URL binding and the events managed by the class and stores them in Maps
     * for fast lookup.
     *
     * @param clazz a class that implements ActionBean
     */
    protected void addActionBean(Class<? extends ActionBean> clazz) {
        // Ignore abstract classes
        if (Modifier.isAbstract(clazz.getModifiers()) || clazz.isAnnotationPresent(DontAutoLoad.class))
            return;

        String binding = getUrlBinding(clazz);
        if (binding == null)
            return;

        // make sure mapping exists in cache
        UrlBinding proto = getUrlBindingFactory().getBindingPrototype(clazz);
        if (proto == null) {
            getUrlBindingFactory().addBinding(clazz, new UrlBinding(clazz, binding));
        }

        // Construct the mapping of event->method for the class
        Map<String, Method> classMappings = new HashMap<String, Method>();
        processMethods(clazz, classMappings);

        // Put the event->method mapping for the class into the set of mappings
        this.eventMappings.put(clazz, classMappings);

        if (log.getRealLog().isDebugEnabled()) {
            // Print out the event mappings nicely
            for (Map.Entry<String, Method> entry : classMappings.entrySet()) {
                String event = entry.getKey();
                Method handler = entry.getValue();
                boolean isDefault = DEFAULT_HANDLER_KEY.equals(event);

                log.debug("Bound: ", clazz.getSimpleName(), ".", handler.getName(), "() ==> ",
                        binding, isDefault ? "" : "?" + event);
            }
        }
    }

    /**
     * Removes an ActionBean class from the set that this resolver can resolve. The URL binding
     * and the events managed by the class are removed from the cache.
     *
     * @param clazz a class that implements ActionBean
     */
    protected void removeActionBean(Class<? extends ActionBean> clazz) {
        String binding = getUrlBinding(clazz);
        if (binding != null) {
            getUrlBindingFactory().removeBinding(clazz);
        }
        eventMappings.remove(clazz);
    }

    /**
     * Returns the URL binding that is a substring of the path provided. For example, if there
     * is an ActionBean bound to {@code /user/Profile.action} the path
     * {@code /user/Profile.action/view} would return {@code /user/Profile.action}.
     *
     * @param path the path being used to access an ActionBean, either in a form or link tag,
     *        or in a request that is hitting the DispatcherServlet.
     * @return the UrlBinding of the ActionBean appropriate for the request, or null if the path
     *         supplied cannot be mapped to an ActionBean.
     */
    public String getUrlBindingFromPath(String path) {
        UrlBinding mapping = getUrlBindingFactory().getBindingPrototype(path);
        return mapping == null ? null : mapping.toString();
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
        UrlBinding mapping = getUrlBindingFactory().getBindingPrototype(clazz);
        return mapping == null ? null : mapping.toString();
    }

    /**
     * Helper method that examines a class, starting at it's highest super class and
     * working it's way down again, to find method annotations and ensure that child
     * class annotations take precedence.
     */
    protected void processMethods(Class<?> clazz, Map<String,Method> classMappings) {
        // Do the super class first if there is one
        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null) {
            processMethods(superclass, classMappings);
        }

        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if ( Modifier.isPublic(method.getModifiers()) && !method.isBridge() ) {
                String eventName = getHandledEvent(method);

                // look for duplicate event names within the current class
                if (classMappings.containsKey(eventName)
                        && clazz.equals(classMappings.get(eventName).getDeclaringClass())) {
                    throw new StripesRuntimeException("The ActionBean " + clazz
                            + " declares multiple event handlers for event '" + eventName + "'");
                }

                DefaultHandler defaultMapping = method.getAnnotation(DefaultHandler.class);
                if (eventName != null) {
                    classMappings.put(eventName, method);
                }
                if (defaultMapping != null) {
                    // look for multiple default handlers within the current class
                    if (classMappings.containsKey(DEFAULT_HANDLER_KEY)
                            && clazz.equals(classMappings.get(DEFAULT_HANDLER_KEY).getDeclaringClass())) {
                        throw new StripesRuntimeException("The ActionBean " + clazz
                                + " declares multiple default event handlers");
                    }

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
     * <p>Fetches the Class representing the type of ActionBean that would respond were a
     * request made with the path specified. Checks to see if the full path matches any
     * bean's UrlBinding. If no ActionBean matches then successively removes path segments
     * (separated by slashes) from the end of the path until a match is found.</p>
     *
     * @param path the path segment of a URL
     * @return the Class object for the type of action bean that will respond if a request
     *         is made using the path specified or null if no ActionBean matches.
     */
    public Class<? extends ActionBean> getActionBeanType(String path) {
        UrlBinding binding = getUrlBindingFactory().getBindingPrototype(path);
        return binding == null ? null : binding.getBeanType();
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
        HttpServletRequest request = context.getRequest();
        String path = HttpUtil.getRequestedPath(request);
        ActionBean bean = getActionBean(context, path);
        request.setAttribute(RESOLVED_ACTION, getUrlBindingFromPath(path));
        return bean;
    }

    /**
     * Returns the ActionBean class that is bound to the UrlBinding supplied. If the action
     * bean already exists in the appropriate scope (request or session) then the existing
     * instance will be supplied.  If not, then a new instance will be manufactured and have
     * the supplied ActionBeanContext set on it.
     *
     * @param path a URL to which an ActionBean is bound, or a path starting with the URL
     *        to which an ActionBean has been bound.
     * @param context the current ActionBeanContext
     * @return a Class<ActionBean> for the ActionBean requested
     * @throws StripesServletException if the UrlBinding does not match an ActionBean binding
     */
    public ActionBean getActionBean(ActionBeanContext context, String path) throws StripesServletException {
        Class<? extends ActionBean> beanClass = getActionBeanType(path);
        ActionBean bean;

        if (beanClass == null) {
            throw new ActionBeanNotFoundException(path, getUrlBindingFactory().getPathMap());
        }

        String bindingPath = getUrlBinding(beanClass);
        try {
            HttpServletRequest request = context.getRequest();

            if (beanClass.isAnnotationPresent(SessionScope.class)) {
                bean = (ActionBean) request.getSession().getAttribute(bindingPath);

                if (bean == null) {
                    bean = makeNewActionBean(beanClass, context);
                    request.getSession().setAttribute(bindingPath, bean);
                }
            }
            else {
                bean = (ActionBean) request.getAttribute(bindingPath);
                if (bean == null) {
                    bean = makeNewActionBean(beanClass, context);
                    request.setAttribute(bindingPath, bean);
                }
            }

            setActionBeanContext(bean, context);
        }
        catch (Exception e) {
            StripesServletException sse = new StripesServletException(
                "Could not create instance of ActionBean type [" + beanClass.getName() + "].", e);
            log.error(sse);
            throw sse;
        }

        assertGetContextWorks(bean);
        return bean;

    }

    /**
     * Calls {@link ActionBean#setContext(ActionBeanContext)} with the given {@code context} only if
     * necessary. Subclasses should use this method instead of setting the context directly because
     * it can be somewhat tricky to determine when it needs to be done.
     * 
     * @param bean The bean whose context may need to be set.
     * @param context The context to pass to the bean if necessary.
     */
    protected void setActionBeanContext(ActionBean bean, ActionBeanContext context) {
        ActionBeanContext abcFromBean = bean.getContext();
        if (abcFromBean == null) {
            bean.setContext(context);
        }
        else {
            StripesRequestWrapper wrapperFromBean = StripesRequestWrapper
                    .findStripesWrapper(abcFromBean.getRequest());
            StripesRequestWrapper wrapperFromRequest = StripesRequestWrapper
                    .findStripesWrapper(context.getRequest());
            if (wrapperFromBean != wrapperFromRequest)
                bean.setContext(context);
        }
    }

    /**
     * Since many down stream parts of Stripes rely on the ActionBean properly returning the
     * context it is given, we'll just test it up front. Called after the bean is instantiated.
     *
     * @param bean the ActionBean to test to see if getContext() works correctly
     * @throws StripesServletException if getContext() returns null
     */
    protected void assertGetContextWorks(final ActionBean bean) throws StripesServletException {
        if (bean.getContext() == null) {
            throw new StripesServletException("Ahem. Stripes has just resolved and instantiated " +
                    "the ActionBean class " + bean.getClass().getName() + " and set the ActionBeanContext " +
                    "on it. However calling getContext() isn't returning the context back! Since " +
                    "this is required for several parts of Stripes to function correctly you should " +
                    "now stop and implement setContext()/getContext() correctly. Thank you.");
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

        return getConfiguration().getObjectFactory().newInstance(type);
    }


    /**
     * <p>
     * Try various means to determine which event is to be executed on the current ActionBean. If a
     * 'special' request attribute ({@link StripesConstants#REQ_ATTR_EVENT_NAME}) is present in
     * the request, then return its value. This attribute is used to handle internal forwards, when
     * request parameters are merged and cannot reliably determine the desired event name.
     * </p>
     * 
     * <p>
     * If that doesn't work, the value of a 'special' request parameter ({@link StripesConstants#URL_KEY_EVENT_NAME})
     * is checked to see if contains a single value matching an event name.
     * </p>
     * 
     * <p>
     * Failing that, search for a parameter in the request whose name matches one of the named
     * events handled by the ActionBean. For example, if the ActionBean can handle events foo and
     * bar, this method will scan the request for foo=somevalue and bar=somevalue. If it finds a
     * request parameter with a matching name it will return that name. If there are multiple
     * matching names, the result of this method cannot be guaranteed and a
     * {@link StripesRuntimeException} will be thrown.
     * </p>
     * 
     * <p>
     * Finally, if the event name cannot be determined through the parameter names and there is
     * extra path information beyond the URL binding of the ActionBean, it is checked to see if it
     * matches an event name.
     * </p>
     * 
     * @param bean the ActionBean type bound to the request
     * @param context the ActionBeanContect for the current request
     * @return String the name of the event submitted, or null if none can be found
     */
    public String getEventName(Class<? extends ActionBean> bean, ActionBeanContext context) {
        String event = getEventNameFromRequestAttribute(bean, context);
        if (event == null) event = getEventNameFromEventNameParam(bean, context);
        if (event == null) event = getEventNameFromRequestParams(bean, context);
        if (event == null) event = getEventNameFromPath(bean, context);
        return event;
    }

    /**
     * Checks a special request attribute to get the event name. This attribute
     * may be set when the presence of the original request parameters on a
     * forwarded request makes it difficult to determine which event to fire.
     * 
     * @param bean the ActionBean type bound to the request
     * @param context the ActionBeanContect for the current request
     * @return the name of the event submitted, or null if none can be found
     * @see StripesConstants#REQ_ATTR_EVENT_NAME
     */
    protected String getEventNameFromRequestAttribute(
            Class<? extends ActionBean> bean, ActionBeanContext context) {
        return (String) context.getRequest().getAttribute(
                StripesConstants.REQ_ATTR_EVENT_NAME);
    }

    /**
     * Loops through the set of known events for the ActionBean to see if the event
     * names are present as parameter names in the request.  Returns the first event
     * name found in the request, or null if none is found.
     *
     * @param bean the ActionBean type bound to the request
     * @param context the ActionBeanContext for the current request
     * @return String the name of the event submitted, or null if none can be found
     */
    @SuppressWarnings("unchecked")
    protected String getEventNameFromRequestParams(Class<? extends ActionBean> bean,
                                                   ActionBeanContext context) {

        List<String> eventParams = new ArrayList<String>();
        Map<String,String[]> parameterMap = context.getRequest().getParameterMap();
        for (String event : this.eventMappings.get(bean).keySet()) {
            if (parameterMap.containsKey(event) || parameterMap.containsKey(event + ".x")) {
                eventParams.add(event);
            }
        }

        if (eventParams.size() == 0) {
            return null;
        }
        else if (eventParams.size() == 1) {
            return eventParams.get(0);
        }
        else {
            throw new StripesRuntimeException("Multiple event parameters " + eventParams
                    + " are present in this request. Only one event parameter may be specified "
                    + "per request. Otherwise, Stripes would be unable to determine which event "
                    + "to execute.");
        }
    }

    /**
     * Looks to see if there is extra path information beyond simply the url binding of the
     * bean. If it does and the next /-separated part of the path matches one of the known
     * event names for the bean, that event name will be returned, otherwise null.
     *
     * @param bean the ActionBean type bound to the request
     * @param context the ActionBeanContect for the current request
     * @return String the name of the event submitted, or null if none can be found
     */
    protected String getEventNameFromPath(Class<? extends ActionBean> bean,
                                          ActionBeanContext context) {
        Map<String,Method> mappings = this.eventMappings.get(bean);
        String path = HttpUtil.getRequestedPath(context.getRequest());
        UrlBinding prototype = getUrlBindingFactory().getBindingPrototype(path);
        String binding = prototype == null ? null : prototype.getPath();

        if (binding != null && path.length() != binding.length()) {
            String extra = path.substring(binding.length() + 1);
            int index = extra.indexOf("/");
            String event = extra.substring(0, (index != -1) ? index : extra.length());
            if (mappings.containsKey(event)) {
                return event;
            }
        }

        return null;
    }

    /**
     * Looks to see if there is a single non-empty parameter value for the parameter name
     * specified by {@link StripesConstants#URL_KEY_EVENT_NAME}. If there is, and it
     * matches a known event it is returned, otherwise returns null.
     *
     * @param bean the ActionBean type bound to the request
     * @param context the ActionBeanContect for the current request
     * @return String the name of the event submitted, or null if none can be found
     */
    protected String getEventNameFromEventNameParam(Class<? extends ActionBean> bean,
                                                    ActionBeanContext context) {
        String[] values = context.getRequest().getParameterValues(StripesConstants.URL_KEY_EVENT_NAME);
        String event = null;
        if (values != null && values.length == 1 && this.eventMappings.get(bean).containsKey(values[0])) {
            event = values[0];
        }

        // Warn of non-backward-compatible behavior
        if (event != null) {
            try {
                String otherName = getEventNameFromRequestParams(bean, context);
                if (otherName != null && !otherName.equals(event)) {
                    String[] otherValue = context.getRequest().getParameterValues(otherName);
                    log.warn("The event name was specified by two request parameters: ",
                            StripesConstants.URL_KEY_EVENT_NAME, "=", event, " and ", otherName,
                            "=", Arrays.toString(otherValue), ". ", "As of Stripes 1.5, ",
                            StripesConstants.URL_KEY_EVENT_NAME,
                            " overrides all other request parameters.");
                }
            }
            catch (StripesRuntimeException e) {
                // Ignore this. It means there were too many event params, which is OK in this case.
            }
        }

        return event;
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

    /** Provides subclasses with access to the configuration object. */
    protected Configuration getConfiguration() { return this.configuration; }

    /**
     * Helper method to find implementations of ActionBean in the packages specified in
     * Configuration using the {@link ResolverUtil} class.
     *
     * @return a set of Class objects that represent subclasses of ActionBean
     */
    protected Set<Class<? extends ActionBean>> findClasses() {
        BootstrapPropertyResolver bootstrap = getConfiguration().getBootstrapPropertyResolver();

        String packages = bootstrap.getProperty(PACKAGES);
        if (packages == null) {
            throw new StripesRuntimeException(
                "You must supply a value for the configuration parameter '" + PACKAGES + "'. The " +
                "value should be a list of one or more package roots (comma separated) that are " +
                "to be scanned for ActionBean implementations. The packages specified and all " +
                "subpackages are examined for implementations of ActionBean."
            );
        }

        String[] pkgs = StringUtil.standardSplit(packages);
        ResolverUtil<ActionBean> resolver = new ResolverUtil<ActionBean>();
        resolver.findImplementations(ActionBean.class, pkgs);
        return resolver.getClasses();
    }

    /**
     * Get all the classes implementing {@link ActionBean} that are recognized by this
     * {@link ActionResolver}.
     */
    public Collection<Class<? extends ActionBean>> getActionBeanClasses() {
        return getUrlBindingFactory().getActionBeanClasses();
    }
}
