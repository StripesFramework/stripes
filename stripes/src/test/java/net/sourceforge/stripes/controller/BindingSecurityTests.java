package net.sourceforge.stripes.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import net.sourceforge.stripes.FilterEnabledTestBase;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StrictBinding;
import net.sourceforge.stripes.action.StrictBinding.Policy;
import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.util.bean.PropertyExpression;
import net.sourceforge.stripes.util.bean.PropertyExpressionEvaluation;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;
import org.junit.Assert;
import org.junit.Test;

/** Tests binding security. */
public class BindingSecurityTests extends FilterEnabledTestBase {
  public static class NoAnnotation implements ActionBean {
    private ActionBeanContext context;

    public String[] getTestProperties() {
      return new String[] {"foo", "bar", "baz"};
    }

    public boolean[] getExpectSuccess() {
      return new boolean[] {true, true, true};
    }

    public ActionBeanContext getContext() {
      return context;
    }

    public void setContext(ActionBeanContext context) {
      this.context = context;
    }

    private String foo, bar, baz;

    public String getFoo() {
      return foo;
    }

    public void setFoo(String foo) {
      this.foo = foo;
    }

    public String getBar() {
      return bar;
    }

    public void setBar(String bar) {
      this.bar = bar;
    }

    public String getBaz() {
      return baz;
    }

    public void setBaz(String baz) {
      this.baz = baz;
    }

    @DefaultHandler
    public Resolution execute() {
      return null;
    }
  }

  @StrictBinding
  public static class DefaultAnnotation extends BindingSecurityTests.NoAnnotation {
    @Override
    public boolean[] getExpectSuccess() {
      return new boolean[] {false, false, false};
    }
  }

  @StrictBinding(allow = "foo,bar")
  public static class ImplicitDeny extends BindingSecurityTests.NoAnnotation {
    @Override
    public boolean[] getExpectSuccess() {
      return new boolean[] {true, true, false};
    }
  }

  @StrictBinding(allow = "foo,bar,baz", deny = "baz,baz.**")
  public static class ExplicitDeny extends BindingSecurityTests.NoAnnotation {
    @Override
    public boolean[] getExpectSuccess() {
      return new boolean[] {true, true, false};
    }
  }

  @StrictBinding(defaultPolicy = Policy.ALLOW)
  public static class ImplicitAllow extends BindingSecurityTests.NoAnnotation {
    @Override
    public boolean[] getExpectSuccess() {
      return new boolean[] {true, true, true};
    }
  }

  public static class Blah {
    private String name;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }

  @StrictBinding
  public static class HonorValidateAnnotations extends BindingSecurityTests.NoAnnotation {
    @Override
    public boolean[] getExpectSuccess() {
      return new boolean[] {true, true, true, true, true};
    }

    @Override
    public String[] getTestProperties() {
      return new String[] {"foo", "bar", "baz", "blah", "blah.name"};
    }

    @Validate private String foo;
    private String bar;
    private String baz;

    @ValidateNestedProperties(@Validate(field = "name"))
    private Blah blah;

    @Override
    public String getFoo() {
      return foo;
    }

    @Override
    public void setFoo(String foo) {
      this.foo = foo;
    }

    @Validate
    @Override
    public String getBar() {
      return bar;
    }

    @Override
    public void setBar(String bar) {
      this.bar = bar;
    }

    @Override
    public String getBaz() {
      return baz;
    }

    @Validate
    @Override
    public void setBaz(String baz) {
      this.baz = baz;
    }

    public Blah getBlah() {
      return blah;
    }

    public void setBlah(Blah blah) {
      this.blah = blah;
    }
  }

  @StrictBinding(deny = "**")
  public static class OverrideValidateAnnotations
      extends BindingSecurityTests.HonorValidateAnnotations {
    @Override
    public boolean[] getExpectSuccess() {
      return new boolean[] {false, false, false, false, false};
    }
  }

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
    } catch (Exception e) {
      StripesRuntimeException re = new StripesRuntimeException(e.getMessage(), e);
      re.setStackTrace(e.getStackTrace());
      throw re;
    }
  }

  public void evaluate(NoAnnotation bean) throws Exception {
    String[] properties = bean.getTestProperties();
    boolean[] expect = bean.getExpectSuccess();

    Class<? extends NoAnnotation> beanType = bean.getClass();
    MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), beanType);
    for (String p : properties) trip.addParameter(p, p + "Value");
    trip.execute();

    bean = trip.getActionBean(beanType);
    for (int i = 0; i < properties.length; i++) {
      String fullName = beanType.getSimpleName() + "." + properties[i];
      log.debug("Testing binding security on ", fullName);
      PropertyExpression pe = PropertyExpression.getExpression(properties[i]);
      PropertyExpressionEvaluation eval = new PropertyExpressionEvaluation(pe, bean);
      Object value = eval.getValue();
      Assert.assertEquals(
          "Property "
              + fullName
              + " should"
              + (expect[i] ? " not" : "")
              + " be null but it is"
              + (expect[i] ? "" : " not"),
          value != null,
          expect[i]);
    }
  }

  @Test
  @SuppressWarnings("unused")
  public void protectedClasses() {
    class TestBean implements ActionBean {
      public void setContext(ActionBeanContext context) {}

      public ClassLoader getClassLoader() {
        return null;
      }

      public ActionBeanContext getContext() {
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

      public TestBean getOther() {
        return null;
      }
    }

    final String[] expressions = {
      // Direct, single node
      "class",
      "classLoader",
      "context",
      "request",
      "response",
      "session",

      // Indirect, last node
      "other.class",
      "other.classLoader",
      "other.context",
      "other.request",
      "other.response",
      "other.session",

      // Indirect, not first node, not last node
      "other.class.name",
      "other.request.cookies",
      "other.session.id",
    };

    final TestBean bean = new TestBean();
    final BindingPolicyManager bpm = new BindingPolicyManager(TestBean.class);
    for (String expression : expressions) {
      log.debug("Testing illegal expression: " + expression);
      PropertyExpression pe = PropertyExpression.getExpression(expression);
      PropertyExpressionEvaluation eval = new PropertyExpressionEvaluation(pe, bean);
      Assert.assertFalse(
          "Binding should not be allowed for expression " + expression, bpm.isBindingAllowed(eval));
    }
  }

  public static void main(String[] args) {
    BindingSecurityTests tests = new BindingSecurityTests();
    try {
      tests.initCtx();
      tests.bindingPolicyEnforcement();
      tests.protectedClasses();
    } finally {
      tests.closeCtx();
    }
  }
}
