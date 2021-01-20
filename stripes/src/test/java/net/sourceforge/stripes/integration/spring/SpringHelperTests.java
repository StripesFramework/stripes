package net.sourceforge.stripes.integration.spring;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import net.sourceforge.stripes.StripesTestFixture;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.controller.DefaultObjectFactory;
import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.testbeans.TestActionBean;
import net.sourceforge.stripes.testbeans.TestBean;


/**
 * Unit tests for the SpringHelper class that injects spring managed beans
 * into objects.
 *
 * @author Tim Fennell
 */
@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public class SpringHelperTests {

   StaticApplicationContext ctx;

   @Test(groups = "fast", expectedExceptions = StripesRuntimeException.class)
   public void testAmbiguousByTypeInjection() {
      AmbiguousByTypeTarget target = new AmbiguousByTypeTarget();
      SpringHelper.injectBeans(target, ctx);
   }

   ///////////////////////////////////////////////////////////////////////////

   @Test(groups = "fast")
   public void testByTypeInjection() {
      ByTypeTarget target = new ByTypeTarget();
      SpringHelper.injectBeans(target, ctx);
      Assert.assertNotNull(target.getBean());
   }

   @Test(groups = "slow", threadPoolSize = 10, invocationCount = 1000)
   public void testConcurrentInjection() {
      MultipleInjectionTarget2 target = new MultipleInjectionTarget2();
      SpringHelper.injectBeans(target, ctx);
      Assert.assertNotNull(target.someBeanOrOther);
      Assert.assertNotNull(target.testActionBean);
      Assert.assertNotNull(target.number3);
      Assert.assertNotNull(target.number4);
   }

   ///////////////////////////////////////////////////////////////////////////

   @Test(groups = "fast")
   public void testDerivedFromImplicitFieldInjection() {
      DerivedFromImplicitFieldTarget target = new DerivedFromImplicitFieldTarget();
      SpringHelper.injectBeans(target, ctx);
      Assert.assertNotNull(target.getBean());
   }

   @Test(groups = "fast", expectedExceptions = StripesRuntimeException.class)
   public void testExplicitMisNamedTargetInjection() {
      ExplicitMisNamedTarget target = new ExplicitMisNamedTarget();
      SpringHelper.injectBeans(target, ctx);
   }

   ///////////////////////////////////////////////////////////////////////////

   @Test(groups = "fast")
   public void testExplicitNonStandardSetterInjection() {
      ExplicitNonStandardSetterTarget target = new ExplicitNonStandardSetterTarget();
      SpringHelper.injectBeans(target, ctx);
      Assert.assertNotNull(target.getBean());
   }

   @Test(groups = "fast")
   public void testExplicitSetterInjection() {
      ExplicitPublicSetterTarget target = new ExplicitPublicSetterTarget();
      SpringHelper.injectBeans(target, ctx);
      Assert.assertNotNull(target.getBean());
   }

   ///////////////////////////////////////////////////////////////////////////

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

   @Test(groups = "fast")
   public void testImplicitFieldInjection() {
      ImplicitFieldTarget target = new ImplicitFieldTarget();
      SpringHelper.injectBeans(target, ctx);
      Assert.assertNotNull(target.getBean());
   }

   @Test(groups = "fast", expectedExceptions = StripesRuntimeException.class)
   public void testImplicitMisNamedTargetInjection() {
      ImplicitMisNamedTarget target = new ImplicitMisNamedTarget();
      SpringHelper.injectBeans(target, ctx);
   }

   ///////////////////////////////////////////////////////////////////////////

   @Test(groups = "fast")
   public void testImplicitNonStandardSetterInjection() {
      ImplicitNonStandardSetterTarget target = new ImplicitNonStandardSetterTarget();
      SpringHelper.injectBeans(target, ctx);
      Assert.assertNotNull(target.getBean());
   }

   @Test(groups = "fast")
   public void testImplicitStandardSetterInjection() {
      ImplicitStandardSetterTarget target = new ImplicitStandardSetterTarget();
      SpringHelper.injectBeans(target, ctx);
      Assert.assertNotNull(target.getBean());
   }

   ///////////////////////////////////////////////////////////////////////////

   @Test(groups = "fast", dependsOnMethods = "testExplicitSetterInjection")
   public void testInjectionViaObjectPostProcessor() throws Exception {
      Configuration configuration = StripesTestFixture.getDefaultConfiguration();
      ServletContext sc = configuration.getServletContext();
      sc.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, ctx);
      DefaultObjectFactory factory = new DefaultObjectFactory();
      factory.init(configuration);
      factory.addPostProcessor(new SpringInjectionPostProcessor());
      PostProcessorTarget target = factory.newInstance(PostProcessorTarget.class);
      Assert.assertNotNull(target.getBean());
   }

   @Test(groups = "fast", expectedExceptions = StripesRuntimeException.class)
   public void testInvalidSetterSignatureInjection() {
      InvalidSetterSignatureTarget target = new InvalidSetterSignatureTarget();
      SpringHelper.injectBeans(target, ctx);
   }

   ///////////////////////////////////////////////////////////////////////////

   @Test(groups = "fast")
   public void testMultipleInjection() {
      MultipleInjectionTarget target = new MultipleInjectionTarget();
      SpringHelper.injectBeans(target, ctx);
      Assert.assertNotNull(target.someBeanOrOther);
      Assert.assertNotNull(target.testActionBean);
      Assert.assertNotNull(target.number3);
      Assert.assertNotNull(target.number4);
   }

   @Test(groups = "fast", expectedExceptions = StripesRuntimeException.class)
   public void testNoBeansOfTargetTypeInjection() {
      NoBeanOfTypeTarget target = new NoBeanOfTypeTarget();
      SpringHelper.injectBeans(target, ctx);
   }

   ///////////////////////////////////////////////////////////////////////////

   @Test(groups = "fast")
   public void testNotRequiredFieldInjection() {
      NotRequiredBeans target = new NotRequiredBeans();

      SpringHelper.injectBeans(target, ctx);

      Assert.assertNotNull(target.beanExists);
      Assert.assertNull(target.beanDoesNotExist);
   }

   @Test(groups = "fast")
   public void testNotRequiredMethodInjection() {
      NotRequiredMethodBeans target = new NotRequiredMethodBeans();

      SpringHelper.injectBeans(target, ctx);

      Assert.assertNotNull(target.beanExists);
      Assert.assertNull(target.beanDoesNotExist);
   }

   @Test(groups = "fast")
   public void testPrivateFieldInjection() {
      ExplicitPrivateFieldTarget target = new ExplicitPrivateFieldTarget();
      SpringHelper.injectBeans(target, ctx);
      Assert.assertNotNull(target.getBean());
   }

   ///////////////////////////////////////////////////////////////////////////

   @Test(groups = "fast")
   public void testPrivateSetterInjection() {
      ExplicitPrivateSetterTarget target = new ExplicitPrivateSetterTarget();
      SpringHelper.injectBeans(target, ctx);
      Assert.assertNotNull(target.getBean());
   }

   @BeforeClass(alwaysRun = true)
   protected void setupSpringContext() {
      ctx = new StaticWebApplicationContext();
      ctx.registerSingleton("test/TestBean", TestBean.class);
      ctx.registerSingleton("testActionBean", TestActionBean.class);
      ctx.registerPrototype("test/testActionBean", TestActionBean.class);
   }

   ///////////////////////////////////////////////////////////////////////////


   public static class HiddenPrivateFieldTarget1 {

      @Autowired
      private TestBean a;
      @Autowired
      TestBean b;
      @Autowired
      protected TestBean c;
      @Autowired
      public    TestBean d;

      public TestBean getA1() { return a; }

      public TestBean getB1() { return b; }

      public TestBean getC1() { return c; }

      public TestBean getD1() { return d; }
   }


   public static class HiddenPrivateFieldTarget2 extends HiddenPrivateFieldTarget1 {

      @Autowired
      private TestBean a;
      @Autowired
      TestBean b;
      @Autowired
      protected TestBean c;
      @Autowired
      public    TestBean d;

      public TestBean getA2() { return a; }

      public TestBean getB2() { return b; }

      public TestBean getC2() { return c; }

      public TestBean getD2() { return d; }
   }

   ///////////////////////////////////////////////////////////////////////////


   // /////////////////////////////////////////////////////////////////////////
   public static class PostProcessorTarget {

      private TestBean bean;

      public TestBean getBean() { return bean; }

      @Autowired
      @Qualifier("test/TestBean")
      public void setBean( TestBean bean ) { this.bean = bean; }
   }


   private static class AmbiguousByTypeTarget {

      @SuppressWarnings("unused")
      @Autowired
      TestActionBean someBeanOrOther;
   }

   ///////////////////////////////////////////////////////////////////////////


   private static class ByTypeTarget {

      @Autowired
      private TestBean someBeanOrOther;

      TestBean getBean() { return someBeanOrOther; }
   }


   private static class DerivedFromImplicitFieldTarget extends ImplicitFieldTarget {}

   ///////////////////////////////////////////////////////////////////////////


   private static class ExplicitMisNamedTarget {

      @SuppressWarnings("unused")
      @Autowired
      @Qualifier("nonExistentBean")
      TestActionBean someBeanOrOther;
   }


   private static class ExplicitNonStandardSetterTarget {

      private TestBean bean;

      @SuppressWarnings("unused")
      @Autowired
      @Qualifier("test/TestBean")
      protected void injectHere( TestBean bean ) { this.bean = bean; }

      TestBean getBean() { return bean; }
   }

   ///////////////////////////////////////////////////////////////////////////


   private static class ExplicitPrivateFieldTarget {

      @Autowired
      @Qualifier("test/TestBean")
      private TestBean bean;

      TestBean getBean() { return bean; }
   }


   private static class ExplicitPrivateSetterTarget {

      private TestBean bean;

      TestBean getBean() { return bean; }

      @SuppressWarnings("unused")
      @Autowired
      @Qualifier("test/TestBean")
      private void setBean( TestBean bean ) { this.bean = bean; }
   }

   ///////////////////////////////////////////////////////////////////////////


   private static class ExplicitPublicSetterTarget {

      private TestBean bean;

      public TestBean getBean() { return bean; }

      @SuppressWarnings("unused")
      @Autowired
      @Qualifier("test/TestBean")
      public void setBean( TestBean bean ) { this.bean = bean; }
   }


   private static class ImplicitFieldTarget {

      @Autowired
      private TestActionBean testActionBean;

      TestActionBean getBean() { return testActionBean; }
   }

   ///////////////////////////////////////////////////////////////////////////


   private static class ImplicitMisNamedTarget {

      @SuppressWarnings("unused")
      @Autowired
      TestActionBean tstActionBea;
   }


   private static class ImplicitNonStandardSetterTarget {

      private TestActionBean bean;

      @SuppressWarnings("unused")
      @Autowired
      protected void testActionBean( TestActionBean bean ) { this.bean = bean; }

      TestActionBean getBean() { return bean; }
   }


   private static class ImplicitStandardSetterTarget {

      private TestActionBean bean;

      @SuppressWarnings("unused")
      @Autowired
      protected void setTestActionBean( TestActionBean bean ) { this.bean = bean; }

      TestActionBean getBean() { return bean; }
   }


   private static class InvalidSetterSignatureTarget {

      @SuppressWarnings("unused")
      TestActionBean testActionBean;

      @SuppressWarnings("unused")
      @Autowired
      public void setTestActionBean( TestActionBean bean, TestActionBean other ) {
         testActionBean = bean;
      }
   }

   ///////////////////////////////////////////////////////////////////////////


   private static class MultipleInjectionTarget {

      @Autowired
      TestBean       someBeanOrOther; // by type
      @Autowired
      TestActionBean testActionBean; // by field name
      TestActionBean number3; // explicit private method
      TestActionBean number4; // explicit public method

      @SuppressWarnings("unused")
      @Autowired
      @Qualifier("testActionBean")
      public void whee( TestActionBean value ) { number4 = value; }

      @SuppressWarnings("unused")
      @Autowired
      @Qualifier("test/testActionBean")
      private void setNumber3( TestActionBean value ) { number3 = value; }
   }


   private static class MultipleInjectionTarget2 {

      @Autowired
      TestBean       someBeanOrOther; // by type
      @Autowired
      TestActionBean testActionBean; // by field name
      TestActionBean number3; // explicit private method
      TestActionBean number4; // explicit public method

      @SuppressWarnings("unused")
      @Autowired
      @Qualifier("testActionBean")
      public void whee( TestActionBean value ) { number4 = value; }

      @SuppressWarnings("unused")
      @Autowired
      @Qualifier("test/testActionBean")
      private void setNumber3( TestActionBean value ) { number3 = value; }
   }


   private static class NoBeanOfTypeTarget {

      @SuppressWarnings("unused")
      @Autowired
      SpringHelperTests noBeansOfType;
   }


   private static class NotRequiredBeans {

      @Autowired(required = false)
      TestBean beanExists;

      @Autowired(required = false)
      SpringHelperTests beanDoesNotExist;
   }


   private static class NotRequiredMethodBeans {

      TestBean          beanExists;
      SpringHelperTests beanDoesNotExist;

      @Autowired(required = false)
      public void setBeanDoesNotExist( SpringHelperTests beanDoesNotExist ) {
         this.beanDoesNotExist = beanDoesNotExist;
      }

      @Autowired(required = false)
      public void setBeanExists( TestBean beanExists ) {
         this.beanExists = beanExists;
      }
   }
}