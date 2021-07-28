package org.stripesframework.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.jupiter.api.Test;
import org.stripesframework.web.FilterEnabledTestBase;
import org.stripesframework.web.action.ActionBean;
import org.stripesframework.web.action.ActionBeanContext;
import org.stripesframework.web.action.DefaultHandler;
import org.stripesframework.web.action.Resolution;
import org.stripesframework.web.mock.MockRoundtrip;
import org.stripesframework.web.util.Log;
import org.stripesframework.web.util.bean.EvaluationException;
import org.stripesframework.web.util.bean.PropertyExpression;
import org.stripesframework.web.util.bean.PropertyExpressionEvaluation;
import org.stripesframework.web.validation.Validate;
import org.stripesframework.web.validation.ValidateNestedProperties;


/**
 * Tests binding security.
 */
class BindingSecurityTests extends FilterEnabledTestBase {

   private static final Log log = Log.getInstance(BindingSecurityTests.class);

   @Test
   void testDefaultAnnotation() {
      evaluate(new DefaultAnnotation());
   }

   @Test
   void testHonorValidateAnnotations() {
      evaluate(new HonorValidateAnnotations());
   }

   @Test
   void testIllegalBeanAccess() {
      Throwable throwable = catchThrowable(() -> evaluate(new IllegalBeanAccess()));

      assertThat(throwable).isInstanceOf(EvaluationException.class)
            .hasMessage("The expression \"blah[name]\" illegally attempts to access a bean property using bracket notation");
   }

   @Test
   void testNestedListValidateAnnotations() {
      evaluate(new NestedListValidateAnnotations());
   }

   @Test
   void testNestedListValidateAnnotationsDotNotation() {
      Throwable throwable = catchThrowable(() -> evaluate(new NestedListValidateAnnotationsDotNotation()));

      assertThat(throwable).isInstanceOf(EvaluationException.class)
            .hasMessage("The expression \"blahList.0.name\" illegally attempts to access a bean property using dot notation");
   }

   @Test
   void testNestedMapValidateAnnotations() {
      evaluate(new NestedMapValidateAnnotations());
   }

   @Test
   void testNestedMapValidateAnnotationsDotNotation() {
      Throwable throwable = catchThrowable(() -> evaluate(new NestedMapValidateAnnotationsDotNotation()));

      assertThat(throwable).isInstanceOf(EvaluationException.class)
            .hasMessage("The expression \"blahMap.foo.name\" illegally attempts to access a bean property using dot notation");
   }

   @Test
   void testNoAnnotation() {
      evaluate(new NoAnnotation());
   }

   @Test
   @SuppressWarnings("unused")
   void testProtectedClasses() {
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

   @Test
   void testValidateAnnotationOnComplexType() {
      evaluate(new ValidateAnnotationOnComplexType());
   }

   private void evaluate( NoAnnotation bean ) {
      String[] properties = bean.getTestProperties();
      boolean[] expect = bean.getExpectSuccess();

      Class<? extends NoAnnotation> beanType = bean.getClass();
      MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), beanType);
      for ( String p : properties ) {
         trip.addParameter(p, p + "Value");
      }
      try {
         trip.execute();
      }
      catch ( Exception e ) {
         throw new RuntimeException(e);
      }

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

   public static class Blah {

      private String name;
      private String internalName;

      public String getInternalName() {
         return internalName;
      }

      public String getName() {
         return name;
      }

      public void setInternalName( String internalName ) {
         this.internalName = internalName;
      }

      public void setName( String name ) {
         this.name = name;
      }
   }


   public static class DefaultAnnotation extends BindingSecurityTests.NoAnnotation {

      @Override
      public boolean[] getExpectSuccess() {
         return new boolean[] { false, false, false };
      }
   }


   @SuppressWarnings("unused")
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
         return new boolean[] { true, true, true, true, true, false };
      }

      @Override
      public String getFoo() {
         return foo;
      }

      @Override
      public String[] getTestProperties() {
         return new String[] { "foo", "bar", "baz", "blah", "blah.name", "blah.internalName" };
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


   @SuppressWarnings("unused")
   public static class IllegalBeanAccess extends BindingSecurityTests.NoAnnotation {

      private Blah blah;

      @ValidateNestedProperties(@Validate(field = "name"))
      public Blah getBlah() {
         return blah;
      }

      @Override
      public boolean[] getExpectSuccess() {
         return new boolean[] { false };
      }

      @Override
      public String[] getTestProperties() {
         return new String[] { "blah[name]" };
      }

   }


   @SuppressWarnings("unused")
   public static class NestedListValidateAnnotations extends BindingSecurityTests.NoAnnotation {

      private final List<Blah> _blahList = new ArrayList<>();

      @ValidateNestedProperties({ //
                                  @Validate(field = "name"), //
                                })
      public List<Blah> getBlahList() {
         return _blahList;
      }

      @Override
      public boolean[] getExpectSuccess() {
         return new boolean[] { true, false };
      }

      @Override
      public String[] getTestProperties() {
         return new String[] { "blahList[0].name", "blahList[0].internalName" };
      }

   }


   @SuppressWarnings("unused")
   public static class NestedListValidateAnnotationsDotNotation extends BindingSecurityTests.NoAnnotation {

      private final List<Blah> _blahList = new ArrayList<>();

      @ValidateNestedProperties({ //
                                  @Validate(field = "name"), //
                                })
      public List<Blah> getBlahList() {
         return _blahList;
      }

      @Override
      public boolean[] getExpectSuccess() {
         return new boolean[] { true, false };
      }

      @Override
      public String[] getTestProperties() {
         return new String[] { "blahList.0.name", "blahList.0.internalName" };
      }

   }


   @SuppressWarnings("unused")
   public static class NestedMapValidateAnnotations extends BindingSecurityTests.NoAnnotation {

      private final Map<String, Blah> _blahMap = new HashMap<>();

      @ValidateNestedProperties({ //
                                  @Validate(field = "name"), //
                                })
      public Map<String, Blah> getBlahMap() {
         return _blahMap;
      }

      @Override
      public boolean[] getExpectSuccess() {
         return new boolean[] { true, false };
      }

      @Override
      public String[] getTestProperties() {
         return new String[] { "blahMap[foo].name", "blahMap[foo].internalName" };
      }

   }


   @SuppressWarnings("unused")
   public static class NestedMapValidateAnnotationsDotNotation extends BindingSecurityTests.NoAnnotation {

      private final Map<String, Blah> _blahMap = new HashMap<>();

      @ValidateNestedProperties({ //
                                  @Validate(field = "name"), //
                                })
      public Map<String, Blah> getBlahMap() {
         return _blahMap;
      }

      @Override
      public boolean[] getExpectSuccess() {
         return new boolean[] { false, false };
      }

      @Override
      public String[] getTestProperties() {
         return new String[] { "blahMap.foo.name", "blahMap.foo.internalName" };
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
         return new boolean[] { false, false, false };
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


   @SuppressWarnings("unused")
   public static class ValidateAnnotationOnComplexType extends BindingSecurityTests.NoAnnotation {

      private final Blah blah = new Blah();

      @Validate
      public Blah getBlah() {
         return blah;
      }

      @Override
      public boolean[] getExpectSuccess() {
         return new boolean[] { false, false };
      }

      @Override
      public String[] getTestProperties() {
         return new String[] { "blah.name", "blah.internalName" };
      }
   }

}
