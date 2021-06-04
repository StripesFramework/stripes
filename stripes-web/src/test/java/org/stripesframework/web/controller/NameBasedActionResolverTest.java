package org.stripesframework.web.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.stripesframework.web.action.ActionBean;
import org.stripesframework.web.action.ActionBeanContext;
import org.stripesframework.web.action.UrlBinding;


/**
 * Tests for various methods in the NameBasedActionResolver that can be tested in isolation.
 * The resolver is also tested by a lot of the mock tests that run from request through the
 * action layer.
 *
 * @author Tim Fennell
 */
public class NameBasedActionResolverTest {

   private static final NameBasedActionResolver resolver = new NameBasedActionResolver() {

      @Override
      protected Set<Class<? extends ActionBean>> findClasses() {
         Set<Class<? extends ActionBean>> classes = new HashSet<>();
         classes.add(SimpleActionBean.class);
         classes.add(OverloadedActionBean.class);
         classes.add(Container1.OverloadedActionBean.class);
         classes.add(Container2.OverloadedActionBean.class);
         return classes;
      }
   };

   @BeforeAll
   public static void setUp() throws Exception {
      resolver.init(null);
   }

   @Test
   public void generateBinding() {
      String binding = resolver.getUrlBinding("foo.bar.web.admin.ControlCenterActionBean");
      assertThat(binding).isEqualTo("/admin/ControlCenter.action");
   }

   @Test
   public void generateBindingForClassWithSingleBasePackage() {
      String binding = resolver.getUrlBinding("www.ControlCenterActionBean");
      assertThat(binding).isEqualTo("/ControlCenter.action");
   }

   @Test
   public void generateBindingForNonPackagedClass() {
      String binding = resolver.getUrlBinding("ControlCenterActionBean");
      assertThat(binding).isEqualTo("/ControlCenter.action");
   }

   @Test
   public void generateBindingWithDifferentSuffix() {
      String binding = resolver.getUrlBinding("foo.web.stripes.www.admin.ControlCenterBean");
      assertThat(binding).isEqualTo("/admin/ControlCenter.action");
   }

   @Test
   public void generateBindingWithDifferentSuffix2() {
      String binding = resolver.getUrlBinding("foo.web.stripes.www.admin.ControlCenterAction");
      assertThat(binding).isEqualTo("/admin/ControlCenter.action");
   }

   @Test
   public void generateBindingWithMultipleBasePackages() {
      String binding = resolver.getUrlBinding("foo.web.stripes.bar.www.ControlCenterActionBean");
      assertThat(binding).isEqualTo("/ControlCenter.action");
   }

   @Test
   public void generateBindingWithMultipleBasePackages2() {
      String binding = resolver.getUrlBinding("foo.web.stripes.www.admin.ControlCenterActionBean");
      assertThat(binding).isEqualTo("/admin/ControlCenter.action");
   }

   @Test
   public void generateBindingWithoutSuffix() {
      String binding = resolver.getUrlBinding("foo.web.stripes.www.admin.ControlCenter");
      assertThat(binding).isEqualTo("/admin/ControlCenter.action");
   }

   @Test
   public void testFindByNameWithSuffixes() {
      assertThat(resolver.getActionBeanByName("Simple")).isNotNull();
      assertThat(resolver.getActionBeanByName("SimpleAction")).isNotNull();
   }

   @Test
   public void testGetFindViewAttempts() {
      String urlBinding = "/account/ViewAccount.action";
      List<String> viewAttempts = resolver.getFindViewAttempts(urlBinding);

      assertThat(viewAttempts).containsExactly( //
            "/account/ViewAccount.jsp",  //
            "/account/viewAccount.jsp",  //
            "/account/view_account.jsp");
   }

   @Test
   public void testOverloadedBeanNameWithSuffixes() {
      assertThat(resolver.getActionBeanByName("Overloaded")).isNull();
   }

   @Test
   public void testWithAnnotatedClass() {
      String name = org.stripesframework.web.testbeans.TestActionBean.class.getName();
      String binding = resolver.getUrlBinding(name);
      assertThat(binding).isEqualTo("/testbeans/Test.action");

      binding = resolver.getUrlBinding(org.stripesframework.web.testbeans.TestActionBean.class);
      assertThat(binding).isEqualTo(org.stripesframework.web.testbeans.TestActionBean.class.getAnnotation(UrlBinding.class).value());
   }

   static class Container1 {

      static class OverloadedActionBean implements ActionBean {

         @Override
         public ActionBeanContext getContext() {
            return null;
         }

         @Override
         public void setContext( ActionBeanContext context ) {
         }
      }
   }


   static class Container2 {

      static class OverloadedActionBean implements ActionBean {

         @Override
         public ActionBeanContext getContext() {
            return null;
         }

         @Override
         public void setContext( ActionBeanContext context ) {
         }
      }
   }


   static class OverloadedActionBean implements ActionBean {

      @Override
      public ActionBeanContext getContext() {
         return null;
      }

      @Override
      public void setContext( ActionBeanContext context ) {
      }
   }


   static class SimpleActionBean implements ActionBean {

      @Override
      public ActionBeanContext getContext() {
         return null;
      }

      @Override
      public void setContext( ActionBeanContext context ) {
      }
   }
}