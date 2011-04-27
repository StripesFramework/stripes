package net.sourceforge.stripes.action;

import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import net.sourceforge.stripes.StripesTestFixture;
import net.sourceforge.stripes.mock.MockHttpServletRequest;
import net.sourceforge.stripes.mock.MockHttpServletResponse;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class RedirectResolutionTest {
    
    @BeforeClass
    public void setupServletContext(){
        StripesTestFixture.getServletContext();
    }
    //helper method
    private MockHttpServletRequest buildMockServletRequest(){
        MockHttpServletRequest request = new MockHttpServletRequest("/context", "/whatever");
        request.addLocale(Locale.US);
        return request;
    }

    @Test(groups = "fast")
    public void testPermanantRedirect() throws Exception {
        RedirectResolution resolution = new RedirectResolution("http://www.stripesframework.org", false).setPermanent(true);
        MockHttpServletResponse response = new MockHttpServletResponse();
        resolution.execute(buildMockServletRequest(), response);
        
        Assert.assertEquals(response.getStatus(), HttpServletResponse.SC_MOVED_PERMANENTLY);
        Assert.assertEquals(response.getHeaderMap().get("Location").iterator().next(), "http://www.stripesframework.org");
    }

    @Test(groups = "fast")
    public void testTemporaryRedirect() throws Exception {
        RedirectResolution resolution = new RedirectResolution("http://www.stripesframework.org", false);
        MockHttpServletResponse response = new MockHttpServletResponse();
        resolution.execute(buildMockServletRequest(), response);
        
        Assert.assertEquals(response.getStatus(), HttpServletResponse.SC_MOVED_TEMPORARILY);
        Assert.assertEquals(response.getRedirectUrl(), "http://www.stripesframework.org");
    }
    
    @Test(groups = "fast")
    public void testPermanantRedirectWithParameters() throws Exception {
        RedirectResolution resolution = new RedirectResolution("http://www.stripesframework.org", false).setPermanent(true).addParameter("test", "test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        resolution.execute(buildMockServletRequest(), response);
        
        Assert.assertEquals(response.getStatus(), HttpServletResponse.SC_MOVED_PERMANENTLY);
        Assert.assertEquals(response.getHeaderMap().get("Location").iterator().next(), "http://www.stripesframework.org?test=test");
    }

    @Test(groups = "fast")
    public void testTemporaryRedirectWithParameters() throws Exception {
        RedirectResolution resolution = new RedirectResolution("http://www.stripesframework.org", false).addParameter("test", "test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        resolution.execute(buildMockServletRequest(), response);
        
        Assert.assertEquals(response.getStatus(), HttpServletResponse.SC_MOVED_TEMPORARILY);
        Assert.assertEquals(response.getRedirectUrl(), "http://www.stripesframework.org?test=test");
    }

}
