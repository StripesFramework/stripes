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

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.validation.ValidationError;
import net.sourceforge.stripes.validation.TypeConverter;
import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.util.Log;

import java.util.Map;
import java.util.Locale;
import java.util.Collection;
import java.util.ArrayList;
import java.lang.reflect.Constructor;

/**
 * Implementation of {@link PropertyAccessor} for interacting with Maps. Uses information
 * stored in the node evaluation to determine the correct type for the Map key and converts
 * the key using Stripes' type converion system.
 *
 * @author Tim Fennell
 * @since Stripes 1.4
 */
public class MapPropertyAccessor implements PropertyAccessor<Map> {
    private static final Log log = Log.getInstance(MapPropertyAccessor.class);

    /**
     * Gets the value stored in the Map under the key specified by the current node.
     * @param evaluation the current node evaluation
     * @param map the target Map
     * @return the value stored in the map under the specified key
     */
    public Object getValue(NodeEvaluation evaluation, Map map) {
        Object key = getKey(evaluation);
        return map.get(key);
    }

    /**
     * Sets the value stored in the Map under the key specified by the current node.
     * @param evaluation the current node evaluation
     * @param map the target Map
     * @value the value to be stored in the map under the specified key
     */
    @SuppressWarnings("unchecked")
	public void setValue(NodeEvaluation evaluation, Map map, Object value) {
        Object key = getKey(evaluation);
        if (value == null) {
            map.remove(key);
        }
        else {
            map.put(key, value);
        }
    }

    /**
     * Attempts to convert the key to from the expression node to the correct type
     * as determined by reflection (using generics to find the Map key type). If generics
     * information is not present then the type found in the expression is used (String, int etc.).
     *
     * @param evaluation the current node evaluation
     * @return the key to use when interacting with the Map
     */
    @SuppressWarnings("unchecked")
	protected Object getKey(NodeEvaluation evaluation) {
        Class declaredType = evaluation.getKeyType();
        Class nodeType     = evaluation.getNode().getTypedValue().getClass();

        if (nodeType.equals(declaredType) || declaredType == null) {
            return evaluation.getNode().getTypedValue();
        }
        else {
            try {
                // Collect the things needed to grab a type converter
                String stringKey = evaluation.getNode().getStringValue();
                ActionBean bean = (ActionBean) evaluation.getExpressionEvaluation().getBean();
                Locale locale = bean.getContext().getLocale();
                Collection errors = new ArrayList<ValidationError>();

                TypeConverter tc = StripesFilter.getConfiguration()
                        .getTypeConverterFactory().getTypeConverter(declaredType, locale);

                // If there is a type converter, try using it!
                if (tc != null) {
                    Object retval = tc.convert(stringKey, declaredType, errors);
                    if (errors.size() == 0) return retval;
                }
                // Otherwise look for a String constructor
                else {
                    Constructor c = declaredType.getConstructor(String.class);
                    if (c != null) {
                        return c.newInstance(stringKey);
                    }
                }
            }
            catch (Exception e) {
                log.warn("Exception while converting Map key to appropriate type. Key: ",
                         evaluation.getNode().getStringValue());
            }

            // Return the original key if we couldn't type convert it
            return evaluation.getNode().getTypedValue();
        }
    }
}
