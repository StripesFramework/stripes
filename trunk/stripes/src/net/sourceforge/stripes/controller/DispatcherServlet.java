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
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.validation.BooleanTypeConverter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.PageContext;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Stack;

/**
 * <p>Servlet that controls how requests to the Stripes framework are processed.  Uses an instance of
 * the ActionResolver interface to locate the bean and method used to handle the current request and
 * then delegates processing to the bean.</p>
 *
 * <p>While the DispatcherServlet is structured so that it can be easily subclassed and
 * overridden much of the processing work is delegated to the {@link DispatcherHelper} class.</p>
 *
 * @author Tim Fennell
 */
public class DispatcherServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
     * Configuration key used to lookup up a property that determines whether or not beans'
     * custom validate() method gets invoked when validation errors are generated during
     * the binding process
     */
    public static final String RUN_CUSTOM_VALIDATION_WHEN_ERRORS =
            "Validation.InvokeValidateWhenErrorsExist";

    private Boolean alwaysInvokeValidate;

    /** Log used throughout the class. */
    private static final Log log = Log.getInstance(DispatcherServlet.class);

    /** Implemented as a simple call to doPost(request, response). */
    @Override
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
     *   <li>{@link #doBindingAndValidation(ExecutionContext)}</li>
     *   <li>{@link #doCustomValidation(ExecutionContext)}</li>
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
    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
        throws ServletException {

        // It sucks that we have to do this here (in the request cycle), but there doesn't
        // seem to be a good way to get at the Configuration from the Filter in init()
        doOneTimeConfiguration();

        ///////////////////////////////////////////////////////////////////////
        // Here beings the reall processing of the request!
        ///////////////////////////////////////////////////////////////////////
        log.trace("Dispatching request to URL: ", request.getRequestURI());

        PageContext pageContext = null;

        try {
            final Configuration config = StripesFilter.getConfiguration();

            // First manufacture an ActionBeanContext
            final ActionBeanContext context =
                    config.getActionBeanContextFactory().getContextInstance(request, response);
            context.setServletContext(getServletContext());

            // Then setup the ExecutionContext that we'll use to process this request
            final ExecutionContext ctx = new ExecutionContext();
            ctx.setInterceptors(config.getInterceptors(LifecycleStage.ActionBeanResolution));
            ctx.setLifecycleStage(LifecycleStage.ActionBeanResolution);
            ctx.setActionBeanContext(context);

            // It's unclear whether this usage of the JspFactory will work in all containers. It looks
            // like it should, but still, we should be careful not to screw up regular request
            // processing if it should fail. Why do we do this?  So we can have a container-agnostic
            // way of getting an ExpressionEvaluator to do expression based validation
            try {
                ActionBeanContext abc = ctx.getActionBeanContext();
                pageContext = JspFactory.getDefaultFactory().getPageContext(this, // the servlet inst
                                                                            abc.getRequest(), // req
                                                                            abc.getResponse(), // res
                                                                            null,   // error page url
                                                                            (request.getSession(false) != null), // needsSession - don't force a session creation if one doesn't already exist
                                                                            abc.getResponse().getBufferSize(),
                                                                            true); // autoflush
                DispatcherHelper.setPageContext(pageContext);
            }
            catch (Exception e) {
                // Don't even log this, this failure gets reported if action beans actually
                // try and make use of expression validation, otherwise this is just noise
            }


            // Resolve the ActionBean, and if an interceptor returns a resolution, bail now
            saveActionBean(request);
            Resolution resolution = resolveActionBean(ctx);

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
                                // And finally(ish) invoking of the event handler
                                resolution = invokeEventHandler(ctx);

                                // If the event produced errors, fill them in
                                DispatcherHelper.fillInValidationErrors(ctx);
                            }
                        }
                    }
                }
            }

            // Whatever stage it came from, execute the resolution
            if (resolution != null) {
                executeResolution(ctx, resolution);
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
        finally {
            // Make sure to release the page context
            if (pageContext != null) {
                JspFactory.getDefaultFactory().releasePageContext(pageContext);
                DispatcherHelper.setPageContext(null);
            }
            restoreActionBean(request);
        }
    }

    /**
     * Responsible for resolving the ActionBean for the current request. Delegates to
     * {@link DispatcherHelper#resolveActionBean(ExecutionContext)}.
     */
    protected Resolution resolveActionBean(ExecutionContext ctx) throws Exception {
        return DispatcherHelper.resolveActionBean(ctx);
    }

    /**
     * Responsible for resolving the event handler method for the current request. Delegates to
     * {@link DispatcherHelper#resolveHandler(ExecutionContext)}.
     */
    protected Resolution resolveHandler(ExecutionContext ctx) throws Exception {
        return DispatcherHelper.resolveHandler(ctx);
    }

    /**
     * Responsible for executing binding and validation for the current request. Delegates to
     * {@link DispatcherHelper#doBindingAndValidation(ExecutionContext, boolean)}.
     */
    protected Resolution doBindingAndValidation(ExecutionContext ctx) throws Exception {
        return DispatcherHelper.doBindingAndValidation(ctx, true);
    }

    /**
     * Responsible for executing custom validation methods for the current request. Delegates to
     * {@link DispatcherHelper#doCustomValidation(ExecutionContext, boolean)}.
     */
    protected Resolution doCustomValidation(ExecutionContext ctx) throws Exception {
        return DispatcherHelper.doCustomValidation(ctx, alwaysInvokeValidate);
    }

    /**
     * Responsible for handling any validation errors that arise during validation. Delegates to
     * {@link DispatcherHelper#handleValidationErrors(ExecutionContext)}.
     */
    protected Resolution handleValidationErrors(ExecutionContext ctx) throws Exception {
        return DispatcherHelper.handleValidationErrors(ctx);
    }

    /**
     * Responsible for invoking the event handler if no validation errors occur. Delegates to
     * {@link DispatcherHelper#invokeEventHandler(ExecutionContext)}.
     */
    protected Resolution invokeEventHandler(ExecutionContext ctx) throws Exception {
        return DispatcherHelper.invokeEventHandler(ctx);
    }

    /**
     * Responsible for executing the Resolution for the current request. Delegates to
     * {@link DispatcherHelper#executeResolution(ExecutionContext, Resolution)}.
     */
    protected void executeResolution(ExecutionContext ctx, Resolution resolution) throws Exception {
        DispatcherHelper.executeResolution(ctx, resolution);
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
     * Fetches, and lazily creates if required, a Stack in the request to store ActionBeans
     * should the current request involve forwards or includes to other ActionBeans.
     *
     * @param request the current HttpServletRequest
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
     * Saves the current value of the 'actionBean' attribute in the request so that it
     * can be restored at a later date by calling {@link #restoreActionBean(HttpServletRequest)}.
     * If no ActionBean is currently stored in the request, nothing is changed.
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
     * Restores the previous value of the 'actionBean' attribute in the request. If no
     * ActionBeans have been saved using {@link #saveActionBean(HttpServletRequest)} then this
     * method has no effect.
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