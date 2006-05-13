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

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Queue;
import java.util.LinkedList;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Arrays;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Common utilty methods that are useful when working with reflection.
 *
 * @author Tim Fennell
 */
public class ReflectUtil {
    /** Static helper class, shouldn't be constructed. */
    private ReflectUtil() {}

    /**
     * Holds a map of commonly used interface types (mostly collections) to a class that
     * implements the interface and will, by default, be instantiated when an instance
     * of the iterface is needed.
     */
    protected static final Map<Class,Class> interfaceImplementations = new HashMap<Class,Class>();

    static {
        interfaceImplementations.put(Collection.class, ArrayList.class);
        interfaceImplementations.put(List.class,       ArrayList.class);
        interfaceImplementations.put(Set.class,        HashSet.class);
        interfaceImplementations.put(SortedSet.class,  TreeSet.class);
        interfaceImplementations.put(Queue.class,      LinkedList.class);
        interfaceImplementations.put(Map.class,        HashMap.class);
        interfaceImplementations.put(SortedMap.class,  TreeMap.class);
    }

    /**
     * The set of method that annotation classes inherit, and should be avoided when
     * toString()ing an annotation class.
     */
    private static final Set<String> INHERITED_ANNOTATION_METHODS =
            Literal.set("toString", "equals", "hashCode", "annotationType");

    /**
     * Looks up the default implementing type for the supplied interface. This is done
     * based on a static map of known common interface types and implementing classes.
     *
     * @param iface an interface for which an implementing class is needed
     * @return a Class object representing the implementing type, or null if one is
     *         not found
     */
    public static Class getImplementingClass(Class iface) {
        return interfaceImplementations.get(iface);
    }

    /**
     * Attempts to determine an implementing class for the interface provided and instantiate
     * it using a default constructror.
     *
     * @param interfaceType an interface (or abstract class) to make an instance of
     * @return an instance of the interface type supplied
     * @throws InstantiationException if no implementation type has been configured
     * @throws IllegalAccessException if thrown by the JVM during class instantiation
     */
    public static <T> T getInterfaceInstance(Class<T> interfaceType)
            throws InstantiationException, IllegalAccessException {
        Class impl = getImplementingClass(interfaceType);
        if (impl == null) {
            throw new InstantiationException(
                    "Stripes needed to instantiate a property who's declared type as an " +
                    "interface (which obviously cannot be instantiated. The interface is not " +
                    "one that Stripes is aware of, so no implementing class was known. The " +
                    "interface type was: '" + interfaceType.getName() + "'. To fix this " +
                    "you'll need to do one of three things. 1) Change the getter/setter methods " +
                    "to use a concrete type so that Stripes can instantiate it. 2) in the bean's " +
                    "setContext() method pre-instantiate the property so Stripes doesn't have to. " +
                    "3) Bug the Stripes author ;)  If the interface is a JDK type it can easily be " +
                    "fixed. If not, if enough people ask, a generic way to handle the problem " +
                    "might get implemented.");
        }
        else {
            return (T) impl.newInstance();
        }
    }

    /**
     * Utility method used to load a class.  Any time that Stripes needs to load of find a
     * class by name it uses this method.  As a result any time the classloading strategy
     * needs to change it can be done in one place!  Currently uses
     * {@code Thread.currentThread().getContextClassLoader().loadClass(String)}.
     *
     * @param name the fully qualified (binary) name of the class to find or load
     * @return the Class object representing the class
     * @throws ClassNotFoundException if the class cannot be loaded
     */
    public static Class findClass(String name) throws ClassNotFoundException {
        return Thread.currentThread().getContextClassLoader().loadClass(name);
    }

    /**
     * <p>A better (more concise) toString method for annotation types that yields a String
     * that should look more like the actual usage of the annotation in a class. The String produced
     * is similar to that produced by calling toString() on the annotation directly, with the
     * following differences:</p>
     *
     * <ul>
     *   <li>Uses the classes simple name instead of it's fully qualified name.</li>
     *   <li>Only outputs attributes that are set to non-default values.</li>
     *
     * <p>If, for some unforseen reason, an exception is thrown within this method it will be
     * caught and the return value will be {@code ann.toString()}.
     *
     * @param ann the annotation to convert to a human readable String
     * @return a human readable String form of the annotation and it's attributes
     */
    public static String toString(Annotation ann) {
        try {
            Class<? extends Annotation> type = ann.annotationType();
            StringBuilder builder = new StringBuilder(128);
            builder.append("@");
            builder.append(type.getSimpleName());

            boolean appendedAnyParameters = false;
            Method[] methods = type.getMethods();
            for (Method method : methods) {
                if (!INHERITED_ANNOTATION_METHODS.contains(method.getName())) {
                    Object defaultValue = method.getDefaultValue();
                    Object actualValue  = method.invoke(ann);

                    // If we have arrays, they have to be treated a little differently
                    Object[] defaultArray = null, actualArray = null;
                    if ( Object[].class.isAssignableFrom(method.getReturnType()) ) {
                        defaultArray = (Object[]) defaultValue;
                        actualArray  = (Object[]) actualValue;
                    }

                    // Only print an attribute if it isn't set to the default value
                    if ( (defaultArray != null && !Arrays.equals(defaultArray, actualArray)) ||
                            (defaultArray == null && !actualValue.equals(defaultValue)) ) {

                        if (appendedAnyParameters) {
                            builder.append(", ");
                        }
                        else {
                            builder.append("(");
                        }

                        builder.append(method.getName());
                        builder.append("=");

                        if (actualArray != null) {
                            builder.append( Arrays.toString(actualArray) );
                        }
                        else {
                            builder.append(actualValue);
                        }

                        appendedAnyParameters = true;
                    }
                }
            }

            if (appendedAnyParameters) {
                builder.append(")");
            }

            return builder.toString();
        }
        catch (Exception e) {
            return ann.toString();
        }
    }

    /**
     * Fetches all methods of all access types from the supplied class and super
     * classes. Methods that have been overridden in the inheritance hierachy are
     * only returned once, using the instance lowest down the hierarchy.
     *
     * @param clazz the class to inspect
     * @return a collection of methods
     */
    public static Collection<Method> getMethods(Class<?> clazz) {
        Collection<Method> found = new ArrayList<Method>();
        while (clazz != null) {
            for (Method m1 : clazz.getDeclaredMethods()) {
                boolean overridden = false;

                for (Method m2 : found) {
                    if ( m2.getName().equals(m1.getName()) &&
                            Arrays.deepEquals(m1.getParameterTypes(), m2.getParameterTypes())) {
                        overridden = true;
                        break;
                    }
                }

                if (!overridden) found.add(m1);
            }

            clazz = clazz.getSuperclass();
        }

        return found;
    }
}
