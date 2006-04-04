package net.sourceforge.stripes.controller;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.After;
import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import org.testng.Assert;
import org.testng.annotations.Test;

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
        Assert.assertEquals(actionBean.getHasCalledProtectedAfterMethod(), 0);
        Assert.assertEquals(actionBean.getHasCalledProtectedBeforeMethod(), 0);
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

        public void setContext(ActionBeanContext context) {
        }

        public ActionBeanContext getContext() {
            return null;
        }

        @Before(LifecycleStage.ActionBeanResolution)
        public void beforeActionBeanResolutionWillNeverBeCalled() {
            hasCalledBeforeActionBeanResolutionWillNeverBeCalled++;
        }

        @Before
        public void beforeDefaultStage() {
            hasCalledBeforeDefaultStage++;
        }

        @Before(LifecycleStage.HandlerResolution)
        public void beforeSpecificStage() {
            hasCalledBeforeSpecificStage++;
        }

        @Before({LifecycleStage.BindingAndValidation, LifecycleStage.CustomValidation})
        public void beforeTwoStages() {
            hasCalledBeforeTwoStages++;
        }

        @Before
        public String beforeWithReturn() {
            hasCalledBeforeWithReturn++;
            return null;
        }

        /** Parameters are not allowed. */
        @Before
        public void beforeWithParameter(String var) {
            hasCalledBeforeWithParameter++;
        }

        /** Parameters are not allowed. */
        @Before
        public String beforeWithReturnAndParameter(String var) {
            hasCalledBeforeWithReturnAndParameter++;
            return null;
        }

        /** Intercept methods must be public. */
        @Before
        protected void protectedBeforeMethod() {
            hasCalledProtectedBeforeMethod++;
        }

        /** Not annotated to be called by anyone */
        public void dummyMethod() {
            hasCalledDummyMethod++;
        }

        @After
        public void afterDefaultStage() {
            hasCalledAfterDefaultStage++;
        }

        @After(LifecycleStage.ActionBeanResolution)
        public void afterSpecificStage() {
            hasCalledAfterSpecificStage++;
        }

        @After({LifecycleStage.HandlerResolution, LifecycleStage.CustomValidation})
        public void afterTwoStages() {
            hasCalledAfterTwoStages++;
        }

        /** Returns are ok, and will just be ignored if not Resolutions. */
        @After
        public String afterWithReturn() {
            hasCalledAfterWithReturn++;
            return null;
        }

        /** Not invoked because parameters are not kosher. */
        @After
        public void afterWithParameter(String var) {
            hasCalledAfterWithParameter++;
        }

        /** Not invoked because parameters are not kosher. */
        @After
        public String afterWithReturnAndParameter(String var) {
            hasCalledAfterWithReturnAndParameter++;
            return null;
        }

        /** Not included because methods must be public. */
        @After
        protected void protectedAfterMethod() {
            hasCalledProtectedAfterMethod++;
        }


        /** Not invoked because parameters are not kosher. */
        @Before @After
        public String beforeAfterWithParameter(String var) {
            hasCalledBeforeAfterWithParameter++;
            return null;
        }

        /** Invoked only at those stages listed. */
        @Before(LifecycleStage.BindingAndValidation)
        @After(LifecycleStage.CustomValidation)
        public void beforeAfterSpecificStage() {
            hasCalledBeforeAfterSpecificStage++;
        }

        /** Invoked only at default EventHandling stage. */
        @Before @After
        public void beforeAfterDefaultStage() {
            hasCalledBeforeAfterDefaultStage++;
        }

        // -- Unit test properties --
        public int getHasCalledAfterDefaultStage() { return hasCalledAfterDefaultStage; }
        public int getHasCalledAfterSpecificStage() { return hasCalledAfterSpecificStage; }
        public int getHasCalledAfterTwoStages() { return hasCalledAfterTwoStages; }
        public int getHasCalledAfterWithReturn() { return hasCalledAfterWithReturn; }
        public int getHasCalledAfterWithParameter() { return hasCalledAfterWithParameter; }
        public int getHasCalledAfterWithReturnAndParameter() { return hasCalledAfterWithReturnAndParameter; }
        public int getHasCalledBeforeDefaultStage() { return hasCalledBeforeDefaultStage; }
        public int getHasCalledBeforeSpecificStage() { return hasCalledBeforeSpecificStage; }
        public int getHasCalledBeforeTwoStages() { return hasCalledBeforeTwoStages; }
        public int getHasCalledBeforeAfterSpecificStage() { return hasCalledBeforeAfterSpecificStage; }
        public int getHasCalledBeforeAfterWithParameter() { return hasCalledBeforeAfterWithParameter; }
        public int getHasCalledBeforeActionBeanResolutionWillNeverBeCalled() { return hasCalledBeforeActionBeanResolutionWillNeverBeCalled; }
        public int getHasCalledBeforeWithReturn() { return hasCalledBeforeWithReturn; }
        public int getHasCalledBeforeWithParameter() { return hasCalledBeforeWithParameter; }
        public int getHasCalledBeforeWithReturnAndParameter() { return hasCalledBeforeWithReturnAndParameter; }
        public int getHasCalledDummyMethod() { return hasCalledDummyMethod; }
        public int getHasCalledProtectedAfterMethod() { return hasCalledProtectedAfterMethod; }
        public int getHasCalledProtectedBeforeMethod() { return hasCalledProtectedBeforeMethod; }
        public int getHasCalledBeforeAfterDefaultStage() { return hasCalledBeforeAfterDefaultStage; }
    }

    private static class TestExecutionContext extends ExecutionContext {
        @Override
        public Resolution proceed() throws Exception {
            return new ForwardResolution("wakker");
        }

    }

}
