package net.sourceforge.stripes;

import java.util.HashMap;
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
    private static Configuration configuration;

    /**
     * Create and return a new MockServletContext.
     *
     * @return an instance of MockServletContext for testing wiith
     */
    public static synchronized MockServletContext createServletContext() {
        return new MockServletContext("test")
                .addFilter(StripesFilter.class, "StripesFilter", getDefaultFilterParams())
                .setServlet(DispatcherServlet.class, "StripesDispatcher", null);
    }

    /** Gets a reference to the default configuration, which can be used for simple testing. */
    public static synchronized Configuration getDefaultConfiguration() {
        if (configuration == null) {
            Configuration configuration = new DefaultConfiguration();
            MockFilterConfig filterConfig = new MockFilterConfig();
            filterConfig.addAllInitParameters(getDefaultFilterParams());
            MockServletContext mockServletContext = createServletContext();
            try {
                filterConfig.setServletContext(mockServletContext);
                configuration.setBootstrapPropertyResolver(new BootstrapPropertyResolver(filterConfig));
                configuration.init();
                StripesTestFixture.configuration = configuration;
            } finally {
                mockServletContext.close();
            }
        }

        return configuration;
    }

    /** Gets a map containing the default initialization parameters for StripesFilter */
    public static Map<String, String> getDefaultFilterParams() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("ActionResolver.Packages", "net.sourceforge.stripes");
        map.put("LocalePicker.Class", "net.sourceforge.stripes.localization.MockLocalePicker");
        return map;
    }
}
