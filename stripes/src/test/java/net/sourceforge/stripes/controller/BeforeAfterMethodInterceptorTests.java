package net.sourceforge.stripes.controller;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.After;
import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import org.junit.Assert;
import org.junit.Test;

/**
 * TestNG based unit test of the {@link BeforeAfterMethodInterceptor} class.
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
    Assert.assertNotNull(interceptor.intercept(context));

    Assert.assertEquals(actionBean.getHasCalledAfterDefaultStage(), 0);
    Assert.assertEquals(actionBean.getHasCalledAfterSpecificStage(), 1);
    Assert.assertEquals(actionBean.getHasCalledAfterTwoStages(), 0);
    Assert.assertEquals(actionBean.getHasCalledAfterWithReturn(), 0);
    Assert.assertEquals(actionBean.getHasCalledAfterWithParameter(), 0);
    Assert.assertEquals(actionBean.getHasCalledAfterWithReturnAndParameter(), 0);
    Assert.assertEquals(actionBean.getHasCalledBeforeDefaultStage(), 0);
    Assert.assertEquals(actionBean.getHasCalledBeforeSpecificStage(), 0);
    Assert.assertEquals(actionBean.getHasCalledBeforeTwoStages(), 0);
    Assert.assertEquals(actionBean.getHasCalledBeforeAfterSpecificStage(), 0);
    Assert.assertEquals(actionBean.getHasCalledBeforeAfterDefaultStage(), 0);
    Assert.assertEquals(actionBean.getHasCalledBeforeAfterWithParameter(), 0);
    Assert.assertEquals(actionBean.getHasCalledBeforeWithParameter(), 0);
    Assert.assertEquals(actionBean.getHasCalledBeforeWithReturnAndParameter(), 0);
    Assert.assertEquals(actionBean.getHasCalledDummyMethod(), 0);
    Assert.assertEquals(actionBean.getHasCalledProtectedAfterMethod(), 0);
    Assert.assertEquals(actionBean.getHasCalledProtectedBeforeMethod(), 0);
  }

  @Test
  public void testInterceptAtLifeCycleStage_BindingAndValidation() throws Exception {
    ExecutionContext context = new TestExecutionContext();
    TestActionBean2 actionBean = new TestActionBean2();
    context.setActionBean(actionBean);
    context.setLifecycleStage(LifecycleStage.BindingAndValidation);

    BeforeAfterMethodInterceptor interceptor = new BeforeAfterMethodInterceptor();
    Assert.assertNotNull(interceptor.intercept(context));

    Assert.assertEquals(actionBean.getHasCalledAfterDefaultStage(), 0);
    Assert.assertEquals(actionBean.getHasCalledAfterSpecificStage(), 0);
    Assert.assertEquals(actionBean.getHasCalledAfterTwoStages(), 0);
    Assert.assertEquals(actionBean.getHasCalledAfterWithReturn(), 0);
    Assert.assertEquals(actionBean.getHasCalledAfterWithParameter(), 0);
    Assert.assertEquals(actionBean.getHasCalledAfterWithReturnAndParameter(), 0);
    Assert.assertEquals(actionBean.getHasCalledBeforeDefaultStage(), 0);
    Assert.assertEquals(actionBean.getHasCalledBeforeSpecificStage(), 0);
    Assert.assertEquals(actionBean.getHasCalledBeforeTwoStages(), 1);
    Assert.assertEquals(actionBean.getHasCalledBeforeAfterSpecificStage(), 1);
    Assert.assertEquals(actionBean.getHasCalledBeforeAfterDefaultStage(), 0);
    Assert.assertEquals(actionBean.getHasCalledBeforeAfterWithParameter(), 0);
    Assert.assertEquals(actionBean.getHasCalledBeforeWithParameter(), 0);
    Assert.assertEquals(actionBean.getHasCalledBeforeWithReturnAndParameter(), 0);
    Assert.assertEquals(actionBean.getHasCalledDummyMethod(), 0);
    Assert.assertEquals(actionBean.getHasCalledProtectedAfterMethod(), 0);
    Assert.assertEquals(actionBean.getHasCalledProtectedBeforeMethod(), 0);
  }

  @Test
  public void testInterceptAtLifeCycleStage_CustomValidation() throws Exception {
    ExecutionContext context = new TestExecutionContext();
    TestActionBean2 actionBean = new TestActionBean2();
    context.setActionBean(actionBean);
    context.setLifecycleStage(LifecycleStage.CustomValidation);

    BeforeAfterMethodInterceptor interceptor = new BeforeAfterMethodInterceptor();
    Assert.assertNotNull(interceptor.intercept(context));

    Assert.assertEquals(actionBean.getHasCalledAfterDefaultStage(), 0);
    Assert.assertEquals(actionBean.getHasCalledAfterSpecificStage(), 0);
    Assert.assertEquals(actionBean.getHasCalledAfterTwoStages(), 1);
    Assert.assertEquals(actionBean.getHasCalledAfterWithReturn(), 0);
    Assert.assertEquals(actionBean.getHasCalledAfterWithParameter(), 0);
    Assert.assertEquals(actionBean.getHasCalledAfterWithReturnAndParameter(), 0);
    Assert.assertEquals(actionBean.getHasCalledBeforeDefaultStage(), 0);
    Assert.assertEquals(actionBean.getHasCalledBeforeSpecificStage(), 0);
    Assert.assertEquals(actionBean.getHasCalledBeforeTwoStages(), 1);
    Assert.assertEquals(actionBean.getHasCalledBeforeAfterSpecificStage(), 1);
    Assert.assertEquals(actionBean.getHasCalledBeforeAfterDefaultStage(), 0);
    Assert.assertEquals(actionBean.getHasCalledBeforeAfterWithParameter(), 0);
    Assert.assertEquals(actionBean.getHasCalledBeforeWithParameter(), 0);
    Assert.assertEquals(actionBean.getHasCalledBeforeWithReturnAndParameter(), 0);
    Assert.assertEquals(actionBean.getHasCalledDummyMethod(), 0);
    Assert.assertEquals(actionBean.getHasCalledProtectedAfterMethod(), 0);
    Assert.assertEquals(actionBean.getHasCalledProtectedBeforeMethod(), 0);
  }

  @Test
  public void testInterceptAtLifeCycleStage_EventHandling() throws Exception {
    ExecutionContext context = new TestExecutionContext();
    TestActionBean2 actionBean = new TestActionBean2();
    context.setActionBean(actionBean);
    context.setLifecycleStage(LifecycleStage.EventHandling);

    BeforeAfterMethodInterceptor interceptor = new BeforeAfterMethodInterceptor();
    Assert.assertNotNull(interceptor.intercept(context));

    Assert.assertEquals(actionBean.getHasCalledAfterDefaultStage(), 1);
    Assert.assertEquals(actionBean.getHasCalledAfterSpecificStage(), 0);
    Assert.assertEquals(actionBean.getHasCalledAfterTwoStages(), 0);
    Assert.assertEquals(actionBean.getHasCalledAfterWithReturn(), 1);
    Assert.assertEquals(actionBean.getHasCalledAfterWithParameter(), 0);
    Assert.assertEquals(actionBean.getHasCalledAfterWithReturnAndParameter(), 0);
    Assert.assertEquals(actionBean.getHasCalledBeforeDefaultStage(), 1);
    Assert.assertEquals(actionBean.getHasCalledBeforeSpecificStage(), 0);
    Assert.assertEquals(actionBean.getHasCalledBeforeTwoStages(), 0);
    Assert.assertEquals(actionBean.getHasCalledBeforeWithReturn(), 1);
    Assert.assertEquals(actionBean.getHasCalledBeforeAfterSpecificStage(), 0);
    Assert.assertEquals(actionBean.getHasCalledBeforeAfterDefaultStage(), 2);
    Assert.assertEquals(actionBean.getHasCalledBeforeAfterWithParameter(), 0);
    Assert.assertEquals(actionBean.getHasCalledBeforeWithParameter(), 0);
    Assert.assertEquals(actionBean.getHasCalledBeforeWithReturnAndParameter(), 0);
    Assert.assertEquals(actionBean.getHasCalledDummyMethod(), 0);
    Assert.assertEquals(actionBean.getHasCalledProtectedAfterMethod(), 1);
    Assert.assertEquals(actionBean.getHasCalledProtectedBeforeMethod(), 1);
  }

  @Test
  public void testInterceptAtLifeCycleStage_HandlerResolution() throws Exception {
    ExecutionContext context = new TestExecutionContext();
    TestActionBean2 actionBean = new TestActionBean2();
    context.setActionBean(actionBean);
    context.setLifecycleStage(LifecycleStage.HandlerResolution);

    BeforeAfterMethodInterceptor interceptor = new BeforeAfterMethodInterceptor();
    Assert.assertNotNull(interceptor.intercept(context));

    Assert.assertEquals(actionBean.getHasCalledAfterDefaultStage(), 0);
    Assert.assertEquals(actionBean.getHasCalledAfterSpecificStage(), 0);
    Assert.assertEquals(actionBean.getHasCalledAfterTwoStages(), 1);
    Assert.assertEquals(actionBean.getHasCalledAfterWithReturn(), 0);
    Assert.assertEquals(actionBean.getHasCalledAfterWithParameter(), 0);
    Assert.assertEquals(actionBean.getHasCalledAfterWithReturnAndParameter(), 0);
    Assert.assertEquals(actionBean.getHasCalledBeforeDefaultStage(), 0);
    Assert.assertEquals(actionBean.getHasCalledBeforeSpecificStage(), 1);
    Assert.assertEquals(actionBean.getHasCalledBeforeTwoStages(), 0);
    Assert.assertEquals(actionBean.getHasCalledBeforeAfterSpecificStage(), 0);
    Assert.assertEquals(actionBean.getHasCalledBeforeAfterDefaultStage(), 0);
    Assert.assertEquals(actionBean.getHasCalledBeforeAfterWithParameter(), 0);
    Assert.assertEquals(actionBean.getHasCalledBeforeWithParameter(), 0);
    Assert.assertEquals(actionBean.getHasCalledBeforeWithReturnAndParameter(), 0);
    Assert.assertEquals(actionBean.getHasCalledDummyMethod(), 0);
    Assert.assertEquals(actionBean.getHasCalledProtectedAfterMethod(), 0);
    Assert.assertEquals(actionBean.getHasCalledProtectedBeforeMethod(), 0);
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
    Assert.assertNotNull(interceptor.intercept(context));

    context.getActionBeanContext().setEventName("save");
    context.setLifecycleStage(LifecycleStage.EventHandling); // default
    Assert.assertNotNull(interceptor.intercept(context));

    Assert.assertEquals(actionBean.getHasCalledAfterDefaultStage(), 2);
    Assert.assertEquals(actionBean.getHasCalledBeforeWithReturn(), 2);
    Assert.assertEquals(actionBean.getHasCalledBeforeDefaultStage(), 2);
    Assert.assertEquals(actionBean.getHasCalledAfterWithReturn(), 2);
    Assert.assertEquals(actionBean.getHasCalledBeforeAfterDefaultStage(), 4);
    Assert.assertEquals(actionBean.getHasCalledBeforeAfterOnSingleEvent(), 2);
    Assert.assertEquals(actionBean.getHasCalledProtectedAfterMethod(), 2);
    Assert.assertEquals(actionBean.getHasCalledProtectedBeforeMethod(), 2);

    Assert.assertEquals(actionBean.getHasCalledAfterSpecificStage(), 0);
    Assert.assertEquals(actionBean.getHasCalledAfterTwoStages(), 0);
    Assert.assertEquals(actionBean.getHasCalledAfterWithParameter(), 0);
    Assert.assertEquals(actionBean.getHasCalledAfterWithReturnAndParameter(), 0);
    Assert.assertEquals(actionBean.getHasCalledBeforeSpecificStage(), 0);
    Assert.assertEquals(actionBean.getHasCalledBeforeTwoStages(), 0);
    Assert.assertEquals(actionBean.getHasCalledBeforeAfterSpecificStage(), 0);
    Assert.assertEquals(actionBean.getHasCalledBeforeAfterWithParameter(), 0);
    Assert.assertEquals(actionBean.getHasCalledBeforeWithParameter(), 0);
    Assert.assertEquals(actionBean.getHasCalledBeforeWithReturnAndParameter(), 0);
    Assert.assertEquals(actionBean.getHasCalledDummyMethod(), 0);
  }

  /**
   * Test ActionBean class
   *
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

    public void setContext(ActionBeanContext context) {}

    public ActionBeanContext getContext() {
      return null;
    }

    @SuppressWarnings("unused")
    @Before(stages = LifecycleStage.ActionBeanResolution)
    public void beforeActionBeanResolutionWillNeverBeCalled() {
      hasCalledBeforeActionBeanResolutionWillNeverBeCalled++;
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
    @Before(stages = {LifecycleStage.BindingAndValidation, LifecycleStage.CustomValidation})
    public void beforeTwoStages() {
      hasCalledBeforeTwoStages++;
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
    public void beforeWithParameter(String var) {
      hasCalledBeforeWithParameter++;
    }

    /** Parameters are not allowed. */
    @SuppressWarnings("unused")
    @Before
    public String beforeWithReturnAndParameter(String var) {
      hasCalledBeforeWithReturnAndParameter++;
      return null;
    }

    /** Should work just like a public method. */
    @SuppressWarnings("unused")
    @Before
    protected void protectedBeforeMethod() {
      hasCalledProtectedBeforeMethod++;
    }

    /** Not annotated to be called by anyone */
    @SuppressWarnings("unused")
    public void dummyMethod() {
      hasCalledDummyMethod++;
    }

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
    @After(stages = {LifecycleStage.HandlerResolution, LifecycleStage.CustomValidation})
    public void afterTwoStages() {
      hasCalledAfterTwoStages++;
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
    public void afterWithParameter(String var) {
      hasCalledAfterWithParameter++;
    }

    /** Not invoked because parameters are not kosher. */
    @SuppressWarnings("unused")
    @After
    public String afterWithReturnAndParameter(String var) {
      hasCalledAfterWithReturnAndParameter++;
      return null;
    }

    /** Should work just like a public method. */
    @SuppressWarnings("unused")
    @After
    protected void protectedAfterMethod() {
      hasCalledProtectedAfterMethod++;
    }

    /** Not invoked because parameters are not kosher. */
    @SuppressWarnings("unused")
    @Before
    @After
    public String beforeAfterWithParameter(String var) {
      hasCalledBeforeAfterWithParameter++;
      return null;
    }

    /** Invoked only at those stages listed. */
    @SuppressWarnings("unused")
    @Before(stages = LifecycleStage.BindingAndValidation)
    @After(stages = LifecycleStage.CustomValidation)
    public void beforeAfterSpecificStage() {
      hasCalledBeforeAfterSpecificStage++;
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

    // -- Unit test properties --
    public int getHasCalledAfterDefaultStage() {
      return hasCalledAfterDefaultStage;
    }

    public int getHasCalledAfterSpecificStage() {
      return hasCalledAfterSpecificStage;
    }

    public int getHasCalledAfterTwoStages() {
      return hasCalledAfterTwoStages;
    }

    public int getHasCalledAfterWithReturn() {
      return hasCalledAfterWithReturn;
    }

    public int getHasCalledAfterWithParameter() {
      return hasCalledAfterWithParameter;
    }

    public int getHasCalledAfterWithReturnAndParameter() {
      return hasCalledAfterWithReturnAndParameter;
    }

    public int getHasCalledBeforeDefaultStage() {
      return hasCalledBeforeDefaultStage;
    }

    public int getHasCalledBeforeSpecificStage() {
      return hasCalledBeforeSpecificStage;
    }

    public int getHasCalledBeforeTwoStages() {
      return hasCalledBeforeTwoStages;
    }

    public int getHasCalledBeforeAfterSpecificStage() {
      return hasCalledBeforeAfterSpecificStage;
    }

    public int getHasCalledBeforeAfterWithParameter() {
      return hasCalledBeforeAfterWithParameter;
    }

    @SuppressWarnings("unused")
    public int getHasCalledBeforeActionBeanResolutionWillNeverBeCalled() {
      return hasCalledBeforeActionBeanResolutionWillNeverBeCalled;
    }

    public int getHasCalledBeforeWithReturn() {
      return hasCalledBeforeWithReturn;
    }

    public int getHasCalledBeforeWithParameter() {
      return hasCalledBeforeWithParameter;
    }

    public int getHasCalledBeforeWithReturnAndParameter() {
      return hasCalledBeforeWithReturnAndParameter;
    }

    public int getHasCalledDummyMethod() {
      return hasCalledDummyMethod;
    }

    public int getHasCalledProtectedAfterMethod() {
      return hasCalledProtectedAfterMethod;
    }

    public int getHasCalledProtectedBeforeMethod() {
      return hasCalledProtectedBeforeMethod;
    }

    public int getHasCalledBeforeAfterDefaultStage() {
      return hasCalledBeforeAfterDefaultStage;
    }

    public int getHasCalledBeforeAfterOnSingleEvent() {
      return hasCalledBeforeAfterOnSingleEvent;
    }
  }

  private static class TestExecutionContext extends ExecutionContext {
    @Override
    public Resolution proceed() throws Exception {
      return new ForwardResolution("wakker");
    }
  }
}
