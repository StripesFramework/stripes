package net.sourceforge.stripes;

import java.util.Collections;
import java.util.Map;

import net.sourceforge.stripes.config.BootstrapPropertyResolver;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.config.DefaultConfiguration;
import net.sourceforge.stripes.controller.DispatcherServlet;
import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.mock.MockFilterConfig;
import net.sourceforge.stripes.mock.MockServletContext;

/**
 * Test fixture that sets up a MockServletContext in a way that it can then be
 * used be any test in Stripes.
 *
 * @author Tim Fennell
 */
public class StripesTestFixture {
    private static MockServletContext context;
    private static Configuration configuration;

    /**
     * Gets a reference to the test MockServletContext. If the context is not already
     * instantiated and setup, it will be built lazily.
     *
     * @return an instance of MockServletContext for testing wiith
     */
    public static synchronized MockServletContext getServletContext() {
        if (context == null) {
            context = new MockServletContext("test");
            context.addFilter(StripesFilter.class, "StripesFilter", getDefaultFilterParams());

            // Add the Stripes Dispatcher
            context.setServlet(DispatcherServlet.class, "StripesDispatcher", null);
        }

        return context;
    }

    /** Gets a reference to the default configuration, which can be used for simple testing. */
    public static synchronized Configuration getDefaultConfiguration() {
        if (configuration == null) {
            Configuration configuration = new DefaultConfiguration();
            MockFilterConfig filterConfig = new MockFilterConfig();
            filterConfig.addAllInitParameters(getDefaultFilterParams());
            filterConfig.setServletContext(getServletContext());
            configuration.setBootstrapPropertyResolver(new BootstrapPropertyResolver(filterConfig));
            configuration.init();
            StripesTestFixture.configuration = configuration;
        }

        return configuration;
    }

    /** Gets a map containing the default initialization parameters for StripesFilter */
    public static Map<String, String> getDefaultFilterParams() {
        return Collections.singletonMap("ActionResolver.Packages", "net.sourceforge.stripes");
    }
}
