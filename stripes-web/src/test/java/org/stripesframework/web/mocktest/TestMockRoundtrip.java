package org.stripesframework.web.mocktest;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringReader;

import org.junit.jupiter.api.Test;
import org.stripesframework.web.FilterEnabledTestBase;
import org.stripesframework.web.action.ActionBean;
import org.stripesframework.web.action.ActionBeanContext;
import org.stripesframework.web.action.DefaultHandler;
import org.stripesframework.web.action.ForwardResolution;
import org.stripesframework.web.action.HandlesEvent;
import org.stripesframework.web.action.RedirectResolution;
import org.stripesframework.web.action.Resolution;
import org.stripesframework.web.action.StreamingResolution;
import org.stripesframework.web.action.UrlBinding;
import org.stripesframework.web.mock.MockHttpServletRequest;
import org.stripesframework.web.mock.MockRoundtrip;
import org.stripesframework.web.validation.SimpleError;
import org.stripesframework.web.validation.Validate;


/**
 * Unit test that is designed to do some fairly simple testing of the mock engine to ensure that
 * it works correctly.
 *
 * @author Tim Fennell
 */
@SuppressWarnings("unused")
@UrlBinding("/mock/MockRoundtrip.test")
public class TestMockRoundtrip extends FilterEnabledTestBase implements ActionBean {

   private ActionBeanContext context;
   private double            lhs;
   private double            rhs;
   private double            result;

   /** A very simple add event that returns a Forward reslution. */
   @DefaultHandler
   @HandlesEvent("add")
   public Resolution add() {
      result = lhs + rhs;
      return new ForwardResolution("/mock/success.jsp");
   }

   /** A very simple add event that returns a Redirect reslution. */
   @HandlesEvent("addAndRedirect")
   public Resolution addAndRedirect() {
      result = lhs + rhs;
      return new RedirectResolution("/mock/success.jsp");
   }

   /** A very simple add event that returns the result in the response stream. */
   @HandlesEvent("addAndStream")
   public Resolution addAndStream() {
      result = lhs + rhs;
      return new StreamingResolution("text/plain", new StringReader(String.valueOf(result)));
   }

   /** A divide event that validates that we're not dividing by zero. */
   @HandlesEvent("divide")
   public Resolution divide() {
      if ( rhs == 0 ) {
         getContext().getValidationErrors().add("rhs", new SimpleError("Rhs may not be zero"));
         return getContext().getSourcePageResolution();
      }

      result = lhs / rhs;
      getContext().getRequest().setAttribute("integerResult", (int)result);
      return new ForwardResolution("/mock/success.jsp");
   }

   @Override
   public ActionBeanContext getContext() { return context; }

   public double getLhs() { return lhs; }

   public double getResult() { return result; }

   public double getRhs() { return rhs; }

   /** A very simple multiplication event. */
   @HandlesEvent("multiply")
   public Resolution multiply() {
      result = lhs * rhs;
      return new ForwardResolution("/mock/success.jsp");
   }

   @Override
   public void setContext( ActionBeanContext context ) { this.context = context; }

   @Validate(required = true)
   public void setLhs( double lhs ) { this.lhs = lhs; }

   @Validate(ignore = true)
   public void setResult( double result ) { this.result = result; }

   @Validate(required = true)
   public void setRhs( double rhs ) { this.rhs = rhs; }

   ///////////////////////////////////////////////////////////////////////////
   // End of ActionBean methods and beginning of test methods. Everything
   // below this line is a test!
   ///////////////////////////////////////////////////////////////////////////

   @Test
   public void testAddParameter() throws Exception {
      // Setup the servlet engine
      MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), TestMockRoundtrip.class);
      trip.addParameter("param", "a");
      trip.addParameter("param", "b");
      trip.execute();

      TestMockRoundtrip bean = trip.getActionBean(TestMockRoundtrip.class);
      String[] params = bean.getContext().getRequest().getParameterValues("param");
      assertThat(params).containsExactly("a", "b");
   }

   @Test
   public void testDefaultEvent() throws Exception {
      MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), TestMockRoundtrip.class);
      trip.setParameter("lhs", "2");
      trip.setParameter("rhs", "2");
      trip.execute();

      TestMockRoundtrip bean = trip.getActionBean(TestMockRoundtrip.class);
      assertThat(bean.getResult()).isEqualTo(4.0);
      assertThat(trip.getValidationErrors()).isEmpty();
      assertThat(trip.getDestination()).isEqualTo("/mock/success.jsp");
   }

   @Test
   public void testFetchingRequestAttributes() throws Exception {
      MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), TestMockRoundtrip.class);
      trip.setParameter("lhs", "10");
      trip.setParameter("rhs", "4");
      trip.execute("divide");

      TestMockRoundtrip bean = trip.getActionBean(TestMockRoundtrip.class);
      assertThat(bean.getResult()).isEqualTo(2.5);
      assertThat(trip.getValidationErrors()).isEmpty();
      assertThat(trip.getDestination()).isEqualTo("/mock/success.jsp");
      assertThat(trip.getRequest().getAttribute("integerResult")).isEqualTo(2);
   }

   @Test
   public void testRequestCaseInsensitive() {
      final MockHttpServletRequest request = new MockHttpServletRequest("", "");

      String headerName = "User-Agent";
      Object value = "Netscape/6.0";
      request.addHeader(headerName, value);
      String[] variants = { headerName, headerName.toLowerCase(), headerName.toUpperCase() };
      for ( String v : variants ) {
         assertThat(request.getHeader(v)).isEqualTo(value).describedAs("MockHttpServletRequest.addHeader/getHeader are case sensitive");
      }

      headerName = "Content-Length";
      value = 1024;
      request.addHeader(headerName, value);
      variants = new String[] { headerName, headerName.toLowerCase(), headerName.toUpperCase() };
      for ( String v : variants ) {
         assertThat(request.getIntHeader(v)).isEqualTo(value).describedAs("MockHttpServletRequest.addHeader/getIntHeader are case sensitive");
      }
   }

   @Test
   public void testWithCustomValidationErrors() throws Exception {
      MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), TestMockRoundtrip.class);
      trip.setParameter("lhs", "2");
      trip.setParameter("rhs", "0");
      trip.execute("divide");

      assertThat(trip.getValidationErrors()).hasSize(1);
      assertThat(trip.getDestination()).isEqualTo(MockRoundtrip.DEFAULT_SOURCE_PAGE);
   }

   @Test
   public void testWithDifferentNamedEvent() throws Exception {
      MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), TestMockRoundtrip.class);
      trip.setParameter("lhs", "4");
      trip.setParameter("rhs", "4");
      trip.execute("multiply");

      TestMockRoundtrip bean = trip.getActionBean(TestMockRoundtrip.class);
      assertThat(bean.getResult()).isEqualTo(16.0);
      assertThat(trip.getValidationErrors()).isEmpty();
      assertThat(trip.getDestination()).isEqualTo("/mock/success.jsp");
   }

   @Test
   public void testWithNamedEvent() throws Exception {
      MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), TestMockRoundtrip.class);
      trip.setParameter("lhs", "2");
      trip.setParameter("rhs", "2");
      trip.execute("add");

      TestMockRoundtrip bean = trip.getActionBean(TestMockRoundtrip.class);
      assertThat(bean.getResult()).isEqualTo(4.0);
      assertThat(trip.getValidationErrors()).isEmpty();
      assertThat(trip.getDestination()).isEqualTo("/mock/success.jsp");
   }

   @Test
   public void testWithRedirect() throws Exception {
      MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), TestMockRoundtrip.class);
      trip.setParameter("lhs", "2");
      trip.setParameter("rhs", "2");
      trip.execute("addAndRedirect");

      TestMockRoundtrip bean = trip.getActionBean(TestMockRoundtrip.class);
      assertThat(bean.getResult()).isEqualTo(4.0);
      assertThat(trip.getValidationErrors()).isEmpty();
      assertThat(trip.getDestination()).isEqualTo("/mock/success.jsp");
      assertThat(trip.getRequest().getForwardUrl()).isNull();
   }

   @Test
   public void testWithStreamingOutput() throws Exception {
      MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), TestMockRoundtrip.class);
      trip.setParameter("lhs", "2");
      trip.setParameter("rhs", "2");
      trip.execute("addAndStream");

      TestMockRoundtrip bean = trip.getActionBean(TestMockRoundtrip.class);
      assertThat(bean.getResult()).isEqualTo(4.0);
      assertThat(trip.getValidationErrors()).isEmpty();
      assertThat(trip.getDestination()).isNull();
      assertThat(trip.getOutputString()).isEqualTo("4.0");
   }

   @Test
   public void testWithValidationErrors() throws Exception {
      MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), TestMockRoundtrip.class);
      trip.setParameter("lhs", "");
      trip.setParameter("rhs", "abc");
      trip.execute();

      assertThat(trip.getValidationErrors()).hasSize(2); // both fields in error
      assertThat(trip.getDestination()).isEqualTo(MockRoundtrip.DEFAULT_SOURCE_PAGE);
   }
}
