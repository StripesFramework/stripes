package net.sourceforge.stripes.mocktest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.junit.jupiter.api.Test;

import net.sourceforge.stripes.StripesTestFixture;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.mock.MockServletContext;


public class TestSts725Sts494 {

   @Test
   public void testSts494() {
      final List<String> l = new ArrayList<>();
      MockServletContext c = StripesTestFixture.createServletContext();
      try {
         c.addListener(new ServletContextListener() {

            @Override
            public void contextDestroyed( final ServletContextEvent servletContextEvent ) {
               l.add("destroy");
            }

            @Override
            public void contextInitialized( final ServletContextEvent servletContextEvent ) {
               l.add("init");
            }
         });
      }
      finally {
         c.close();
      }
      assertThat(l).containsExactly("init", "destroy");
   }

   @Test
   public void testSts725() {
      int count = 2;
      for ( int i = 0; i < count; i++ ) {
         MockServletContext mockServletContext = StripesTestFixture.createServletContext();
         try {
            Configuration config = StripesFilter.getConfiguration();
            assertThat(config).isNotNull().describedAs("config is null for context " + mockServletContext.getServletContextName());
         }
         finally {
            mockServletContext.close();
         }
      }
   }
}
