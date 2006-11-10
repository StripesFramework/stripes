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

import net.sourceforge.stripes.util.ReflectUtil;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Implementation of {@link PropertyAccessor} for reading JavaBean properties from
 * JavaBeans.  Will attempt property acccess first using the standard PropertyDescriptor,
 * but if no PropertyDescriptor is present then falls back to attempting Field access for
 * public fields.
 *
 * @author Tim Fennell
 * @since Stripes 1.4
 */
public class JavaBeanPropertyAccessor implements PropertyAccessor<Object> {
    /**
     * Fetches the specified property value from the bean if it exists.
     * @param evaluation the current node evaluation
     * @param bean the bean from which to fetch the property
     * @return the value of the property
     * @throws NoSuchPropertyException if there is no property with the supplied name
     * @throws EvaluationException if the value cannot be retrieved for any other reason
     */
    public Object getValue(NodeEvaluation evaluation, Object bean)
            throws NoSuchPropertyException, EvaluationException {
        String property = evaluation.getNode().getStringValue();
        PropertyDescriptor pd = ReflectUtil.getPropertyDescriptor(bean.getClass(), property);

        try {
            if (pd != null) {
                Method m = pd.getReadMethod();
                if (m != null) {
                    return m.invoke(bean);
                }
                else {
                    throw new EvaluationException("Could not read write-only property '" +
                        property + "' on bean of type " + bean.getClass().getName());
                }
            }
            else {
                Field field = ReflectUtil.getField(bean.getClass(), property);
                if (field != null) {
                    return field.get(bean);
                }
                else {
                    throw new NoSuchPropertyException("Bean class " + bean.getClass().getName() +
                    " does not contain a property called '" + property + "'.");
                }
            }
        }
        catch (EvaluationException ee) { throw ee; }
        catch (Exception e) {
            throw new EvaluationException("Could not read value of property '" + property +
            "' on bean of type " + bean.getClass().getName() + " due to an exception.", e);
        }
    }

    /**
     * Sets the specified property value to the supplied value.
     * @param evaluation the current node evaluation
     * @param bean the bean on to which to set the property
     * @param value the value of the property
     * @throws NoSuchPropertyException if there is no property with the supplied name
     * @throws EvaluationException if the value cannot be set for any other reason
     */
    public void setValue(NodeEvaluation evaluation, Object bean, Object value) {
        String property = evaluation.getNode().getStringValue();
        PropertyDescriptor pd = ReflectUtil.getPropertyDescriptor(bean.getClass(), property);

        try {
            if (pd != null) {
                Method m = pd.getWriteMethod();
                if (m != null) {
                    m.invoke(bean, value);
                }
                else {
                    throw new EvaluationException("Could not write read-only property '" +
                            property + "' on bean of type " + bean.getClass().getName());
                }
            }
            else {
                Field field = ReflectUtil.getField(bean.getClass(), property);
                if (field != null) {
                    field.set(bean, value);
                }
                else {
                    throw new NoSuchPropertyException("Bean class " + bean.getClass().getName() +
                            " does not contain a property called '" + property + "'.");
                }
            }
        }
        catch (EvaluationException ee) { throw ee; }
        catch (Exception e) {
            throw new EvaluationException("Could not write value of property '" + property +
                    "' on bean of type " + bean.getClass().getName() + " due to an exception.", e);
        }
    }
}
