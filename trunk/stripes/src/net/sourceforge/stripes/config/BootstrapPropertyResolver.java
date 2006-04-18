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
