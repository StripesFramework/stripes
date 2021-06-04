package org.stripesframework.web.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.stripesframework.web.StripesTestFixture;
import org.stripesframework.web.action.ActionBean;
import org.stripesframework.web.action.ActionBeanContext;
import org.stripesframework.web.action.DefaultHandler;
import org.stripesframework.web.action.DontValidate;
import org.stripesframework.web.action.HandlesEvent;
import org.stripesframework.web.action.RedirectResolution;
import org.stripesframework.web.action.Resolution;
import org.stripesframework.web.action.UrlBinding;
import org.stripesframework.web.mock.MockRoundtrip;
import org.stripesframework.web.mock.MockServletContext;
import org.stripesframework.web.validation.Validate;
import org.stripesframework.web.validation.ValidationErrors;


/**
 * Tests that when one ActionBean extends another that the results are predictable
 * and that subclass annotations override those in the superclass.
 *
 * @author Tim Fennell
 */
@UrlBinding("/InheritanceTests.action")
public class InheritanceTests extends SuperclassActionBean {

   private static MockServletContext ctx;

   @AfterAll
   public static void closeServletContext() {
      ctx.close();
   }

   @BeforeAll
   public static void setupServletContext() {
      ctx = StripesTestFixture.createServletContext();
   }

   /** Another handler method that will cause validation to run. */
   @HandlesEvent("/Validate.action")
   public Resolution another() { return new RedirectResolution("/child.jsp"); }

   /** A new handler method, that is now the default. */
   @DefaultHandler
   @DontValidate
   public Resolution different() { return new RedirectResolution("/child.jsp"); }

   @Override
   @Validate(mask = "\\d+") // add valiations where there were none
   public String getFour() { return super.getFour(); }

   // Overridden getter methods that simply allow additional validations to be added

   @Override
   @Validate(required = false) // override validation on the Field
   public String getOne() { return super.getOne(); }

   @Override
   @Validate(required = true, minlength = 25) // override and add validation on the Method
   public String getTwo() { return super.getTwo(); }

   /**
    * When we invoke the action without an event it should get routed to the default
    * handler in this class, not the one in the super class!
    */
   @Test
   public void invokeDefault() throws Exception {
      MockRoundtrip trip = new MockRoundtrip(ctx, InheritanceTests.class);
      trip.execute();
      assertThat(trip.getDestination()).describedAs("Wrong default handler called!").isEqualTo("/child.jsp");
   }

   /**
    * Check that the validations from the superclass are active, except where
    * overridden by this class.
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
         assertThat(errors.get("one")).isNull();
         assertThat(errors.get("two")).hasSize(1);
         assertThat(errors.get("three")).hasSize(1);
         assertThat(errors.get("four")).hasSize(1);
      }
      finally {
         ctx.close();
      }
   }
}


/**
 * A super class that can be extended by the test class.
 */
class SuperclassActionBean implements ActionBean {

   private ActionBeanContext context;
   @Validate(required = true)
   private String            one;
   private String            two;
   private String            three;
   private String            four;

   @Override
   public ActionBeanContext getContext() { return context; }

   public String getFour() { return four; }

   public String getOne() { return one; }

   @Validate(minlength = 3)
   public String getThree() { return three; }

   @Validate(required = true)
   public String getTwo() { return two; }

   @DefaultHandler
   public Resolution index() {
      return new RedirectResolution("/super.jsp");
   }

   @Override
   public void setContext( ActionBeanContext context ) { this.context = context; }

   public void setFour( String four ) { this.four = four;}

   public void setOne( String one ) { this.one = one; }

   public void setThree( String three ) { this.three = three; }

   public void setTwo( String two ) { this.two = two; }
}