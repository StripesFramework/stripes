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

import net.sourceforge.stripes.exception.StripesRuntimeException;

import java.util.Iterator;
import java.util.LinkedHashMap;
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
import java.util.concurrent.ConcurrentHashMap;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static java.lang.reflect.Modifier.isPublic;
import java.beans.PropertyDescriptor;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.IntrospectionException;

/**
 * Common utilty methods that are useful when working with reflection.
 *
 * @author Tim Fennell
 */
public class ReflectUtil {
    private static final Log log = Log.getInstance(ReflectUtil.class);

    /** A cache of property descriptors by class and property name */
    private static Map<Class<?>, Map<String, PropertyDescriptor>> propertyDescriptors =
            new ConcurrentHashMap<Class<?>, Map<String, PropertyDescriptor>>();

    /** Static helper class, shouldn't be constructed. */
    private ReflectUtil() {}

    /**
     * Holds a map of commonly used interface types (mostly collections) to a class that
     * implements the interface and will, by default, be instantiated when an instance
     * of the interface is needed.
     */
    protected static final Map<Class<?>,Class<?>> interfaceImplementations = new HashMap<Class<?>,Class<?>>();

    /**
     * Holds a map of primitive type to the default value for that primitive type.  Isn't it
     * odd that there's no way to get this programmatically from the Class objects?
     */
    protected static final Map<Class<?>,Object> primitiveDefaults = new HashMap<Class<?>,Object>();

    static {
        interfaceImplementations.put(Collection.class, ArrayList.class);
        interfaceImplementations.put(List.class,       ArrayList.class);
        interfaceImplementations.put(Set.class,        HashSet.class);
        interfaceImplementations.put(SortedSet.class,  TreeSet.class);
        interfaceImplementations.put(Queue.class,      LinkedList.class);
        interfaceImplementations.put(Map.class,        HashMap.class);
        interfaceImplementations.put(SortedMap.class,  TreeMap.class);

        primitiveDefaults.put(Boolean.TYPE,    false);
        primitiveDefaults.put(Character.TYPE, '\0');
        primitiveDefaults.put(Byte.TYPE,       new Byte("0"));
        primitiveDefaults.put(Short.TYPE,      new Short("0"));
        primitiveDefaults.put(Integer.TYPE,    new Integer(0));
        primitiveDefaults.put(Long.TYPE,       new Long(0l));
        primitiveDefaults.put(Float.TYPE,      new Float(0f));
        primitiveDefaults.put(Double.TYPE,     new Double(0.0));
    }

    /**
     * The set of method that annotation classes inherit, and should be avoided when
     * toString()ing an annotation class.
     */
    private static final Set<String> INHERITED_ANNOTATION_METHODS =
            Literal.set("toString", "equals", "hashCode", "annotationType");

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
    @SuppressWarnings("unchecked") // this allows us to assign without casting
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
     * classes. Methods that have been overridden in the inheritance hierarchy are
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

    /**
     * Fetches all fields of all access types from the supplied class and super classes.
     * 
     * @param clazz the class to inspect
     * @return a collection of fields
     */
    public static Collection<Field> getFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<Field>();
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                fields.add(field);
            }

            clazz = clazz.getSuperclass();
        }

        return fields;
    }

    /**
     * Fetches the property descriptor for the named property of the supplied class. To
     * speed things up a cache is maintained of propertyName to PropertyDescriptor for
     * each class used with this method.  If there is no property with the specified name,
     * returns null.
     *
     * @param clazz the class who's properties to examine
     * @param property the String name of the property to look for
     * @return the PropertyDescriptor or null if none is found with a matching name
     */
    public static PropertyDescriptor getPropertyDescriptor(Class<?> clazz, String property) {
        if (!propertyDescriptors.containsKey(clazz))
            getPropertyDescriptors(clazz);
        return propertyDescriptors.get(clazz).get(property);
    }

    /**
     * <p>Attempts to find an accessible version of the method passed in, where accessible
     * is defined as the method itself being public and the declaring class being public.
     * Mostly useful as a workaround to the situation when
     * {@link PropertyDescriptor#getReadMethod()} and/or
     * {@link java.beans.PropertyDescriptor#getWriteMethod()} returns methods that are not
     * accessible (usually due to public implementations of interface methods in private
     * classes).</p>
     *
     * <p>Checks the method passed in and if it already meets these criteria it is returned
     * immediately. In general this leads to very little performance overhead</p>
     *
     * <p>If the method does not meet the criteria then the class' interfaces are scanned
     * for a matching method. If one is not found, then the class' superclass hierarchy
     * is searched. Finally, if no matching method can be found the original method is
     * returned.</p>
     *
     * @param m a method that may or may not be accessible
     * @return either an accessible version of the same method, or the method passed in if
     *         an accessible version cannot be found
     */
    public static Method findAccessibleMethod(final Method m) {
        // If the passed in method is accessible, then just give it back.
        if (isPublic(m.getModifiers()) && isPublic(m.getDeclaringClass().getModifiers())) return m;
        if (m.isAccessible()) return m;

        final Class<?> clazz    = m.getDeclaringClass();
        final String name    = m.getName();
        final Class<?>[] ptypes = m.getParameterTypes();

        // Else, loop through the interfaces for the declaring class, looking for a
        // public version of the method that we can call
        for (Class<?> iface : clazz.getInterfaces()) {
            try {
                Method m2 = iface.getMethod(name, ptypes);
                if (m2.isAccessible()) return m2;
                if (isPublic(iface.getModifiers()) && isPublic(m2.getModifiers())) return m2;
            }
            catch (NoSuchMethodException nsme) { /* Not Unexpected. */ }
        }

        // Else loop through the superclasses looking for a public method
        Class<?> c = clazz.getSuperclass();
        while (c != null) {
            try {
                Method m2 = c.getMethod(name, ptypes);
                if (m2.isAccessible()) return m2;
                if (isPublic(c.getModifiers()) && isPublic(m2.getModifiers())) return m2;
            }
            catch (NoSuchMethodException nsme) { /* Not Unexpected. */ }

            c = c.getSuperclass();
        }

        // If we haven't found anything at this point, just give up!
        return m;
    }



    /**
     * Looks for an instance (i.e. non-static) public field with the matching name and
     * returns it if one exists.  If no such field exists, returns null.
     *
     * @param clazz the clazz who's fields to examine
     * @param property the name of the property/field to look for
     * @return the Field object or null if no matching field exists
     */
    public static Field getField(Class<?> clazz, String property) {
        try {
            Field field = clazz.getField(property);
            return !Modifier.isStatic(field.getModifiers()) ? field : null;
        }
        catch (NoSuchFieldException nsfe) {
            return null;
        }
    }

    /**
     * Returns an appropriate default value for the class supplied. Mirrors the defaults used
     * when the JVM initializes instance variables.
     *
     * @param clazz the class for which to find the default value
     * @return null for non-primitive types and an appropriate wrapper instance for primitives
     */
    public static Object getDefaultValue(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            return primitiveDefaults.get(clazz);
        }
        else {
            return null;
        }
    }
    
    /**
     * Returns a set of all interfaces implemented by class supplied. This includes all
     * interfaces directly implemented by this class as well as those implemented by
     * superclasses or interface superclasses.
     * 
     * @param clazz
     * @return all interfaces implemented by this class
     */
    public static Set<Class<?>> getImplementedInterfaces(Class<?> clazz)
    {
        Set<Class<?>> interfaces = new HashSet<Class<?>>();
        
        if (clazz.isInterface())
            interfaces.add(clazz);

        while (clazz != null) {
            for (Class<?> iface : clazz.getInterfaces())
                interfaces.addAll(getImplementedInterfaces(iface));
            clazz = clazz.getSuperclass();
        } 

        return interfaces;
    }

    /**
     * Returns an array of Type objects representing the actual type arguments
     * to targetType used by clazz.
     * 
     * @param clazz the implementing class (or subclass)
     * @param targetType the implemented generic class or interface
     * @return an array of Type objects or null
     */
    public static Type[] getActualTypeArguments(Class<?> clazz, Class<?> targetType) {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        classes.add(clazz);

        if (targetType.isInterface())
            classes.addAll(getImplementedInterfaces(clazz));

        Class<?> superClass = clazz.getSuperclass();
        while (superClass != null) {
            classes.add(superClass);
            superClass = superClass.getSuperclass();
        }

        for (Class<?> search : classes) {
            for (Type type : (targetType.isInterface() ? search.getGenericInterfaces()
                    : new Type[] { search.getGenericSuperclass() })) {
                if (type instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) type;
                    if (targetType.equals(parameterizedType.getRawType()))
                        return parameterizedType.getActualTypeArguments();
                }
            }
        }

        return null;
    }

    /**
     * Get the {@link PropertyDescriptor}s for a bean class. This is normally easy enough to do
     * except that Java versions 6 and earlier have a bug that can return bridge methods for
     * property getters and/or setters. That can mess up validation and binding and possibly other
     * areas. This method accounts for that bug and attempts to work around it, ensuring the
     * property descriptors contain the true getter and setter methods.
     * 
     * @param clazz The bean class to introspect
     * @return The property descriptors for the bean class, as returned by
     *         {@link BeanInfo#getPropertyDescriptors()}.
     */
    public static PropertyDescriptor[] getPropertyDescriptors(Class<?> clazz) {
        // Look in the cache first
        if (propertyDescriptors.containsKey(clazz)) {
            Collection<PropertyDescriptor> pds = propertyDescriptors.get(clazz).values();
            return pds.toArray(new PropertyDescriptor[pds.size()]);
        }

        // A subclass that is aware of bridge methods
        class BridgedPropertyDescriptor extends PropertyDescriptor {
            private Method readMethod, writeMethod;
            private Class<?> propertyType;

            public BridgedPropertyDescriptor(PropertyDescriptor pd) throws IntrospectionException {
                super(pd.getName(), pd.getReadMethod(), pd.getWriteMethod());
                readMethod = resolveBridgedReadMethod(pd);
                writeMethod = resolveBridgedWriteMethod(pd);
                propertyType = resolvePropertyType(this);
            }

            @Override
            public synchronized Class<?> getPropertyType() {
                return propertyType;
            }

            @Override
            public synchronized Method getReadMethod() {
                return readMethod;
            }

            @Override
            public synchronized Method getWriteMethod() {
                return writeMethod;
            }

            @Override
            public synchronized void setReadMethod(Method readMethod) {
                this.readMethod = readMethod;
            }

            @Override
            public synchronized void setWriteMethod(Method writeMethod) {
                this.writeMethod = writeMethod;
            }
        }

        // Not cached yet. Look it up the normal way.
        try {
            // Make a copy of the array to avoid poking stuff into Introspector's cache!
            PropertyDescriptor[] pds = Introspector.getBeanInfo(clazz).getPropertyDescriptors();
            pds = Arrays.asList(pds).toArray(new PropertyDescriptor[pds.length]);

            // Make a new local cache entry
            Map<String, PropertyDescriptor> map = new LinkedHashMap<String, PropertyDescriptor>();

            // Check each descriptor for bridge methods and handle accordingly
            for (int i = 0; i < pds.length; i++) {
                PropertyDescriptor pd = pds[i];
                if ((pd.getReadMethod() != null && pd.getReadMethod().isBridge())
                        || (pd.getWriteMethod() != null && pd.getWriteMethod().isBridge())) {
                    log.debug("Working around JVM bug involving PropertyDescriptors ",
                            "and bridge methods for ", clazz);
                    pd = new BridgedPropertyDescriptor(pd);
                    pds[i] = pd;
                }
                map.put(pd.getName(), pd);
            }

            // Put local cache entry
            propertyDescriptors.put(clazz, map);

            return pds;
        }
        catch (IntrospectionException ie) {
            throw new StripesRuntimeException("Could not examine class '" + clazz.getName()
                    + "' using Introspector.getBeanInfo() to determine property information.", ie);
        }
    }

    /**
     * Locate and return the bridged read method for a bean property.
     * 
     * @param pd The bean property descriptor
     * @return The bridged method or the property descriptor's read method, if it is not a bridge
     *         method.
     */
    public static Method resolveBridgedReadMethod(PropertyDescriptor pd) {
        Method getter = pd.getReadMethod();

        if (getter != null && getter.isBridge()) {
            try {
                getter = getter.getDeclaringClass().getMethod(getter.getName());
            }
            catch (SecurityException e) {
                // Ignore exception and keep whatever was in the property descriptor
            }
            catch (NoSuchMethodException e) {
                // Ignore exception and keep whatever was in the property descriptor
            }
        }

        return getter;
    }

    /**
     * Locate and return the bridged write method for a bean property.
     * 
     * @param pd The bean property descriptor
     * @return The bridged method or the property descriptor's write method, if it is not a bridge
     *         method.
     */
    public static Method resolveBridgedWriteMethod(PropertyDescriptor pd) {
        Method setter = pd.getWriteMethod();

        if (setter != null && setter.isBridge()) {
            // Make a list of methods with the same name that take a single parameter
            List<Method> candidates = new ArrayList<Method>();
            Method[] methods = setter.getDeclaringClass().getMethods();
            for (Method method : methods) {
                if (!method.isBridge() && method.getName().equals(setter.getName())
                        && method.getParameterTypes().length == 1
                        && pd.getPropertyType().isAssignableFrom(method.getParameterTypes()[0])) {
                    candidates.add(method);
                }
            }

            if (candidates.size() == 1) {
                setter = candidates.get(0);
            }
            else if (candidates.isEmpty()) {
                log.error("Something has gone awry! I have a bridge to nowhere: ", setter);
            }
            else {
                // Create a set of all type arguments for all classes declaring the matching methods
                Set<Type> typeArgs = new HashSet<Type>();
                for (Method method : candidates) {
                    Class<?> declarer = method.getDeclaringClass();

                    // Add type arguments for interfaces
                    for (Class<?> iface : getImplementedInterfaces(declarer)) {
                        Type[] types = getActualTypeArguments(declarer, iface);
                        if (types != null)
                            typeArgs.addAll(Arrays.asList(types));
                    }

                    // Add type arguments for superclasses
                    for (Class<?> c = declarer.getSuperclass(); c != null; c = c.getSuperclass()) {
                        Type[] types = getActualTypeArguments(declarer, c);
                        if (types != null)
                            typeArgs.addAll(Arrays.asList(types));
                    }
                }

                // Now cycle through, collecting only those methods whose return type is a type arg
                List<Method> primeCandidates = new ArrayList<Method>(candidates);
                Iterator<Method> iterator = primeCandidates.iterator();
                while (iterator.hasNext()) {
                    if (!typeArgs.contains(iterator.next().getParameterTypes()[0]))
                        iterator.remove();
                }

                // If we are left with exactly one match, then go with it
                if (primeCandidates.size() == 1) {
                    setter = primeCandidates.get(0);
                }
                else {
                    log.warn("Unable to locate a bridged setter for ", pd.getName(),
                            " due to a JVM bug and an overloaded method with ",
                            "the same name as the property setter. This could be a problem. ",
                            "The offending overloaded methods are: ", candidates);
                }
            }
        }

        return setter;
    }

    /**
     * Under normal circumstances, a property's getter will return exactly the same type as its
     * setter accepts as a parameter. However, because we have to hack around the JVM bug dealing
     * with bridge methods this might not always be the case. This method resolves the actual type
     * of the property. In the case where the two types (return type and parameter type) are not
     * identical, the property type is whichever of the two is lower in the class hierarchy.
     * 
     * @param pd The property descriptor
     * @return The type of the property
     */
    public static Class<?> resolvePropertyType(PropertyDescriptor pd) {
        Method readMethod = pd.getReadMethod();
        Method writeMethod = pd.getWriteMethod();
        Class<?> returnType = readMethod == null ? null : readMethod.getReturnType();
        Class<?> paramType = writeMethod == null ? null : writeMethod.getParameterTypes()[0];

        // For a read-only property, use the getter's return type
        if (readMethod != null && writeMethod == null)
            return returnType;

        // For a write-only property, use the setter's parameter type
        if (writeMethod != null && readMethod == null)
            return paramType;

        // If the two types are identical (generally the case), then this is easy
        if (returnType == paramType)
            return returnType;

        // Otherwise, take the type that is *lower* in the class hierarchy
        return returnType.isAssignableFrom(paramType) ? paramType : returnType;
    }
}
