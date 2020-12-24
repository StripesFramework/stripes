package net.sourceforge.stripes.integration.spring;

import javax.servlet.ServletContext;

import net.sourceforge.stripes.StripesTestFixture;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.controller.DefaultObjectFactory;
import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.test.TestActionBean;
import net.sourceforge.stripes.test.TestBean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
@SuppressWarnings("SpringJavaAutowiredMembersInspection")
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
        @Autowired
        @Qualifier("test/TestBean")
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
        @Autowired
        @Qualifier("test/TestBean")
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
        @Autowired
        @Qualifier("test/TestBean")
        private TestBean bean;

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
        @Autowired
        @Qualifier("test/TestBean")
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
        @Autowired protected void testActionBean(TestActionBean bean) { this.bean = bean; }
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
        @Autowired protected void setTestActionBean(TestActionBean bean) { this.bean = bean; }
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
        @Autowired private TestActionBean testActionBean;
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
        @Autowired private TestBean someBeanOrOther;
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
        @Autowired TestBean someBeanOrOther; // by type
        @Autowired TestActionBean testActionBean; // by field name
        TestActionBean number3; // explicit private method
        TestActionBean number4; // explicit public method

        @SuppressWarnings("unused")
        @Autowired
        @Qualifier("test/testActionBean")
        private void setNumber3(TestActionBean value) { this.number3 = value; }

        @SuppressWarnings("unused")
        @Autowired
        @Qualifier("testActionBean")
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
        @Autowired TestActionBean someBeanOrOther;
    }

    @Test(groups="fast", expectedExceptions=StripesRuntimeException.class)
    public void testAmbiguousByTypeInjection() {
        AmbiguousByTypeTarget target = new AmbiguousByTypeTarget();
        SpringHelper.injectBeans(target, ctx);
    }

    ///////////////////////////////////////////////////////////////////////////

    private static class ExplicitMisNamedTarget {
        @SuppressWarnings("unused")
        @Autowired
        @Qualifier("nonExistentBean")
        TestActionBean someBeanOrOther;
    }

    @Test(groups="fast", expectedExceptions=StripesRuntimeException.class)
    public void testExplicitMisNamedTargetInjection() {
        ExplicitMisNamedTarget target = new ExplicitMisNamedTarget();
        SpringHelper.injectBeans(target, ctx);
    }

    ///////////////////////////////////////////////////////////////////////////

    private static class ImplicitMisNamedTarget {
        @SuppressWarnings("unused")
        @Autowired TestActionBean tstActionBea;
    }

    @Test(groups="fast", expectedExceptions=StripesRuntimeException.class)
    public void testImplicitMisNamedTargetInjection() {
        ImplicitMisNamedTarget target = new ImplicitMisNamedTarget();
        SpringHelper.injectBeans(target, ctx);
    }

    ///////////////////////////////////////////////////////////////////////////

    private static class NoBeanOfTypeTarget {
        @SuppressWarnings("unused")
        @Autowired
        SpringHelperTests noBeansOfType;
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
        @Autowired
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
        @Autowired TestBean someBeanOrOther; // by type
        @Autowired TestActionBean testActionBean; // by field name
        TestActionBean number3; // explicit private method
        TestActionBean number4; // explicit public method

        @SuppressWarnings("unused")
        @Autowired
        @Qualifier("test/testActionBean")
        private void setNumber3(TestActionBean value) { this.number3 = value; }

        @SuppressWarnings("unused")
        @Autowired
        @Qualifier("testActionBean")
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
        @Autowired
        @Qualifier("test/TestBean")
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

    ///////////////////////////////////////////////////////////////////////////

    public static class HiddenPrivateFieldTarget1 {
        @Autowired private TestBean a;
        @Autowired TestBean b;
        @Autowired protected TestBean c;
        @Autowired public TestBean d;
        public TestBean getA1() { return a; }
        public TestBean getB1() { return b; }
        public TestBean getC1() { return c; }
        public TestBean getD1() { return d; }
    }

    public static class HiddenPrivateFieldTarget2 extends HiddenPrivateFieldTarget1 {
        @Autowired private TestBean a;
        @Autowired TestBean b;
        @Autowired protected TestBean c;
        @Autowired public TestBean d;
        public TestBean getA2() { return a; }
        public TestBean getB2() { return b; }
        public TestBean getC2() { return c; }
        public TestBean getD2() { return d; }
    }

    @Test(groups = "fast")
    public void testHiddenFields() {
        HiddenPrivateFieldTarget2 target = new HiddenPrivateFieldTarget2();
        SpringHelper.injectBeans(target, ctx);
        Assert.assertNotNull(target.getA1());
        Assert.assertNotNull(target.getA2());
        Assert.assertNotNull(target.getB1());
        Assert.assertNotNull(target.getB2());
        Assert.assertNotNull(target.getC1());
        Assert.assertNotNull(target.getC2());
        Assert.assertNotNull(target.getD1());
        Assert.assertNotNull(target.getD2());
    }
}