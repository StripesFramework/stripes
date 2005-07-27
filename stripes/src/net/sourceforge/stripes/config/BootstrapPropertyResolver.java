package net.sourceforge.stripes.config;

import javax.servlet.ServletConfig;

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
    private ServletConfig servletConfig;

    /** Constructs a new BootstrapPropertyResolver with the given ServletConfig. */
    public BootstrapPropertyResolver(ServletConfig servletConfig) {
        setServletConfig(servletConfig);
    }

    /** Stores a reference to the dispatcher servlet's ServletConfig object. */
    public void setServletConfig(ServletConfig servletConfig) {
        this.servletConfig = servletConfig;
    }

    /** Returns a reference to the dispatcher servlet's ServletConfig object. */
    public ServletConfig getServletConfig(ServletConfig servletConfig) {
        return this.servletConfig;
    }

    /**
     * Fetches a configuration property in the manner described in the class level javadoc for
     * this class.
     *
     * @param key the String name of the configuration value to be looked up
     * @return String the value of the configuration item or null
     */
    public String getProperty(String key) {
        String value = this.servletConfig.getInitParameter(key);

        if (value == null) {
            value = this.servletConfig.getServletContext().getInitParameter(key);
        }

        if (value == null) {
            value = System.getProperty(key);
        }

        return value;
    }
}
