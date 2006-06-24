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
package net.sourceforge.stripes.util;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.validation.TypeConverter;
import net.sourceforge.stripes.validation.ValidationError;
import ognl.MapPropertyAccessor;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.OgnlRuntime;
import ognl.SimpleNode;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.Constructor;
import java.util.Locale;
import java.util.Map;
import java.util.Collection;
import java.util.ArrayList;

/**
 * <p>A {@link ognl.PropertyAccessor} implementation for interacting with Maps that
 * intercepts the property access and uses Stripes' type conversion system to convert
 * the keys into the Map.  The Key type is determined using Reflection, by looking at
 * the getter or field for the Map within it's parent object and extracting the generics
 * information embedded there.</p>
 *
 * <p>The {@link #getProperty(java.util.Map, Object, Object)} method invokes the
 * {@link ognl.NullHandler} directly if a property is not present in the Map. This is
 * done to ensure that the NullHandler is provided with the correctly typed key.</p> 
 *
 * @author Tim Fennell
 * @since Stripes 1.4
 */
public class OgnlMapPropertyAccessor extends MapPropertyAccessor {
    private static final Log log = Log.getInstance(OgnlMapPropertyAccessor.class);

    /**
     * <p>Gets the value stored in the map under the specified value. Converts the key
     * to the appropriate type using Stripes' built in type conversion system and then
     * delegates to the OGNL MapPropertyAccess to finish the job.</p>
     *
     * <p>If the Map does not contain a value under the specified key, the NullHandler
     * will be invoked directly. This is done so that the correctly type converted key
     * is provided to the NullHandler; if it is left to OGNL to perform this step it will
     * not use the type-converted Map key.</p>
     *
     * @param context the OgnlContext for the current evaluation
     * @param map the Map from which a property is being fetched
     * @param key the Key into the Map provided by OGNL, probably a String or Integer
     * @return the value stored in the Map, which may have been created by virtue
     *         of attempting to accessing it through this API.
     * @throws OgnlException if OGNL's MapPropertyAccessor of NullHandler throws it.
     */
    @Override
    public Object getProperty(Map context, Object map, Object key) throws OgnlException {
        key = convertKey(context, key);
        Object value = super.getProperty(context, map, key);

        // If we don't invoke the null handler ourselves here, then Ognl will do it
        // for us, but it will use the un-converted key, which will cause problems.
        if (value == null) {
            value = OgnlRuntime.getNullHandler(map.getClass()).nullPropertyValue(context, map, key);
        }

        return value;
    }

    /**
     * Attempts to convert the Map key using Stripes' type conversion system and then
     * delegates to Ognl's built in MapPropertyAccessor to set the value into the map.
     */
    @Override
    public void setProperty(Map context, Object map, Object key, Object value) throws OgnlException {
        key = convertKey(context, key);
        super.setProperty(context, map, key, value);
    }

    /**
     * <p>Attempts to type convert the key from the String value embedded in the OGNL
     * expression, to the correct type as denoted by the generics information for
     * the Map getter/field in the Map's containing class. If the key cannot be converted
     * for any reason, the original key as provided by OGNL is returned.
     *
     * @param context the current OgnlContext for the expression being evaluated
     * @param key the Map key provided by OGNL
     * @return a type converted key if possible, otherwise the key provided by OGNL
     */
    protected Object convertKey(Map context, Object key) {
        OgnlContext ctx = (OgnlContext) context;
        Class keyType = getKeyType(ctx);

        // Short circuit if we're not really gonna do anything
        if (keyType == null || key == null || keyType.equals(key.getClass())) {
            return key;
        }
        else {
            try {
                // Collect the things needed to grab a type converter
                String stringKey = ctx.getCurrentNode().getValue(ctx, null).toString();
                ActionBean bean = (ActionBean) ctx.getRoot();
                Locale locale = bean.getContext().getLocale();
                Collection errors = new ArrayList<ValidationError>();

                TypeConverter tc = StripesFilter.getConfiguration()
                        .getTypeConverterFactory().getTypeConverter(keyType, locale);

                // If there is a type converter, try using it!
                if (tc != null) {
                    Object retval = tc.convert(stringKey, keyType, errors);
                    if (errors.size() == 0) return retval;
                }
                // Otherwise look for a String constructor
                else {
                    Constructor c = keyType.getConstructor(String.class);
                    if (c != null) {
                        return c.newInstance(stringKey);
                    }
                }
            }
            catch (Exception e) {
                log.warn("Exception while converting Map key to appropriate type. Key: ", key);
            }

            // Return the original key if we couldn't type convert it
            return key;
        }
    }


    /**
     * Attempts to determine the correct type for the Map key by navigating back up the
     * chain of objects to the Map's parent, fetching it's getter or field and examining
     * any generics information contained therein.
     *
     * @param ctx the OgnlContext for this stage of the expression evaluation
     * @return a Class representing the key type, or null if no determination can be made
     */
    protected Class getKeyType(OgnlContext ctx) {
        try {
            // Get the bean that contains the map, and the name of the map property
            Object mapContainer = ctx.getCurrentEvaluation().getPrevious().getSource();
            SimpleNode parent = (SimpleNode) ctx.getCurrentNode().jjtGetParent();
            int index = parent.getIndexInParent();
            String propertyName = parent.jjtGetParent().jjtGetChild(index-1).toString();

            // First look for a getter, and if that's not there, try field access
            Type mapType = null;
            Method getter = OgnlRuntime.getGetMethod(ctx, mapContainer.getClass(), propertyName);
            if (getter != null) {
                mapType = getter.getGenericReturnType();
            }
            else {
                Field field = OgnlRuntime.getField(mapContainer.getClass(), propertyName);
                if (field != null) mapType = field.getGenericType();
            }

            // Then if we have a type, see if it's parameterized in a useful way :)
            if (mapType != null && mapType instanceof ParameterizedType) {
                Type[] types = ((ParameterizedType) mapType).getActualTypeArguments();
                if (types.length == 2 && types[0] instanceof Class) {
                    return (Class) types[0];
                }
            }

            return null;
        }
        catch (Exception e) {
            log.warn("Exception while determining Map key type.", e);
            return null;
        }
    }
}
