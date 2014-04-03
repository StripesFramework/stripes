package net.sourceforge.stripes.mock;

import net.sourceforge.stripes.StripesTestFixture;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.controller.StripesFilter;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestSts725Sts494 {

    @Test
    public void testSts725() {
        int count = 2;
            for (int i = 0; i < count; i++) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("ActionResolver.Packages", "foo.bar");
            MockServletContext mockServletContext = StripesTestFixture.createServletContext();
            try {
                Configuration config = StripesFilter.getConfiguration();
                Assert.assertNotNull(config, "config is null for context " + mockServletContext.getServletContextName());
            } finally {
                mockServletContext.close();
            }
        }
    }

    @Test
    public void testSts494() {
        final List<String> l = new ArrayList<String>();
        MockServletContext c = StripesTestFixture.createServletContext();
        try {
            c.addListener(new ServletContextListener() {
                public void contextInitialized(final ServletContextEvent servletContextEvent) {
                    l.add("init");
                }

                public void contextDestroyed(final ServletContextEvent servletContextEvent) {
                    l.add("destroy");
                }
            });
        } finally {
            c.close();
        }
        Assert.assertEquals(2, l.size());
        Assert.assertEquals("init", l.get(0));
        Assert.assertEquals("destroy", l.get(1));
    }
}
