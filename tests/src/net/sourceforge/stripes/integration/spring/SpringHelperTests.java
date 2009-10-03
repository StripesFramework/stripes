package net.sourceforge.stripes.integration.spring;

import javax.servlet.ServletContext;

import net.sourceforge.stripes.StripesTestFixture;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.controller.DefaultObjectFactory;
import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.test.TestActionBean;
import net.sourceforge.stripes.test.TestBean;

import org.springframework.context.support.StaticApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Unit tests for the SpringHelper class that injects spring managed beans
 * into objects.
 *
 * @author Tim Fennell
 */
public class SpringHelperTests {
    StaticApplicationContext ctx;

    @BeforeClass(alwaysRun=true)
    protected void setupSpringContext() {
        ctx = new StaticWebApplicationContext();
        ctx.registerSingleton("test/TestBean", TestBean.class);
        ctx.registerSingleton("testActionBean", TestActionBean.class);
        ctx.registerPrototype("test/testActionBean", TestActionBean.class);
    }

    ///////////////////////////////////////////////////////////////////////////

    private static class ExplicitPublicSetterTarget {
        private TestBean bean;
        @SuppressWarnings("unused")
        @SpringBean("test/TestBean")
        public void setBean(TestBean bean) { this.bean = bean; }
        public TestBean getBean() { return bean; }
    }

    @Test(groups="fast")
    public void testExplicitSetterInjection() {
        ExplicitPublicSetterTarget target = new ExplicitPublicSetterTarget();
        SpringHelper.injectBeans(target, ctx);
        Assert.assertNotNull(target.getBean());
    }

    ///////////////////////////////////////////////////////////////////////////

    private static class ExplicitPrivateSetterTarget {
        private TestBean bean;
        @SuppressWarnings("unused")
        @SpringBean("test/TestBean")
        private void setBean(TestBean bean) { this.bean = bean; }
        TestBean getBean() { return bean; }
    }

    @Test(groups="fast")
    public void testPrivateSetterInjection() {
        ExplicitPrivateSetterTarget target = new ExplicitPrivateSetterTarget();
        SpringHelper.injectBeans(target, ctx);
        Assert.assertNotNull(target.getBean());
    }

    ///////////////////////////////////////////////////////////////////////////

    private static class ExplicitPrivateFieldTarget {
        @SpringBean("test/TestBean") private TestBean bean;
        TestBean getBean() { return bean; }
    }

    @Test(groups="fast")
    public void testPrivateFieldInjection() {
        ExplicitPrivateFieldTarget target = new ExplicitPrivateFieldTarget();
        SpringHelper.injectBeans(target, ctx);
        Assert.assertNotNull(target.getBean());
    }

    ///////////////////////////////////////////////////////////////////////////

    private static class ExplicitNonStandardSetterTarget {
        private TestBean bean;
        @SuppressWarnings("unused")
        @SpringBean("test/TestBean")
        protected void injectHere(TestBean bean) { this.bean = bean; }
        TestBean getBean() { return bean; }
    }

    @Test(groups="fast")
    public void testExplicitNonStandardSetterInjection() {
        ExplicitNonStandardSetterTarget target = new ExplicitNonStandardSetterTarget();
        SpringHelper.injectBeans(target, ctx);
        Assert.assertNotNull(target.getBean());
    }

    ///////////////////////////////////////////////////////////////////////////

    private static class ImplicitNonStandardSetterTarget {
        private TestActionBean bean;
        @SuppressWarnings("unused")
        @SpringBean protected void testActionBean(TestActionBean bean) { this.bean = bean; }
        TestActionBean getBean() { return bean; }
    }

    @Test(groups="fast")
    public void testImplicitNonStandardSetterInjection() {
        ImplicitNonStandardSetterTarget target = new ImplicitNonStandardSetterTarget();
        SpringHelper.injectBeans(target, ctx);
        Assert.assertNotNull(target.getBean());
    }

    ///////////////////////////////////////////////////////////////////////////

    private static class ImplicitStandardSetterTarget {
        private TestActionBean bean;
        @SuppressWarnings("unused")
        @SpringBean protected void setTestActionBean(TestActionBean bean) { this.bean = bean; }
        TestActionBean getBean() { return bean; }
    }

    @Test(groups="fast")
    public void testImplicitStandardSetterInjection() {
        ImplicitStandardSetterTarget target = new ImplicitStandardSetterTarget();
        SpringHelper.injectBeans(target, ctx);
        Assert.assertNotNull(target.getBean());
    }

    ///////////////////////////////////////////////////////////////////////////

    private static class ImplicitFieldTarget {
        @SpringBean private TestActionBean testActionBean;
        TestActionBean getBean() { return testActionBean; }
    }

    @Test(groups="fast")
    public void testImplicitFieldInjection() {
        ImplicitFieldTarget target = new ImplicitFieldTarget();
        SpringHelper.injectBeans(target, ctx);
        Assert.assertNotNull(target.getBean());
    }

    ///////////////////////////////////////////////////////////////////////////

    private static class DerivedFromImplicitFieldTarget extends ImplicitFieldTarget {
    }

    @Test(groups="fast")
    public void testDerivedFromImplicitFieldInjection() {
        DerivedFromImplicitFieldTarget target = new DerivedFromImplicitFieldTarget();
        SpringHelper.injectBeans(target, ctx);
        Assert.assertNotNull(target.getBean());
    }

    ///////////////////////////////////////////////////////////////////////////

    private static class ByTypeTarget {
        @SpringBean private TestBean someBeanOrOther;
        TestBean getBean() { return someBeanOrOther; }
    }

    @Test(groups="fast")
    public void testByTypeInjection() {
        ByTypeTarget target = new ByTypeTarget();
        SpringHelper.injectBeans(target, ctx);
        Assert.assertNotNull(target.getBean());
    }

    ///////////////////////////////////////////////////////////////////////////

    private static class MultipleInjectionTarget {
        @SpringBean TestBean someBeanOrOther; // by type
        @SpringBean TestActionBean testActionBean; // by field name
        TestActionBean number3; // explicit private method
        TestActionBean number4; // explicit public method

        @SuppressWarnings("unused")
        @SpringBean("test/testActionBean")
        private void setNumber3(TestActionBean value) { this.number3 = value; }

        @SuppressWarnings("unused")
        @SpringBean("testActionBean")
        public void whee(TestActionBean value) { this.number4 = value; }
    }

    @Test(groups="fast")
    public void testMultipleInjection() {
        MultipleInjectionTarget target = new MultipleInjectionTarget();
        SpringHelper.injectBeans(target, ctx);
        Assert.assertNotNull(target.someBeanOrOther);
        Assert.assertNotNull(target.testActionBean);
        Assert.assertNotNull(target.number3);
        Assert.assertNotNull(target.number4);
    }

    ///////////////////////////////////////////////////////////////////////////

    private static class AmbiguousByTypeTarget {
        @SuppressWarnings("unused")
        @SpringBean TestActionBean someBeanOrOther;
    }

    @Test(groups="fast", expectedExceptions=StripesRuntimeException.class)
    public void testAmbiguousByTypeInjection() {
        AmbiguousByTypeTarget target = new AmbiguousByTypeTarget();
        SpringHelper.injectBeans(target, ctx);
    }

    ///////////////////////////////////////////////////////////////////////////

    private static class ExplicitMisNamedTarget {
        @SuppressWarnings("unused")
        @SpringBean("nonExistentBean") TestActionBean someBeanOrOther;
    }

    @Test(groups="fast", expectedExceptions=StripesRuntimeException.class)
    public void testExplicitMisNamedTargetInjection() {
    ExplicitMisNamedTarget target = new ExplicitMisNamedTarget();
        SpringHelper.injectBeans(target, ctx);
    }

    ///////////////////////////////////////////////////////////////////////////

    private static class ImplicitMisNamedTarget {
        @SuppressWarnings("unused")
        @SpringBean TestActionBean tstActionBea;
    }

    @Test(groups="fast", expectedExceptions=StripesRuntimeException.class)
    public void testImplicitMisNamedTargetInjection() {
        ImplicitMisNamedTarget target = new ImplicitMisNamedTarget();
        SpringHelper.injectBeans(target, ctx);
    }

    ///////////////////////////////////////////////////////////////////////////

    private static class NoBeanOfTypeTarget {
        @SuppressWarnings("unused")
        @SpringBean SpringHelperTests noBeansOfType;
    }

    @Test(groups="fast", expectedExceptions=StripesRuntimeException.class)
    public void testNoBeansOfTargetTypeInjection() {
        NoBeanOfTypeTarget target = new NoBeanOfTypeTarget();
        SpringHelper.injectBeans(target, ctx);
    }

    ///////////////////////////////////////////////////////////////////////////

    private static class InvalidSetterSignatureTarget {
        @SuppressWarnings("unused")
        TestActionBean testActionBean;
        @SuppressWarnings("unused")
        @SpringBean
        public void setTestActionBean(TestActionBean bean, TestActionBean other) {
            this.testActionBean = bean;
        }
    }

    @Test(groups="fast", expectedExceptions=StripesRuntimeException.class)
    public void testInvalidSetterSignatureInjection() {
        InvalidSetterSignatureTarget target = new InvalidSetterSignatureTarget();
        SpringHelper.injectBeans(target, ctx);
    }

    ///////////////////////////////////////////////////////////////////////////

    private static class MultipleInjectionTarget2 {
        @SpringBean TestBean someBeanOrOther; // by type
        @SpringBean TestActionBean testActionBean; // by field name
        TestActionBean number3; // explicit private method
        TestActionBean number4; // explicit public method

        @SuppressWarnings("unused")
        @SpringBean("test/testActionBean")
        private void setNumber3(TestActionBean value) { this.number3 = value; }

        @SuppressWarnings("unused")
        @SpringBean("testActionBean")
        public void whee(TestActionBean value) { this.number4 = value; }
    }

    @Test(groups="slow", threadPoolSize=10, invocationCount=1000)
    public void testConcurrentInjection() {
        MultipleInjectionTarget2 target = new MultipleInjectionTarget2();
        SpringHelper.injectBeans(target, ctx);
        Assert.assertNotNull(target.someBeanOrOther);
        Assert.assertNotNull(target.testActionBean);
        Assert.assertNotNull(target.number3);
        Assert.assertNotNull(target.number4);
    }

    // /////////////////////////////////////////////////////////////////////////
    public static class PostProcessorTarget {
        private TestBean bean;
        @SpringBean("test/TestBean")
        public void setBean(TestBean bean) { this.bean = bean; }
        public TestBean getBean() { return bean; }
    }

    @Test(groups = "fast", dependsOnMethods = "testExplicitSetterInjection")
    public void testInjectionViaObjectPostProcessor() throws Exception {
        Configuration configuration = StripesTestFixture.getDefaultConfiguration();
        ServletContext sc = configuration.getServletContext();
        sc.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, this.ctx);
        DefaultObjectFactory factory = new DefaultObjectFactory();
        factory.init(configuration);
        factory.addPostProcessor(new SpringInjectionPostProcessor());
        PostProcessorTarget target = factory.newInstance(PostProcessorTarget.class);
        Assert.assertNotNull(target.getBean());
    }
}