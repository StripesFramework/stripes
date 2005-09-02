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
import java.util.regex.Pattern;
import java.util.regex.Matcher;

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

    /** Key used to lookup the name of the maximum post size. */
    public static final String MAX_POST = "FileUpload.MaximumPostSize";

    /** Path to a temporary directory that will be used to process file uploads. */
    protected String temporaryDirectoryPath;

    /** Log used throughout the class. */
    private static Log log = Log.getInstance(StripesFilter.class);

    /** The configuration instance for Stripes. */
    private Configuration configuration;

    /** Stores the maximum post size for a form upload request. */
    private int maxPostSize = 1024 * 1024 * 10;

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

        // See if a maximum post size was configured
        String limit = bootstrap.getProperty(MAX_POST);
        if (limit != null) {
            Pattern pattern = Pattern.compile("([\\d,]+)([kKmMgG]?).*");
            Matcher matcher = pattern.matcher(limit);
            if (!matcher.matches()) {
                log.warn("Did not understand value of configuration parameter ", MAX_POST,
                         " You supplied: ", limit, ". Valid values are any string of numbers ",
                         "optionally followed by (case insensitive) [k|kb|m|mb|g|gb]. ",
                         "Default value of ", this.maxPostSize, " bytes will be used instead.");
            }
            else {
                String digits = matcher.group(1);
                String suffix = matcher.group(2).toLowerCase();
                int number = Integer.parseInt(digits);

                if ("k".equals(suffix)) { number = number * 1024; }
                else if ("m".equals(suffix)) {  number = number * 1024 * 1024; }
                else if ("g".equals(suffix)) { number = number * 1024 * 1024 * 1024; }

                this.maxPostSize = number;
                log.info("Configured file upload post size limit: ", number, " bytes.");
            }
        }

        Package pkg = getClass().getPackage();
        log.info("Stripes Initialization Complete. Version: ", pkg.getSpecificationVersion(),
                 ", Build: ", pkg.getImplementationVersion());
    }

    /**
     * Returns the Configuration that is being used to process the current request.
     */
    public static Configuration getConfiguration() {
        return StripesFilter.configurationStash.get();
    }

    /**
     * Performs the primary work of the filter, including constructing a StripesRequestWrapper to
     * wrap the HttpServletRequest, and using the configured LocalePicker to decide which
     * Locale will be used to process the request.
     */
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        try {
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
        }
        finally {
            // Once the request is processed, take the Configuration back out of thread local
            StripesFilter.configurationStash.remove();
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
        String tempDirPath = getTempDirectoryPath();

        StripesRequestWrapper request =
                new StripesRequestWrapper(servletRequest, tempDirPath, this.maxPostSize);
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
