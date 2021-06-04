package org.stripesframework.spring;

import java.util.HashMap;
import java.util.Map;

import org.stripesframework.web.config.BootstrapPropertyResolver;
import org.stripesframework.web.config.Configuration;
import org.stripesframework.web.config.DefaultConfiguration;
import org.stripesframework.web.controller.DispatcherServlet;
import org.stripesframework.web.controller.StripesFilter;
import org.stripesframework.web.mock.MockFilterConfig;
import org.stripesframework.web.mock.MockServletContext;


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
      return new MockServletContext("test").addFilter(StripesFilter.class, "StripesFilter", getDefaultFilterParams())
            .setServlet(DispatcherServlet.class, "StripesDispatcher", null);
   }

   /** Gets a reference to the default configuration, which can be used for simple testing. */
   public static synchronized Configuration getDefaultConfiguration() {
      if ( configuration == null ) {
         Configuration configuration = new DefaultConfiguration();
         MockFilterConfig filterConfig = new MockFilterConfig();
         filterConfig.addAllInitParameters(getDefaultFilterParams());
         MockServletContext mockServletContext = createServletContext();
         try {
            filterConfig.setServletContext(mockServletContext);
            configuration.setBootstrapPropertyResolver(new BootstrapPropertyResolver(filterConfig));
            configuration.init();
            StripesTestFixture.configuration = configuration;
         }
         finally {
            mockServletContext.close();
         }
      }

      return configuration;
   }

   /** Gets a map containing the default initialization parameters for StripesFilter */
   public static Map<String, String> getDefaultFilterParams() {
      Map<String, String> map = new HashMap<>();
      map.put("ActionResolver.Packages", "org.stripesframework.web");
      map.put("LocalePicker.Class", "org.stripesframework.web.localization.MockLocalePicker");
      return map;
   }
}
