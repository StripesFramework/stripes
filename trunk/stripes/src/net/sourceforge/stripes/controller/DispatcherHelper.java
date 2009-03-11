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
import net.sourceforge.stripes.action.DontBind;
import net.sourceforge.stripes.action.DontValidate;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.exception.StripesServletException;
import net.sourceforge.stripes.util.HtmlUtil;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.util.CollectionUtil;
import net.sourceforge.stripes.validation.ValidationError;
import net.sourceforge.stripes.validation.ValidationErrorHandler;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationMethod;
import net.sourceforge.stripes.validation.ValidationState;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import java.lang.reflect.Method;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.WeakHashMap;

/**
 * Helper class that contains much of the logic used when dispatching requests in Stripes.
 * Used primarily by the DispatcherSerlvet, but also by the UseActionBean tag.
 *
 * @author Tim Fennell
 */
public class DispatcherHelper {
    private static final Log log = Log.getInstance(DispatcherHelper.class);

    /**
     * A Map that is used to cache the validation method that are discovered for each
     * ActionBean.  Entries are added to this map the first time that a request is made
     * to a particular ActionBean.  The Map will contain a zero length array for ActionBeans
     * that do not have any validation methods.
     */
    private static final Map<Class<?>, WeakReference<Method[]>> customValidations =
            Collections.synchronizedMap(new WeakHashMap<Class<?>, WeakReference<Method[]>>());

    /** A place to hide a page context object so that we can get access to EL classes. */
    private static ThreadLocal<PageContext> pageContextStash = new ThreadLocal<PageContext>();

    /**
     * Should be called prior to invoking validation related methods to provide the helper
     * with access to a PageContext object that can be used to manufacture expression
     * evaluator instances.
     *
     * @param ctx a page context object supplied by the container
     */
    public static void setPageContext(PageContext ctx) {
        if (ctx == null)  pageContextStash.remove();
        else pageContextStash.set(ctx);
    }

    /**
     * Used by the validation subsystem to access a page context that can be used to
     * create an expression evaluator for use in the expression validation code.
     *
     * @return a page context object if one was set with setPageContext()
     */
    public static PageContext getPageContext() {
        return pageContextStash.get();
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
    public static Resolution resolveActionBean(final ExecutionContext ctx) throws Exception {
        final Configuration config = StripesFilter.getConfiguration();
        ctx.setLifecycleStage(LifecycleStage.ActionBeanResolution);
        ctx.setInterceptors(config.getInterceptors(LifecycleStage.ActionBeanResolution));
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
    public static Resolution resolveHandler(final ExecutionContext ctx) throws Exception {
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
     * @return a Resolution if any interceptor determines that the request processing should
     *         be aborted in favor of another Resolution, null otherwise.
     */
    public static Resolution doBindingAndValidation(final ExecutionContext ctx,
                                                    final boolean validate) throws Exception {
        // Bind the value to the bean - this includes performing field level validation
        final Method handler = ctx.getHandler();
        final boolean doBind = handler == null || handler.getAnnotation(DontBind.class) == null;
        final boolean doValidate = doBind && validate && (handler == null || handler.getAnnotation(DontValidate.class) == null);
        final Configuration config = StripesFilter.getConfiguration();

        ctx.setLifecycleStage(LifecycleStage.BindingAndValidation);
        ctx.setInterceptors(config.getInterceptors(LifecycleStage.BindingAndValidation));

        return ctx.wrap(new Interceptor() {
            public Resolution intercept(ExecutionContext ctx) throws Exception {
                if (doBind) {
                    ActionBeanPropertyBinder binder = config.getActionBeanPropertyBinder();
                    binder.bind(ctx.getActionBean(), ctx.getActionBeanContext(), doValidate);
                    fillInValidationErrors(ctx);
                }
                return null;
            }
        });
    }

    /**
     * Responsible for coordinating the invocation of any custom validation logic exposed
     * by the ActionBean.  Will only call the validation methods if certain conditions are
     * met (there are no errors, or the always call validate flag is set etc.).
     *
     * @param ctx the ExecutionContext being used to process the current request
     * @return a Resolution if any interceptor determines that the request processing should
     *         be aborted in favor of another Resolution, null otherwise.
     */
    public static Resolution doCustomValidation(final ExecutionContext ctx,
                                                final boolean alwaysInvokeValidate) throws Exception {
        final ValidationErrors errors = ctx.getActionBeanContext().getValidationErrors();
        final ActionBean bean = ctx.getActionBean();
        final Method handler = ctx.getHandler();
        final boolean doBind = handler != null && handler.getAnnotation(DontBind.class) == null;
        final boolean doValidate = doBind && handler.getAnnotation(DontValidate.class) == null;
        Configuration config = StripesFilter.getConfiguration();

        // Run the bean's methods annotated with @ValidateMethod if the following conditions are met:
        //   l. This event is not marked to bypass binding 
        //   2. This event is not marked to bypass validation (doValidate == true)
        //   3. We have no errors so far OR alwaysInvokeValidate is true
        if (doValidate) {

            ctx.setLifecycleStage(LifecycleStage.CustomValidation);
            ctx.setInterceptors(config.getInterceptors(LifecycleStage.CustomValidation));

            return ctx.wrap( new Interceptor() {
				public Resolution intercept(ExecutionContext context) throws Exception {
                    // Run any of the annotated validation methods
                    Method[] validations = findCustomValidationMethods(bean.getClass());
                    for (Method validation : validations) {
                        ValidationMethod ann = validation.getAnnotation(ValidationMethod.class);

                        boolean run = (ann.when() == ValidationState.ALWAYS)
                                   || (ann.when() == ValidationState.DEFAULT && alwaysInvokeValidate)
                                   || errors.isEmpty();

                        if (run && applies(ann, ctx.getActionBeanContext().getEventName())) {
                            Class<?>[] args = validation.getParameterTypes();
                            if (args.length == 1 && args[0].equals(ValidationErrors.class)) {
                                validation.invoke(bean, errors);
                            }
                            else {
                                validation.invoke(bean);
                            }
                        }
                    }

                    fillInValidationErrors(ctx);
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
    public static boolean applies(ValidationMethod info, String event) {
        return CollectionUtil.applies(info.on(), event);
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
    public static Method[] findCustomValidationMethods(Class<? extends ActionBean> type) throws Exception {
        Method[] validations = null;
        WeakReference<Method[]> ref = customValidations.get(type);
        if (ref != null) validations = ref.get();

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

            Class<?> temp = type;
            while ( temp != null ) {
                for (Method method : temp.getDeclaredMethods()) {
                    Class<?>[] args = method.getParameterTypes();

                    if ((method.getAnnotation(ValidationMethod.class) != null) &&
                            ((args.length == 0) || (args.length == 1 && args[0].equals(ValidationErrors.class)))) {
                        validationMethods.add(method);
                    }
                }

                temp = temp.getSuperclass();
            }

            validations = validationMethods.toArray(new Method[validationMethods.size()]);
            customValidations.put(type, new WeakReference<Method[]>(validations));
        }

        return validations;
    }

    /**
     * Responsible for checking to see if validation errors exist and if so for handling
     * them appropriately.  This includes ensuring that the error objects have all information
     * necessary to render themselves appropriately and invoking any error handling code.
     *
     * @param ctx the ExecutionContext being used to process the current request
     * @return a Resolution if the error handling code determines that some kind of resolution
     *         should be processed in favor of continuing on to handler invocation
     */
    public static Resolution handleValidationErrors(ExecutionContext ctx) throws Exception {
        DontValidate annotation = ctx.getHandler().getAnnotation(DontValidate.class);
        boolean doValidate = annotation == null || !annotation.ignoreBindingErrors();

        // If we have errors, add the action path to them
        fillInValidationErrors(ctx);

        Resolution resolution = null;
        if (doValidate) {
            ActionBean bean = ctx.getActionBean();
            ActionBeanContext context = ctx.getActionBeanContext();
            ValidationErrors errors = context.getValidationErrors();

            // Now if we have errors and the bean wants to handle them...
            if (errors.size() > 0 && bean instanceof ValidationErrorHandler) {
                resolution = ((ValidationErrorHandler) bean).handleValidationErrors(errors);
                fillInValidationErrors(ctx);
            }

            // If there are still errors see if we need to lookup the resolution
            if (errors.size() > 0 && resolution == null) {
                logValidationErrors(context);
                resolution = context.getSourcePageResolution();
            }
        }

        return resolution;
    }

    /**
     * Makes sure that validation errors have all the necessary information to render
     * themselves properly, including the UrlBinding of the action bean and the field
     * value if it hasn't already been set.
     *
     * @param ctx the ExecutionContext being used to process the current request
     */
    public static void fillInValidationErrors(ExecutionContext ctx) {
        ActionBeanContext context = ctx.getActionBeanContext();
        ValidationErrors errors = context.getValidationErrors();

        if (errors.size() > 0) {
            String formAction = StripesFilter.getConfiguration().getActionResolver()
                    .getUrlBinding(ctx.getActionBean().getClass());
            HttpServletRequest request = ctx.getActionBeanContext().getRequest();

            /** Since we don't pass form action down the stack, we add it to the errors here. */
            for (Map.Entry<String, List<ValidationError>> entry : errors.entrySet()) {
                String parameterName = entry.getKey();
                List<ValidationError> listOfErrors = entry.getValue();

                for (ValidationError error : listOfErrors) {
                    // Make sure we process each error only once, no matter how often we're called
                    if (error.getActionPath() == null) {
                        error.setActionPath(formAction);
                        error.setBeanclass(ctx.getActionBean().getClass());

                        // If the value isn't set, set it, otherwise encode the one that's there
                        if (error.getFieldValue() == null) {
                            error.setFieldValue(HtmlUtil.encode(request.getParameter(parameterName)));
                        }
                        else {
                            error.setFieldValue(HtmlUtil.encode(error.getFieldValue()));
                        }
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
    public static Resolution invokeEventHandler(ExecutionContext ctx) throws Exception {
        final Configuration config = StripesFilter.getConfiguration();
        final Method handler = ctx.getHandler();
        final ActionBean bean = ctx.getActionBean();

        // Finally execute the handler method!
        ctx.setLifecycleStage(LifecycleStage.EventHandling);
        ctx.setInterceptors(config.getInterceptors(LifecycleStage.EventHandling));

        return ctx.wrap( new Interceptor() {
            public Resolution intercept(ExecutionContext ctx) throws Exception {
                Object returnValue = handler.invoke(bean);
                fillInValidationErrors(ctx);

                if (returnValue != null && returnValue instanceof Resolution) {
                    ctx.setResolutionFromHandler(true);
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
     * execution context to {@link net.sourceforge.stripes.controller.LifecycleStage#ResolutionExecution}, sets the resolution
     * on the execution context and then invokes the interceptor chain to execute the
     * Resolution.
     *
     * @param ctx the current execution context representing the request
     * @param resolution the resolution to be executed unless another is substituted by an
     *        interceptor before calling ctx.proceed()
     */
    public static void executeResolution(ExecutionContext ctx, Resolution resolution) throws Exception {
        final Configuration config = StripesFilter.getConfiguration();

        ctx.setLifecycleStage(LifecycleStage.ResolutionExecution);
        ctx.setInterceptors(config.getInterceptors(LifecycleStage.ResolutionExecution));
        ctx.setResolution(resolution);

        Resolution retval = ctx.wrap( new Interceptor() {
            public Resolution intercept(ExecutionContext context) throws Exception {
                ActionBeanContext abc = context.getActionBeanContext();
                Resolution resolution = context.getResolution();

                if (resolution != null) {
                    resolution.execute(abc.getRequest(), abc.getResponse());
                }

                return null;
            }
        });

        if (retval != null) {
            log.warn("An interceptor wrapping LifecycleStage.ResolutionExecution returned ",
                     "a Resolution. This almost certainly did NOT have the desired effect. ",
                     "At this LifecycleStage interceptors are running *around* the actual ",
                     "execution of the Resolution, and so returning an alternate Resolution ",
                     "has the effect of stopping the original Resolution from being executed ",
                     "while NOT causing the alternate Resolution to get executed. Interceptor ",
                     "code running before the Resolution is executed (i.e. before calling ",
                     "ExecutionContext.proceed()) can alter the Resolution by calling ",
                     "ExecutionContext.setResolution() instead. Code running after the Resolution ",
                     "has been executed can no longer alter what Resolution is executed for ",
                     "what are hopefully obvious reasons!");
        }
    }

    /** Log validation errors at DEBUG to help during development. */
    public static final void logValidationErrors(ActionBeanContext context) {
        Locale locale = context.getRequest().getLocale();
        StringBuilder buf = new StringBuilder("The following validation errors need to be fixed:");
        for (List<ValidationError> list : context.getValidationErrors().values()) {
            for (ValidationError error : list) {
                String fieldName = error.getFieldName();
                if (ValidationErrors.GLOBAL_ERROR.equals(fieldName))
                    fieldName = "GLOBAL";

                buf.append("\n    -> [").append(fieldName).append("] ").append(
                        error.getMessage(locale));
            }
        }

        log.debug(buf);
    }
}
