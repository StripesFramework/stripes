package net.sourceforge.stripes.config;

import net.sourceforge.stripes.exception.StripesServletException;
import net.sourceforge.stripes.controller.ActionResolver;
import net.sourceforge.stripes.controller.ActionBeanPropertyBinder;

/**
 * Type safe interface for accessing configuration information used to configure Stripes. All
 * Configuration implementations are handed a reference to the BootstrapPropertyResolver to
 * enable them to find initial values and fully initialize themselves.  Through the
 * BootstrapPropertyResolver implementations also get access to the ServletConfig of the
 * DispatcherServlet which can be used for locating configuration values if desired.
 *
 * @author Tim Fennell
 */
public interface Configuration {
    /**
     * Called by the DispatcherServlet to initialize the Configuration. Any operations which may
     * fail and cause the Configuration to be inaccessible should be performed here (e.g.
     * opening a configuration file and reading the contents).
     *
     * @param resolver a BootStrapPropertyResolver which can be used to find any values required
     *        by the Configuration in order to initialize
     * @throws StripesServletException should be thrown if the Configuration cannot be initialized
     */
    void init(BootstrapPropertyResolver resolver) throws StripesServletException;

    /**
     * Implementations should implement this method to simply return a reference to the
     * BootstrapPropertyResolver passed to the Configuration at initialization time.
     *
     * @return BootstrapPropertyResolver the instance passed to the init() method
     */
    BootstrapPropertyResolver getBootstrapPropertyResolver();

    /**
     * Returns the Class that will be used to determine which ActionBean will handle a given
     * request, and which method in the ActionBean will be invoked.
     *
     * @return the Class representing the configured ActionResolver
     */
    Class<? extends ActionResolver> getActionResolver() throws StripesServletException;

    /**
     * Returns the class which is responsible for binding properties in the incoming request
     * into the ActionBean that has been bound to the request.
     *
     * @return Class the Class object for the ActionBeanPropertyBinder to be used
     * @throws StripesServletException if there is a problem locatin the Class object
     */
    Class<? extends ActionBeanPropertyBinder> getActionBeanPropertyBinder()
         throws StripesServletException;
}
