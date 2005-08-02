package net.sourceforge.stripes.controller;

import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.config.BootstrapPropertyResolver;
import net.sourceforge.stripes.config.DefaultConfiguration;
import net.sourceforge.stripes.exception.StripesServletException;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.File;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA. User: tfenne Date: Aug 1, 2005 Time: 6:29:52 PM To change this template
 * use File | Settings | File Templates.
 */
public class StripesFilter implements Filter {
    /** Key used to lookup the name of the Configuration class used to configure Stripes. */
    public static final String CONFIG_CLASS = "Configuration.Class";

    /** Path to a temporary directory that will be used to process file uploads. */
    protected String temporaryDirectoryPath;

    /** Log used throughout the class. */
    private static Log log = Log.getInstance(StripesFilter.class);

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
     * Performs the necessary initialization for the StripesFilter.  Mainly this involved deciding
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
                Class clazz = Class.forName(configurationClassName);
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
            this.configuration = new DefaultConfiguration();
        }

        this.configuration.setBootstrapPropertyResolver(bootstrap);
        this.configuration.init();

        // Figure out where the temp directory is, and store that info
        File tempDir = (File) filterConfig.getServletContext().getAttribute("javax.servlet.context.tempdir");
        if (tempDir != null) {
            this.temporaryDirectoryPath = tempDir.getAbsolutePath();
        }
        else {
            this.temporaryDirectoryPath = System.getProperty("java.io.tmpdir");
        }
    }

    /**
     * Returns the Configuration that is being used to process the current request.
     */
    public static Configuration getConfiguration() {
        return StripesFilter.configurationStash.get();
    }


    /**
     *
     * @param servletRequest
     * @param servletResponse
     * @param filterChain
     * @throws IOException
     * @throws ServletException
     */
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;

        // Pop the configuration into thread local
        StripesFilter.configurationStash.set(this.configuration);

        // Figure out the locale to use, and then wrap the request
        Locale locale = this.configuration.getLocalePicker().pickLocale(httpRequest);
        StripesRequestWrapper request = wrapRequest(httpRequest);
        request.setLocale(locale);
        log.info("LocalePicker selected locale: ", locale);

        // Execute the rest of the chain
        filterChain.doFilter(request, servletResponse);

        // Once the request is processed, take the Configuration back out of thread local
        StripesFilter.configurationStash.remove();
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
        String tempDirPath = getTempDirectoryPath();

        StripesRequestWrapper request =
                new StripesRequestWrapper(servletRequest, tempDirPath, Integer.MAX_VALUE);
        return request;
    }

    /** Returns the path to the temporary directory that is used to store file uploads. */
    protected String getTempDirectoryPath() {
        return this.temporaryDirectoryPath;
    }


    /** Does nothing. */
    public void destroy() {
        // Do nothing
    }
}
