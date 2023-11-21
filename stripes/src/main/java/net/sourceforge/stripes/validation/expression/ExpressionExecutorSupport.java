/* Copyright 2008 Tim Fennell
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sourceforge.stripes.validation.expression;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.util.bean.BeanUtil;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.controller.ParameterName;
import net.sourceforge.stripes.controller.StripesConstants;
import net.sourceforge.stripes.validation.ValidationMetadata;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationError;
import net.sourceforge.stripes.validation.ScopedLocalizableError;
import net.sourceforge.stripes.exception.StripesRuntimeException;

import jakarta.servlet.jsp.el.VariableResolver;
import jakarta.servlet.jsp.el.ELException;
import jakarta.servlet.jsp.el.Expression;
import jakarta.servlet.jsp.el.ExpressionEvaluator;
import java.util.List;

/**
 * A base class that provides the general plumbing for running expression validation
 * using the old JSP 2.0 style ExpressionEvaluator. Uses a custom VariableResolver
 * to make fields of the ActionBean available in the expression.
 *
 * @author Tim Fennell
 * @since Stripes 1.5
 */
@SuppressWarnings("deprecation")
public abstract class ExpressionExecutorSupport implements ExpressionExecutor {

    private static final Log log = Log.getInstance(ExpressionExecutorSupport.class);

    /**
     * A JSP EL VariableResolver that first attempts to look up the value of the variable as a first
     * level property on the ActionBean, and if does not exist then delegates to the built in resolver.
     *
     * @author Tim Fennell
     * @since Stripes 1.3
     */
    protected static class BeanVariableResolver implements VariableResolver {
        private ActionBean bean;
        private Object currentValue;

        /** Constructs a resolver based on the action bean . */
        BeanVariableResolver(ActionBean bean) {
            this.bean = bean;
        }

        /** Sets the value that the 'this' variable will point at. */
        void setCurrentValue(Object value) {
            this.currentValue = value;
        }

        /**
         * Recognizes a couple of special variables, and if the property requested
         * isn't one of them, just looks up a property on the action bean.
         *
         * @param property the name of the variable/property being looked for
         * @return the property value or null
         * @throws jakarta.servlet.jsp.el.ELException
         */
        public Object resolveVariable(String property) throws ELException {
            if (isSelfKeyword(bean, property)) {
                return this.currentValue;
            }
            else if (StripesConstants.REQ_ATTR_ACTION_BEAN.equals(property)) {
                return this.bean;
            }
            else {
                try { return BeanUtil.getPropertyValue(property, bean); }
                catch (Exception e) { return null; }
            }
        }
    }

    // See interface for javadoc
    public void evaluate(final ActionBean bean, final ParameterName name, final List<Object> values,
                         final ValidationMetadata validationInfo, final ValidationErrors errors) {
        Expression expr = null;
        BeanVariableResolver resolver = null;

        if (validationInfo.expression() != null) {
            try {
                // Make sure we can get an evaluator
                ExpressionEvaluator evaluator = getEvaluator();
                if (evaluator == null) return;

                // If this turns out to be slow we could probably cache the parsed expression
                String expression = validationInfo.expression();
                expr = evaluator.parseExpression(expression, Boolean.class, null);
                resolver = new BeanVariableResolver(bean);
            }
            catch (ELException ele) {
                throw new StripesRuntimeException(
                        "Could not parse the EL expression being used to validate field " +
                        name.getName() + ". This is not a transient error. Please double " +
                        "check the following expression for errors: " +
                        validationInfo.expression(), ele);
            }
        }

        // Then validate each value we have and add error messages
        for (Object value : values) {
            // And then if we have an expression to use
            if (expr != null) {
                try {
                    resolver.setCurrentValue(value);
                    Boolean result = (Boolean) expr.evaluate(resolver);
                    if (!Boolean.TRUE.equals(result)) {
                        ValidationError error = new ScopedLocalizableError(ERROR_DEFAULT_SCOPE,
                                                                           ERROR_KEY);
                        error.setFieldValue(String.valueOf(value));
                        errors.add(name.getName(), error);
                    }
                }
                catch (ELException ele) {
                    log.error("Error evaluating expression for property ", name.getName(),
                              " of class ", bean.getClass().getSimpleName(), ". Expression: ",
                              validationInfo.expression());
                }
            }
        }
    }

    /**
     * Must be implemented by subclasses to return an instance of ExpressionEvaluator
     * that can be used to execute expressions.
     *
     * @return a working ExpressionEvaluator implementation.
     */
    protected abstract ExpressionEvaluator getEvaluator() ;

    /**
     * Utility method for checking deprecated use of 'this' in expressions. Checks if
     * <code>prop</code> == 'this' and logs a Warning message inviting the user to
     * update to the new keyword.
     */
    static boolean isSelfKeyword(ActionBean bean, Object prop) {
        boolean isDeprecatedThis = THIS.equals(prop);
        if (isDeprecatedThis) {
            // log a message if using the deprecated 'this' keyword. This can happen
            // if the appserver (e.g. tomcat 7) is configured to skip identifier
            // check.
            // this message encourages the user to update EL validation
            // expressions using the new keyword
            log.warn("You are using the 'this' keyword in ActionBean class '" + bean.getClass().getName() +
                    "'. It is a reserved keyword. Your application server doesn't seem to complain, but " +
                    "the application could malfunction on other servers. " +
                    "Please use the keyword '" + SELF + "' in replacement of 'this' in your EL expressions.");
        }
        return isDeprecatedThis || SELF.equals(prop);
    }

}
