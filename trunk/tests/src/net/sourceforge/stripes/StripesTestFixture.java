package net.sourceforge.stripes;

import net.sourceforge.stripes.mock.MockServletContext;
import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.controller.DispatcherServlet;

import java.util.Map;
import java.util.HashMap;

/**
 * Test fixture that sets up a MockServletContext in a way that it can then be
 * used be any test in Stripes.
 *
 * @author Tim Fennell
 */
public class StripesTestFixture {
    private static MockServletContext context;

    /**
     * Gets a reference to the test MockServletContext. If the context is not already
     * instantiated and setup, it will be built lazily.
     *
     * @return an instance of MockServletContext for testing wiith
     */
    public static synchronized MockServletContext getServletContext() {
        if (context == null) {
            context = new MockServletContext("test");

            // Add the Stripes Filter
            Map<String,String> filterParams = new HashMap<String,String>();
            filterParams.put("ActionResolver.UrlFilters", "tests");
            filterParams.put("ActionResolver.PackageFilters", "net.sourceforge.stripes.*");
            context.addFilter(StripesFilter.class, "StripesFilter", filterParams);

            // Add the Stripes Dispatcher
            context.setServlet(DispatcherServlet.class, "StripesDispatcher", null);
        }

        return context;
    }
}
