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
import net.sourceforge.stripes.controller.ParameterName;
import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.controller.StripesConstants;
import net.sourceforge.stripes.validation.ValidationMetadata;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationError;
import net.sourceforge.stripes.validation.ScopedLocalizableError;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.util.bean.BeanUtil;
import net.sourceforge.stripes.exception.StripesRuntimeException;

import jakarta.servlet.jsp.JspFactory;
import jakarta.servlet.jsp.JspApplicationContext;
import jakarta.servlet.ServletContext;
import jakarta.el.ExpressionFactory;
import jakarta.el.ValueExpression;
import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import jakarta.el.PropertyNotWritableException;
import jakarta.el.FunctionMapper;
import jakarta.el.VariableMapper;
import jakarta.el.ELException;
import java.util.List;
import java.util.Iterator;
import java.beans.FeatureDescriptor;
import java.lang.reflect.Method;

/**
 * An implementation of {@link ExpressionExecutor} that uses the new EL API available in Java
 * EE 5 in the {@link jakarta.el} package. While more complicated that the JSP 2.0 API it has
 * one advantage which is that it can be used without the need to allocate a PageContext
 * object and without any other libraries being available.
 *
 * @author tfenne
 * @since Stripes 1.5
 */
public class Jsp21ExpressionExecutor implements ExpressionExecutor {
    private static final Log log = Log.getInstance(Jsp21ExpressionExecutor.class);

    /**
     * Implementation of the EL interface to resolve variables. Resolves variables by
     * checking two special names ("this" and "actionBean") and then falling back to
     * retrieving property values from the ActionBean passed in to the constructor.
     *
     * @author Tim Fennell
     * @since Stripes 1.5
     */
    protected static class StripesELResolver extends ELResolver {
        private ActionBean bean;
        private Object currentValue;

        /** Constructs a resolver based on the action bean . */
        StripesELResolver(ActionBean bean) {
            this.bean = bean;
        }

        /** Sets the value that the 'this' variable will point at. */
        void setCurrentValue(Object value) {
            this.currentValue = value;
        }

        /**
         * Attempts to resolve the value as described in the class level javadoc.
         * @param ctx the ELContext for the expression
         * @param base the object on which the property resides (null == root property)
         * @param prop the name of the property being looked for
         * @return the value of the property or null if one can't be found
         */
        @Override
        public Object getValue(ELContext ctx, Object base, Object prop) {
            if (ExpressionExecutorSupport.isSelfKeyword(this.bean, prop)) {
                ctx.setPropertyResolved(true);
                return this.currentValue;
            }
            else if (StripesConstants.REQ_ATTR_ACTION_BEAN.equals(prop)) {
                ctx.setPropertyResolved(true);
                return this.bean;
            }
            else {
                try {
                    base = base == null ? this.bean : base;
                    Object retval = BeanUtil.getPropertyValue(String.valueOf(prop), base);
                    ctx.setPropertyResolved(true);
                    return retval;
                }
                catch (Exception e) { return null; }
            }
        }

        /** Does nothing. Always returns Object.class. */
        @Override
        public Class<?> getType(final ELContext ctx, final Object base, final Object prop) {
            ctx.setPropertyResolved(true);
            return Object.class;
        }

        /** Does nothing. Always throws PropertyNotWritableException. */
        @Override
        public void setValue(ELContext elContext, Object o, Object o1, Object o2) throws PropertyNotWritableException {
            throw new PropertyNotWritableException("Unsupported Op");
        }

        /** Always returns true. */
        @Override
        public boolean isReadOnly(final ELContext elContext, final Object o, final Object o1) { return true; }

        /** Always returns null. */
        @Override
        public Iterator<FeatureDescriptor> getFeatureDescriptors(final ELContext elContext, final Object o) { return null; }

        /** Always returns Object.class. */
        @Override
        public Class<?> getCommonPropertyType(final ELContext elContext, final Object o) { return Object.class; }
    }

    /**
     * Implementation of the EL interface for managing expression context. Resolves variables
     * using the StripesELResolver above.  Both the FunctionMapper and VariableResolver are
     * essentially no-op implementations.
     *
     * @author Tim Fennell
     * @since Stripes 1.5
     */
    protected static class StripesELContext extends ELContext {
        @SuppressWarnings("unused")
        private ActionBean bean;
        private StripesELResolver resolver;
        private VariableMapper vmapper;
        private static final FunctionMapper fmapper = new FunctionMapper() {
            @Override
            public Method resolveFunction(final String s, final String s1) { return null; }
        };

        /**
         * Constructs a new instance using the ActionBean provided as the source for most
         * property resolutions.
         *
         * @param bean the ActionBean to resolve properties against
         */
        public StripesELContext(ActionBean bean) {
            this.bean = bean;
            this.resolver = new StripesELResolver(bean);

            this.vmapper = new VariableMapper() {
                @Override
                public ValueExpression resolveVariable(final String s) {
                    return null;
                }

                @Override
                public ValueExpression setVariable(final String s, final ValueExpression valueExpression) {
                    return null;
                }
            };
        }

        /** Sets the current value of the 'this' special variable. */
        public void setCurrentValue(final Object value) {resolver.setCurrentValue(value);}

        /** Returns the StripesELResovler. */
        @Override
        public StripesELResolver getELResolver() { return this.resolver; }

        /** Returns a no-op implementation of FunctionMapper. */
        @Override
        public FunctionMapper getFunctionMapper() { return fmapper; }

        /** Returns a no-op implementation of VariableMapper. */
        @Override
        public VariableMapper getVariableMapper() { return vmapper; }
    }

    /** Default constructor that throws an exception if the JSP2.1 APIs are not available. */
    public Jsp21ExpressionExecutor() {
        if (getExpressionFactory() == null) {
            throw new StripesRuntimeException("Could not create a JSP2.1 ExpressionFactory.");
        }
    }

    // See interface for javadoc.  
    public void evaluate(final ActionBean bean, final ParameterName name, final List<Object> values,
                         final ValidationMetadata validationInfo, final ValidationErrors errors) {

        StripesELContext ctx = null;
        String expressionString = validationInfo.expression();
        ValueExpression expression = null;

        try {
            if (expressionString != null) {
                // Make sure we can get an factory
                ExpressionFactory factory = getExpressionFactory();
                if (factory == null) return;

                ctx = new StripesELContext(bean);

                // If this turns out to be slow we could probably cache the parsed expression
                expression = factory.createValueExpression(ctx, expressionString, Boolean.class);
            }
        }
        catch (ELException ele) {
            throw new StripesRuntimeException(
                    "Could not parse the EL expression being used to validate field " +
                    name.getName() + ". This is not a transient error. Please double " +
                    "check the following expression for errors: " +
                    validationInfo.expression(), ele);
        }

        for (Object value : values) {
            // And then if we have an expression to use
            if (expression != null) {
                try {
                    ctx.setCurrentValue(value);
                    Boolean result = (Boolean) expression.getValue(ctx);
                    if (!Boolean.TRUE.equals(result)) {
                        ValidationError error = new ScopedLocalizableError(ERROR_DEFAULT_SCOPE, ERROR_KEY);
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

    /** Creates an ExpressionFactory using the JspApplicationContext. */
    protected ExpressionFactory getExpressionFactory() {
        ServletContext ctx = StripesFilter.getConfiguration().getServletContext();
        JspApplicationContext jspCtx = JspFactory.getDefaultFactory().getJspApplicationContext(ctx);
        return jspCtx.getExpressionFactory();
    }
}
