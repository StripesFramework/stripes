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
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.OgnlRuntime;

import java.beans.IntrospectionException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class used to configure and use Ognl in a consistent manner across the pieces of
 * Stripes that use it.  Ensures that the Stripes specific OgnlCustomNullHandler is registered with
 * the OgnlRuntime before it is used, and that the appropriate OgnlContexts are used.  Also provides
 * facilities for caching the Ognl expression objects that are created from parsing property names -
 * this provides a significant performance boost.
 *
 * @author Tim Fennell
 */
public class OgnlUtil {
    /** Store for pre-parsed expressions. */
    private static Map<String,Object> expressions = new HashMap<String,Object>();

    /** Static initializer block that inserts a custom NullHandler into the OgnlRuntime. */
    static {
        OgnlRuntime.setNullHandler(Object.class, new OgnlCustomNullHandler());
        OgnlRuntime.setPropertyAccessor(List.class, new OgnlSafeListPropertyAccessor());
    }

    /** Private default constructor to prevent anyone from instantiating the class. */
    private OgnlUtil() {
        // Do Nothing
    }

    /**
     * Fetches the value of a named property (nested, indexed etc.) using Ognl.
     *
     * @param property an expression representing the property desired
     * @param root the object from which to fetch the property
     * @return Object the value of the property, or null if the property is null
     * @throws OgnlException thrown when a problem occurs such as one or more properties not
     *         existing or being inaccessible
     */
    public static Object getValue(String property, Object root) throws OgnlException {
        return Ognl.getValue(getExpression(property), createContext(), root);
    }

    /**
     * Fetches the value of a named property (nested, indexed etc.) using Ognl. Can optionally
     * instantiate the final property if it is null, and return a default instance of the
     * type.
     *
     * @param property an expression representing the property desired
     * @param root the object from which to fetch the property
     * @param instantiateLeaf if true, an attmept will be made to instantiate the property if it
     *        is null, and return a default value.
     * @return Object the value of the property, or null if the property is null
     * @throws OgnlException thrown when a problem occurs such as one or more properties not
     *         existing or being inaccessible
     */
    public static Object getValue(String property, Object root, boolean instantiateLeaf) throws OgnlException {
        OgnlContext context = createContext();
        if (instantiateLeaf) {
            context.put(OgnlCustomNullHandler.INSTANTIATE_LEAF_NODES,
                        OgnlCustomNullHandler.INSTANTIATE_LEAF_NODES);
        }

        return Ognl.getValue(getExpression(property), context, root);
    }

    /**
     * Sets the value of a named property (nested, indexed etc.) using Ognl. If any of the values
     * along the way are not set then the values are created and set into the object graph.
     *
     * @param property an expression representing the property to set
     * @param root the object on which to set the property
     * @param value the value of the property to be set
     * @throws OgnlException thrown when a problem occurs such as one or more properties not
     *         existing or being inaccessible
     */
    public static void setValue(String property, Object root, Object value) throws OgnlException {
        Ognl.setValue(getExpression(property), createContext(), root, value);
    }

    /**
     * Sets the value of a named property to null. If there are intermediate null values then
     * objects are not instantiated and the method returns. If there are no intermediate
     * nulls then the appropriate set method will be invoked with null.
     *
     * @param property an expression representing the property to set
     * @param root the object on which the proeprty is to be set back to null
     */
    public static void setNullValue(String property, Object root) throws OgnlException {
        OgnlContext context = createContext();
        context.put(OgnlCustomNullHandler.INSTANTIATE_NOTHING,
                    OgnlCustomNullHandler.INSTANTIATE_NOTHING);
        try {
            Ognl.setValue(getExpression(property), context, root, null);
        }
        catch (OgnlException oe) {
            // Ognl is slighly retarded in that if there is an intermediate null in a nested
            // property there is no way to abort processing without Ognl throwing an OgnlException.
            // So, if we get here, it just means things were already null, and it's ok.
        }
    }

    /**
     * Retrieves the class type of the specified property without instantiating the property
     * itself. All earlier properties in a chain will, however, be instantiated.
     *
     * @param property an expression for the property to be inspected
     * @param root the object on which the property lives
     * @return the Class type of the property or null if there is no such property visible
     * @throws OgnlException thrown when a problem occurs such as one or more properties not
     *         being inaccessible
     */
    public static Class getPropertyClass(String property, Object root) throws OgnlException,
                                                                              IntrospectionException {
        Class propertyClass = null;

        String childProperty = null;
        Object newRoot = null;
        int propertyIndex = propertySplit(property);

        // If we have a nested property, grab the penultimate object, and the last property name
        if (propertyIndex > 0) {
            // foo.bar.baz.splat => parent: "foo.bar.baz" and child: "splat"
            String parentProperty = property.substring(0,propertyIndex);
            childProperty = property.substring(propertyIndex+1);
            newRoot = getValue(parentProperty, root, true);
        }
        else {
            childProperty = property;
            newRoot = root;
        }

        // We need to handle the case where the last chunk of the expression references
        // a List or a Map
        int listIndexPosition = childProperty.indexOf("[");

        if (listIndexPosition > 0) {
            Method method = OgnlRuntime.getGetMethod(createContext(),
                                                     newRoot.getClass(),
                                                     childProperty.substring(0, listIndexPosition));
            Type returnType = method.getGenericReturnType();
            if (returnType instanceof ParameterizedType) {
                ParameterizedType ptype = (ParameterizedType) returnType;
                Type actualType = null;

                // Get the right type parameter for List<X> and Map<?,X>
                if (List.class.isAssignableFrom((Class) ptype.getRawType())) {
                    actualType = ptype.getActualTypeArguments()[0];
                }
                else if (Map.class.isAssignableFrom((Class) ptype.getRawType())) {
                    actualType = ptype.getActualTypeArguments()[1];
                }

                if (actualType instanceof Class) {
                    propertyClass = (Class) actualType;
                }
            }

            // If we can't figure it out, let's try String!
            if (propertyClass == null) {
                propertyClass = String.class;
            }
        }
        else {
            Method method =
                    OgnlRuntime.getGetMethod(createContext(), newRoot.getClass(), childProperty);
            if (method != null) {
                propertyClass = method.getReturnType();
            }
            else {
                method = OgnlRuntime.getSetMethod(createContext(), newRoot.getClass(), childProperty);
                if (method != null) {
                    propertyClass = method.getParameterTypes()[0];
                }
                else {
                    try { propertyClass = newRoot.getClass().getField(childProperty).getType(); }
                    catch (NoSuchFieldException nsfe) { /* suppress. */ }
                }
            }
        }

        return propertyClass;
    }

    /**
     * Returns the Class representing the type that is stored inside the named collection
     * property.  For example, for a property of type Set&lt;Long&gt; this will return
     * Long.class.  If the collection in question is without type parameters then the method
     * will simply return null to indicate that it cannot infer type.
     *
     * @param bean the ActionBean with a collection typed property
     * @param propertyName the name (potentially nested) of the list property
     * @return the class representing the type of object stored in the list, or null
     * @throws OgnlException if Ognl throws an OgnlException
     * @throws IntrospectionException if Ognl throws an IntrospectionException
     */
    public static Class getCollectionPropertyComponentClass(ActionBean bean, String propertyName)
        throws OgnlException, IntrospectionException {
        // The eventual holder of the real type
        Class propertyClass = null;

        // If we have a nested property, grab the penultimate object, and the last property name
        Object root = bean;
        int propertyIndex = propertySplit(propertyName);

        if (propertyIndex > 0) {
            String parentProperty = propertyName.substring(0, propertyIndex);
            propertyName = propertyName.substring(propertyIndex + 1);
            root = getValue(parentProperty, bean, true);
        }

        // Get the getter method and figure out if it's got generic info on it
        Method method = OgnlRuntime.getGetMethod(createContext(), root.getClass(), propertyName);

        Type returnType = method.getGenericReturnType();
        if (returnType instanceof ParameterizedType) {
            ParameterizedType ptype = (ParameterizedType) returnType;
            Type actualType = ptype.getActualTypeArguments()[0];

            if (actualType instanceof Class) {
                propertyClass = (Class) actualType;
            }
        }

        return propertyClass;
    }

    /**
     * Finds the split point for the last property of an OGNL expression. If the last
     * property includes a map reference, the map reference is part of the last property
     * expression. This method also accounts for literals in single-ticks inside of that
     * last map expression.
     * @param property the property expression to evaluate
     * @return the index of the property splitter for the last property of the expression
     */
    private static int propertySplit(String property) {
        int endTick = property.lastIndexOf("'");
        int childSplit;
        if (endTick > 0) {
            // We have ticks, assume there are two and they're a map literal (e.g. foo.bar['baz.splat'])
            String first = property.substring(0,endTick);
            int startTick = first.lastIndexOf("'");
            assert startTick > 0 : "Ticks in expression don't match";

            childSplit = property.lastIndexOf('.');
            if (childSplit > startTick && childSplit < endTick) {
                String beforeTicks = property.substring(0, startTick);
                childSplit = beforeTicks.lastIndexOf('.');
            }
        } else {
            childSplit = property.lastIndexOf('.');
        }
        return childSplit;
    }

    /**
     * Supplies the Ognl expression object for a given property name string.  The property name
     * can be any combination of the types that Ognl can handle (simple, indexed, nested etc.).
     * First checks to see if a cached expression object exists, and if so supplied that. If no
     * cached expression exists, one is manufactured, placed into the cache and then returned.
     *
     * @param property the property name string being used to access a property
     * @return an Ognl expression object representing the property name string
     * @throws OgnlException if the property name is not a well formed Ognl property name string
     */
    private static synchronized Object getExpression(String property) throws OgnlException {
        Object ognlExpression = OgnlUtil.expressions.get(property);
        if (ognlExpression == null) {
            ognlExpression = Ognl.parseExpression(property);
            OgnlUtil.expressions.put(property, ognlExpression);
        }

        return ognlExpression;
    }

    /**
     * Manufactures an OgnlContext configured as needed for Stripes to operate correctly.
     *
     * @return a pre-configured OgnlContext
     */
    private static OgnlContext createContext() {
        OgnlContext context = new OgnlContext();
        context.setTraceEvaluations(true);
        return context;
    }
}
