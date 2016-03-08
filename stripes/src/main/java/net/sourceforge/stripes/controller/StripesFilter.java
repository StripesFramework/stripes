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

import net.sourceforge.stripes.config.BootstrapPropertyResolver;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.config.RuntimeConfiguration;
import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.exception.StripesServletException;
import net.sourceforge.stripes.util.HttpUtil;
import net.sourceforge.stripes.util.Log;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.beans.Introspector;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

/**
 * The Stripes filter is used to ensure that all requests coming to a Stripes application
 * are handled in the same way.  It detects and wraps any requests that contain multipart/form
 * data, so that they may be treated much like any other request.  Also ensures that
 * all downstream components have access to essential configuration and services whether
 * the request goes through the dispatcher, or straight to a JSP.
 *
 * @author Tim Fennell
 */
public class StripesFilter implements Filter {
    /** Key used to lookup the name of the Configuration class used to configure Stripes. */
    public static final String CONFIG_CLASS = "Configuration.Class";

    /** Log used throughout the class. */
    private static final Log log = Log.getInstance(StripesFilter.class);

    /** The configuration instance for Stripes. */
    private static Configuration configuration;

    /** The servlet context */
    private ServletContext servletContext;
    
    /**
     * Some operations should only be done if the current invocation of
     * {@link #doFilter(ServletRequest, ServletResponse, FilterChain)} is the
     * first in the filter chain. This {@link ThreadLocal} keeps track of
     * whether such operations should be done or not.
     */
    private static final ThreadLocal<Boolean> initialInvocation = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return true;
        }
    };

    /**
     * Performs the necessary initialization for the StripesFilter.  Mainly this involves deciding
     * what configuration class to use, and then instantiating and initializing the chosen
     * Configuration.
     *
     * @throws ServletException thrown if a problem is encountered initializing Stripes
     */
    public void init(FilterConfig filterConfig) throws ServletException {
        configuration = createConfiguration(filterConfig);

        this.servletContext = filterConfig.getServletContext();
        this.servletContext.setAttribute(StripesFilter.class.getName(), this);

        Package pkg = getClass().getPackage();
        log.info("Stripes Initialization Complete. Version: ", pkg.getSpecificationVersion(),
                 ", Build: ", pkg.getImplementationVersion());
    }

    /**
     * Create and configure a new {@link Configuration} instance using the suppied
     * {@link FilterConfig}.
     * 
     * @param filterConfig The filter configuration supplied by the container.
     * @return The new configuration instance.
     * @throws ServletException If the configuration cannot be created.
     */
    protected static Configuration createConfiguration(FilterConfig filterConfig)
            throws ServletException {
        BootstrapPropertyResolver bootstrap = new BootstrapPropertyResolver(filterConfig);

        // Set up the Configuration - if one isn't found by the bootstrapper then
        // we'll just use the default: RuntimeConfiguration
        Class<? extends Configuration> clazz = bootstrap.getClassProperty(CONFIG_CLASS,
                Configuration.class);

        if (clazz == null)
            clazz = RuntimeConfiguration.class;

        try {
            Configuration configuration = clazz.newInstance();
            configuration.setBootstrapPropertyResolver(bootstrap);
            configuration.init();
            return configuration;
        }
        catch (Exception e) {
            log.fatal(e,
                    "Could not instantiate specified Configuration. Class name specified was ",
                    "[", clazz.getName(), "].");
            throw new StripesServletException("Could not instantiate specified Configuration. "
                    + "Class name specified was [" + clazz.getName() + "].", e);
        }
    }

    /**
     * Returns the Configuration for the webapp.
     */
    public static Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Returns the configuration for this instance of the StripesFilter for any class
     * that has a reference to the filter. For normal runtime access to the configuration
     * during a request cycle, call getConfiguration() instead.
     *
     * @return the Configuration of this instance of the StripesFilter
     */
    public Configuration getInstanceConfiguration() {
        return configuration;
    }

    /**
     * Performs the primary work of the filter, including constructing a StripesRequestWrapper to
     * wrap the HttpServletRequest, and using the configured LocalePicker to decide which
     * Locale will be used to process the request.
     */
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        // check the flag that indicates if this is the initial invocation
        boolean initial = initialInvocation.get();
        if (initial) {
            initialInvocation.set(false);
        }

        // Wrap pretty much everything in a try/catch so that we can funnel even the most
        // bizarre or unexpected exceptions into the exception handler
        try {
            log.trace("Intercepting request to URL: ", HttpUtil.getRequestedPath(httpRequest));

            if (initial) {

                // Figure out the locale and character encoding to use. The ordering of things here
                // is very important!! We pick the locale first since picking the encoding is
                // locale dependent, but the encoding *must* be set on the request before any
                // parameters or parts are accessed, and wrapping the request accesses stuff.
                Locale locale = configuration.getLocalePicker().pickLocale(httpRequest);
                log.debug("LocalePicker selected locale: ", locale);

                String encoding = configuration.getLocalePicker().pickCharacterEncoding(
                        httpRequest, locale);
                if (encoding != null) {
                    httpRequest.setCharacterEncoding(encoding);
                    log.debug("LocalePicker selected character encoding: ", encoding);
                }
                else {
                    log.debug("LocalePicker did not pick a character encoding, using default: ",
                            httpRequest.getCharacterEncoding());
                }

                StripesRequestWrapper request = wrapRequest(httpRequest);
                request.setLocale(locale);
                httpResponse.setLocale(locale);
                if (encoding != null) {
                    httpResponse.setCharacterEncoding(encoding);
                }
                httpRequest = request;
            }
            else {
                // process URI parameters on subsequent invocations
                StripesRequestWrapper.findStripesWrapper(httpRequest).pushUriParameters(
                        (HttpServletRequestWrapper) httpRequest);
            }

            // Execute the rest of the chain
            flashInbound(httpRequest);
            filterChain.doFilter(httpRequest, servletResponse);
        }
        catch (Throwable t) {
            this.configuration.getExceptionHandler().handle(t, httpRequest, httpResponse);
        }
        finally {
            // reset the flag that indicates if this is the initial invocation
            if (initial) {
                // Once the request is processed, clean up thread locals
                StripesFilter.initialInvocation.remove();

                flashOutbound(httpRequest);
            }
            else {
                // restore URI parameters to their previous state
                StripesRequestWrapper.findStripesWrapper(httpRequest).popUriParameters();
            }
        }
    }

    /**
     * Wraps the HttpServletRequest with a StripesServletRequest.  This is done to ensure that any
     * form posts that contain file uploads get handled appropriately.
     *
     * @param servletRequest the HttpServletRequest handed to the dispatcher by the container
     * @return an instance of StripesRequestWrapper, which is an HttpServletRequestWrapper
     * @throws StripesServletException if the wrapper cannot be constructed
     */
    protected StripesRequestWrapper wrapRequest(HttpServletRequest servletRequest)
            throws StripesServletException {
        try {
            return StripesRequestWrapper.findStripesWrapper(servletRequest);
        }
        catch (IllegalStateException e) {
            return new StripesRequestWrapper(servletRequest);
        }
    }

    /**
     * <p>Checks to see if there is a flash scope identified by a parameter to the current
     * request, and if there is, retrieves items from the flash scope and moves them
     * back to request attributes.</p>
     */
    protected void flashInbound(HttpServletRequest req) {
        // Copy the attributes from the previous flash scope to the new request
        FlashScope flash = FlashScope.getPrevious(req);
        if (flash != null) {
            flash.beginRequest(req);
        }
    }

    /**
     * Manages the work that ensures that flash scopes get cleaned up properly when
     * requests go missing.  Firstly timestamps the current flash scope (if one exists)
     * to record the time that the request exited the container.  Then checks all
     * flash scopes to make sure none have been hanging out for more than a minute.
     */
    protected void flashOutbound(HttpServletRequest req) {
        // Start the timer on the current flash scope
        FlashScope flash = FlashScope.getCurrent(req, false);
        if (flash != null) {
            flash.completeRequest();
        }
    }

    /** Calls the cleanup() method on the log to release resources held by commons logging. */
    public void destroy() {
        this.servletContext.removeAttribute(StripesFilter.class.getName());
        Log.cleanup();
        Introspector.flushCaches(); // Not 100% sure this is necessary, but it doesn't  hurt
    }
}
