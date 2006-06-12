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
import net.sourceforge.stripes.config.BootstrapPropertyResolver;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.util.ResolverUtil;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p>An alternative implementation of {@link ExceptionHandler} that discovers and automatically
 * configures individual {@link AutoExceptionHandler} classes to handle specific types of
 * exceptions. This implementation is most useful when ActionBeans may produce many different
 * types of exceptions which require different handling, since independent AutoExceptionHandler
 * classes can be used to manage the different types of exceptions.</p>
 *
 * <p>Searches for implementations of AutoExceptionHandler using the same mechanism as is used
 * to discover ActionBean implementations - a search of the classpath for classes that implement
 * the interface. As such is it highly recommended that you specify filters for the search using
 * the following configuration parameters (usually as init-params for the Stripes Filter):</p>
 *
 * <ul>
 *   <li>
 *     <tt>DelegatingExceptionHandler.UrlFilters</tt> - limits the classpath locations that are
 *     checked.  E.g. <tt>/WEB-INF/classes,/WEB-INF/lib/myproject.jar</tt>
 *   </li>
 *   <li>
 *     <tt>DelegatingExceptionHandler.PackageFilters</tt> - limits the classes checked to those
 *     contained within packages matching one or more filters. E.g. <tt>myproject,exception</tt>
 *   </li>
 * </ul>
 *
 * <p>When the {@link #handle(Throwable, HttpServletRequest, HttpServletResponse)} is invoked
 * the set of AutoExceptionHandlers is examined to find the handler with the most specific
 * signature that is capable of handling the exception. If no handler is available to handle the
 * exception type supplied then the exception will be rethrown; if the exception is not a
 * ServletException it will be wrapped in a StripesServletException before being rethrown.</p>
 *
 * <p>If it is desirable to ensure that all exceptions are handled simply create an
 * AutoExceptionHandler that takes with {@link java.lang.Exception} (preferable) or
 * {@link java.lang.Throwable} (this may catch unhandlable errors like OutOfMemoryError).</p>
 *
 * @author Jeppe Cramon, Tim Fennell
 * @since Stripes 1.3
 */
public class DelegatingExceptionHandler implements ExceptionHandler {
    /** Log instance for use within in this class. */
    private static final Log log = Log.getInstance(DelegatingExceptionHandler.class);

    /** Configuration key used to lookup the URL filters used when scanning for handlers. */
    public static final String URL_FILTERS = "DelegatingExceptionHandler.UrlFilters";

    /** Configuration key used to lookup the package filters used when scanning for handlers. */
    public static final String PACAKGE_FILTERS = "DelegatingExceptionHandler.PackageFilters";

    private Configuration configuration;

    /** Provides subclasses with access to the configuration object. */
    protected Configuration getConfiguration() { return this.configuration; }

    /**
     * Inner class that ties a class and method together an invokable object.
     * @author Tim Fennell
     * @since Stripes 1.3
     */
    private static class HandlerProxy {
        private AutoExceptionHandler handler;
        private Method handlerMethod;

        /** Constructs a new HandlerProxy that will tie together the instance and method used. */
        public HandlerProxy(AutoExceptionHandler handler, Method handlerMethod) {
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
    }

    /** A cache of exception types handled mapped to proxy objects that can do the handling. */
    Map<Class<? extends Throwable>, HandlerProxy> handlers =
            new HashMap<Class<? extends Throwable>, HandlerProxy>();

    /**
     * Looks up the filters as defined in the Configuration and then invokes the
     * {@link ResolverUtil} to find implementations of AutoExceptionHandler. Each
     * implementation found is then examined and cached by calling
     * {@link #addHandler(Class)}
     *
     * @param configuration the Configuration for this Stripes application
     * @throws Exception thrown if any of the discovered handler types cannot be safely
     *         instantiated
     */
    public void init(Configuration configuration) throws Exception {
        this.configuration = configuration;

        // Fetch the AutoExceptionHandler implementations and add them to the cache
        Set<Class<? extends AutoExceptionHandler>> handlers = findClasses(AutoExceptionHandler.class);
        for (Class<? extends AutoExceptionHandler> handler : handlers) {
            log.debug("Processing class ", handler, " looking for exception handling methods.");
            addHandler(handler);
        }
    }

    /**
     * Adds an AutoExceptionHandler class to the set of configured handles. Examines
     * all the methods on the class looking for public non-abstract methods with a signature
     * matching that described in the documentation for AutoExceptionHandler.  Each method
     * is wrappped in a HandlerProxy and stored in a cache by the exception type it takes.
     *
     * @param handlerClass the AutoExceptionHandler class being configured
     * @throws Exception if the AutoExceptionHandler class cannot be instantiated
     */
    protected void addHandler(Class<? extends AutoExceptionHandler> handlerClass) throws Exception {
        Method[] methods = handlerClass.getMethods();
        for (Method method : methods) {
            // Check the method Signature
            Class[] parameters = method.getParameterTypes();
            int mods = method.getModifiers();

            if (Modifier.isPublic(mods) && !Modifier.isAbstract(mods) &&
                 parameters.length == 3 && Throwable.class.isAssignableFrom(parameters[0]) &&
                    HttpServletRequest.class.equals(parameters[1]) &&
                        HttpServletResponse.class.equals(parameters[2])) {

                Class<? extends Throwable> type = parameters[0];
                AutoExceptionHandler handler = handlerClass.newInstance();
                HandlerProxy proxy = new HandlerProxy(handler, method);
                handlers.put(type, proxy);

                log.debug("Added exception handler '", handlerClass.getSimpleName(), ".",
                          method.getName(), "()' for exception type: ", type);
            }
        }
    }

    /**
     * Implementation of the ExceptionHandler interface that attempts to find an
     * {@link AutoExceptionHandler} that is capable of handing the exception. If it finds one
     * then it is delegated to, and if it returns a resolution it will be executed. Otherwise
     * behaves like the default implementation by rethrowing any unhandled exceptions, wrapped
     * in a StripesServletException if necessary.
     *
     * @param throwable the exception being handled
     * @param request the current request being processed
     * @param response the response paired with the current request
     */
    public void handle(Throwable throwable,
                       HttpServletRequest request,
                       HttpServletResponse response) throws ServletException {
        try {
            Throwable actual = unwrap(throwable);
            Class type = actual.getClass();
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
                throw throwable;
            }
        }
        catch (ServletException se) {
            throw se;
        }
        catch (Throwable t) {
            throw new StripesServletException("Unhandled exception in exception handler.", t);
        }
    }

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

    /**
     * Looks up the URL filters that will be used when scanning the classpath etc. for
     * implementations of exception handlers.
     *
     * @return a set of String filters, possibly empty, never null
     */
    protected Set<String> getUrlFilters() {
        BootstrapPropertyResolver bootstrap = this.configuration.getBootstrapPropertyResolver();
        Set<String> urlFilters =new HashSet<String>();

        String temp = bootstrap.getProperty(URL_FILTERS);
        if (temp != null) {
            urlFilters.addAll(Arrays.asList( temp.split(",")));
        }

        return urlFilters;
    }

    /**
     * Looks up the package filters that will be used when scanning the classpath etc. for
     * implementations of exception handlers.
     *
     * @return a set of String filters, possibly empty, never null
     */
    protected Set<String> getPackageFilters() {
        BootstrapPropertyResolver bootstrap = this.configuration.getBootstrapPropertyResolver();
        Set<String> packageFilters =new HashSet<String>();

        String temp = bootstrap.getProperty(PACAKGE_FILTERS);
        if (temp != null) {
            packageFilters.addAll(Arrays.asList( temp.split(",")));
        }

        return packageFilters;
    }

    /**
     * Uses the configured URL and Package filters to find subclasses/implementations of the
     * type specified. First tries to find classes using the context classloader, and if that
     * fails, falls back to using the ServletContext mechanism.
     *
     * @param parentType the interface or base class of which to find implementations/subclasses
     * @return a set of Class objects that represent subclasses of the type provided
     */
    protected <T> Set<Class<? extends T>> findClasses(Class<T> parentType) {
        ResolverUtil<T> resolver = new ResolverUtil<T>();
        resolver.setPackageFilters(getPackageFilters());
        resolver.setLocationFilters(getUrlFilters());

        if (!resolver.loadImplementationsFromContextClassloader(parentType)) {
            ServletContext context = this.configuration.getBootstrapPropertyResolver()
                    .getFilterConfig().getServletContext();

            resolver.loadImplementationsFromServletContext(parentType, context);
        }

        return resolver.getClasses();
    }
}
