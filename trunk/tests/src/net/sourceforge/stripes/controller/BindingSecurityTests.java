package net.sourceforge.stripes.controller;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.StrictBinding;
import net.sourceforge.stripes.action.StrictBinding.Policy;
import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.util.bean.PropertyExpression;
import net.sourceforge.stripes.util.bean.PropertyExpressionEvaluation;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests binding security.
 */
public class BindingSecurityTests {
    public static class NoAnnotation implements ActionBean {
        public String[] getTestProperties() {
            return new String[] { "foo", "bar", "baz" };
        }

        public boolean[] getExpectSuccess() {
            return new boolean[] { true, true, true };
        }

        public ActionBeanContext getContext() {
            return null;
        }

        public void setContext(ActionBeanContext context) {
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

        public void reset() {
            foo = null;
            bar = null;
            baz = null;
        }
    }

    @StrictBinding
    public static class DefaultAnnotation extends BindingSecurityTests.NoAnnotation {
        @Override
        public boolean[] getExpectSuccess() {
            return new boolean[] { false, false, false };
        }
    }

    @StrictBinding(allow = "foo,bar")
    public static class ImplicitDeny extends BindingSecurityTests.NoAnnotation {
        @Override
        public boolean[] getExpectSuccess() {
            return new boolean[] { true, true, false };
        }
    }

    @StrictBinding(allow = "foo,bar,baz", deny = "baz,baz.**")
    public static class ExplicitDeny extends BindingSecurityTests.NoAnnotation {
        @Override
        public boolean[] getExpectSuccess() {
            return new boolean[] { true, true, false };
        }
    }

    @StrictBinding(defaultPolicy = Policy.ALLOW)
    public static class ImplicitAllow extends BindingSecurityTests.NoAnnotation {
        @Override
        public boolean[] getExpectSuccess() {
            return new boolean[] { true, true, true };
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
            return new boolean[] { true, true, true, false, true };
        }

        @Override
        public String[] getTestProperties() {
            return new String[] { "foo", "bar", "baz", "blah", "blah.name" };
        }

        @Validate
        private String foo;
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
    public static class OverrideValidateAnnotations extends
            BindingSecurityTests.HonorValidateAnnotations {
        @Override
        public boolean[] getExpectSuccess() {
            return new boolean[] { false, false, false, false, false };
        }
    }

    private static final Log log = Log.getInstance(BindingSecurityTests.class);

    @Test(groups = "fast")
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
        catch (Exception e) {
            StripesRuntimeException re = new StripesRuntimeException(e.getMessage(), e);
            re.setStackTrace(e.getStackTrace());
            throw re;
        }
    }

    public void evaluate(NoAnnotation bean) throws InstantiationException, IllegalAccessException {
        String[] properties = bean.getTestProperties();
        boolean[] expect = bean.getExpectSuccess();
        for (int i = 0; i < properties.length; i++) {
            log.debug("Testing ", bean.getClass().getSimpleName(), ".", properties[i]);
            PropertyExpression pe = PropertyExpression.getExpression(properties[i]);
            PropertyExpressionEvaluation eval = new PropertyExpressionEvaluation(pe, bean);
            boolean allowed = BindingPolicyManager.getInstance(bean.getClass()).isBindingAllowed(
                    eval);
            Assert.assertEquals(allowed, expect[i]);
        }
    }

    public static void main(String[] args) {
        new BindingSecurityTests().bindingPolicyEnforcement();
    }
}
