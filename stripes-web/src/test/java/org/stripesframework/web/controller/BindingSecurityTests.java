package org.stripesframework.web.controller;

import static org.assertj.core.api.Assertions.assertThat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.jupiter.api.Test;
import org.stripesframework.web.FilterEnabledTestBase;
import org.stripesframework.web.action.ActionBean;
import org.stripesframework.web.action.ActionBeanContext;
import org.stripesframework.web.action.DefaultHandler;
import org.stripesframework.web.action.Resolution;
import org.stripesframework.web.action.StrictBinding;
import org.stripesframework.web.action.StrictBinding.Policy;
import org.stripesframework.web.exception.StripesRuntimeException;
import org.stripesframework.web.mock.MockRoundtrip;
import org.stripesframework.web.util.Log;
import org.stripesframework.web.util.bean.PropertyExpression;
import org.stripesframework.web.util.bean.PropertyExpressionEvaluation;
import org.stripesframework.web.validation.Validate;
import org.stripesframework.web.validation.ValidateNestedProperties;


/**
 * Tests binding security.
 */
public class BindingSecurityTests extends FilterEnabledTestBase {

   private static final Log log = Log.getInstance(BindingSecurityTests.class);

   @Test
   public void bindingPolicyEnforcement() {
      try {
         evaluate(new NoAnnotation());
         evaluate(new DefaultAnnotation());
         evaluate(new ImplicitDeny());
         evaluate(new ExplicitDeny());
         evaluate(new ImplicitAllow());
         evaluate(new HonorValidateAnnotations());
         evaluate(new OverrideValidateAnnotations());
      }
      catch ( Exception e ) {
         StripesRuntimeException re = new StripesRuntimeException(e.getMessage(), e);
         re.setStackTrace(e.getStackTrace());
         throw re;
      }
   }

   public void evaluate( NoAnnotation bean ) throws Exception {
      String[] properties = bean.getTestProperties();
      boolean[] expect = bean.getExpectSuccess();

      Class<? extends NoAnnotation> beanType = bean.getClass();
      MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), beanType);
      for ( String p : properties ) {
         trip.addParameter(p, p + "Value");
      }
      trip.execute();

      bean = trip.getActionBean(beanType);
      for ( int i = 0; i < properties.length; i++ ) {
         String fullName = beanType.getSimpleName() + "." + properties[i];
         log.debug("Testing binding security on ", fullName);
         PropertyExpression pe = PropertyExpression.getExpression(properties[i]);
         PropertyExpressionEvaluation eval = new PropertyExpressionEvaluation(pe, bean);
         Object value = eval.getValue();
         assertThat(value != null).describedAs(
               "Property " + fullName + " should" + (expect[i] ? " not" : "") + " be null but it is" + (expect[i] ? "" : " not")).isEqualTo(expect[i]);
      }
   }

   @Test
   @SuppressWarnings("unused")
   public void protectedClasses() {
      class TestBean implements ActionBean {

         public ClassLoader getClassLoader() {
            return null;
         }

         @Override
         public ActionBeanContext getContext() {
            return null;
         }

         public TestBean getOther() {
            return null;
         }

         public HttpServletRequest getRequest() {
            return null;
         }

         public HttpServletResponse getResponse() {
            return null;
         }

         public HttpSession getSession() {
            return null;
         }

         @Override
         public void setContext( ActionBeanContext context ) {
         }
      }

      final String[] expressions = {
            // Direct, single node
            "class", "classLoader", "context", "request", "response", "session",

            // Indirect, last node
            "other.class", "other.classLoader", "other.context", "other.request", "other.response", "other.session",

            // Indirect, not first node, not last node
            "other.class.name", "other.request.cookies", "other.session.id", };

      final TestBean bean = new TestBean();
      final BindingPolicyManager bpm = new BindingPolicyManager(TestBean.class);
      for ( String expression : expressions ) {
         log.debug("Testing illegal expression: " + expression);
         PropertyExpression pe = PropertyExpression.getExpression(expression);
         PropertyExpressionEvaluation eval = new PropertyExpressionEvaluation(pe, bean);
         assertThat(bpm.isBindingAllowed(eval)).describedAs("Binding should not be allowed for expression " + expression).isFalse();
      }
   }

   public static class Blah {

      private String name;

      public String getName() {
         return name;
      }

      public void setName( String name ) {
         this.name = name;
      }
   }


   @StrictBinding
   public static class DefaultAnnotation extends BindingSecurityTests.NoAnnotation {

      @Override
      public boolean[] getExpectSuccess() {
         return new boolean[] { false, false, false };
      }
   }


   @StrictBinding(allow = "foo,bar,baz", deny = "baz,baz.**")
   public static class ExplicitDeny extends BindingSecurityTests.NoAnnotation {

      @Override
      public boolean[] getExpectSuccess() {
         return new boolean[] { true, true, false };
      }
   }


   @SuppressWarnings("unused")
   @StrictBinding
   public static class HonorValidateAnnotations extends BindingSecurityTests.NoAnnotation {

      @Validate
      private String foo;
      private String bar;
      private String baz;
      @ValidateNestedProperties(@Validate(field = "name"))
      private Blah   blah;

      @Validate
      @Override
      public String getBar() {
         return bar;
      }

      @Override
      public String getBaz() {
         return baz;
      }

      public Blah getBlah() {
         return blah;
      }

      @Override
      public boolean[] getExpectSuccess() {
         return new boolean[] { true, true, true, true, true };
      }

      @Override
      public String getFoo() {
         return foo;
      }

      @Override
      public String[] getTestProperties() {
         return new String[] { "foo", "bar", "baz", "blah", "blah.name" };
      }

      @Override
      public void setBar( String bar ) {
         this.bar = bar;
      }

      @Validate
      @Override
      public void setBaz( String baz ) {
         this.baz = baz;
      }

      public void setBlah( Blah blah ) {
         this.blah = blah;
      }

      @Override
      public void setFoo( String foo ) {
         this.foo = foo;
      }
   }


   @StrictBinding(defaultPolicy = Policy.ALLOW)
   public static class ImplicitAllow extends BindingSecurityTests.NoAnnotation {

      @Override
      public boolean[] getExpectSuccess() {
         return new boolean[] { true, true, true };
      }
   }


   @StrictBinding(allow = "foo,bar")
   public static class ImplicitDeny extends BindingSecurityTests.NoAnnotation {

      @Override
      public boolean[] getExpectSuccess() {
         return new boolean[] { true, true, false };
      }
   }


   @SuppressWarnings("unused")
   public static class NoAnnotation implements ActionBean {

      private ActionBeanContext context;
      private String            foo, bar, baz;

      @DefaultHandler
      public Resolution execute() {
         return null;
      }

      public String getBar() {
         return bar;
      }

      public String getBaz() {
         return baz;
      }

      @Override
      public ActionBeanContext getContext() {
         return context;
      }

      public boolean[] getExpectSuccess() {
         return new boolean[] { true, true, true };
      }

      public String getFoo() {
         return foo;
      }

      public String[] getTestProperties() {
         return new String[] { "foo", "bar", "baz" };
      }

      public void setBar( String bar ) {
         this.bar = bar;
      }

      public void setBaz( String baz ) {
         this.baz = baz;
      }

      @Override
      public void setContext( ActionBeanContext context ) {
         this.context = context;
      }

      public void setFoo( String foo ) {
         this.foo = foo;
      }
   }


   @StrictBinding(deny = "**")
   public static class OverrideValidateAnnotations extends BindingSecurityTests.HonorValidateAnnotations {

      @Override
      public boolean[] getExpectSuccess() {
         return new boolean[] { false, false, false, false, false };
      }
   }

}
