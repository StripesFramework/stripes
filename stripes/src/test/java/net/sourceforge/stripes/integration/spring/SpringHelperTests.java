package net.sourceforge.stripes.integration.spring;

import jakarta.servlet.ServletContext;
import net.sourceforge.stripes.StripesTestFixture;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.controller.DefaultObjectFactory;
import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.test.TestActionBean;
import net.sourceforge.stripes.test.TestBean;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.StaticWebApplicationContext;

/**
 * Unit tests for the SpringHelper class that injects spring managed beans into objects.
 *
 * @author Tim Fennell
 */
public class SpringHelperTests {
  static StaticApplicationContext ctx;

  @BeforeClass
  public static void setupSpringContext() {
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
    public void setBean(TestBean bean) {
      this.bean = bean;
    }

    public TestBean getBean() {
      return bean;
    }
  }

  @Test
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
    private void setBean(TestBean bean) {
      this.bean = bean;
    }

    TestBean getBean() {
      return bean;
    }
  }

  @Test
  public void testPrivateSetterInjection() {
    ExplicitPrivateSetterTarget target = new ExplicitPrivateSetterTarget();
    SpringHelper.injectBeans(target, ctx);
    Assert.assertNotNull(target.getBean());
  }

  ///////////////////////////////////////////////////////////////////////////

  private static class ExplicitPrivateFieldTarget {
    @SpringBean("test/TestBean")
    private TestBean bean;

    TestBean getBean() {
      return bean;
    }
  }

  @Test
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
    protected void injectHere(TestBean bean) {
      this.bean = bean;
    }

    TestBean getBean() {
      return bean;
    }
  }

  @Test
  public void testExplicitNonStandardSetterInjection() {
    ExplicitNonStandardSetterTarget target = new ExplicitNonStandardSetterTarget();
    SpringHelper.injectBeans(target, ctx);
    Assert.assertNotNull(target.getBean());
  }

  ///////////////////////////////////////////////////////////////////////////

  private static class ImplicitNonStandardSetterTarget {
    private TestActionBean bean;

    @SuppressWarnings("unused")
    @SpringBean
    protected void testActionBean(TestActionBean bean) {
      this.bean = bean;
    }

    TestActionBean getBean() {
      return bean;
    }
  }

  @Test
  public void testImplicitNonStandardSetterInjection() {
    ImplicitNonStandardSetterTarget target = new ImplicitNonStandardSetterTarget();
    SpringHelper.injectBeans(target, ctx);
    Assert.assertNotNull(target.getBean());
  }

  ///////////////////////////////////////////////////////////////////////////

  private static class ImplicitStandardSetterTarget {
    private TestActionBean bean;

    @SuppressWarnings("unused")
    @SpringBean
    protected void setTestActionBean(TestActionBean bean) {
      this.bean = bean;
    }

    TestActionBean getBean() {
      return bean;
    }
  }

  @Test
  public void testImplicitStandardSetterInjection() {
    ImplicitStandardSetterTarget target = new ImplicitStandardSetterTarget();
    SpringHelper.injectBeans(target, ctx);
    Assert.assertNotNull(target.getBean());
  }

  ///////////////////////////////////////////////////////////////////////////

  private static class ImplicitFieldTarget {
    @SpringBean private TestActionBean testActionBean;

    TestActionBean getBean() {
      return testActionBean;
    }
  }

  @Test
  public void testImplicitFieldInjection() {
    ImplicitFieldTarget target = new ImplicitFieldTarget();
    SpringHelper.injectBeans(target, ctx);
    Assert.assertNotNull(target.getBean());
  }

  ///////////////////////////////////////////////////////////////////////////

  private static class DerivedFromImplicitFieldTarget extends ImplicitFieldTarget {}

  @Test
  public void testDerivedFromImplicitFieldInjection() {
    DerivedFromImplicitFieldTarget target = new DerivedFromImplicitFieldTarget();
    SpringHelper.injectBeans(target, ctx);
    Assert.assertNotNull(target.getBean());
  }

  ///////////////////////////////////////////////////////////////////////////

  private static class ByTypeTarget {
    @SpringBean private TestBean someBeanOrOther;

    TestBean getBean() {
      return someBeanOrOther;
    }
  }

  @Test
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
    private void setNumber3(TestActionBean value) {
      this.number3 = value;
    }

    @SuppressWarnings("unused")
    @SpringBean("testActionBean")
    public void whee(TestActionBean value) {
      this.number4 = value;
    }
  }

  @Test
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
    @SpringBean
    TestActionBean someBeanOrOther;
  }

  @Test(expected = StripesRuntimeException.class)
  public void testAmbiguousByTypeInjection() {
    AmbiguousByTypeTarget target = new AmbiguousByTypeTarget();
    SpringHelper.injectBeans(target, ctx);
  }

  ///////////////////////////////////////////////////////////////////////////

  private static class ExplicitMisNamedTarget {
    @SuppressWarnings("unused")
    @SpringBean("nonExistentBean")
    TestActionBean someBeanOrOther;
  }

  @Test(expected = StripesRuntimeException.class)
  public void testExplicitMisNamedTargetInjection() {
    ExplicitMisNamedTarget target = new ExplicitMisNamedTarget();
    SpringHelper.injectBeans(target, ctx);
  }

  ///////////////////////////////////////////////////////////////////////////

  private static class ImplicitMisNamedTarget {
    @SuppressWarnings("unused")
    @SpringBean
    TestActionBean tstActionBea;
  }

  @Test(expected = StripesRuntimeException.class)
  public void testImplicitMisNamedTargetInjection() {
    ImplicitMisNamedTarget target = new ImplicitMisNamedTarget();
    SpringHelper.injectBeans(target, ctx);
  }

  ///////////////////////////////////////////////////////////////////////////

  private static class NoBeanOfTypeTarget {
    @SuppressWarnings("unused")
    @SpringBean
    SpringHelperTests noBeansOfType;
  }

  @Test(expected = StripesRuntimeException.class)
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

  @Test(expected = StripesRuntimeException.class)
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
    private void setNumber3(TestActionBean value) {
      this.number3 = value;
    }

    @SuppressWarnings("unused")
    @SpringBean("testActionBean")
    public void whee(TestActionBean value) {
      this.number4 = value;
    }
  }

  @Test
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
    public void setBean(TestBean bean) {
      this.bean = bean;
    }

    public TestBean getBean() {
      return bean;
    }
  }

  @Test
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
    @SpringBean private TestBean a;
    @SpringBean TestBean b;
    @SpringBean protected TestBean c;
    @SpringBean public TestBean d;

    public TestBean getA1() {
      return a;
    }

    public TestBean getB1() {
      return b;
    }

    public TestBean getC1() {
      return c;
    }

    public TestBean getD1() {
      return d;
    }
  }

  public static class HiddenPrivateFieldTarget2 extends HiddenPrivateFieldTarget1 {
    @SpringBean private TestBean a;
    @SpringBean TestBean b;
    @SpringBean protected TestBean c;
    @SpringBean public TestBean d;

    public TestBean getA2() {
      return a;
    }

    public TestBean getB2() {
      return b;
    }

    public TestBean getC2() {
      return c;
    }

    public TestBean getD2() {
      return d;
    }
  }

  @Test
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
