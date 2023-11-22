package net.sourceforge.stripes.controller;

import net.sourceforge.stripes.StripesTestFixture;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.DontValidate;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidationErrors;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests that when one ActionBean extends another that the results are predictable and that subclass
 * annotations override those in the superclass.
 *
 * @author Tim Fennell
 */
@UrlBinding("/InheritanceTests.action")
public class InheritanceTests extends SuperclassActionBean {
  /** A new handler method, that is now the default. */
  @DefaultHandler
  @DontValidate
  public Resolution different() {
    return new RedirectResolution("/child.jsp");
  }

  /** Another handler method that will cause validation to run. */
  @HandlesEvent("/Validate.action")
  public Resolution another() {
    return new RedirectResolution("/child.jsp");
  }

  private static MockServletContext ctx;

  @BeforeClass
  public static void setupServletContext() {
    ctx = StripesTestFixture.createServletContext();
  }

  @AfterClass
  public static void closeServletContext() {
    ctx.close();
  }

  /**
   * When we invoke the action without an event it should get routed to the default handler in this
   * class, not the one in the super class!
   */
  @Test
  public void invokeDefault() throws Exception {
    MockRoundtrip trip = new MockRoundtrip(ctx, InheritanceTests.class);
    trip.execute();
    Assert.assertEquals("Wrong default handler called!", trip.getDestination(), "/child.jsp");
  }

  // Overridden getter methods that simply allow additional validations to be added

  @Override
  @Validate(required = false) // override validation on the Field
  public String getOne() {
    return super.getOne();
  }

  @Override
  @Validate(required = true, minlength = 25) // override and add validation on the Method
  public String getTwo() {
    return super.getTwo();
  }

  @Override
  @Validate(mask = "\\d+") // add valiations where there were none
  public String getFour() {
    return super.getFour();
  }

  /**
   * Check that the validations from the superclass are active, except where overridden by this
   * class.
   */
  @Test
  public void testInheritedValidations() throws Exception {
    MockServletContext ctx = StripesTestFixture.createServletContext();
    try {
      MockRoundtrip trip = new MockRoundtrip(ctx, InheritanceTests.class);
      trip.addParameter("two", "not25chars");
      trip.addParameter("three", "3");
      trip.addParameter("four", "onetwothree");
      trip.execute("/Validate.action");

      ValidationErrors errors = trip.getValidationErrors();
      Assert.assertNull("Field one should not have errors.", errors.get("one"));
      Assert.assertEquals("Field two should not have 1 error.", errors.get("two").size(), 1);
      Assert.assertEquals("Field three should not have errors.", errors.get("three").size(), 1);
      Assert.assertEquals("Field one should not have errors.", errors.get("four").size(), 1);
    } finally {
      ctx.close();
    }
  }
}

/** A super class that can be extended by the test class. */
class SuperclassActionBean implements ActionBean {
  private ActionBeanContext context;

  public ActionBeanContext getContext() {
    return context;
  }

  public void setContext(ActionBeanContext context) {
    this.context = context;
  }

  @Validate(required = true)
  private String one;

  private String two;
  private String three;
  private String four;

  public String getOne() {
    return one;
  }

  public void setOne(String one) {
    this.one = one;
  }

  @Validate(required = true)
  public String getTwo() {
    return two;
  }

  public void setTwo(String two) {
    this.two = two;
  }

  @Validate(minlength = 3)
  public String getThree() {
    return three;
  }

  public void setThree(String three) {
    this.three = three;
  }

  public String getFour() {
    return four;
  }

  public void setFour(String four) {
    this.four = four;
  }

  @DefaultHandler
  public Resolution index() {
    return new RedirectResolution("/super.jsp");
  }
}
