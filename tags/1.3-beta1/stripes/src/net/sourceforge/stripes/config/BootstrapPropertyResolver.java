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
package net.sourceforge.stripes.config;

import javax.servlet.FilterConfig;

/**
 * <p>Resolves configuration properties that are used to bootstrap the system.  Essentially this boils
 * down to a handful of properties that are needed to figure out which configuration class should
 * be instantiated, and any values needed by that configuration class to locate configuration
 * information.</p>
 *
 * <p>Properties are looked for in the following order:
 *  <ul>
 *      <li>Initialization Parameters for the Dispatcher servlet</li>
 *      <li>Initialization Parameters for the Servlet Context</li>
 *      <li>Java System Properties</li>
 *  </ul>
 * </p>
 *
 * @author Tim Fennell
 */
public class BootstrapPropertyResolver {
    private FilterConfig filterConfig;

    /** Constructs a new BootstrapPropertyResolver with the given ServletConfig. */
    public BootstrapPropertyResolver(FilterConfig filterConfig) {
        setFilterConfig(filterConfig);
    }

    /** Stores a reference to the filter's FilterConfig object. */
    public void setFilterConfig(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

    /** Returns a reference to the StripesFilter's FilterConfig object. */
    public FilterConfig getFilterConfig() {
        return this.filterConfig;
    }

    /**
     * Fetches a configuration property in the manner described in the class level javadoc for
     * this class.
     *
     * @param key the String name of the configuration value to be looked up
     * @return String the value of the configuration item or null
     */
    public String getProperty(String key) {
        String value = this.filterConfig.getInitParameter(key);

        if (value == null) {
            value = this.filterConfig.getServletContext().getInitParameter(key);
        }

        if (value == null) {
            value = System.getProperty(key);
        }

        return value;
    }
}
