package net.sourceforge.stripes.action;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;
import net.sourceforge.stripes.FilterEnabledTestBase;
import net.sourceforge.stripes.mock.MockHttpServletRequest;
import net.sourceforge.stripes.mock.MockHttpServletResponse;
import org.junit.Assert;
import org.junit.Test;

public class RedirectResolutionTest extends FilterEnabledTestBase {

  // helper method
  private MockHttpServletRequest buildMockServletRequest() {
    MockHttpServletRequest request = new MockHttpServletRequest("/context", "/whatever");
    request.addLocale(Locale.US);
    return request;
  }

  @Test
  public void testPermanentRedirect() throws Exception {
    RedirectResolution resolution =
        new RedirectResolution("https://www.stripesframework.org", false).setPermanent(true);
    MockHttpServletResponse response = new MockHttpServletResponse();
    resolution.execute(buildMockServletRequest(), response);

    Assert.assertEquals(response.getStatus(), HttpServletResponse.SC_MOVED_PERMANENTLY);
    Assert.assertEquals(
        response.getHeaderMap().get("Location").iterator().next(),
        "https://www.stripesframework.org");
  }

  @Test
  public void testTemporaryRedirect() throws Exception {
    RedirectResolution resolution =
        new RedirectResolution("https://www.stripesframework.org", false);
    MockHttpServletResponse response = new MockHttpServletResponse();
    resolution.execute(buildMockServletRequest(), response);

    Assert.assertEquals(response.getStatus(), HttpServletResponse.SC_MOVED_TEMPORARILY);
    Assert.assertEquals(response.getRedirectUrl(), "https://www.stripesframework.org");
  }

  @Test
  public void testPermanentRedirectWithParameters() throws Exception {
    RedirectResolution resolution =
        new RedirectResolution("https://www.stripesframework.org", false)
            .setPermanent(true)
            .addParameter("test", "test");
    MockHttpServletResponse response = new MockHttpServletResponse();
    resolution.execute(buildMockServletRequest(), response);

    Assert.assertEquals(response.getStatus(), HttpServletResponse.SC_MOVED_PERMANENTLY);
    Assert.assertEquals(
        response.getHeaderMap().get("Location").iterator().next(),
        "https://www.stripesframework.org?test=test");
  }

  @Test
  public void testTemporaryRedirectWithParameters() throws Exception {
    RedirectResolution resolution =
        new RedirectResolution("https://www.stripesframework.org", false)
            .addParameter("test", "test");
    MockHttpServletResponse response = new MockHttpServletResponse();
    resolution.execute(buildMockServletRequest(), response);

    Assert.assertEquals(response.getStatus(), HttpServletResponse.SC_MOVED_TEMPORARILY);
    Assert.assertEquals(response.getRedirectUrl(), "https://www.stripesframework.org?test=test");
  }
}
