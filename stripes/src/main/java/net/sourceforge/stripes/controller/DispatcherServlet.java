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
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.exception.StripesServletException;
import net.sourceforge.stripes.util.HttpUtil;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.validation.BooleanTypeConverter;
import net.sourceforge.stripes.validation.expression.ExpressionValidator;
import net.sourceforge.stripes.validation.expression.Jsp20ExpressionExecutor;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.JspFactory;
import jakarta.servlet.jsp.PageContext;
import java.lang.reflect.InvocationTargetException;
import java.util.Stack;

/**
 * <p>
 * Servlet that controls how requests to the Stripes framework are processed.
 * Uses an instance of the ActionResolver interface to locate the bean and
 * method used to handle the current request and then delegates processing to
 * the bean.</p>
 *
 * <p>
 * While the DispatcherServlet is structured so that it can be easily subclassed
 * and overridden much of the processing work is delegated to the
 * {@link DispatcherHelper} class.</p>
 *
 * @author Tim Fennell
 */
public class DispatcherServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /**
     * Configuration key used to lookup up a property that determines whether or
     * not beans' custom validate() method gets invoked when validation errors
     * are generated during the binding process
     */
    public static final String RUN_CUSTOM_VALIDATION_WHEN_ERRORS
            = "Validation.InvokeValidateWhenErrorsExist";

    private Boolean alwaysInvokeValidate;

    /**
     * Log used throughout the class.
     */
    private static final Log log = Log.getInstance(DispatcherServlet.class);

    /**
     * <p>
     * Invokes the following instance level methods in order to coordinate the
     * processing of requests:</p>
     *
     * <ul>
     * <li>{@link #resolveActionBean(ExecutionContext)}</li>
     * <li>{@link #resolveHandler(ExecutionContext)}</li>
     * <li>{@link #doBindingAndValidation(ExecutionContext)}</li>
     * <li>{@link #doCustomValidation(ExecutionContext)}</li>
     * <li>{@link #handleValidationErrors(ExecutionContext)}</li>
     * <li>{@link #invokeEventHandler(ExecutionContext)}</li>
     * </ul>
     *
     * <p>
     * If any of the above methods return a {@link Resolution} the rest of the
     * request processing is aborted and the resolution is executed.</p>
     *
     * @param request the HttpServletRequest handed to the class by the
     * container
     * @param response the HttpServletResponse paired to the request
     * @throws ServletException thrown when the system fails to process the
     * request in any way
     */
    @Override
    protected void service(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException {

        // It sucks that we have to do this here (in the request cycle), but there doesn't
        // seem to be a good way to get at the Configuration from the Filter in init()
        doOneTimeConfiguration();

        ///////////////////////////////////////////////////////////////////////
        // Here beings the real processing of the request!
        ///////////////////////////////////////////////////////////////////////
        log.trace("Dispatching request to URL: ", HttpUtil.getRequestedPath(request));

        PageContext pageContext = null;
        final ExecutionContext ctx = new ExecutionContext();

        boolean async = false;

        try {
            final Configuration config = StripesFilter.getConfiguration();

            // First manufacture an ActionBeanContext
            final ActionBeanContext context
                    = config.getActionBeanContextFactory().getContextInstance(request, response);
            context.setServletContext(getServletContext());

            // Then setup the ExecutionContext that we'll use to process this request
            ctx.setActionBeanContext(context);

            try {
                ActionBeanContext abc = ctx.getActionBeanContext();

                // It's unclear whether this usage of the JspFactory will work in all containers. It looks
                // like it should, but still, we should be careful not to screw up regular request
                // processing if it should fail. Why do we do this?  So we can have a container-agnostic
                // way of getting an ExpressionEvaluator to do expression based validation. And we only
                // need it if the Jsp20 executor is used, so maybe soon we can kill it?
                if (ExpressionValidator.getExecutor() instanceof Jsp20ExpressionExecutor) {
                    pageContext = JspFactory.getDefaultFactory().getPageContext(this, // the servlet inst
                            abc.getRequest(), // req
                            abc.getResponse(), // res
                            null, // error page url
                            (request.getSession(false) != null), // needsSession - don't force a session creation if one doesn't already exist
                            abc.getResponse().getBufferSize(),
                            true); // autoflush
                    DispatcherHelper.setPageContext(pageContext);
                }

            } catch (Exception e) {
                // Don't even log this, this failure gets reported if action beans actually
                // try and make use of expression validation, otherwise this is just noise
            }

            // Resolve the ActionBean, and if an interceptor returns a resolution, bail now
            saveActionBean(request);

            Resolution resolution = requestInit(ctx);

            if (resolution == null) {
                resolution = resolveActionBean(ctx);

                if (resolution == null) {
                    resolution = resolveHandler(ctx);

                    if (resolution == null) {
                        // Then run binding and validation
                        resolution = doBindingAndValidation(ctx);

                        if (resolution == null) {
                            // Then continue on to custom validation
                            resolution = doCustomValidation(ctx);

                            if (resolution == null) {
                                // And then validation error handling
                                resolution = handleValidationErrors(ctx);

                                if (resolution == null) {
                                    // And finally invoking of the event handler
                                    resolution = invokeEventHandler(ctx);
                                }
                            }
                        }
                    }
                }
            }

            // Whatever stage it came from, execute the resolution
            if (resolution != null) {
                if (resolution instanceof AsyncResponse) {
                    // special handling for async resolutions : we defer
                    // cleanup to async processing. We register a
                    // "cleanup" callback that the async processing will
                    // invoke when completed. This allows to hide the details
                    // from dispatcher (and to cut the dependency on the class,
                    // so that Stripes still works with Servlet2.x containers.)
                    async = true;
                    final PageContext pc = pageContext;
                    final AsyncResponse asyncResponse = (AsyncResponse) resolution;
                    asyncResponse.setCleanupCallback(new Runnable() {
                        @Override
                        public void run() {
                            log.debug("Cleaning up AsyncResponse ", asyncResponse);
                            if (pc != null) {
                                JspFactory.getDefaultFactory().releasePageContext(pc);
                                DispatcherHelper.setPageContext(null);
                            }
                            requestComplete(ctx);
                            restoreActionBean(request);
                        }
                    });
                }
                executeResolution(ctx, resolution);
            }
        } catch (ServletException se) {
            throw se;
        } catch (RuntimeException re) {
            throw re;
        } catch (InvocationTargetException ite) {
            if (ite.getTargetException() instanceof ServletException) {
                throw (ServletException) ite.getTargetException();
            } else if (ite.getTargetException() instanceof RuntimeException) {
                throw (RuntimeException) ite.getTargetException();
            } else {
                throw new StripesServletException("ActionBean execution threw an exception.", ite.getTargetException());
            }
        } catch (Exception e) {
            throw new StripesServletException("Exception encountered processing request.", e);
        } finally {
            // Make sure to release the page context
            if (!async) {
                if (pageContext != null) {
                    JspFactory.getDefaultFactory().releasePageContext(pageContext);
                    DispatcherHelper.setPageContext(null);
                }
                requestComplete(ctx);
                restoreActionBean(request);
            }
        }
    }

    /**
     * Calls interceptors listening for RequestInit. There is no Stripes code
     * that executes for this lifecycle stage.
     */
    private Resolution requestInit(ExecutionContext ctx) throws Exception {
        ctx.setLifecycleStage(LifecycleStage.RequestInit);
        ctx.setInterceptors(StripesFilter.getConfiguration().getInterceptors(LifecycleStage.RequestInit));
        return ctx.wrap(new Interceptor() {
            @Override
            public Resolution intercept(ExecutionContext context) throws Exception {
                return null;
            }
        });
    }

    /**
     * Calls interceptors listening for RequestComplete. There is no Stripes
     * code that executes for this lifecycle stage. In addition, any response
     * from interceptors is ignored because it is too late to execute a
     * Resolution at this point.
     */
    private void requestComplete(ExecutionContext ctx) {
        ctx.setLifecycleStage(LifecycleStage.RequestComplete);
        ctx.setInterceptors(StripesFilter.getConfiguration().getInterceptors(LifecycleStage.RequestComplete));
        try {
            Resolution resolution = ctx.wrap(new Interceptor() {
                @Override
                public Resolution intercept(ExecutionContext context) throws Exception {
                    return null;
                }
            });
            if (resolution != null) {
                log.warn("Resolutions returned from interceptors for ", ctx.getLifecycleStage(),
                        " are ignored because it is too late to execute them.");
            }
        } catch (Exception e) {
            log.error(e);
        }
    }

    /**
     * Responsible for resolving the ActionBean for the current request.
     * Delegates to
     * {@link DispatcherHelper#resolveActionBean(ExecutionContext)}.
     *
     * @param ctx - Execution context object
     * @return The resolution after the action bean has been resolved.
     * @throws java.lang.Exception If an issue occurs resolving the action bean.
     */
    protected Resolution resolveActionBean(ExecutionContext ctx) throws Exception {
        return DispatcherHelper.resolveActionBean(ctx);
    }

    /**
     * Responsible for resolving the event handler method for the current
     * request. Delegates to
     * {@link DispatcherHelper#resolveHandler(ExecutionContext)}.
     *
     * @param ctx - The execution context
     * @return The resolution after the handler has been resolved.
     * @throws java.lang.Exception If an error occurs resolving the handler for
     * the execution context.
     */
    protected Resolution resolveHandler(ExecutionContext ctx) throws Exception {
        return DispatcherHelper.resolveHandler(ctx);
    }

    /**
     * Responsible for executing binding and validation for the current request.
     * Delegates to
     * {@link DispatcherHelper#doBindingAndValidation(ExecutionContext, boolean)}.
     *
     * @param ctx - Execution context
     * @return The resolution after binding and validation have occurred
     * @throws java.lang.Exception If an error happens during binding and
     * validation
     */
    protected Resolution doBindingAndValidation(ExecutionContext ctx) throws Exception {
        return DispatcherHelper.doBindingAndValidation(ctx, true);
    }

    /**
     * Responsible for executing custom validation methods for the current
     * request. Delegates to
     * {@link DispatcherHelper#doCustomValidation(ExecutionContext, boolean)}.
     *
     * @param ctx The current execution context
     * @return The resolution after custom validation has been performed
     * @throws java.lang.Exception If an error occurs while performing binding
     * and validation
     */
    protected Resolution doCustomValidation(ExecutionContext ctx) throws Exception {
        return DispatcherHelper.doCustomValidation(ctx, alwaysInvokeValidate);
    }

    /**
     * Responsible for handling any validation errors that arise during
     * validation. Delegates to
     * {@link DispatcherHelper#handleValidationErrors(ExecutionContext)}.
     *
     * @param ctx - The current execution context
     * @return The resolution after validation errors have been handled
     * @throws java.lang.Exception If an error occurs during validation error
     * handling
     */
    protected Resolution handleValidationErrors(ExecutionContext ctx) throws Exception {
        return DispatcherHelper.handleValidationErrors(ctx);
    }

    /**
     * Responsible for invoking the event handler if no validation errors occur.
     * Delegates to
     * {@link DispatcherHelper#invokeEventHandler(ExecutionContext)}.
     *
     * @param ctx - The current execution context
     * @return The resolution after the event handler has been invoked
     * @throws java.lang.Exception If an error occurs while invoking the event
     * handler.
     */
    protected Resolution invokeEventHandler(ExecutionContext ctx) throws Exception {
        return DispatcherHelper.invokeEventHandler(ctx);
    }

    /**
     * Responsible for executing the Resolution for the current request.
     * Delegates to
     * {@link DispatcherHelper#executeResolution(ExecutionContext, Resolution)}.
     *
     * @param ctx - The current execution context
     * @param resolution The resolution to execute
     * @throws java.lang.Exception If an error occurs while executing the
     * resolution.
     */
    protected void executeResolution(ExecutionContext ctx, Resolution resolution) throws Exception {
        DispatcherHelper.executeResolution(ctx, resolution);
    }

    /**
     * Performs a simple piece of one time configuration that requires access to
     * the Configuration object delivered through the Stripes Filter.
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
            } else {
                this.alwaysInvokeValidate = false; // Default behaviour
            }
        }
    }

    /**
     * Fetches, and lazily creates if required, a Stack in the request to store
     * ActionBeans should the current request involve forwards or includes to
     * other ActionBeans.
     *
     * @param request the current HttpServletRequest
     * @param create Whether or not to create the action bean stack if it is not
     * already created
     * @return the Stack if present, or if creation is requested
     */
    @SuppressWarnings("unchecked")
    protected Stack<ActionBean> getActionBeanStack(HttpServletRequest request, boolean create) {
        Stack<ActionBean> stack = (Stack<ActionBean>) request.getAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN_STACK);
        if (stack == null && create) {
            stack = new Stack<ActionBean>();
            request.setAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN_STACK, stack);
        }

        return stack;
    }

    /**
     * Saves the current value of the 'actionBean' attribute in the request so
     * that it can be restored at a later date by calling
     * {@link #restoreActionBean(HttpServletRequest)}. If no ActionBean is
     * currently stored in the request, nothing is changed.
     *
     * @param request the current HttpServletRequest
     */
    protected void saveActionBean(HttpServletRequest request) {
        if (request.getAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN) != null) {
            Stack<ActionBean> stack = getActionBeanStack(request, true);
            stack.push((ActionBean) request.getAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN));
        }
    }

    /**
     * Restores the previous value of the 'actionBean' attribute in the request.
     * If no ActionBeans have been saved using
     * {@link #saveActionBean(HttpServletRequest)} then this method has no
     * effect.
     *
     * @param request the current HttpServletRequest
     */
    protected void restoreActionBean(HttpServletRequest request) {
        Stack<ActionBean> stack = getActionBeanStack(request, false);
        if (stack != null && !stack.empty()) {
            request.setAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN, stack.pop());
        }
    }
}
