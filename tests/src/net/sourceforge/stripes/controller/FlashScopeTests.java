package net.sourceforge.stripes.controller;

import org.testng.annotations.Test;
import org.testng.Assert;
import net.sourceforge.stripes.mock.MockServletContext;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockHttpSession;
import net.sourceforge.stripes.StripesTestFixture;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.action.HandlesEvent;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Performs some basic tests of FlashScope usage.
 *
 * @author Tim Fennell
 */
@UrlBinding("/FlashScopeTests.action")
public class FlashScopeTests implements ActionBean {
    static final Pattern FLASH_ID_REGEX =
            Pattern.compile(".*" + StripesConstants.URL_KEY_FLASH_SCOPE_ID + "=(-?\\d+).*");

    private ActionBeanContext context;
    public ActionBeanContext getContext() { return context; }
    public void setContext(ActionBeanContext context) { this.context = context; }

    /** A test handler that moves all request parameters into a flash scope. */
    @DefaultHandler
    public Resolution flash() {
        HttpServletRequest req = getContext().getRequest();
        Map<String,String[]> params = (Map<String,String[]>) req.getParameterMap();
        
        for (Map.Entry<String,String[]> entry : params.entrySet()) {
            FlashScope flash = FlashScope.getCurrent(getContext().getRequest(), true);
            flash.put(entry.getKey(), entry.getValue()[0]);
        }

        return new RedirectResolution("/FlashScopeTests.action");
    }
    
    @HandlesEvent("FlashBean")
    public Resolution flashBean() {
        return new RedirectResolution("/FlashScopeTests.action").flash(this);
    }

    /** A do-nothing test handler. */
    @HandlesEvent("DoNothing")
    public Resolution doNothing() {
        return null;
    }

    @Test(groups="fast")
    public void positiveCase() throws Exception {
        MockServletContext ctx = StripesTestFixture.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, FlashScopeTests.class);
        trip.addParameter("foo", "foo123");
        trip.execute();

        String url = trip.getDestination();
        Matcher matcher = FLASH_ID_REGEX.matcher(url);
        Assert.assertTrue(matcher.matches(),
                          "Redirect URL should contain request parameter for flash scope id.");

        Assert.assertEquals("foo123", trip.getRequest().getAttribute("foo"),
                            "FlashScope should have inserted 'foo' into a request attribute.");

        MockRoundtrip trip2 = new MockRoundtrip
                (ctx, FlashScopeTests.class, (MockHttpSession) trip.getRequest().getSession());

        // Get the flash scope ID from the redirect URL and add it back as a parameter
        String id = matcher.group(1);
        trip2.addParameter(StripesConstants.URL_KEY_FLASH_SCOPE_ID, id);

        Assert.assertNull(trip2.getRequest().getAttribute("foo"),
                          "Request attribute 'foo' should not exist prior to request.");

        trip2.execute("DoNothing");
        Assert.assertEquals("foo123", trip2.getRequest().getAttribute("foo"),
                            "Request attribute 'foo' should have been set by FlashScope.");

        Assert.assertEquals(FlashScope.getAllFlashScopes(trip2.getRequest()).size(), 0,
                            "FlashScope should have been removed from session after use.");
        
        // Test flashing an ActionBean
        MockRoundtrip trip3 = new MockRoundtrip(ctx, FlashScopeTests.class, (MockHttpSession) trip
                .getRequest().getSession());

        // Get the flash scope ID from the redirect URL and add it back as a parameter
        trip3.addParameter(StripesConstants.URL_KEY_FLASH_SCOPE_ID, id);
        trip3.execute("FlashBean");

        try {
            ActionBeanContext tmp = trip3.getActionBean(getClass()).getContext();
            HttpServletResponse response = tmp.getResponse();
            HttpServletRequest request = tmp.getRequest();
            Assert.assertNotNull(request);
            Assert.assertNotNull(response);
            Assert.assertTrue(Proxy.class.isAssignableFrom(response.getClass()));
            Assert.assertEquals(StripesRequestWrapper.class, request.getClass());
            response.isCommitted();
            Assert.fail(
                    "Response should have thrown IllegalStateException after request cycle complete");
        }
        catch (IllegalStateException e) {
        }
    }
}
