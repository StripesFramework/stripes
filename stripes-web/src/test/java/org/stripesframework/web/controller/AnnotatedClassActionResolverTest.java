package org.stripesframework.web.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.stripesframework.web.action.ActionBean;
import org.stripesframework.web.action.ActionBeanContext;
import org.stripesframework.web.action.UrlBinding;
import org.stripesframework.web.controller.AnnotatedClassActionResolver;


public class AnnotatedClassActionResolverTest {

   private final AnnotatedClassActionResolver resolver = new AnnotatedClassActionResolver() {

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

   @BeforeEach
   public void before() throws Exception {
      resolver.init(null);
   }

   @Test
   public void findByName() {
      Class<? extends ActionBean> actionBean = resolver.getActionBeanByName("SimpleActionBean");
      assertThat(actionBean).isNotNull();
   }

   @Test
   public void multipleActionBeansWithSameSimpleName() {
      Class<? extends ActionBean> actionBean = resolver.getActionBeanByName("OverloadedActionBean");
      assertThat(actionBean).isNull();
   }


   static class Container1 {

      @UrlBinding("/container1/Overloaded.action")
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

      @UrlBinding("/container2/Overloaded.action")
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


   @UrlBinding("/Overloaded.action")
   static class OverloadedActionBean implements ActionBean {

      @Override
      public ActionBeanContext getContext() {
         return null;
      }

      @Override
      public void setContext( ActionBeanContext context ) {
      }
   }


   @UrlBinding("/Simple.action")
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