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
import net.sourceforge.stripes.action.DontValidate;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.exception.StripesServletException;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.validation.BooleanTypeConverter;
import net.sourceforge.stripes.validation.ValidationMethod;
import net.sourceforge.stripes.validation.Validatable;
import net.sourceforge.stripes.validation.ValidationError;
import net.sourceforge.stripes.validation.ValidationErrorHandler;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationState;

import javax.servlet.ServletException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.JspFactory;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servlet that controls how requests to the Stripes framework are processed.  Uses an instance of
 * the ActionResolver interface to locate the bean and method used to handle the current request and
 * then delegates processing to the bean.
 *
 * @author Tim Fennell
 */
public class DispatcherServlet extends HttpServlet {
    /**
     * Configuration key used to lookup up a property that determines whether or not beans'
     * custom validate() method gets invoked when validation errors are generated during
     * the binding process
     */
    public static final String RUN_CUSTOM_VALIDATION_WHEN_ERRORS =
            "Validation.InvokeValidateWhenErrorsExist";

    private Boolean alwaysInvokeValidate;

    /** A place to hide a page context object so that we can get access to EL classes. */
    private static ThreadLocal<PageContext> pageContextStash = new ThreadLocal<PageContext>();

    /**
     * A Map that is used to cache the validation method that are discovered for each
     * ActionBean.  Entries are added to this map the first time that a request is made
     * to a particular ActionBean.  The Map will contain a zero length array for ActionBeans
     * that do not have any valiation methods.
     */
    private final Map<Class,Method[]> customValidations = new ConcurrentHashMap<Class, Method[]>();

    /** Log used throughout the class. */
    private static Log log = Log.getInstance(DispatcherServlet.class);

    /** Implemented as a simple call to doPost(request, response). */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        doPost(request, response);
    }

    /**
     * <p>Invokes the following instance level methods in order to coordinate the processing
     * of requests:</p>
     *
     * <ul>
     *   <li>{@link #resolveActionBean(ExecutionContext)}</li>
     *   <li>{@link #resolveHandler(ExecutionContext)}</li>
     *   <li>{@link #doBindingAndValidation(ExecutionContext, boolean)}</li>
     *   <li>{@link #doCustomValidation(ExecutionContext, boolean)}</li>
     *   <li>{@link #handleValidationErrors(ExecutionContext)}</li>
     *   <li>{@link #invokeEventHandler(ExecutionContext)}</li>
     * </ul>
     *
     * <p>If any of the above methods return a {@link Resolution} the rest of the request processing
     * is aborted and the resolution is executed.</p>
     *
     * @param request the HttpServletRequest handed to the class by the container
     * @param response the HttpServletResponse paired to the request
     * @throws ServletException thrown when the system fails to process the request in any way
     */
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
        throws ServletException {

        // It sucks that we have to do this here (in the request cycle), but there doesn't
        // seem to be a good way to get at the Configuration from the Filter in init()
        doOneTimeConfiguration();

        ///////////////////////////////////////////////////////////////////////
        // Here beings the reall processing of the request!
        ///////////////////////////////////////////////////////////////////////
        log.trace("Dispatching request to URL: ", request.getRequestURI());

        try {
            final Configuration config = StripesFilter.getConfiguration();

            // First manufacture an ActionBeanContext
            final ActionBeanContext context =
                    config.getActionBeanContextFactory().getContextInstance(request, response);

            // Then setup the ExecutionContext that we'll use to process this request
            final ExecutionContext ctx = new ExecutionContext();
            ctx.setInterceptors(config.getInterceptors(LifecycleStage.ActionBeanResolution));
            ctx.setLifecycleStage(LifecycleStage.ActionBeanResolution);
            ctx.setActionBeanContext(context);

            // Resolve the ActionBean, and if an interceptor returns a resolution, bail now
            Resolution resolution = resolveActionBean(ctx);

            if (resolution == null) {
                resolution = resolveHandler(ctx);

                if (resolution == null) {
                    // Find out if we're validating for this event or not
                    final boolean doValidate = (ctx.getHandler().getAnnotation(DontValidate.class) == null);

                    // Then run binding and validation
                    resolution = doBindingAndValidation(ctx, doValidate);

                    if (resolution == null) {
                        // Then continue on to custom validation
                        resolution = doCustomValidation(ctx, doValidate);

                        if (resolution == null) {
                            // And then validation error handling
                            resolution = handleValidationErrors(ctx);

                            if (resolution == null) {
                                // And finally(ish) invoking of the event handler
                                resolution = invokeEventHandler(ctx);

                                // If the event produced errors, fill them in
                                fillInValidationErrors(context.getValidationErrors(),
                                                       context.getRequest());
                            }
                        }
                    }
                }
            }

            // Whatever stage it came from, execute the resolution
            if (resolution != null) {
                resolution.execute(request, response);
            }
        }
        catch (ServletException se) { throw se; }
        catch (RuntimeException re) { throw re; }
        catch (InvocationTargetException ite) {
            if (ite.getTargetException() instanceof ServletException) {
                throw (ServletException) ite.getTargetException();
            }
            else if (ite.getTargetException() instanceof RuntimeException) {
                throw (RuntimeException) ite.getTargetException();
            }
            else {
                throw new StripesServletException
                    ("ActionBean execution threw an exception.", ite.getTargetException());
            }
        }
        catch (Exception e) {
            throw new StripesServletException("Exception encountered processing request.", e);
        }
    }

    /**
     * Performs a simple piece of one time configuration that requires access to the
     * Configuration object delivered through the Stripes Filter.
     */
    private void doOneTimeConfiguration() {
        if (alwaysInvokeValidate == null) {
            // Check to see if, in this application, validate() methods should always be run
            // even when validation errors already exist
            String callValidateWhenErrorsExist = StripesFilter.getConfiguration()
                .getBootstrapPropertyResolver().getProperty(RUN_CUSTOM_VALIDATION_WHEN_ERRORS);

            if (callValidateWhenErrorsExist != null) {
                BooleanTypeConverter c = new BooleanTypeConverter();
                this.alwaysInvokeValidate = c.convert(callValidateWhenErrorsExist, Boolean.class, null);
            }
            else {
                this.alwaysInvokeValidate = false; // Default behaviour
            }
        }
    }


    /**
     * Responsible for resolving the ActionBean for this request and setting it on the
     * ExecutionContext. If no ActionBean can be found the ActionResolver will throw an
     * exception, thereby aborting the current request.
     *
     * @param ctx the ExecutionContext being used to process the current request
     * @return a Resolution if any interceptor determines that the request processing should
     *         be aborted in favor of another Resolution, null otherwise.
     */
    protected Resolution resolveActionBean(final ExecutionContext ctx) throws Exception {
        return  ctx.wrap( new Interceptor() {
            public Resolution intercept(ExecutionContext ctx) throws Exception {
                // Look up the ActionBean and set it on the context
                ActionBeanContext context = ctx.getActionBeanContext();
                ActionBean bean = StripesFilter.getConfiguration().getActionResolver().getActionBean(context);
                ctx.setActionBean(bean);

                // Then register it in the Request as THE ActionBean for this request
                HttpServletRequest request = context.getRequest();
                request.setAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN, bean);
                return null;
            }
        });
    }

    /**
     * Responsible for resolving the event name for this request and setting it on the
     * ActionBeanContext contained within the ExecutionContext. Once the event name has
     * been determined this method must resolve the handler method. If a handler method
     * cannot be determined an exception should be thrown to abort processing.
     *
     * @param ctx the ExecutionContext being used to process the current request
     * @return a Resolution if any interceptor determines that the request processing should
     *         be aborted in favor of another Resolution, null otherwise.
     */
    protected Resolution resolveHandler(final ExecutionContext ctx) throws Exception {
        final Configuration config = StripesFilter.getConfiguration();
        ctx.setLifecycleStage(LifecycleStage.HandlerResolution);
        ctx.setInterceptors(config.getInterceptors(LifecycleStage.HandlerResolution));

        return ctx.wrap( new Interceptor() {
            public Resolution intercept(ExecutionContext ctx) throws Exception {
                ActionBean bean = ctx.getActionBean();
                ActionBeanContext context = ctx.getActionBeanContext();
                ActionResolver resolver = config.getActionResolver();

                // Then lookup the event name and handler method etc.
                String eventName = resolver.getEventName(bean.getClass(), context);
                context.setEventName(eventName);

                final Method handler;
                if (eventName != null) {
                    handler = resolver.getHandler(bean.getClass(), eventName);
                }
                else {
                    handler = resolver.getDefaultHandler(bean.getClass());
                    if (handler != null) {
                        context.setEventName(resolver.getHandledEvent(handler));
                    }
                }

                // Insist that we have a handler
                if (handler == null) {
                    throw new StripesServletException(
                            "No handler method found for request with  ActionBean [" +
                            bean.getClass().getName() + "] and eventName [ " + eventName + "]");
                }

                log.debug("Resolved event: ", context.getEventName(), "; will invoke: ",
                          bean.getClass().getSimpleName(), ".", handler.getName(), "()");

                ctx.setHandler(handler);
                return null;
            }
        });
    }

    /**
     * Responsible for performing binding and validation. Once properties have been bound and
     * validation complete then they ValidationErrors object must be accessible through the
     * ActionBeanContext contained within the ExecutionContext (regardless of whether any
     * errors were generated or not).
     *
     * @param ctx the ExecutionContext being used to process the current request
     * @param doValidate true if validations should be applied, false if only binding and
     *        type conversion should occur
     * @return a Resolution if any interceptor determines that the request processing should
     *         be aborted in favor of another Resolution, null otherwise.
     */
    protected Resolution doBindingAndValidation(final ExecutionContext ctx,
                                                final boolean doValidate) throws Exception {
        // Bind the value to the bean - this includes performing field level validation
        final Configuration config = StripesFilter.getConfiguration();
        ctx.setLifecycleStage(LifecycleStage.BindingAndValidation);
        ctx.setInterceptors(config.getInterceptors(LifecycleStage.BindingAndValidation));

        // It's unclear whether this usage of the JspFactory will work in all containers. It looks
        // like it should, but still, we should be careful not to screw up regular request
        // processing if it should fail. Why do we do this?  So we can have a container-agnostic
        // way of getting an ExpressionEvaluator to do expression based validation
        PageContext pageContext = null;
        try {
            ActionBeanContext abc = ctx.getActionBeanContext();
            pageContext = JspFactory.getDefaultFactory().getPageContext(this, // the servlet inst
                                                                        abc.getRequest(), // req
                                                                        abc.getResponse(), // res
                                                                        null,   // error page url
                                                                        true,   // need session
                                                                        abc.getResponse().getBufferSize(),
                                                                        true); // autoflush
            DispatcherServlet.pageContextStash.set(pageContext);
        }
        catch (Exception e) {
            // Don't even log this, this failure gets reported if action beans actually
            // try and make use of expression validation, otherwise this is just noise
        }

        try {
            return ctx.wrap( new Interceptor() {
                public Resolution intercept(ExecutionContext ctx) throws Exception {
                    ActionBeanPropertyBinder binder = config.getActionBeanPropertyBinder();
                    binder.bind(ctx.getActionBean(), ctx.getActionBeanContext(), doValidate);
                    return null;
                }
            });
        }
        finally {
            if (pageContext != null) {
                DispatcherServlet.pageContextStash.set(null);
                JspFactory.getDefaultFactory().releasePageContext(pageContext);
            }
        }
    }

    /**
     * Fetches the page context object stored on the local thread *if* the dispatcher was
     * able to create one.  It's package-access only for now because it's really only meant
     * for the OgnlActionBeanPropertyBinder and it may not always work!
     */
    static PageContext getPageContext() {
        return DispatcherServlet.pageContextStash.get();
    }

    /**
     * Responsible for coordinating the invocation of any custom validation logic exposed
     * by the ActionBean.  Will only call the validation methods if certain conditions are
     * met (there are no errors, or the always call validate flag is set etc.).
     *
     * @param ctx the ExecutionContext being used to process the current request
     * @param doValidate true if validations should be applied, false if only binding and
     *        type conversion should occur
     * @return a Resolution if any interceptor determines that the request processing should
     *         be aborted in favor of another Resolution, null otherwise.
     */
    protected Resolution doCustomValidation(final ExecutionContext ctx, boolean doValidate) throws Exception {
        final ValidationErrors errors = ctx.getActionBeanContext().getValidationErrors();
        final ActionBean bean = ctx.getActionBean();
        Configuration config = StripesFilter.getConfiguration();

        // Run the bean's validate() method if the following conditions are met:
        //   1. This event is not marked to bypass validation (doValidate == true)
        //   2. The bean is an instance of Validatable
        //   3. We have no errors so far OR alwaysInvokeValidate is true
        if (doValidate) {

            ctx.setLifecycleStage(LifecycleStage.CustomValidation);
            ctx.setInterceptors(config.getInterceptors(LifecycleStage.CustomValidation));

            return ctx.wrap( new Interceptor() {
                public Resolution intercept(ExecutionContext context) throws Exception {

                    // Run the legacy style validate() method
                    if ( (alwaysInvokeValidate || errors.isEmpty()) && bean instanceof Validatable) {
                        ((Validatable) bean).validate(errors);
                    }

                    // Run any of the new style validation methods
                    Method[] validations = findCustomValidationMethods(bean.getClass());
                    for (Method validation : validations) {
                        ValidationMethod ann = validation.getAnnotation(ValidationMethod.class);

                        boolean run = (ann.when() == ValidationState.ALWAYS)
                                   || (ann.when() == ValidationState.DEFAULT && alwaysInvokeValidate)
                                   || errors.isEmpty();

                        if (run && applies(ann, ctx.getActionBeanContext().getEventName())) {
                            Class[] args = validation.getParameterTypes();
                            if (args.length == 1 && args[0].equals(ValidationErrors.class)) {
                                validation.invoke(bean, errors);
                            }
                            else {
                                validation.invoke(bean);
                            }
                        }
                    }

                    return null;
                }
            });
        }
        else {
            return null;
        }
    }

    /**
     * <p>Determines if the ValidationMethod annotation should be applied to the named
     * event.  True if the list of events to apply the validation to is empty, or it
     * contains the event name, or it contains event names starting with "!" but not the
     * event name. Some examples to illustrate the point</p>
     *
     * <ul>
     *   <li>info.on={}, event="save" => true</li>
     *   <li>info.on={"save", "update"}, event="save" => true</li>
     *   <li>info.on={"save", "update"}, event="delete" => false</li>
     *   <li>info.on={"!delete"}, event="save" => true</li>
     *   <li>info.on={"!delete"}, event="delete" => false</li>
     * </ul>
     *
     * @param info the ValidationMethod being examined
     * @param event the event being processed
     * @return true if the custom validation should be applied to this event, false otherwise
     */
    protected boolean applies(ValidationMethod info, String event) {
        final String[] events = info.on();

        if (events.length == 0 || event == null) {
            return true;
        }
        else if (events[0].startsWith("!")) {
            return !contains(events, "!" + event);
        }
        else {
            return contains(events, event);
        }
    }

    /** A quick utility method for linear searching an array of Strings. */
    private boolean contains(String[] arr, String target) {
        for (String item : arr) {
            if (item.equals(target)) {
                return true;
            }
        }

        return false;
    }


    /**
     * Finds and returns all methods in the ActionBean class and it's superclasses that
     * are marked with the ValidationMethod annotation and returns them ordered by
     * priority (and alphabetically within priorities).  Looks first in an instance level
     * cache, and if that does not contain information for an ActionBean, examines the
     * ActionBean and adds the information to the cache.
     *
     * @param type a Class representing an ActionBean
     * @return a Method[] containing all methods marked as custom validations. May return
     *         an empty array, but never null.
     */
    protected Method[] findCustomValidationMethods(Class<? extends ActionBean> type) throws Exception {
        Method[] validations = this.customValidations.get(type);

        // Lazily examine the ActionBean and collect this information
        if (validations == null) {
            // A sorted set with a custom comparator that will order the methods in
            // the set based upon the priority in their custom validation annotation
            SortedSet<Method> validationMethods = new TreeSet<Method>( new Comparator<Method>() {
                public int compare(Method o1, Method o2) {
                    // If one of the methods overrides the others, return equal!
                    if (o1.getName().equals(o2.getName()) &&
                            Arrays.equals(o1.getParameterTypes(), o2.getParameterTypes())) {
                        return 0;
                    }

                    ValidationMethod ann1 = o1.getAnnotation(ValidationMethod.class);
                    ValidationMethod ann2 = o2.getAnnotation(ValidationMethod.class);
                    int returnValue =  new Integer(ann1.priority()).compareTo(ann2.priority());

                    if (returnValue == 0) {
                        returnValue = o1.getName().compareTo(o2.getName());
                    }

                    return returnValue;
                }
            });

            Class temp = type;
            while ( temp != null ) {
                for (Method method : temp.getDeclaredMethods()) {
                    if (method.getAnnotation(ValidationMethod.class) != null) {
                        validationMethods.add(method);
                    }
                }

                temp = temp.getSuperclass();
            }

            validations = validationMethods.toArray(new Method[validationMethods.size()]);
            this.customValidations.put(type, validations);
        }

        return validations;
    }

    /**
     * Responsible for checking to see if validation errors exist and if so for handling
     * them appropriately.  This includes ensuring that the error objects have all information
     * necessary to render themselves appropriately and invoking any error handling code.
     *
     * @param ctx the ExecutionContext being used to process the current request
     *        type conversion should occur
     * @return a Resolution if the error handling code determines that some kind of resolution
     *         should be processed in favor of continuing on to handler invocation
     */
    protected Resolution handleValidationErrors(ExecutionContext ctx) throws Exception {
        ActionBean bean            = ctx.getActionBean();
        ActionBeanContext context  = ctx.getActionBeanContext();
        ValidationErrors errors    = context.getValidationErrors();
        HttpServletRequest request = context.getRequest();
        Resolution resolution = null;

        // If we have errors, add the action path to them
        fillInValidationErrors(errors, request);

        // Now if we have errors and the bean wants to handle them...
        if (errors.size() > 0 && bean instanceof ValidationErrorHandler) {
            resolution = ((ValidationErrorHandler) bean).handleValidationErrors(errors);
        }

        // If there are still errors see if we need to lookup the resolution
        if (errors.size() > 0 && resolution == null) {
            resolution  = context.getSourcePageResolution();
        }

        return resolution;
    }

    /**
     * Makes sure that validation errors have all the necessary information to render
     * themselves properly, including the UrlBinding of the action bean and the field
     * value if it hasn't already been set.
     *
     * @param errors the ValidationErrors object for the request
     * @param request the current request
     */
    protected void fillInValidationErrors(ValidationErrors errors, HttpServletRequest request) {
        if (errors.size() > 0) {
            String formAction = (String) request.getAttribute(ActionResolver.RESOLVED_ACTION);

            /** Since we don't pass form action down the stack, we add it to the errors here. */
            for (Map.Entry<String, List<ValidationError>> entry : errors.entrySet()) {
                String parameterName = entry.getKey();
                List<ValidationError> listOfErrors = entry.getValue();

                for (ValidationError error : listOfErrors) {
                    error.setActionPath(formAction);

                    // This is done to fill in parameter values for any errors the user
                    // created and didn't add values to
                    if (error.getFieldValue() == null) {
                        error.setFieldValue(request.getParameter(parameterName));
                    }
                }
            }
        }
    }

    /**
     * Responsible for invoking the event handler identified.  This method will only be
     * called if an event handler was identified, and can assume that the bean and handler
     * are present in the ExecutionContext.
     *
     * @param ctx the ExecutionContext being used to process the current request
     *        type conversion should occur
     * @return a Resolution if the error handling code determines that some kind of resolution
     *         should be processed in favor of continuing on to handler invocation
     */
    protected Resolution invokeEventHandler(ExecutionContext ctx) throws Exception {
        final Configuration config = StripesFilter.getConfiguration();
        final Method handler = ctx.getHandler();
        final ActionBean bean = ctx.getActionBean();

        // Finally execute the handler method!
        ctx.setLifecycleStage(LifecycleStage.EventHandling);
        ctx.setInterceptors(config.getInterceptors(LifecycleStage.EventHandling));

        return ctx.wrap( new Interceptor() {
            public Resolution intercept(ExecutionContext ctx) throws Exception {
                Object returnValue = handler.invoke(bean);

                if (returnValue != null && returnValue instanceof Resolution) {
                    return (Resolution) returnValue;
                }
                else if (returnValue != null) {
                    log.warn("Expected handler method ", handler.getName(), " on class ",
                             bean.getClass().getSimpleName(), " to return a Resolution. Instead it ",
                             "returned: ", returnValue);
                }

                return null;
            }
        });
    }

    /**
     * Responsible for executing the Resolution returned by the request. Transitions the
     * execution context to {@link LifecycleStage#ResolutionExecution}, sets the resolution
     * on the execution context and then invokes the interceptor chain to execute the
     * Resolution.
     *
     * @param ctx the current execution context representing the request
     * @param resolution the resolution to be executed unless another is substituted by an
     *        interceptor before calling ctx.proceed()
     */
    protected void executeResolution(ExecutionContext ctx, Resolution resolution) throws Exception {
        final Configuration config = StripesFilter.getConfiguration();

        ctx.setLifecycleStage(LifecycleStage.ResolutionExecution);
        ctx.setInterceptors(config.getInterceptors(LifecycleStage.ResolutionExecution));
        ctx.setResolution(resolution);

        ctx.wrap( new Interceptor() {
            public Resolution intercept(ExecutionContext context) throws Exception {
                ActionBeanContext abc = context.getActionBeanContext();
                Resolution resolution = context.getResolution();

                if (resolution != null) {
                    resolution.execute(abc.getRequest(), abc.getResponse());
                }

                return null;
            }
        });
    }
}