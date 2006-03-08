/* Copyright (C) 2005 Tim Fennell
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the license with this software. If not,
 * it can be found online at http://www.fsf.org/licensing/licenses/lgpl.html
 */
package net.sourceforge.stripes.util;

import ognl.ObjectNullHandler;
import ognl.OgnlContext;
import ognl.OgnlRuntime;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * NullHandler implementation used to replace the default one that ships with Ognl. During setValue
 * operations it will ensure that the object graph is fully formed so that the target property can
 * be set.  During get operations it will create intermediary objects as necessary, but not add
 * them to the object graph.  This allows callers to discover information about the object graph
 * during get operations without mutating the object graph.
 *
 * @author Tim Fennell
 */
public class OgnlCustomNullHandler extends ObjectNullHandler {

    protected static final String INSTANTIATE_LEAF_NODES = "leafyBaby";

    protected static final String INSTANTIATE_NOTHING = "dontDoItMan!";



    /** Log object used by the class to log messages. */
    private Log log = Log.getInstance(OgnlCustomNullHandler.class);

    /**
     * Figures out what to do when Ognl encounters a null value in one of the expressions that it
     * is evaluating.  The rules followed are as follows:
     * <ul>
     *   <li>If the expression is being evaluated to set an value, the null intermediate object is
     *       instantiated and set on its parent object.
     *   </li>
     *   <li>If the expression is a get expression and the null value is an intermediary value then
     *       the intermediary is <em>temporarily</em> instantiated but the object is never set on
     *       it's parent object
     *   </li>
     *   <li>If the expression is a get expression and the null value is the result of the final
     *       node in the expression, null is returned.
     *   </li>
     * </ul>
     *
     * @param context the OgnlContext, actually of type OgnlContext
     * @param target the object on which the null property was found
     * @param property really a String, the name of the property being retrieved
     * @return the swapped in value if one was created otherwise null
     */
    public Object nullPropertyValue(Map context, Object target, Object property) {
        Object result = null;
        OgnlContext ctx = (OgnlContext) context;

        // If we're not instantiating anything, just return right away
        if (ctx.containsKey(INSTANTIATE_NOTHING)) {
            return null;
        }

        int indexInParent = ctx.getCurrentEvaluation().getNode().getIndexInParent();
        int maxIndex = ctx.getRootEvaluation().getNode().jjtGetNumChildren() -1 ;

        // If the null value isn't the terminal value in the expression...
        if ( (indexInParent != -1 && indexInParent < maxIndex)
                || context.containsKey(INSTANTIATE_LEAF_NODES) ) {

            try {
                if (target instanceof List) {
                    // If the target is a list, cross your fingers and hope the getters and setters
                    // are using generic types...if they are we can fill in the blanks
                    Integer index = (Integer) property;
                    List list = (List) target;
                    Object listContainer = ctx.getCurrentEvaluation().getPrevious().getSource();
                    String listProperty = ctx.getCurrentEvaluation().getPrevious().getNode().toString();
                    Method method = OgnlRuntime.getSetMethod(ctx, listContainer.getClass(), listProperty);
                    Type[] types = method.getGenericParameterTypes();

                    if (types[0] instanceof ParameterizedType) {
                        ParameterizedType ptype = (ParameterizedType) types[0];
                        Type actualType = ptype.getActualTypeArguments()[0];

                        if (actualType instanceof Class) {
                            // Yippee! We know how to make one of these
                            Class clazz = (Class) actualType;
                            result = clazz.newInstance();
                        }
                    }

                    if (result == null) {
                        throw new RuntimeException("You appear to be trying to get or set a nested " +
                            "property through a List without supplying Generic getter/setter methods. " +
                            "without Generic method signatures there is no way for Stripes to get " +
                            "the type of object that should be in your List. List was on an object of " +
                            "type " + listContainer.getClass() + ", property name was: " + listProperty);
                    }
                    else if (ctx.getRootEvaluation().isSetOperation()) {
                        while (list.size() <= index) {
                            list.add(null);
                        }
                        list.set(index, result);
                    }
                }
                else if (target instanceof Map) {
                    // If the target is a Map, cross your fingers and hope the getters and setters
                    // are using generic types...if they are we can fill in the blanks
                    Map map = (Map) target;
                    Object mapContainer = ctx.getCurrentEvaluation().getPrevious().getSource();
                    String mapProperty = ctx.getCurrentEvaluation().getPrevious().getNode().toString();
                    Method method = OgnlRuntime.getSetMethod(ctx, mapContainer.getClass(), mapProperty);
                    Type[] types = method.getGenericParameterTypes();

                    if (types[0] instanceof ParameterizedType) {
                        ParameterizedType ptype = (ParameterizedType) types[0];
                        Type actualType = ptype.getActualTypeArguments()[1];

                        if (actualType instanceof Class) {
                            // Yippee! We know how to make one of these
                            Class clazz = (Class) actualType;
                            result = clazz.newInstance();
                        }
                    }

                    if (result == null) {
                        throw new RuntimeException("You appear to be trying to get or set a nested " +
                                "property through a Map without supplying Generic getter/setter methods. " +
                                "without Generic method signatures there is no way for Stripes to get " +
                                "the type of object that should be in your Map. Map was on an object of " +
                                "type " + mapContainer.getClass() + ", property name was: " + mapProperty);
                    }
                    else if (ctx.getRootEvaluation().isSetOperation()) {
                        map.put(property, result);
                    }
                }
                else {
                    String propertyName = (String) property;

                    // Get the set method, determine the type of object that was null, and make one!
                    Method method =
                        OgnlRuntime.getSetMethod(ctx, target.getClass(), propertyName);
                    Class clazzes[] = method.getParameterTypes();

                    // If the target type is an array we have to instantiate it a little differently
                    if (clazzes[0].isArray()) {
                        result = Array.newInstance(clazzes[0].getComponentType(), 0);
                    }
                    else if (clazzes[0].isEnum()) {
                        result = clazzes[0].getEnumConstants()[0];
                    }
                    else if (clazzes[0].isInterface() ) {
                        result = ReflectUtil.getInterfaceInstance(clazzes[0]);
                    }
                    else {
                        result = clazzes[0].newInstance();
                    }

                    // Now, if the caller was doing a set operation, lets make this change permanent
                    if (ctx.getRootEvaluation().isSetOperation()) {
                        Object[] args = new Object[1];
                        args[0] = result;
                        method.invoke(target, args);
                    }
                }
            }
            catch (Exception e) { // There's really not a lot we can do about this.
                log.info("Problem encountered trying to create and set property [", property,
                        "] on object of type [", target.getClass().getName(), "]. ", e.getMessage());
            }
        }

        return result;
    }
}