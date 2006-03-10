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
import net.sourceforge.stripes.exception.StripesServletException;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.validation.Validatable;
import net.sourceforge.stripes.validation.ValidationError;
import net.sourceforge.stripes.validation.ValidationErrorHandler;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.BooleanTypeConverter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

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
     * Uses the configured actionResolver to locate the appropriate ActionBean type and method to handle
     * the current request.  Instantiates the ActionBean, provides it references to the request and
     * response and then invokes the handler method.
     *
     * @param request the HttpServletRequest handed to the class by the container
     * @param response the HttpServletResponse paired to the request
     * @throws ServletException thrown when the system fails to process the request in any way
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException {

        // It sucks that we have to do this here (in the request cycle), but there doesn't
        // seem to be a good way to get at the Configuration from the Filter in init()
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

        log.trace("Dispatching request to URL: ", request.getRequestURI());

        try {
            // Lookup the bean, handler method and hook everything together
            ActionBeanContext context =  StripesFilter.getConfiguration()
                    .getActionBeanContextFactory().getContextInstance(request, response);
            Resolution resolution = null;

            ActionResolver actionResolver = StripesFilter.getConfiguration().getActionResolver();
            ActionBean bean = actionResolver.getActionBean(context);
            String eventName = actionResolver.getEventName(bean.getClass(), context);
            context.setEventName(eventName);

            Method handler = null;
            if (eventName != null) {
                handler = actionResolver.getHandler(bean.getClass(), eventName);
            }
            else {
                handler = actionResolver.getDefaultHandler(bean.getClass());
                if (handler != null) {
                    context.setEventName(actionResolver.getHandledEvent(handler));
                }
            }

            // Insist that we have a handler
            if (handler == null) {
                throw new StripesServletException("No handler method found for request with " +
                    "ActionBean [" + bean.getClass().getName() + "] and eventName [ " + eventName + "]");
            }

            bean.setContext(context);
            request.setAttribute((String) request.getAttribute(ActionResolver.RESOLVED_ACTION), bean);
            request.setAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN, bean);

            // Find out if we're validating for this event or not
            boolean doValidate = (handler.getAnnotation(DontValidate.class) == null);

            // Bind the value to the bean - this includes performing field level validation
            ValidationErrors errors = bindValues(bean, context, doValidate);
            bean.getContext().setValidationErrors(errors);

            // If blah blah blah, run the bean's validate method, or maybe handleErrors
            if ( (errors.size() == 0 || this.alwaysInvokeValidate)
                    && bean instanceof Validatable && doValidate) {
                ((Validatable) bean).validate(errors);
            }

            // If we have errors, add the action path to them
            if (errors.size() > 0) {
                String formAction = (String) request.getAttribute(ActionResolver.RESOLVED_ACTION);

                /** Since we don't pass form action down the stack, we add it to the errors here. */
                for (List<ValidationError> listOfErrors : errors.values()) {
                    for (ValidationError error : listOfErrors) {
                        error.setActionPath(formAction);
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
            else if (errors.size() == 0) {
                Object returnValue = handler.invoke(bean);

                if (returnValue != null && returnValue instanceof Resolution) {
                    resolution = (Resolution) returnValue;
                }
                else if (returnValue != null) {
                    log.warn("Expected handler method ", handler.getName(), " on class ",
                             bean.getClass().getSimpleName(), " to return a Resolution. Instead it ",
                             "returned: ", returnValue);
                }
            }

            // Finally, execute the resolution
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
     * Invokes the configured property binder in order to populate the bean's properties from the
     * values contained in the request.
     *
     * @param bean the bean to be populated
     * @param context the ActionBeanContext containing the request and other information
     */
    protected ValidationErrors bindValues(ActionBean bean,
                                          ActionBeanContext context,
                                          boolean validate) throws StripesServletException {
        return StripesFilter.getConfiguration()
                .getActionBeanPropertyBinder().bind(bean, context, validate);
    }
}
