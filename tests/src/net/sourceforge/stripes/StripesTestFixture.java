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

    /**
     * Create and return a new MockServletContext.
     *
     * @return an instance of MockServletContext for testing wiith
     */
    public static synchronized MockServletContext createServletContext() {
        Map<String,String> filterParams = new HashMap<String,String>();
        filterParams.put("ActionResolver.Packages", "net.sourceforge.stripes");
        filterParams.put("LocalePicker.Class", "net.sourceforge.stripes.localization.MockLocalePicker");
        return new MockServletContext("test")
            .addFilter(StripesFilter.class, "StripesFilter", filterParams)
            .setServlet(DispatcherServlet.class, "StripesDispatcher", null);
    }

}
