package org.stripesframework.web.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.stripesframework.web.FilterEnabledTestBase;
import org.stripesframework.web.action.ActionBean;
import org.stripesframework.web.action.ActionBeanContext;
import org.stripesframework.web.action.DefaultHandler;
import org.stripesframework.web.action.HandlesEvent;
import org.stripesframework.web.action.Resolution;
import org.stripesframework.web.action.UrlBinding;
import org.stripesframework.web.mock.MockRoundtrip;


/**
 * Test out various aspects of the validation subsystem in Stripes with regard to optional
 * application, ordering and flow control. Each of the individual test methods has javadoc
 * explaining why the expected results are as they are.
 *
 * @author Tim Fennell
 */
@SuppressWarnings("unused")
@UrlBinding("/test/ValidationFlowTest.action")
public class ValidationFlowTest extends FilterEnabledTestBase implements ActionBean {

   int counter = 1, validateAlwaysRan, validateOneRan, validateTwoRan;

   private ActionBeanContext context;
   @Validate(required = true)
   private int               numberZero;
   @Validate(required = true, on = "eventOne", minvalue = 0)
   private int               numberOne;
   @Validate(required = true, on = "eventTwo")
   private int               numberTwo;

   @Override
   public ActionBeanContext getContext() { return context; }

   public int getNumberOne() { return numberOne; }

   public int getNumberTwo() { return numberTwo; }

   public int getNumberZero() { return numberZero; }

   @DefaultHandler
   @HandlesEvent("eventOne")
   public Resolution one() { return null; }

   @Override
   public void setContext( ActionBeanContext context ) { this.context = context;}

   public void setNumberOne( int numberOne ) { this.numberOne = numberOne; }

   public void setNumberTwo( int numberTwo ) { this.numberTwo = numberTwo; }

   public void setNumberZero( int numberZero ) { this.numberZero = numberZero; }

   /**
    * Almost identical to testEventOneWithNoErrors except that we invoke the 'default' event.
    * Tests to make sure that event-specific validations are still applied correctly when the
    * event name isn't present in the request.
    */
   @Test
   public void testEventOneAsDefault() throws Exception {
      MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
      trip.addParameter("numberZero", "100");
      trip.addParameter("numberOne", "101");
      trip.execute();

      ValidationFlowTest test = trip.getActionBean(getClass());
      assertThat(test.validateAlwaysRan).isEqualTo(1);
      assertThat(test.validateOneRan).isEqualTo(2);
      assertThat(test.validateTwoRan).isEqualTo(0);
      assertThat(test.numberZero).isEqualTo(100);
      assertThat(test.numberOne).isEqualTo(101);
      assertThat(test.getContext().getValidationErrors()).isEmpty();
   }

   /**
    * Number one is a required field also for this event, so we supply it.  This event should
    * cause both validateAlways and validateOne to run.
    */
   @Test
   public void testEventOneNoErrors() throws Exception {
      MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
      trip.addParameter("numberZero", "100");
      trip.addParameter("numberOne", "101");

      trip.execute("eventOne");

      ValidationFlowTest test = trip.getActionBean(getClass());
      assertThat(test.validateAlwaysRan).isEqualTo(1);
      assertThat(test.validateOneRan).isEqualTo(2);
      assertThat(test.validateTwoRan).isEqualTo(0);
      assertThat(test.numberZero).isEqualTo(100);
      assertThat(test.numberOne).isEqualTo(101);
      assertThat(test.getContext().getValidationErrors()).isEmpty();
   }

   /**
    * Tests that a required field error is raised this time for numberOne which is only
    * required for this event.  Again this single error should prevent both validateAlways
    * and validateOne from running.
    */
   @Test
   public void testEventOneWithErrors() throws Exception {
      MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
      trip.addParameter("numberZero", "100");
      trip.addParameter("numberOne", "");  // required field for event one
      trip.execute("eventOne");

      ValidationFlowTest test = trip.getActionBean(getClass());
      assertThat(test.validateAlwaysRan).isEqualTo(0);
      assertThat(test.validateOneRan).isEqualTo(0);
      assertThat(test.validateTwoRan).isEqualTo(0);
      assertThat(test.numberZero).isEqualTo(100);
      assertThat(test.getContext().getValidationErrors()).hasSize(1);
   }

   /**
    * Straightforward test for event two that makes sure it's validations run.  This time
    * numberTwo should be required (and is supplied) and validateAlways and validateTwo should
    * run but not validateOne.
    */
   @Test
   public void testEventTwoNoErrors() throws Exception {
      MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
      trip.addParameter("numberZero", "100");
      trip.addParameter("numberTwo", "102");

      trip.execute("eventTwo");

      ValidationFlowTest test = trip.getActionBean(getClass());
      assertThat(test.validateAlwaysRan).isEqualTo(1);
      assertThat(test.validateOneRan).isEqualTo(0);
      assertThat(test.validateTwoRan).isEqualTo(2);
      assertThat(test.numberZero).isEqualTo(100);
      assertThat(test.numberTwo).isEqualTo(102);
      assertThat(test.getContext().getValidationErrors()).isEmpty();
   }

   /**
    * Tests that validateTwo is run event though there are errors and valiateAlways did not run,
    * because validateTwo is marked to run always.
    */
   @Test
   public void testEventTwoWithErrors() throws Exception {
      MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
      trip.addParameter("numberZero", ""); // required field always
      trip.addParameter("numberTwo", "");  // required field for event one
      trip.execute("eventTwo");

      ValidationFlowTest test = trip.getActionBean(getClass());
      assertThat(test.validateAlwaysRan).isEqualTo(0);
      assertThat(test.validateOneRan).isEqualTo(0);
      assertThat(test.validateTwoRan).isEqualTo(1);
      assertThat(test.getContext().getValidationErrors()).hasSize(2);
   }

   /**
    * numberZero is the only required field for eventZero, so there should be no validation
    * errors generated. The only validation method that should be run is validateAlways() because
    * the others are tied to specific events.
    */
   @Test
   public void testEventZeroNoErrors() throws Exception {
      MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
      trip.addParameter("numberZero", "99");
      trip.execute("eventZero");

      ValidationFlowTest test = trip.getActionBean(getClass());
      assertThat(test.validateAlwaysRan).isEqualTo(1);
      assertThat(test.validateOneRan).isEqualTo(0);
      assertThat(test.validateTwoRan).isEqualTo(0);
      assertThat(test.getContext().getValidationErrors()).isEmpty();
   }

   /**
    * Generates an error by providing an invalid value for numberOne (which has a minimum
    * value of 0).  Validations other than required are still applied even though that @Validate
    * has a on="one".  The single validaiton error should prevent validateAlways() and
    * validateOne from running.
    */
   @Test
   public void testEventZeroWithErrors() throws Exception {
      MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
      trip.addParameter("numberZero", "99");
      trip.addParameter("numberOne", "-100");
      trip.execute("eventZero");

      ValidationFlowTest test = trip.getActionBean(getClass());
      assertThat(test.validateAlwaysRan).isEqualTo(0);
      assertThat(test.validateOneRan).isEqualTo(0);
      assertThat(test.validateTwoRan).isEqualTo(0);
      assertThat(test.numberZero).isEqualTo(99);
      assertThat(test.getContext().getValidationErrors()).hasSize(1);
   }

   @HandlesEvent("eventTwo")
   public Resolution two() { return null; }

   @SuppressWarnings("DefaultAnnotationParam")
   @ValidationMethod(priority = 0)
   public void validateAlways( ValidationErrors errors ) {
      if ( errors == null ) {
         throw new RuntimeException("errors must not be null");
      }
      validateAlwaysRan = counter++;
   }

   @ValidationMethod(priority = 1, on = "eventOne", when = ValidationState.NO_ERRORS)
   public void validateOne() {
      validateOneRan = counter++;
   }

   @ValidationMethod(priority = 1, on = { "!eventZero", "!eventOne" }, when = ValidationState.ALWAYS)
   public void validateTwo( ValidationErrors errors ) {
      validateTwoRan = counter++;
   }

   @HandlesEvent("eventZero")
   public Resolution zero() { return null; }
}
