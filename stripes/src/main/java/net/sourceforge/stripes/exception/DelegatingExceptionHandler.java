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

import net.sourceforge.stripes.config.BootstrapPropertyResolver;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.controller.AnnotatedClassActionResolver;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.util.ResolverUtil;
import net.sourceforge.stripes.util.StringUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>An alternative implementation of {@link ExceptionHandler} that discovers and automatically
 * configures individual {@link AutoExceptionHandler} classes to handle specific types of
 * exceptions. This implementation is most useful when ActionBeans may produce many different
 * types of exceptions and it is desirable to separate exception handling logic for different
 * groups or classes of exceptions. Using this approach multiple AutoExceptionHandlers can be
 * configured simultaneously but do not have to be co-located.</p>
 *
 * <p>Searches for implementations of AutoExceptionHandler using the same mechanism as is used
 * to discover ActionBean implementations - a search of the classpath for classes that implement
 * the interface. The search requires one parameter, DelegatingExceptionHandler.Packages, which
 * should contain a comma separated list of root packages to search for AutoExceptionHandler
 * classes. If this parameter is <i>not</i> specified, the DelegatingExceptionHandler will use
 * the configuration parameter that is used for discovering ActionBean instances
 * (ActionResolver.Packages).  The configuration parameter is usually specified as an
 * init-param for the Stripes Filter, e.g.:</p>
 *
 *<pre>
 *&lt;init-param&gt;
 *    &lt;param-name&gt;DelegatingExceptionHandler.Packages&lt;/param-name&gt;
 *    &lt;param-value&gt;com.myco.web,com.myco.shared&lt;/param-value&gt;
 *&lt;/init-param&gt;
 *</pre>
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
public class DelegatingExceptionHandler extends DefaultExceptionHandler {
    /** Log instance for use within in this class. */
    private static final Log log = Log.getInstance(DelegatingExceptionHandler.class);

    /**
     * Configuration key used to lookup the list of packages to scan for auto handlers.
     * @since Stripes 1.5
     */
    public static final String PACKAGES = "DelegatingExceptionHandler.Packages";

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
    @Override
    public void init(Configuration configuration) throws Exception {
        super.init(configuration);

        // Fetch the AutoExceptionHandler implementations and add them to the cache
        Set<Class<? extends AutoExceptionHandler>> handlers = findClasses();
        for (Class<? extends AutoExceptionHandler> handler : handlers) {
            if (!Modifier.isAbstract(handler.getModifiers())) {
                log.debug("Processing class ", handler, " looking for exception handling methods.");
                addHandler(handler);
            }
        }
    }

    /**
     * Helper method to find implementations of AutoExceptionHandler in the packages specified in
     * Configuration using the {@link ResolverUtil} class.
     *
     * @return a set of Class objects that represent subclasses of AutoExceptionHandler
     */
    protected Set<Class<? extends AutoExceptionHandler>> findClasses() {
        BootstrapPropertyResolver bootstrap = getConfiguration().getBootstrapPropertyResolver();

        // Try the config param that is specific to this class
        String[] packages = StringUtil.standardSplit(bootstrap.getProperty(PACKAGES));
        if (packages == null || packages.length == 0) {
            // Config param not found so try autodiscovery
            log.info("No config parameter '", PACKAGES, "' found. Trying autodiscovery instead.");
            List<Class<? extends AutoExceptionHandler>> classes = bootstrap
                    .getClassPropertyList(AutoExceptionHandler.class);
            if (!classes.isEmpty()) {
                return new HashSet<Class<? extends AutoExceptionHandler>>(classes);
            }
            else {
                // Autodiscovery found nothing so resort to looking at the ActionBean packages
                log.info("Autodiscovery found no implementations of AutoExceptionHandler. Using ",
                        "the value of '", AnnotatedClassActionResolver.PACKAGES, "' instead.");
                packages = StringUtil.standardSplit(bootstrap
                        .getProperty(AnnotatedClassActionResolver.PACKAGES));
            }
        }

        if (packages != null && packages.length > 0) {
            ResolverUtil<AutoExceptionHandler> resolver = new ResolverUtil<AutoExceptionHandler>();
            resolver.findImplementations(AutoExceptionHandler.class, packages);
            return resolver.getClasses();
        }
        else {
            return Collections.emptySet();
        }
    }
}
