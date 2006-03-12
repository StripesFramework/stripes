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
import net.sourceforge.stripes.action.DontValidate;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.exception.StripesServletException;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.validation.BooleanTypeConverter;
import net.sourceforge.stripes.validation.Validatable;
import net.sourceforge.stripes.validation.ValidationError;
import net.sourceforge.stripes.validation.ValidationErrorHandler;
import net.sourceforge.stripes.validation.ValidationErrors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

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
                bean.setContext(context);
                ctx.setActionBean(bean);

                // Then register it in the Request as THE ActionBean for this request
                HttpServletRequest request = context.getRequest();
                request.setAttribute((String) request.getAttribute(ActionResolver.RESOLVED_ACTION), bean);
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

        return ctx.wrap( new Interceptor() {
            public Resolution intercept(ExecutionContext ctx) throws Exception {
                ActionBeanPropertyBinder binder = config.getActionBeanPropertyBinder();

                ValidationErrors errors = binder.bind(ctx.getActionBean(),
                                                      ctx.getActionBeanContext(),
                                                      doValidate);

                ctx.getActionBeanContext().setValidationErrors(errors);
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
     * @param doValidate true if validations should be applied, false if only binding and
     *        type conversion should occur
     * @return a Resolution if any interceptor determines that the request processing should
     *         be aborted in favor of another Resolution, null otherwise.
     */
    protected Resolution doCustomValidation(ExecutionContext ctx, boolean doValidate) throws Exception {
        final ValidationErrors errors = ctx.getActionBeanContext().getValidationErrors();
        final ActionBean bean = ctx.getActionBean();
        Configuration config = StripesFilter.getConfiguration();

        // Run the bean's validate() method if the following conditions are met:
        //   1. This event is not marked to bypass validation (doValidate == true)
        //   2. The bean is an instance of Validatable
        //   3. We have no errors so far OR alwaysInvokeValidate is true
        if ( doValidate && (this.alwaysInvokeValidate || errors.size() == 0) && bean instanceof Validatable) {

            ctx.setLifecycleStage(LifecycleStage.CustomValidation);
            ctx.setInterceptors(config.getInterceptors(LifecycleStage.CustomValidation));

            return ctx.wrap( new Interceptor() {
                public Resolution intercept(ExecutionContext context) throws Exception {
                    ((Validatable) bean).validate(errors);
                    return null;
                }
            });
        }
        else {
            return null;
        }
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
        if (errors.size() > 0) {
            String formAction = (String) request.getAttribute(ActionResolver.RESOLVED_ACTION);

            /** Since we don't pass form action down the stack, we add it to the errors here. */
            for (Map.Entry<String,List<ValidationError>> entry : errors.entrySet()) {
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
}