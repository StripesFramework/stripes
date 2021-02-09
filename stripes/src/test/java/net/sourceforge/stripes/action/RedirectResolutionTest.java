package net.sourceforge.stripes.action;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;

import net.sourceforge.stripes.FilterEnabledTestBase;
import net.sourceforge.stripes.mock.MockHttpServletRequest;
import net.sourceforge.stripes.mock.MockHttpServletResponse;


public class RedirectResolutionTest extends FilterEnabledTestBase {

   @Test
   public void testPermanantRedirect() throws Exception {
      RedirectResolution resolution = new RedirectResolution("http://www.stripesframework.org", false).setPermanent(true);
      MockHttpServletResponse response = new MockHttpServletResponse();
      resolution.execute(buildMockServletRequest(), response);

      assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_MOVED_PERMANENTLY);
      assertThat(response.getHeaderMap().get("Location").iterator().next()).isEqualTo("http://www.stripesframework.org");
   }

   @Test
   public void testPermanantRedirectWithParameters() throws Exception {
      RedirectResolution resolution = new RedirectResolution("http://www.stripesframework.org", false).setPermanent(true).addParameter("test", "test");
      MockHttpServletResponse response = new MockHttpServletResponse();
      resolution.execute(buildMockServletRequest(), response);

      assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_MOVED_PERMANENTLY);
      assertThat(response.getHeaderMap().get("Location").iterator().next()).isEqualTo("http://www.stripesframework.org?test=test");
   }

   @Test
   public void testTemporaryRedirect() throws Exception {
      RedirectResolution resolution = new RedirectResolution("http://www.stripesframework.org", false);
      MockHttpServletResponse response = new MockHttpServletResponse();
      resolution.execute(buildMockServletRequest(), response);

      assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_MOVED_TEMPORARILY);
      assertThat(response.getRedirectUrl()).isEqualTo("http://www.stripesframework.org");
   }

   @Test
   public void testTemporaryRedirectWithParameters() throws Exception {
      RedirectResolution resolution = new RedirectResolution("http://www.stripesframework.org", false).addParameter("test", "test");
      MockHttpServletResponse response = new MockHttpServletResponse();
      resolution.execute(buildMockServletRequest(), response);

      assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_MOVED_TEMPORARILY);
      assertThat(response.getRedirectUrl()).isEqualTo("http://www.stripesframework.org?test=test");
   }

   //helper method
   private MockHttpServletRequest buildMockServletRequest() {
      MockHttpServletRequest request = new MockHttpServletRequest("/context", "/whatever");
      request.addLocale(Locale.US);
      return request;
   }

}
