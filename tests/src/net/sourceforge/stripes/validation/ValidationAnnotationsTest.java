package net.sourceforge.stripes.validation;

import net.sourceforge.stripes.StripesTestFixture;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.mock.MockRoundtrip;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests combinations of validation annotations.
 *
 * @author Freddy Daoud
 */
public class ValidationAnnotationsTest implements ActionBean {
    private ActionBeanContext context;
    public ActionBeanContext getContext() { return context; }
    public void setContext(ActionBeanContext context) { this.context = context;}

    @Validate(required=true, on="validateRequiredAndIgnored", ignore=true)
    private String first;
    public String getFirst() { return first; }
    public void setFirst(String first) { this.first = first; }

    public Resolution validateRequiredAndIgnored() { return null; }

    /**
     * Tests that a required field that is also ignored, should be ignored and should not produce
     * a validation error.
     *
     * @see http://www.stripesframework.org/jira/browse/STS-600
     */
    @Test(groups="fast")
    public void testValidateRequiredAndIgnored() throws Exception {
        MockRoundtrip trip = new MockRoundtrip(StripesTestFixture.getServletContext(), getClass());
        trip.execute("validateRequiredAndIgnored");
        ActionBean actionBean = trip.getActionBean(getClass());
        Assert.assertEquals(0, actionBean.getContext().getValidationErrors().size());
    }

    @Validate(required=true, on="validatePublicField")
    public String publicField;

    public Resolution validatePublicField() { return null; }

    /**
     * Tests that a validation annotation works on a public field.
     *
     * @see http://www.stripesframework.org/jira/browse/STS-604
     */
    @Test(groups="fast")
    public void testValidatePublicField() throws Exception {
        MockRoundtrip trip = new MockRoundtrip(StripesTestFixture.getServletContext(), getClass());
        trip.execute("validatePublicField");
        ActionBean actionBean = trip.getActionBean(getClass());
        Assert.assertEquals(1, actionBean.getContext().getValidationErrors().size());
    }
}
