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
package net.sourceforge.stripes.exception;


import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.ValidationErrorReportResolution;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.controller.DispatcherHelper;
import net.sourceforge.stripes.controller.ExecutionContext;
import net.sourceforge.stripes.controller.FileUploadLimitExceededException;
import net.sourceforge.stripes.controller.StripesConstants;
import net.sourceforge.stripes.controller.StripesRequestWrapper;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.util.ReflectUtil;
import net.sourceforge.stripes.validation.LocalizableError;

/**
 * <p>Default ExceptionHandler implementation that makes it easy for users to extend and
 * add custom handling for different types of exception. When extending this class methods
 * can be added that meet the following requirements:</p>
 *
 * <ul>
 *   <li>Methods must be public</li>
 *   <li>Methods must be non-abstract</li>
 *   <li>Methods must have exactly three parameters</li>
 *   <li>The first parameter type must be Throwable or a subclass thereof</li>
 *   <li>The second and third arguments must be of type HttpServletRequest and
 *       HttpServletResponse respectively</li>
 *   <li>Methods may <i>optionally</i> return a Resolution in which case the resolution
 *       will be executed</li>
 * </ul>
 *
 * <p>When an exception is caught the exception handler attempts to find a method that
 * can handle that type of exception. If none is found the exception's super-types are
 * iterated through and methods looked for which match the super-types. If a matching
 * method is found it will be invoked.  Otherwise the exception will simply be rethrown
 * by the exception handler - though first it will be wrapped in a StripesServletException
 * if necessary in order to make it acceptable to the container.</p>
 *
 * <p>The following are examples of method signatures that might be added by subclasses:</p>
 *
 * <pre>
 * public Resolution handle(FileUploadLimitExceededException ex, HttpServletRequest req, HttpServletResponse resp) { ... }
 * public void handle(MySecurityException ex, HttpServletRequest req, HttpServletResponse resp) { ... }
 * public void catchAll(Throwable t, HttpServletRequest req, HttpServletResponse resp) { ... }
 * </pre>
 *
 * @author Tim Fennell
 * @since Stripes 1.3
 */
public class DefaultExceptionHandler implements ExceptionHandler {
    private static final Log log = Log.getInstance(DefaultExceptionHandler.class);
    private Configuration configuration;

    /** A cache of exception types handled mapped to proxy objects that can do the handling. */
    private Map<Class<? extends Throwable>, HandlerProxy> handlers =
            new HashMap<Class<? extends Throwable>, HandlerProxy>();

    /**
     * Inner class that ties a class and method together an invokable object.
     * @author Tim Fennell
     * @since Stripes 1.3
     */
    protected static class HandlerProxy {
        private Object handler;
        private Method handlerMethod;

        /** Constructs a new HandlerProxy that will tie together the instance and method used. */
        public HandlerProxy(Object handler, Method handlerMethod) {
            this.handler = handler;
            this.handlerMethod = handlerMethod;
        }

        /** Invokes the handler and executes the resolution if one is returned. */
        public void handle(Throwable t, HttpServletRequest req, HttpServletResponse res)  throws Exception {
            Object resolution = handlerMethod.invoke(this.handler, t, req, res);
            if (resolution != null && resolution instanceof Resolution) {
                ((Resolution) resolution).execute(req, res);
            }
        }

        Method getHandlerMethod() {
            return handlerMethod;
        }
    }

    /**
     * Implementation of the ExceptionHandler interface that attempts to find a method
     * that is capable of handing the exception. If it finds one then it is delegated to, and if
     * it returns a resolution it will be executed. Otherwise rethrows any unhandled exceptions,
     * wrapped in a StripesServletException if necessary.
     *
     * @param throwable the exception being handled
     * @param request the current request being processed
     * @param response the response paired with the current request
     */
    public void handle(Throwable throwable,
                       HttpServletRequest request,
                       HttpServletResponse response) throws ServletException, IOException {
        try {
            Throwable actual = unwrap(throwable);
            Class<?> type = actual.getClass();
            HandlerProxy proxy = null;

            while (type != null && proxy == null) {
                proxy = this.handlers.get(type);
                type = type.getSuperclass();
            }

            if (proxy != null) {
                proxy.handle(actual, request, response);
            }
            else if (throwable instanceof FileUploadLimitExceededException) {
                Resolution resolution = handle((FileUploadLimitExceededException) throwable,
                        request, response);
                if (resolution != null)
                    resolution.execute(request, response);
            }
            else if (throwable instanceof SourcePageNotFoundException) {
                Resolution resolution = handle((SourcePageNotFoundException) throwable, request,
                        response);
                if (resolution != null)
                    resolution.execute(request, response);
            }
            else {
                // If there's no sensible proxy, rethrow the original throwable,
                // NOT the unwrapped one since they may add extra information
                log.warn(throwable, "Unhandled exception caught by the Stripes default exception handler.");
                throw throwable;
            }
        }
        catch (ServletException se) { throw se; }
        catch (IOException ioe) { throw ioe; }
        catch (Throwable t) {
            String message = "Unhandled exception in exception handler.";
            log.error(t, message);
            throw new StripesServletException(message, t);
        }
    }

    /**
     * <p>
     * A default handler for {@link SourcePageNotFoundException}. That exception is thrown when
     * validation errors occur on a request but the source page cannot be determined from the
     * request parameters. Such a condition generally arises during application development when,
     * for example, a parameter is accidentally omitted from a generated hyperlink or AJAX request.
     * </p>
     * <p>
     * In the past, it was very difficult to determine what validation errors triggered the
     * exception. This method returns a {@link ValidationErrorReportResolution}, which sends a
     * simple HTML response to the client that very clearly details the validation errors.
     * </p>
     * <p>
     * In production, most applications will provide their own handler for
     * {@link SourcePageNotFoundException} by extending this class and overriding this method.
     * </p>
     * 
     * @param exception The exception.
     * @param request The servlet request.
     * @param response The servlet response.
     * @return A {@link ValidationErrorReportResolution}
     */
    protected Resolution handle(SourcePageNotFoundException exception, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        return new ValidationErrorReportResolution(exception.getActionBeanContext());
    }

    /**
     * <p>
     * {@link FileUploadLimitExceededException} is notoriously difficult to handle for several
     * reasons:
     * <ul>
     * <li>The exception is thrown during construction of the {@link StripesRequestWrapper}. Many
     * Stripes components rely on the presence of this wrapper, yet it cannot be created normally.</li>
     * <li>It happens before the request lifecycle has begun. There is no {@link ExecutionContext},
     * {@link ActionBeanContext}, or {@link ActionBean} associated with the request yet.</li>
     * <li>None of the request parameters in the POST body can be read without risking denial of
     * service. That includes the {@code _sourcePage} parameter that indicates the page from which
     * the request was submitted.</li>
     * </ul>
     * </p>
     * <p>
     * This exception handler makes an attempt to handle the exception as gracefully as possible. It
     * relies on the HTTP Referer header to determine where the request was submitted from. It uses
     * introspection to guess the field name of the {@link FileBean} field that exceeded the POST
     * limit. It instantiates an {@link ActionBean} and {@link ActionBeanContext} and adds a
     * validation error to report the field name, maximum POST size, and actual POST size. Finally,
     * it forwards to the referer.
     * </p>
     * <p>
     * While this is a best effort, it won't be ideal for all situations. If this method is unable
     * to handle the exception properly for any reason, it rethrows the exception. Subclasses can
     * call this method in a {@code try} block, providing additional processing in the {@code catch}
     * block.
     * </p>
     * <p>
     * A simple way to provide a single, global error page for this type of exception is to override
     * {@link #getFileUploadExceededExceptionPath(HttpServletRequest)} to return the path to your
     * global error page.
     * <p>
     * 
     * @param exception The exception that needs to be handled
     * @param request The servlet request
     * @param response The servlet response
     * @return A {@link Resolution} to forward to the path returned by
     *         {@link #getFileUploadExceededExceptionPath(HttpServletRequest)}
     * @throws FileUploadLimitExceededException If
     *             {@link #getFileUploadExceededExceptionPath(HttpServletRequest)} returns null or
     *             this method is unable for any other reason to forward to the error page
     */
    protected Resolution handle(FileUploadLimitExceededException exception,
            HttpServletRequest request, HttpServletResponse response)
            throws FileUploadLimitExceededException {
        // Get the path to which we will forward to display the message
        final String path = getFileUploadExceededExceptionPath(request);
        if (path == null)
            throw exception;

        final StripesRequestWrapper wrapper;
        final ActionBeanContext context;
        final ActionBean actionBean;
        try {
            // Create a new request wrapper, avoiding the pitfalls of multipart
            wrapper = new StripesRequestWrapper(request) {
                @Override
                protected void constructMultipartWrapper(HttpServletRequest request)
                        throws StripesServletException {
                    setLocale(configuration.getLocalePicker().pickLocale(request));
                }
            };

            // Create the ActionBean and ActionBeanContext
            context = configuration.getActionBeanContextFactory().getContextInstance(wrapper,
                    response);
            actionBean = configuration.getActionResolver().getActionBean(context);
            wrapper.setAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN, actionBean);
        }
        catch (ServletException e) {
            log.error(e);
            throw exception;
        }

        // Try to guess the field name by finding exactly one FileBean field
        String fieldName = null;
        try {
            PropertyDescriptor[] pds = ReflectUtil.getPropertyDescriptors(actionBean.getClass());
            for (PropertyDescriptor pd : pds) {
                if (FileBean.class.isAssignableFrom(pd.getPropertyType())) {
                    if (fieldName == null) {
                        // First FileBean field found so set the field name
                        fieldName = pd.getName();
                    }
                    else {
                        // There's more than one FileBean field so don't use a field name
                        fieldName = null;
                        break;
                    }
                }
            }
        }
        catch (Exception e) {
            // Not a big deal if we can't determine the field name
        }

        // Add validation error with parameters for max post size and actual posted size (KB)
        DecimalFormat format = new DecimalFormat("0.00");
        double max = (double) exception.getMaximum() / 1024;
        double posted = (double) exception.getPosted() / 1024;
        LocalizableError error = new LocalizableError("validation.file.postBodyTooBig", format
                .format(max), format.format(posted));
        if (fieldName == null)
            context.getValidationErrors().addGlobalError(error);
        else
            context.getValidationErrors().add(fieldName, error);

        // Create an ExecutionContext so that the validation errors can be filled in
        ExecutionContext exectx = new ExecutionContext();
        exectx.setActionBean(actionBean);
        exectx.setActionBeanContext(context);
        DispatcherHelper.fillInValidationErrors(exectx);

        // Forward back to referer, using the wrapped request
        return new ForwardResolution(path) {
            @Override
            public void execute(HttpServletRequest request, HttpServletResponse response)
                    throws ServletException, IOException {
                super.execute(wrapper, response);
            }
        };
    }

    /**
     * Get the path to which the {@link Resolution} returned by
     * {@link #handle(FileUploadLimitExceededException, HttpServletRequest, HttpServletResponse)}
     * should forward to report the error. The default implementation attempts to determine this
     * from the HTTP Referer header. If it is unable to do so, it returns null. Subclasses may
     * override this method to return whatever they wish. The return value must be relative to the
     * application context root.
     * 
     * @param request The request that generated the exception
     * @return The context-relative path from which the request was submitted
     */
    protected String getFileUploadExceededExceptionPath(HttpServletRequest request) {
        // Get the referer URL so we can bounce back to it
        URL referer = null;
        try {
            referer = new URL(request.getHeader("referer"));
        }
        catch (Exception e) {
            // Header not found? Invalid? Can't do anything with it :(
            return null;
        }

        // Convert the referer path to a context-relative path
        String path = referer.getFile();
        String contextPath = request.getContextPath();
        if (contextPath.length() > 1) {
            // We can't handle it if the POST came from outside our app
            if (!path.startsWith(contextPath + "/"))
                return null;

            path = path.replace(contextPath, "");
        }

        return path;
    }

    /** Stores the configuration and examines the handler for usable delegate methods. */
    public void init(Configuration configuration) throws Exception {
        this.configuration = configuration;
        addHandler(this);
    }

    /**
     * Adds a class to the set of configured delegate handlers. Examines all the methods on the
     * class looking for public non-abstract methods with a signature matching that described in
     * the class level javadoc.  Each method is wrapped in a HandlerProxy and stored in a cache
     * by the exception type it takes.
     *
     * @param handlerClass the class being configured
     * @throws Exception if the handler class cannot be instantiated
     */
    protected void addHandler(Class<?> handlerClass) throws Exception {
        addHandler(getConfiguration().getObjectFactory().newInstance(handlerClass));
    }

    /**
     * Adds an object instance to the set of configured handles. Examines
     * all the methods on the class looking for public non-abstract methods with a signature
     * matching that described in the class level javadoc.  Each method is wrapped in a
     * HandlerProxy and stored in a cache by the exception type it takes.
     *
     * @param handler the handler instance being configured
     */
    @SuppressWarnings("unchecked")
    protected void addHandler(Object handler) throws Exception {
        Method[] methods = handler.getClass().getMethods();
        for (Method method : methods) {
            // Check the method Signature
            Class[] parameters = method.getParameterTypes();
            int mods = method.getModifiers();

            // Check all the reasons not to add it!
            if (!Modifier.isPublic(mods)) continue;
            if (Modifier.isAbstract(mods)) continue;
            if (parameters.length != 3) continue;
            if (!Throwable.class.isAssignableFrom(parameters[0])) continue;
            if (!HttpServletRequest.class.equals(parameters[1])) continue;
            if (!HttpServletResponse.class.equals(parameters[2])) continue;
            if (handler == this && method.getName().equals("handle") &&
                    Throwable.class.equals(parameters[0])) continue;

            // And if we made it this far, add it!
            Class<? extends Throwable> type = parameters[0];
            HandlerProxy proxy = new HandlerProxy(handler, method);
            HandlerProxy previous = handlers.get(type);
            if (previous != null) {
                log.warn("More than one exception handler for exception type ", type, " in ",
                    handler.getClass().getSimpleName(), ". '", method.getName(),
                    "()' will be used instead of '", previous.getHandlerMethod().getName(), "()'.");
            }
            handlers.put(type, proxy);

            log.debug("Added exception handler '", handler.getClass().getSimpleName(), ".",
                      method.getName(), "()' for exception type: ", type);
        }
    }

    /** Provides subclasses with access to the configuration. */
    protected Configuration getConfiguration() { return configuration; }

    /**
     * Unwraps the throwable passed in.  If the throwable is a ServletException and has
     * a root case, the root cause is returned, otherwise the throwable is returned as is.
     *
     * @param throwable a throwable
     * @return another thowable, either the root cause of the one passed in
     */
    protected Throwable unwrap(Throwable throwable) {
        if (throwable instanceof ServletException) {
            Throwable t = ((ServletException) throwable).getRootCause();

            if (t != null) {
                throwable = t;
            }
        }

        return throwable;
    }
}
