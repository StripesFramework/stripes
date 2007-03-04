package net.sourceforge.stripes.mock;

import net.sourceforge.stripes.StripesTestFixture;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.Validate;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.StringReader;

/**
 * Unit test that is designed to do some fairly simple testing of the mock engine to ensure that
 * it works correctly.
 *
 * @author Tim Fennell
 */
@UrlBinding("/mock/MockRoundtrip.test")
public class TestMockRoundtrip implements ActionBean {
    private ActionBeanContext context;
    private double lhs;
    private double rhs;
    private double result;

    public void setContext(ActionBeanContext context) { this.context = context; }
    public ActionBeanContext getContext() { return this.context; }

    @Validate(required=true)
    public void setLhs(double lhs) { this.lhs = lhs; }
    public double getLhs() { return lhs; }

    @Validate(required=true)
    public void setRhs(double rhs) { this.rhs = rhs; }
    public double getRhs() { return rhs; }

    @Validate(ignore=true)
    public void setResult(double result) { this.result = result; }
    public double getResult() { return result; }

    /** A very simple add event that returns a Forward reslution. */
    @DefaultHandler @HandlesEvent("add")
    public Resolution add() {
        this.result = lhs + rhs;
        return new ForwardResolution("/mock/success.jsp");
    }

    /** A very simple add event that returns a Redirect reslution. */
    @HandlesEvent("addAndRedirect")
    public Resolution addAndRedirect() {
        this.result = lhs + rhs;
        return new RedirectResolution("/mock/success.jsp");
    }

    /** A very simple add event that returns the result in the response stream. */
    @HandlesEvent("addAndStream")
    public Resolution addAndStream() {
        this.result = lhs + rhs;
        return new StreamingResolution("text/plain", new StringReader(String.valueOf(this.result)));
    }

    /** A very simple multiplication event. */
    @HandlesEvent("multiply")
    public Resolution multiply() {
        this.result = lhs * rhs;
        return new ForwardResolution("/mock/success.jsp");
    }

    /** A divide event that validates that we're not dividing by zero. */
    @HandlesEvent("divide")
    public Resolution divide() {
        if (rhs == 0) {
            getContext().getValidationErrors().add("rhs", new SimpleError("Rhs may not be zero"));
            return getContext().getSourcePageResolution();
        }

        this.result = lhs / rhs;
        getContext().getRequest().setAttribute("integerResult", (int) this.result);
        return new ForwardResolution("/mock/success.jsp");
    }

    ///////////////////////////////////////////////////////////////////////////
    // End of ActionBean methods and beginning of test methods. Everything
    // below this line is a test!
    ///////////////////////////////////////////////////////////////////////////

    @Test(groups="fast")
    public void testDefaultEvent() throws Exception {
        // Setup the servlet engine
        MockServletContext ctx = StripesTestFixture.getServletContext();

        MockRoundtrip trip = new MockRoundtrip(ctx, TestMockRoundtrip.class);
        trip.setParameter("lhs", "2");
        trip.setParameter("rhs", "2");
        trip.execute();

        TestMockRoundtrip bean = trip.getActionBean(TestMockRoundtrip.class);
        Assert.assertEquals(bean.getResult(), 4.0);
        Assert.assertEquals(trip.getValidationErrors().size(), 0);
        Assert.assertEquals(trip.getDestination(), "/mock/success.jsp");
    }

    @Test(groups="fast")
    public void testWithNamedEvent() throws Exception {
        // Setup the servlet engine
        MockServletContext ctx = StripesTestFixture.getServletContext();

        MockRoundtrip trip = new MockRoundtrip(ctx, TestMockRoundtrip.class);
        trip.setParameter("lhs", "2");
        trip.setParameter("rhs", "2");
        trip.execute("add");

        TestMockRoundtrip bean = trip.getActionBean(TestMockRoundtrip.class);
        Assert.assertEquals(bean.getResult(), 4.0);
        Assert.assertEquals(trip.getValidationErrors().size(), 0);
        Assert.assertEquals(trip.getDestination(), "/mock/success.jsp");
    }

    @Test(groups="fast")
    public void testWithRedirect() throws Exception {
        // Setup the servlet engine
        MockServletContext ctx = StripesTestFixture.getServletContext();

        MockRoundtrip trip = new MockRoundtrip(ctx, TestMockRoundtrip.class);
        trip.setParameter("lhs", "2");
        trip.setParameter("rhs", "2");
        trip.execute("addAndRedirect");

        TestMockRoundtrip bean = trip.getActionBean(TestMockRoundtrip.class);
        Assert.assertEquals(bean.getResult(), 4.0);
        Assert.assertEquals(trip.getValidationErrors().size(), 0);
        Assert.assertEquals(trip.getDestination(), "/mock/success.jsp");
        Assert.assertNull(trip.getRequest().getForwardUrl());
    }

    @Test(groups="fast")
    public void testWithStreamingOutput() throws Exception {
        // Setup the servlet engine
        MockServletContext ctx = StripesTestFixture.getServletContext();

        MockRoundtrip trip = new MockRoundtrip(ctx, TestMockRoundtrip.class);
        trip.setParameter("lhs", "2");
        trip.setParameter("rhs", "2");
        trip.execute("addAndStream");

        TestMockRoundtrip bean = trip.getActionBean(TestMockRoundtrip.class);
        Assert.assertEquals(bean.getResult(), 4.0);
        Assert.assertEquals(trip.getValidationErrors().size(), 0);
        Assert.assertNull(trip.getDestination());
        Assert.assertEquals(trip.getOutputString(), "4.0");
    }

    @Test(groups="fast")
    public void testWithValidationErrors() throws Exception {
        // Setup the servlet engine
        MockServletContext ctx = StripesTestFixture.getServletContext();

        MockRoundtrip trip = new MockRoundtrip(ctx, TestMockRoundtrip.class);
        trip.setParameter("lhs", "");
        trip.setParameter("rhs", "abc");
        trip.execute();

        Assert.assertEquals(trip.getValidationErrors().size(), 2); // both fields in error
        Assert.assertEquals(trip.getDestination(), MockRoundtrip.DEFAULT_SOURCE_PAGE);
    }

    @Test(groups="fast")
    public void testWithDifferentNamedEvent() throws Exception {
        // Setup the servlet engine
        MockServletContext ctx = StripesTestFixture.getServletContext();

        MockRoundtrip trip = new MockRoundtrip(ctx, TestMockRoundtrip.class);
        trip.setParameter("lhs", "4");
        trip.setParameter("rhs", "4");
        trip.execute("multiply");

        TestMockRoundtrip bean = trip.getActionBean(TestMockRoundtrip.class);
        Assert.assertEquals(bean.getResult(), 16.0);
        Assert.assertEquals(trip.getValidationErrors().size(), 0);
        Assert.assertEquals(trip.getDestination(), "/mock/success.jsp");
    }

    @Test(groups="fast")
    public void testWithCustomValidationErrors() throws Exception {
        // Setup the servlet engine
        MockServletContext ctx = StripesTestFixture.getServletContext();

        MockRoundtrip trip = new MockRoundtrip(ctx, TestMockRoundtrip.class);
        trip.setParameter("lhs", "2");
        trip.setParameter("rhs", "0");
        trip.execute("divide");

        Assert.assertEquals(trip.getValidationErrors().size(), 1);
        Assert.assertEquals(trip.getDestination(), MockRoundtrip.DEFAULT_SOURCE_PAGE);
    }

    @Test(groups="fast")
    public void testFetchingRequestAttributes() throws Exception {
        // Setup the servlet engine
        MockServletContext ctx = StripesTestFixture.getServletContext();

        MockRoundtrip trip = new MockRoundtrip(ctx, TestMockRoundtrip.class);
        trip.setParameter("lhs", "10");
        trip.setParameter("rhs", "4");
        trip.execute("divide");

        TestMockRoundtrip bean = trip.getActionBean(TestMockRoundtrip.class);
        Assert.assertEquals(bean.getResult(), 2.5);
        Assert.assertEquals(trip.getValidationErrors().size(), 0);
        Assert.assertEquals(trip.getDestination(), "/mock/success.jsp");
        Assert.assertEquals(trip.getRequest().getAttribute("integerResult"), new Integer(2));
    }
    
    @Test(groups="fast")
    public void testRequestCaseInsensitive(){
    	MockHttpServletRequest request = new MockHttpServletRequest("", "");
    	request.addHeader("User-Agent", "Netscape/6.0");
    	Assert.assertEquals(request.getHeader("User-Agent"), "Netscape/6.0", MockHttpServletRequest.class + ".addHeader/getHeader do not properly");
    }
}
