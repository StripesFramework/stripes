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

import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.util.Log;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

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
            throw new StripesServletException("Unhandled exception in exception handler.", t);
        }
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
        addHandler(handlerClass.newInstance());
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
