package net.sourceforge.stripes.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;

import net.sourceforge.stripes.StripesTestFixture;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.mock.MockHttpSession;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;


/**
 * Performs some basic tests of FlashScope usage.
 *
 * @author Tim Fennell
 */
@UrlBinding("/FlashScopeTests.action")
public class FlashScopeTests implements ActionBean {

   static final Pattern FLASH_ID_REGEX = Pattern.compile(".*" + StripesConstants.URL_KEY_FLASH_SCOPE_ID + "=(-?\\d+).*");

   private ActionBeanContext context;

   /** A do-nothing test handler. */
   @HandlesEvent("DoNothing")
   public Resolution doNothing() {
      return null;
   }

   /** A test handler that moves all request parameters into a flash scope. */
   @DefaultHandler
   public Resolution flash() {
      HttpServletRequest req = getContext().getRequest();
      Map<String, String[]> params = req.getParameterMap();

      for ( Map.Entry<String, String[]> entry : params.entrySet() ) {
         FlashScope flash = FlashScope.getCurrent(getContext().getRequest(), true);
         flash.put(entry.getKey(), entry.getValue()[0]);
      }

      return new RedirectResolution("/FlashScopeTests.action");
   }

   @HandlesEvent("FlashBean")
   public Resolution flashBean() {
      return new RedirectResolution("/FlashScopeTests.action").flash(this);
   }

   @Override
   public ActionBeanContext getContext() { return context; }

   @Test
   public void positiveCase() throws Exception {
      MockServletContext ctx = StripesTestFixture.createServletContext();
      try {
         MockRoundtrip trip = new MockRoundtrip(ctx, FlashScopeTests.class);
         trip.addParameter("foo", "foo123");
         trip.execute();

         String url = trip.getDestination();
         Matcher matcher = FLASH_ID_REGEX.matcher(url);
         assertThat(matcher.matches()).describedAs("Redirect URL should contain request parameter for flash scope id.").isTrue();

         assertThat(trip.getRequest().getAttribute("foo")).describedAs("FlashScope should have inserted 'foo' into a request attribute.").isEqualTo("foo123");

         MockRoundtrip trip2 = new MockRoundtrip(ctx, FlashScopeTests.class, (MockHttpSession)trip.getRequest().getSession());

         // Get the flash scope ID from the redirect URL and add it back as a parameter
         String id = matcher.group(1);
         trip2.addParameter(StripesConstants.URL_KEY_FLASH_SCOPE_ID, id);

         assertThat(trip2.getRequest().getAttribute("foo")).describedAs("Request attribute 'foo' should not exist prior to request.").isNull();

         trip2.execute("DoNothing");
         assertThat(trip2.getRequest().getAttribute("foo")).describedAs("Request attribute 'foo' should have been set by FlashScope.").isEqualTo("foo123");

         assertThat(FlashScope.getAllFlashScopes(trip2.getRequest())).describedAs("FlashScope should have been removed from session after use.").isEmpty();

         // Test flashing an ActionBean
         MockRoundtrip trip3 = new MockRoundtrip(ctx, FlashScopeTests.class, (MockHttpSession)trip.getRequest().getSession());

         // Get the flash scope ID from the redirect URL and add it back as a parameter
         trip3.addParameter(StripesConstants.URL_KEY_FLASH_SCOPE_ID, id);
         trip3.execute("FlashBean");

         ActionBeanContext tmp = trip3.getActionBean(getClass()).getContext();
         HttpServletResponse response = tmp.getResponse();
         HttpServletRequest request = tmp.getRequest();
         assertThat(request).isNotNull();
         assertThat(response).isNotNull();
         assertThat(Proxy.class.isAssignableFrom(response.getClass())).isTrue();
         assertThat(request.getClass()).isEqualTo(StripesRequestWrapper.class);

         Throwable throwable = catchThrowable(response::isCommitted);

         assertThat(throwable).describedAs("Response should have thrown IllegalStateException after request cycle complete")
               .isInstanceOf(IllegalStateException.class);
      }
      finally {
         ctx.close();
      }
   }

   @Override
   public void setContext( ActionBeanContext context ) { this.context = context; }
}
