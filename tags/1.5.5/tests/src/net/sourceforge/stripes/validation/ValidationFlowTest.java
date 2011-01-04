package net.sourceforge.stripes.validation;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.StripesTestFixture;
import org.testng.annotations.Test;
import org.testng.Assert;

/**
 * Test out various aspects of the validation subsystem in Stripes with regard to optional
 * application, ordering and flow control. Each of the individual test methods has javadoc
 * explaining why the expected results are as they are.
 *
 * @author Tim Fennell
 */
@UrlBinding("/test/ValidationFlowTest.action")
public class ValidationFlowTest implements ActionBean {
    int counter=1, validateAlwaysRan, validateOneRan, validateTwoRan;

    private ActionBeanContext context;
    public ActionBeanContext getContext() { return context; }
    public void setContext(ActionBeanContext context) { this.context = context;}

    @Validate(required=true) private int numberZero;
    public int getNumberZero() { return numberZero; }
    public void setNumberZero(int numberZero) { this.numberZero = numberZero; }

    @Validate(required=true, on="eventOne", minvalue=0) private int numberOne;
    public int getNumberOne() { return numberOne; }
    public void setNumberOne(int numberOne) { this.numberOne = numberOne; }

    @Validate(required=true,on="eventTwo") private int numberTwo;
    public int getNumberTwo() { return numberTwo; }
    public void setNumberTwo(int numberTwo) { this.numberTwo = numberTwo;  }

    @ValidationMethod(priority=0)
    public void validateAlways(ValidationErrors errors) {
        if (errors == null) throw new RuntimeException("errors must not be null");
        validateAlwaysRan = counter++;
    }

    @ValidationMethod(priority=1, on="eventOne", when=ValidationState.NO_ERRORS)
    public void validateOne() {
        validateOneRan = counter++;
    }

    @ValidationMethod(priority=1, on={"!eventZero", "!eventOne"}, when=ValidationState.ALWAYS)
    public void validateTwo(ValidationErrors errors) {
        validateTwoRan = counter++;
    }

    @HandlesEvent("eventZero") public Resolution zero() { return null; }
    @DefaultHandler @HandlesEvent("eventOne") public Resolution one() { return null; }
    @HandlesEvent("eventTwo") public Resolution two() { return null; }

    // Test methods begin here!

    /**
     * numberZero is the only required field for eventZero, so there should be no validation
     * errors generated. The only validation method that should be run is validateAlways() because
     * the others are tied to specific events.
     */
    @Test(groups="fast")
    public void testEventZeroNoErrors() throws Exception {
        MockRoundtrip trip = new MockRoundtrip(StripesTestFixture.getServletContext(), getClass());
        trip.addParameter("numberZero", "99");
        trip.execute("eventZero");

        ValidationFlowTest test = trip.getActionBean(getClass());
        Assert.assertEquals(1, test.validateAlwaysRan);
        Assert.assertEquals(0, test.validateOneRan);
        Assert.assertEquals(0, test.validateTwoRan);
        Assert.assertEquals(0, test.getContext().getValidationErrors().size());
    }

    /**
     * Generates an error by providing an invalid value for numberOne (which has a minimum
     * value of 0).  Validations other than required are still applied even though that @Validate
     * has a on="one".  The single validaiton error should prevent validateAlways() and
     * validateOne from running.
     */
    @Test(groups="fast")
    public void testEventZeroWithErrors() throws Exception {
        MockRoundtrip trip = new MockRoundtrip(StripesTestFixture.getServletContext(), getClass());
        trip.addParameter("numberZero", "99");
        trip.addParameter("numberOne", "-100");
        trip.execute("eventZero");

        ValidationFlowTest test = trip.getActionBean(getClass());
        Assert.assertEquals(0, test.validateAlwaysRan);
        Assert.assertEquals(0, test.validateOneRan);
        Assert.assertEquals(0, test.validateTwoRan);
        Assert.assertEquals(test.numberZero, 99);
        Assert.assertEquals(1, test.getContext().getValidationErrors().size());
    }

    /**
     * Number one is a required field also for this event, so we supply it.  This event should
     * cause both validateAlways and validateOne to run.
     */
    @Test(groups="fast")
    public void testEventOneNoErrors() throws Exception {
        MockRoundtrip trip = new MockRoundtrip(StripesTestFixture.getServletContext(), getClass());
        trip.addParameter("numberZero", "100");
        trip.addParameter("numberOne", "101");

        trip.execute("eventOne");

        ValidationFlowTest test = trip.getActionBean(getClass());
        Assert.assertEquals(1, test.validateAlwaysRan);
        Assert.assertEquals(2, test.validateOneRan);
        Assert.assertEquals(0, test.validateTwoRan);
        Assert.assertEquals(test.numberZero, 100);
        Assert.assertEquals(test.numberOne, 101);
        Assert.assertEquals(0, test.getContext().getValidationErrors().size());
    }

    /**
     * Tests that a required field error is raised this time for numberOne which is only
     * required for this event.  Again this single error should prevent both validateAlways
     * and validateOne from running.
     */
    @Test(groups="fast")
    public void testEventOneWithErrors() throws Exception {
        MockRoundtrip trip = new MockRoundtrip(StripesTestFixture.getServletContext(), getClass());
        trip.addParameter("numberZero", "100");
        trip.addParameter("numberOne", "");  // required field for event one
        trip.execute("eventOne");

        ValidationFlowTest test = trip.getActionBean(getClass());
        Assert.assertEquals(0, test.validateAlwaysRan);
        Assert.assertEquals(0, test.validateOneRan);
        Assert.assertEquals(0, test.validateTwoRan);
        Assert.assertEquals(test.numberZero, 100);
        Assert.assertEquals(1, test.getContext().getValidationErrors().size());
    }

    /**
     * Almost identical to testEventOneWithNoErrors except that we invoke the 'default' event.
     * Tests to make sure that event-specific validations are still applied correctly when the
     * event name isn't present in the request.
     */
    @Test(groups="fast")
    public void testEventOneAsDefault() throws Exception {
        MockRoundtrip trip = new MockRoundtrip(StripesTestFixture.getServletContext(), getClass());
        trip.addParameter("numberZero", "100");
        trip.addParameter("numberOne", "101");
        trip.execute();

        ValidationFlowTest test = trip.getActionBean(getClass());
        Assert.assertEquals(1, test.validateAlwaysRan);
        Assert.assertEquals(2, test.validateOneRan);
        Assert.assertEquals(0, test.validateTwoRan);
        Assert.assertEquals(test.numberZero, 100);
        Assert.assertEquals(test.numberOne, 101);
        Assert.assertEquals(0, test.getContext().getValidationErrors().size());
    }

    /**
     * Straightforward test for event two that makes sure it's validations run.  This time
     * numberTwo should be required (and is supplied) and validateAlways and validateTwo should
     * run but not validateOne.
     */
    @Test(groups="fast")
    public void testEventTwoNoErrors() throws Exception {
        MockRoundtrip trip = new MockRoundtrip(StripesTestFixture.getServletContext(), getClass());
        trip.addParameter("numberZero", "100");
        trip.addParameter("numberTwo",  "102");

        trip.execute("eventTwo");

        ValidationFlowTest test = trip.getActionBean(getClass());
        Assert.assertEquals(1, test.validateAlwaysRan);
        Assert.assertEquals(0, test.validateOneRan);
        Assert.assertEquals(2, test.validateTwoRan);
        Assert.assertEquals(test.numberZero, 100);
        Assert.assertEquals(test.numberTwo, 102);
        Assert.assertEquals(0, test.getContext().getValidationErrors().size());
    }

    /**
     * Tests that validateTwo is run event though there are errors and valiateAlways did not run,
     * because validateTwo is marked to run always.
     */
    @Test(groups="fast")
    public void testEventTwoWithErrors() throws Exception {
        MockRoundtrip trip = new MockRoundtrip(StripesTestFixture.getServletContext(), getClass());
        trip.addParameter("numberZero", ""); // required field always
        trip.addParameter("numberTwo", "");  // required field for event one
        trip.execute("eventTwo");

        ValidationFlowTest test = trip.getActionBean(getClass());
        Assert.assertEquals(0, test.validateAlwaysRan);
        Assert.assertEquals(0, test.validateOneRan);
        Assert.assertEquals(1, test.validateTwoRan);
        Assert.assertEquals(2, test.getContext().getValidationErrors().size());
    }
}
