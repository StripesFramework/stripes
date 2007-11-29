/* Copyright 2005-2006 Tim Fennell
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
package net.sourceforge.stripes.util.bean;

/**
 * <p>Provides a simple way to manipulate properties and nested properties specified by complex
 * property expressions. It should be noted that while the interface to this class is simple it
 * will be more efficient to use {@link PropertyExpressionEvaluation} directly if you plan
 * to call more than one of the methods in this class with the same exact expression.</p>
 *
 * @author Tim Fennell
 * @since Stripes 1.4
 */
public class BeanUtil {
    /**
     * Attempts to determine the type of the property specified by the property expression
     * in the context of the supplied bean. Will return a Class if one is determinable or
     * null if the type cannot be determined due to either there being no such property
     * or a lack of type information.
     *
     * @param expression an expression representing a property or nested/indexed property
     * @param bean the bean against which to evaluate the expression
     * @return the Class representing the type of object that would be returned if
     *         {@link #getPropertyValue(String, Object)} were invoked with the same parameters
     * @throws ParseException if the expression is invalid and cannot be parsed
     * @throws EvaluationException if the expression is valid, but cannot be evaluated against
     *         this bean
     */
    public static Class<?> getPropertyType(String expression, Object bean)
            throws ParseException, EvaluationException {

        return getEvaluation(expression, bean).getType();
    }

    /**
     * Attempts to fetch the property specified by the property expression
     * in the context of the supplied bean. If a value is present one will be returned. If
     * the value or any intermediate value is null, then null will be returned.
     *
     * @param expression an expression representing a property or nested/indexed property
     * @param bean the bean against which to evaluate the expression
     * @return the value of the property or null
     * @throws ParseException if the expression is invalid and cannot be parsed
     * @throws EvaluationException if the expression is valid, but cannot be evaluated against
     *         this bean
     */
    public static Object getPropertyValue(String expression, Object bean)
            throws ParseException, EvaluationException {

        return getEvaluation(expression, bean).getValue();
    }

    /**
     * Attempts to set the property specified by the property expression
     * in the context of the supplied bean.
     *
     * @param expression an expression representing a property or nested/indexed property
     * @param bean the bean against which to evaluate the expression
     * @param value the value to be set for the property, may be null
     * @throws ParseException if the expression is invalid and cannot be parsed
     * @throws EvaluationException if the expression is valid, but cannot be evaluated against
     *         this bean
     */
    public static void setPropertyValue(String expression, Object bean, Object value)
            throws ParseException, EvaluationException {

        getEvaluation(expression, bean).setValue(value);
    }

    /**
     * Attempts to set the property to null or an equivelant value. In most cases this leads
     * to the property being set to null, but in the case of primitives the default value is
     * used and in the case of Maps the key is removed from the Map.
     *
     * @param expression an expression representing a property or nested/indexed property
     * @param bean the bean against which to evaluate the expression
     * @throws ParseException if the expression is invalid and cannot be parsed
     * @throws EvaluationException if the expression is valid, but cannot be evaluated against
     *         this bean
     */
    public static void setPropertyToNull(String expression, Object bean)
            throws ParseException, EvaluationException {

        getEvaluation(expression, bean).setToNull();
    }


    /**
     * Parses the expression and then creates an evaluation out of it with the supplied bean.
     * @param expression an expression representing a property or nested/indexed property
     * @param bean the bean against which to evaluate the expression
     * @return a PropertyEvaluation object to be used to get/set information
     * @throws ParseException if the expression is invalid and cannot be parsed
     * @throws EvaluationException if the expression is valid, but cannot be evaluated against
     *         this bean
     */
    private static PropertyExpressionEvaluation getEvaluation(String expression, Object bean)
            throws ParseException, EvaluationException {

        PropertyExpression expr = PropertyExpression.getExpression(expression);
        return new PropertyExpressionEvaluation(expr, bean);
    }
}
