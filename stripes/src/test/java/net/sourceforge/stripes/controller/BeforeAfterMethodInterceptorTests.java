package net.sourceforge.stripes.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.After;
import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;


/**
 * Unit test of the {@link BeforeAfterMethodInterceptor} class.
 *
 * @author Jeppe Cramon
 */
public class BeforeAfterMethodInterceptorTests {

   @Test
   public void testInterceptAtLifeCycleStage_ActionBeanResolution() throws Exception {
      ExecutionContext context = new TestExecutionContext();
      TestActionBean2 actionBean = new TestActionBean2();
      context.setActionBean(actionBean);
      context.setLifecycleStage(LifecycleStage.ActionBeanResolution);

      BeforeAfterMethodInterceptor interceptor = new BeforeAfterMethodInterceptor();
      assertThat(interceptor.intercept(context)).isNotNull();

      assertThat(actionBean.getHasCalledAfterDefaultStage()).isEqualTo(0);
      assertThat(actionBean.getHasCalledAfterSpecificStage()).isEqualTo(1);
      assertThat(actionBean.getHasCalledAfterTwoStages()).isEqualTo(0);
      assertThat(actionBean.getHasCalledAfterWithReturn()).isEqualTo(0);
      assertThat(actionBean.getHasCalledAfterWithParameter()).isEqualTo(0);
      assertThat(actionBean.getHasCalledAfterWithReturnAndParameter()).isEqualTo(0);
      assertThat(actionBean.getHasCalledBeforeDefaultStage()).isEqualTo(0);
      assertThat(actionBean.getHasCalledBeforeSpecificStage()).isEqualTo(0);
      assertThat(actionBean.getHasCalledBeforeTwoStages()).isEqualTo(0);
      assertThat(actionBean.getHasCalledBeforeAfterSpecificStage()).isEqualTo(0);
      assertThat(actionBean.getHasCalledBeforeAfterDefaultStage()).isEqualTo(0);
      assertThat(actionBean.getHasCalledBeforeAfterWithParameter()).isEqualTo(0);
      assertThat(actionBean.getHasCalledBeforeWithParameter()).isEqualTo(0);
      assertThat(actionBean.getHasCalledBeforeWithReturnAndParameter()).isEqualTo(0);
      assertThat(actionBean.getHasCalledDummyMethod()).isEqualTo(0);
      assertThat(actionBean.getHasCalledProtectedAfterMethod()).isEqualTo(0);
      assertThat(actionBean.getHasCalledProtectedBeforeMethod()).isEqualTo(0);
   }

   @Test
   public void testInterceptAtLifeCycleStage_BindingAndValidation() throws Exception {
      ExecutionContext context = new TestExecutionContext();
      TestActionBean2 actionBean = new TestActionBean2();
      context.setActionBean(actionBean);
      context.setLifecycleStage(LifecycleStage.BindingAndValidation);

      BeforeAfterMethodInterceptor interceptor = new BeforeAfterMethodInterceptor();
      assertThat(interceptor.intercept(context)).isNotNull();

      assertThat(actionBean.getHasCalledAfterDefaultStage()).isEqualTo(0);
      assertThat(actionBean.getHasCalledAfterSpecificStage()).isEqualTo(0);
      assertThat(actionBean.getHasCalledAfterTwoStages()).isEqualTo(0);
      assertThat(actionBean.getHasCalledAfterWithReturn()).isEqualTo(0);
      assertThat(actionBean.getHasCalledAfterWithParameter()).isEqualTo(0);
      assertThat(actionBean.getHasCalledAfterWithReturnAndParameter()).isEqualTo(0);
      assertThat(actionBean.getHasCalledBeforeDefaultStage()).isEqualTo(0);
      assertThat(actionBean.getHasCalledBeforeSpecificStage()).isEqualTo(0);
      assertThat(actionBean.getHasCalledBeforeTwoStages()).isEqualTo(1);
      assertThat(actionBean.getHasCalledBeforeAfterSpecificStage()).isEqualTo(1);
      assertThat(actionBean.getHasCalledBeforeAfterDefaultStage()).isEqualTo(0);
      assertThat(actionBean.getHasCalledBeforeAfterWithParameter()).isEqualTo(0);
      assertThat(actionBean.getHasCalledBeforeWithParameter()).isEqualTo(0);
      assertThat(actionBean.getHasCalledBeforeWithReturnAndParameter()).isEqualTo(0);
      assertThat(actionBean.getHasCalledDummyMethod()).isEqualTo(0);
      assertThat(actionBean.getHasCalledProtectedAfterMethod()).isEqualTo(0);
      assertThat(actionBean.getHasCalledProtectedBeforeMethod()).isEqualTo(0);
   }

   @Test
   public void testInterceptAtLifeCycleStage_CustomValidation() throws Exception {
      ExecutionContext context = new TestExecutionContext();
      TestActionBean2 actionBean = new TestActionBean2();
      context.setActionBean(actionBean);
      context.setLifecycleStage(LifecycleStage.CustomValidation);

      BeforeAfterMethodInterceptor interceptor = new BeforeAfterMethodInterceptor();
      assertThat(interceptor.intercept(context)).isNotNull();

      assertThat(actionBean.getHasCalledAfterDefaultStage()).isEqualTo(0);
      assertThat(actionBean.getHasCalledAfterSpecificStage()).isEqualTo(0);
      assertThat(actionBean.getHasCalledAfterTwoStages()).isEqualTo(1);
      assertThat(actionBean.getHasCalledAfterWithReturn()).isEqualTo(0);
      assertThat(actionBean.getHasCalledAfterWithParameter()).isEqualTo(0);
      assertThat(actionBean.getHasCalledAfterWithReturnAndParameter()).isEqualTo(0);
      assertThat(actionBean.getHasCalledBeforeDefaultStage()).isEqualTo(0);
      assertThat(actionBean.getHasCalledBeforeSpecificStage()).isEqualTo(0);
      assertThat(actionBean.getHasCalledBeforeTwoStages()).isEqualTo(1);
      assertThat(actionBean.getHasCalledBeforeAfterSpecificStage()).isEqualTo(1);
      assertThat(actionBean.getHasCalledBeforeAfterDefaultStage()).isEqualTo(0);
      assertThat(actionBean.getHasCalledBeforeAfterWithParameter()).isEqualTo(0);
      assertThat(actionBean.getHasCalledBeforeWithParameter()).isEqualTo(0);
      assertThat(actionBean.getHasCalledBeforeWithReturnAndParameter()).isEqualTo(0);
      assertThat(actionBean.getHasCalledDummyMethod()).isEqualTo(0);
      assertThat(actionBean.getHasCalledProtectedAfterMethod()).isEqualTo(0);
      assertThat(actionBean.getHasCalledProtectedBeforeMethod()).isEqualTo(0);
   }

   @Test
   public void testInterceptAtLifeCycleStage_EventHandling() throws Exception {
      ExecutionContext context = new TestExecutionContext();
      TestActionBean2 actionBean = new TestActionBean2();
      context.setActionBean(actionBean);
      context.setLifecycleStage(LifecycleStage.EventHandling);

      BeforeAfterMethodInterceptor interceptor = new BeforeAfterMethodInterceptor();
      assertThat(interceptor.intercept(context)).isNotNull();

      assertThat(actionBean.getHasCalledAfterDefaultStage()).isEqualTo(1);
      assertThat(actionBean.getHasCalledAfterSpecificStage()).isEqualTo(0);
      assertThat(actionBean.getHasCalledAfterTwoStages()).isEqualTo(0);
      assertThat(actionBean.getHasCalledAfterWithReturn()).isEqualTo(1);
      assertThat(actionBean.getHasCalledAfterWithParameter()).isEqualTo(0);
      assertThat(actionBean.getHasCalledAfterWithReturnAndParameter()).isEqualTo(0);
      assertThat(actionBean.getHasCalledBeforeDefaultStage()).isEqualTo(1);
      assertThat(actionBean.getHasCalledBeforeSpecificStage()).isEqualTo(0);
      assertThat(actionBean.getHasCalledBeforeTwoStages()).isEqualTo(0);
      assertThat(actionBean.getHasCalledBeforeWithReturn()).isEqualTo(1);
      assertThat(actionBean.getHasCalledBeforeAfterSpecificStage()).isEqualTo(0);
      assertThat(actionBean.getHasCalledBeforeAfterDefaultStage()).isEqualTo(2);
      assertThat(actionBean.getHasCalledBeforeAfterWithParameter()).isEqualTo(0);
      assertThat(actionBean.getHasCalledBeforeWithParameter()).isEqualTo(0);
      assertThat(actionBean.getHasCalledBeforeWithReturnAndParameter()).isEqualTo(0);
      assertThat(actionBean.getHasCalledDummyMethod()).isEqualTo(0);
      assertThat(actionBean.getHasCalledProtectedAfterMethod()).isEqualTo(1);
      assertThat(actionBean.getHasCalledProtectedBeforeMethod()).isEqualTo(1);
   }

   @Test
   public void testInterceptAtLifeCycleStage_HandlerResolution() throws Exception {
      ExecutionContext context = new TestExecutionContext();
      TestActionBean2 actionBean = new TestActionBean2();
      context.setActionBean(actionBean);
      context.setLifecycleStage(LifecycleStage.HandlerResolution);

      BeforeAfterMethodInterceptor interceptor = new BeforeAfterMethodInterceptor();
      assertThat(interceptor.intercept(context)).isNotNull();

      assertThat(actionBean.getHasCalledAfterDefaultStage()).isEqualTo(0);
      assertThat(actionBean.getHasCalledAfterSpecificStage()).isEqualTo(0);
      assertThat(actionBean.getHasCalledAfterTwoStages()).isEqualTo(1);
      assertThat(actionBean.getHasCalledAfterWithReturn()).isEqualTo(0);
      assertThat(actionBean.getHasCalledAfterWithParameter()).isEqualTo(0);
      assertThat(actionBean.getHasCalledAfterWithReturnAndParameter()).isEqualTo(0);
      assertThat(actionBean.getHasCalledBeforeDefaultStage()).isEqualTo(0);
      assertThat(actionBean.getHasCalledBeforeSpecificStage()).isEqualTo(1);
      assertThat(actionBean.getHasCalledBeforeTwoStages()).isEqualTo(0);
      assertThat(actionBean.getHasCalledBeforeAfterSpecificStage()).isEqualTo(0);
      assertThat(actionBean.getHasCalledBeforeAfterDefaultStage()).isEqualTo(0);
      assertThat(actionBean.getHasCalledBeforeAfterWithParameter()).isEqualTo(0);
      assertThat(actionBean.getHasCalledBeforeWithParameter()).isEqualTo(0);
      assertThat(actionBean.getHasCalledBeforeWithReturnAndParameter()).isEqualTo(0);
      assertThat(actionBean.getHasCalledDummyMethod()).isEqualTo(0);
      assertThat(actionBean.getHasCalledProtectedAfterMethod()).isEqualTo(0);
      assertThat(actionBean.getHasCalledProtectedBeforeMethod()).isEqualTo(0);
   }

   @Test
   public void testIntercept_withEventSpecifier() throws Exception {
      ExecutionContext context = new TestExecutionContext();
      BeforeAfterMethodInterceptor interceptor = new BeforeAfterMethodInterceptor();
      TestActionBean2 actionBean = new TestActionBean2();
      context.setActionBean(actionBean);
      context.setActionBeanContext(new ActionBeanContext());

      context.getActionBeanContext().setEventName("edit");
      context.setLifecycleStage(LifecycleStage.EventHandling); // default
      assertThat(interceptor.intercept(context)).isNotNull();

      context.getActionBeanContext().setEventName("save");
      context.setLifecycleStage(LifecycleStage.EventHandling); // default
      assertThat(interceptor.intercept(context)).isNotNull();

      assertThat(actionBean.getHasCalledAfterDefaultStage()).isEqualTo(2);
      assertThat(actionBean.getHasCalledBeforeWithReturn()).isEqualTo(2);
      assertThat(actionBean.getHasCalledBeforeDefaultStage()).isEqualTo(2);
      assertThat(actionBean.getHasCalledAfterWithReturn()).isEqualTo(2);
      assertThat(actionBean.getHasCalledBeforeAfterDefaultStage()).isEqualTo(4);
      assertThat(actionBean.getHasCalledBeforeAfterOnSingleEvent()).isEqualTo(2);
      assertThat(actionBean.getHasCalledProtectedAfterMethod()).isEqualTo(2);
      assertThat(actionBean.getHasCalledProtectedBeforeMethod()).isEqualTo(2);

      assertThat(actionBean.getHasCalledAfterSpecificStage()).isEqualTo(0);
      assertThat(actionBean.getHasCalledAfterTwoStages()).isEqualTo(0);
      assertThat(actionBean.getHasCalledAfterWithParameter()).isEqualTo(0);
      assertThat(actionBean.getHasCalledAfterWithReturnAndParameter()).isEqualTo(0);
      assertThat(actionBean.getHasCalledBeforeSpecificStage()).isEqualTo(0);
      assertThat(actionBean.getHasCalledBeforeTwoStages()).isEqualTo(0);
      assertThat(actionBean.getHasCalledBeforeAfterSpecificStage()).isEqualTo(0);
      assertThat(actionBean.getHasCalledBeforeAfterWithParameter()).isEqualTo(0);
      assertThat(actionBean.getHasCalledBeforeWithParameter()).isEqualTo(0);
      assertThat(actionBean.getHasCalledBeforeWithReturnAndParameter()).isEqualTo(0);
      assertThat(actionBean.getHasCalledDummyMethod()).isEqualTo(0);
   }

   /**
    * Test ActionBean class
    * @author Jeppe Cramon
    */
   private static class TestActionBean2 implements ActionBean {

      private int hasCalledBeforeActionBeanResolutionWillNeverBeCalled;
      private int hasCalledBeforeDefaultStage;
      private int hasCalledBeforeSpecificStage;
      private int hasCalledBeforeTwoStages;
      private int hasCalledBeforeWithReturn;
      private int hasCalledBeforeWithParameter;
      private int hasCalledBeforeWithReturnAndParameter;
      private int hasCalledProtectedBeforeMethod;
      private int hasCalledDummyMethod;
      private int hasCalledAfterDefaultStage;
      private int hasCalledAfterSpecificStage;
      private int hasCalledAfterTwoStages;
      private int hasCalledAfterWithReturn;
      private int hasCalledAfterWithParameter;
      private int hasCalledAfterWithReturnAndParameter;
      private int hasCalledProtectedAfterMethod;
      private int hasCalledBeforeAfterWithParameter;
      private int hasCalledBeforeAfterSpecificStage;
      private int hasCalledBeforeAfterDefaultStage;
      private int hasCalledBeforeAfterOnSingleEvent;

      @SuppressWarnings("unused")
      @After
      public void afterDefaultStage() {
         hasCalledAfterDefaultStage++;
      }

      @SuppressWarnings("unused")
      @After(stages = LifecycleStage.ActionBeanResolution)
      public void afterSpecificStage() {
         hasCalledAfterSpecificStage++;
      }

      @SuppressWarnings("unused")
      @After(stages = { LifecycleStage.HandlerResolution, LifecycleStage.CustomValidation })
      public void afterTwoStages() {
         hasCalledAfterTwoStages++;
      }

      /** Not invoked because parameters are not kosher. */
      @SuppressWarnings("unused")
      @After
      public void afterWithParameter( String var ) {
         hasCalledAfterWithParameter++;
      }

      /** Returns are ok, and will just be ignored if not Resolutions. */
      @SuppressWarnings("unused")
      @After
      public String afterWithReturn() {
         hasCalledAfterWithReturn++;
         return null;
      }

      /** Not invoked because parameters are not kosher. */
      @SuppressWarnings("unused")
      @After
      public String afterWithReturnAndParameter( String var ) {
         hasCalledAfterWithReturnAndParameter++;
         return null;
      }

      @SuppressWarnings("unused")
      @Before(stages = LifecycleStage.ActionBeanResolution)
      public void beforeActionBeanResolutionWillNeverBeCalled() {
         hasCalledBeforeActionBeanResolutionWillNeverBeCalled++;
      }

      /** Invoked only at default EventHandling stage. */
      @SuppressWarnings("unused")
      @Before
      @After
      public void beforeAfterDefaultStage() {
         hasCalledBeforeAfterDefaultStage++;
      }

      /** Invoked only at default EventHandling stage. */
      @SuppressWarnings("unused")
      @Before(on = "edit")
      @After(on = "save")
      public void beforeAfterOnSingleEvent() {
         hasCalledBeforeAfterOnSingleEvent++;
      }

      /** Invoked only at those stages listed. */
      @SuppressWarnings("unused")
      @Before(stages = LifecycleStage.BindingAndValidation)
      @After(stages = LifecycleStage.CustomValidation)
      public void beforeAfterSpecificStage() {
         hasCalledBeforeAfterSpecificStage++;
      }

      /** Not invoked because parameters are not kosher. */
      @SuppressWarnings("unused")
      @Before
      @After
      public String beforeAfterWithParameter( String var ) {
         hasCalledBeforeAfterWithParameter++;
         return null;
      }

      @SuppressWarnings("unused")
      @Before
      public void beforeDefaultStage() {
         hasCalledBeforeDefaultStage++;
      }

      @SuppressWarnings("unused")
      @Before(stages = LifecycleStage.HandlerResolution)
      public void beforeSpecificStage() {
         hasCalledBeforeSpecificStage++;
      }

      @SuppressWarnings("unused")
      @Before(stages = { LifecycleStage.BindingAndValidation, LifecycleStage.CustomValidation })
      public void beforeTwoStages() {
         hasCalledBeforeTwoStages++;
      }

      /** Parameters are not allowed. */
      @SuppressWarnings("unused")
      @Before
      public void beforeWithParameter( String var ) {
         hasCalledBeforeWithParameter++;
      }

      @SuppressWarnings("unused")
      @Before
      public String beforeWithReturn() {
         hasCalledBeforeWithReturn++;
         return null;
      }

      /** Parameters are not allowed. */
      @SuppressWarnings("unused")
      @Before
      public String beforeWithReturnAndParameter( String var ) {
         hasCalledBeforeWithReturnAndParameter++;
         return null;
      }

      /** Not annotated to be called by anyone */
      @SuppressWarnings("unused")
      public void dummyMethod() {
         hasCalledDummyMethod++;
      }

      @Override
      public ActionBeanContext getContext() {
         return null;
      }

      // -- Unit test properties --
      public int getHasCalledAfterDefaultStage() { return hasCalledAfterDefaultStage; }

      public int getHasCalledAfterSpecificStage() { return hasCalledAfterSpecificStage; }

      public int getHasCalledAfterTwoStages() { return hasCalledAfterTwoStages; }

      public int getHasCalledAfterWithParameter() { return hasCalledAfterWithParameter; }

      public int getHasCalledAfterWithReturn() { return hasCalledAfterWithReturn; }

      public int getHasCalledAfterWithReturnAndParameter() { return hasCalledAfterWithReturnAndParameter; }

      @SuppressWarnings("unused")
      public int getHasCalledBeforeActionBeanResolutionWillNeverBeCalled() { return hasCalledBeforeActionBeanResolutionWillNeverBeCalled; }

      public int getHasCalledBeforeAfterDefaultStage() { return hasCalledBeforeAfterDefaultStage; }

      public int getHasCalledBeforeAfterOnSingleEvent() { return hasCalledBeforeAfterOnSingleEvent; }

      public int getHasCalledBeforeAfterSpecificStage() { return hasCalledBeforeAfterSpecificStage; }

      public int getHasCalledBeforeAfterWithParameter() { return hasCalledBeforeAfterWithParameter; }

      public int getHasCalledBeforeDefaultStage() { return hasCalledBeforeDefaultStage; }

      public int getHasCalledBeforeSpecificStage() { return hasCalledBeforeSpecificStage; }

      public int getHasCalledBeforeTwoStages() { return hasCalledBeforeTwoStages; }

      public int getHasCalledBeforeWithParameter() { return hasCalledBeforeWithParameter; }

      public int getHasCalledBeforeWithReturn() { return hasCalledBeforeWithReturn; }

      public int getHasCalledBeforeWithReturnAndParameter() { return hasCalledBeforeWithReturnAndParameter; }

      public int getHasCalledDummyMethod() { return hasCalledDummyMethod; }

      public int getHasCalledProtectedAfterMethod() { return hasCalledProtectedAfterMethod; }

      public int getHasCalledProtectedBeforeMethod() { return hasCalledProtectedBeforeMethod; }

      @Override
      public void setContext( ActionBeanContext context ) {
      }

      /** Should work just like a public method. */
      @SuppressWarnings("unused")
      @After
      protected void protectedAfterMethod() {
         hasCalledProtectedAfterMethod++;
      }

      /** Should work just like a public method. */
      @SuppressWarnings("unused")
      @Before
      protected void protectedBeforeMethod() {
         hasCalledProtectedBeforeMethod++;
      }
   }


   private static class TestExecutionContext extends ExecutionContext {

      @Override
      public Resolution proceed() {
         return new ForwardResolution("wakker");
      }
   }
}
