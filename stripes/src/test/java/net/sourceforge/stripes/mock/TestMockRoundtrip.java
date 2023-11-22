package net.sourceforge.stripes.mock;

import java.io.StringReader;

import net.sourceforge.stripes.FilterEnabledTestBase;
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

import org.junit.Test;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.AfterClass;


/**
 * Unit test that is designed to do some fairly simple testing of the mock engine to ensure that
 * it works correctly.
 *
 * @author Tim Fennell
 */
@UrlBinding("/mock/MockRoundtrip.test")
public class TestMockRoundtrip extends FilterEnabledTestBase implements ActionBean {
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


    @Test
    public void testDefaultEvent() throws Exception {
        MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), TestMockRoundtrip.class);
        trip.setParameter("lhs", "2");
        trip.setParameter("rhs", "2");
        trip.execute();

        TestMockRoundtrip bean = trip.getActionBean(TestMockRoundtrip.class);
        Assert.assertEquals(bean.getResult(), 4.0, 0);
        Assert.assertEquals(trip.getValidationErrors().size(), 0);
        Assert.assertEquals(trip.getDestination(), "/mock/success.jsp");
    }

    @Test
    public void testWithNamedEvent() throws Exception {
        MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), TestMockRoundtrip.class);
        trip.setParameter("lhs", "2");
        trip.setParameter("rhs", "2");
        trip.execute("add");

        TestMockRoundtrip bean = trip.getActionBean(TestMockRoundtrip.class);
        Assert.assertEquals(bean.getResult(), 4.0, 0);
        Assert.assertEquals(trip.getValidationErrors().size(), 0);
        Assert.assertEquals(trip.getDestination(), "/mock/success.jsp");
    }

    @Test
    public void testWithRedirect() throws Exception {
        MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), TestMockRoundtrip.class);
        trip.setParameter("lhs", "2");
        trip.setParameter("rhs", "2");
        trip.execute("addAndRedirect");

        TestMockRoundtrip bean = trip.getActionBean(TestMockRoundtrip.class);
        Assert.assertEquals(bean.getResult(), 4.0, 0);
        Assert.assertEquals(trip.getValidationErrors().size(), 0);
        Assert.assertEquals(trip.getDestination(), "/mock/success.jsp");
        Assert.assertNull(trip.getRequest().getForwardUrl());
    }

    @Test
    public void testWithStreamingOutput() throws Exception {
        MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), TestMockRoundtrip.class);
        trip.setParameter("lhs", "2");
        trip.setParameter("rhs", "2");
        trip.execute("addAndStream");

        TestMockRoundtrip bean = trip.getActionBean(TestMockRoundtrip.class);
        Assert.assertEquals(bean.getResult(), 4.0, 0);
        Assert.assertEquals(trip.getValidationErrors().size(), 0);
        Assert.assertNull(trip.getDestination());
        Assert.assertEquals(trip.getOutputString(), "4.0");
    }

    @Test
    public void testWithValidationErrors() throws Exception {
        MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), TestMockRoundtrip.class);
        trip.setParameter("lhs", "");
        trip.setParameter("rhs", "abc");
        trip.execute();

        Assert.assertEquals(trip.getValidationErrors().size(), 2); // both fields in error
        Assert.assertEquals(trip.getDestination(), MockRoundtrip.DEFAULT_SOURCE_PAGE);
    }

    @Test
    public void testWithDifferentNamedEvent() throws Exception {
        MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), TestMockRoundtrip.class);
        trip.setParameter("lhs", "4");
        trip.setParameter("rhs", "4");
        trip.execute("multiply");

        TestMockRoundtrip bean = trip.getActionBean(TestMockRoundtrip.class);
        Assert.assertEquals(bean.getResult(), 16.0, 0);
        Assert.assertEquals(trip.getValidationErrors().size(), 0);
        Assert.assertEquals(trip.getDestination(), "/mock/success.jsp");
    }

    @Test
    public void testWithCustomValidationErrors() throws Exception {
        MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), TestMockRoundtrip.class);
        trip.setParameter("lhs", "2");
        trip.setParameter("rhs", "0");
        trip.execute("divide");

        Assert.assertEquals(trip.getValidationErrors().size(), 1);
        Assert.assertEquals(trip.getDestination(), MockRoundtrip.DEFAULT_SOURCE_PAGE);
    }

    @Test
    public void testFetchingRequestAttributes() throws Exception {
        MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), TestMockRoundtrip.class);
        trip.setParameter("lhs", "10");
        trip.setParameter("rhs", "4");
        trip.execute("divide");

        TestMockRoundtrip bean = trip.getActionBean(TestMockRoundtrip.class);
        Assert.assertEquals(bean.getResult(), 2.5, 0);
        Assert.assertEquals(trip.getValidationErrors().size(), 0);
        Assert.assertEquals(trip.getDestination(), "/mock/success.jsp");
        Assert.assertEquals(trip.getRequest().getAttribute("integerResult"), 2);
    }
    
    @Test
    public void testRequestCaseInsensitive() {
        final MockHttpServletRequest request = new MockHttpServletRequest("", "");

        String headerName = "User-Agent";
        Object value = "Netscape/6.0";
        request.addHeader(headerName, value);
        String[] variants = { headerName, headerName.toLowerCase(), headerName.toUpperCase() };
        for (String v : variants) {
            Assert.assertEquals("MockHttpServletRequest.addHeader/getHeader are case sensitive", request.getHeader(v), value);
        }

        headerName = "Content-Length";
        value = 1024;
        request.addHeader(headerName, value);
        variants = new String[] { headerName, headerName.toLowerCase(), headerName.toUpperCase() };
        for (String v : variants) {
            Assert.assertEquals("MockHttpServletRequest.addHeader/getIntHeader are case sensitive", request.getIntHeader(v), value);
        }
    }

    @Test
    public void testAddParameter() throws Exception {
        // Setup the servlet engine
        MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), TestMockRoundtrip.class);
        trip.addParameter("param", "a");
        trip.addParameter("param", "b");
        trip.execute();

        TestMockRoundtrip bean = trip.getActionBean(TestMockRoundtrip.class);
        String[] params = bean.getContext().getRequest().getParameterValues("param");
        Assert.assertEquals(2, params.length);
        Assert.assertEquals(new String[] {"a", "b"}, params);
    }
}
