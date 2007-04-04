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
import net.sourceforge.stripes.config.BootstrapPropertyResolver;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.config.RuntimeConfiguration;
import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.exception.StripesServletException;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.util.ReflectUtil;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

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
    private Configuration configuration;

    /**
     * A place to stash the Configuration object so that other classes in Stripes can access it
     * without resorting to ferrying it, or the request, to every class that needs access to the
     * Configuration.  Doing this allows multiple Stripes Configurations to exist in a single
     * Classloader since the Configuration is not located statically.
     */
    private static ThreadLocal<Configuration> configurationStash = new ThreadLocal<Configuration>();

    /**
     * Performs the necessary initialization for the StripesFilter.  Mainly this involves deciding
     * what configuration class to use, and then instantiating and initializing the chosen
     * Configuration.
     *
     * @throws ServletException thrown if a problem is encountered initializing Stripes
     */
    public void init(FilterConfig filterConfig) throws ServletException {
        BootstrapPropertyResolver bootstrap = new BootstrapPropertyResolver(filterConfig);
        String configurationClassName = bootstrap.getProperty(CONFIG_CLASS);

        // Set up the Configuration - if one isn't found by the bootstrapper then
        // we'll just use the DefaultConfiguration
        if (configurationClassName != null) {
            try {
                Class clazz = ReflectUtil.findClass(configurationClassName);
                this.configuration = (Configuration) clazz.newInstance();
            }
            catch (Exception e) {
                log.fatal(e, "Could not instantiate specified Configuration. Class name specified was ",
                          "[", configurationClassName, "].");
                throw new StripesServletException("Could not instantiate specified Configuration. " +
                        "Class name specified was [" + configurationClassName + "].", e);
            }
        }
        else {
            this.configuration = new RuntimeConfiguration();
        }

        this.configuration.setBootstrapPropertyResolver(bootstrap);
        this.configuration.init();

        Package pkg = getClass().getPackage();
        log.info("Stripes Initialization Complete. Version: ", pkg.getSpecificationVersion(),
                 ", Build: ", pkg.getImplementationVersion());
    }

    /**
     * Returns the Configuration that is being used to process the current request.
     */
    public static Configuration getConfiguration() {
        Configuration configuration = StripesFilter.configurationStash.get();

        if (configuration == null) {
            StripesRuntimeException sre = new StripesRuntimeException(
                    "Something is trying to access the current Stripes configuration but the " +
                    "current request was never routed through the StripesFilter! As a result " +
                    "the appropriate Configuration object cannot be located. Please take a look " +
                    "at the exact URL in your browser's address bar and ensure that any " +
                    "requests to that URL will be filtered through the StripesFilter according " +
                    "to the filter mappings in your web.xml."
            );
            log.error(sre);  // log through an exception so that users get a stracktrace
        }

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
        return this.configuration;
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

        // Wrap pretty much everything in a try/catch so that we can funnel even the most
        // bizarre or unexpected exceptions into the exception handler
        try {
            log.trace("Intercepting request to URL: ", httpRequest.getRequestURI());

            // Pop the configuration into thread local
            StripesFilter.configurationStash.set(this.configuration);

            // Figure out the locale and character encoding to use. The ordering of things here
            // is very important!! We pick the locale first since picking the encoding is
            // locale dependent, but the encoding *must* be set on the request before any
            // parameters or parts are accessed, and wrapping the request accesses stuff.
            Locale locale = this.configuration.getLocalePicker().pickLocale(httpRequest);
            log.debug("LocalePicker selected locale: ", locale);

            String encoding = this.configuration.getLocalePicker().pickCharacterEncoding(httpRequest, locale);
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

            // Execute the rest of the chain
            flashInbound(request);
            filterChain.doFilter(request, servletResponse);
        }
        catch (Throwable t) {
            this.configuration.getExceptionHandler().handle(t, httpRequest, httpResponse);
        }
        finally {
            // Once the request is processed, take the Configuration back out of thread local
            flashOutbound(httpRequest);
            StripesFilter.configurationStash.set(null);
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
        return new StripesRequestWrapper(servletRequest);
    }

    /**
     * <p>Checks to see if there is a flash scope identified by a parameter to the current
     * request, and if there is, retrieves items from the flash scope and moves them
     * back to request attributes.</p>
     */
    protected void flashInbound(HttpServletRequest req) {
        FlashScope flash = FlashScope.getPrevious(req);

        if (flash != null) {
            for (Map.Entry<String, Object> entry : flash.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof ActionBean) {
                    HttpServletRequest tmp = ((ActionBean) value).getContext().getRequest();
                    if (tmp != null) {
                        tmp = StripesRequestWrapper.findStripesWrapper(tmp);
                        if (tmp != null) {
                            tmp = (HttpServletRequest) ((StripesRequestWrapper) tmp).getRequest();
                            ((FlashRequest) tmp).setDelegate(req);
                        }
                    }
                }
                req.setAttribute(entry.getKey(), value);
            }
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
            flash.requestComplete();
        }
    }

    /** Does nothing. */
    public void destroy() {
        // Do nothing
    }
}
