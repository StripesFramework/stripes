package net.sourceforge.stripes.mocktest;

import static org.assertj.core.api.Assertions.assertThat;

import javax.servlet.http.Cookie;

import org.junit.jupiter.api.Test;

import net.sourceforge.stripes.StripesTestFixture;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;


/**
 * Unit test that is designed to test how MockRoundTrip does NOT copy cookies from the request to the
 * response.
 *
 * @author Scott Archer
 */
@UrlBinding("/mock/MockRoundtripCookies.test")
public class TestMockRoundtripCookies implements ActionBean {

   private ActionBeanContext context;

   @Override
   public ActionBeanContext getContext() {
      return context;
   }

   /** A very simple add event that returns a Forward reslution. */
   @DefaultHandler
   public Resolution index() {
      context.getResponse().addCookie(new Cookie("testCookie", "testCookie"));
      return new ForwardResolution("/mock/success.jsp");
   }

   @Override
   public void setContext( ActionBeanContext context ) {
      this.context = context;
   }

   // /////////////////////////////////////////////////////////////////////////
   // End of ActionBean methods and beginning of test methods. Everything
   // below this line is a test!
   // /////////////////////////////////////////////////////////////////////////

   @Test
   public void testDefaultEvent() throws Exception {
      // Setup the servlet engine
      MockServletContext ctx = StripesTestFixture.createServletContext();
      try {
         MockRoundtrip trip = new MockRoundtrip(ctx, TestMockRoundtripCookies.class);

         Cookie[] cookies = new Cookie[] { new Cookie("Cookie", "1"), new Cookie("Monster", "2"), new Cookie("Test", "3") };
         trip.getRequest().setCookies(cookies);
         trip.execute();

         assertThat(trip.getResponse().getCookies()).hasSize(1);
         assertThat(trip.getResponse().getCookies()[0].getName()).isEqualTo("testCookie"); // Only the cookie set by the action
      }
      finally {
         ctx.close();
      }
   }
}
