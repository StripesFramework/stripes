/*
 * Copyright 2015 Stripes Framework.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sourceforge.stripes.action;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import net.sourceforge.stripes.util.Log;

/**
 * This abstract class should be implemented by "builders" which take Java
 * objects and convert them into a specific format (such as JavaScript, XML, or
 * JSON). Originally created by using the JavaScript builder which contains
 * functionality for property and class exclusion.
 *
 * @author Rick Grashel
 * @param <T> Subclass of this object
 */
public abstract class ObjectOutputBuilder< T extends ObjectOutputBuilder> {

    /**
     * Log instance used to log messages.
     */
    private static final Log log = Log.getInstance(ObjectOutputBuilder.class);

    /**
     * Holds the set of classes representing the primitive types in Java.
     */
    static final Set<Class<?>> simpleTypes = new HashSet<Class<?>>();

    /**
     * Holds the set of types that will be skipped over by default.
     */
    static final Set<Class<?>> ignoredTypes = new HashSet<Class<?>>();

    static {
        simpleTypes.add(Byte.TYPE);
        simpleTypes.add(Short.TYPE);
        simpleTypes.add(Integer.TYPE);
        simpleTypes.add(Long.TYPE);
        simpleTypes.add(Float.TYPE);
        simpleTypes.add(Double.TYPE);
        simpleTypes.add(Boolean.TYPE);
        simpleTypes.add(Character.TYPE);

        ignoredTypes.add(Class.class);
    }

    /**
     * Holds the root object which is to be converted to an output format.
     */
    private final Object rootObject;

    /**
     * Holds the (potentially empty) set of user classes that should be skipped
     * over.
     */
    private final Set<Class<?>> excludeClasses;

    /**
     * Holds the (potentially empty) set of properties that should be skipped
     * over.
     */
    private final Set<String> excludeProperties;

    /**
     * Holds an optional user-supplied name for the root property.
     */
    private String rootVariableName = null;

    /**
     * Constructs a new ObjectOutputBuilder to build output for the root object
     * supplied.
     *
     * @param root The root object from which to being translation into
     * JavaScript
     * @param objectsToExclude Zero or more Strings and/or Classes to be
     * excluded from translation.
     */
    public ObjectOutputBuilder(Object root, Object... objectsToExclude) {
        this.rootObject = root;
        this.excludeClasses = new HashSet<Class<?>>();
        this.excludeProperties = new HashSet<String>();

        for (Object object : objectsToExclude) {
            if (object instanceof Class<?>) {
                addClassExclusion((Class<?>) object);
            } else if (object instanceof String) {
                addPropertyExclusion((String) object);
            } else {
                log.warn("Don't know to determine exclusion for objects of type ", object.getClass().getName(), ". You may only pass in instances of Class and/or String.");
            }
        }

        this.excludeClasses.addAll(ignoredTypes);
    }

    /**
     * Adds one or more properties to the list of property to exclude when
     * translating to the formatted output.
     *
     * @param property one or more property names to be excluded
     * @return the ObjectOutputBuilder instance to simplify method chaining
     */
    public final T addPropertyExclusion(String... property) {
        this.excludeProperties.addAll(Arrays.asList(property));
        return (T) this;
    }

    /**
     * Adds one or more properties to the list of properties to exclude when
     * translating to JavaScript.
     *
     * @param clazz one or more classes to exclude
     * @return the JavaScripBuilder instance to simplify method chaining
     */
    public final T addClassExclusion(Class<?>... clazz) {
        this.excludeClasses.addAll(Arrays.asList(clazz));
        return (T) this;
    }

    /**
     * Sets an optional user-supplied root variable name. If set this name will
     * be used by the building when declaring the root variable to which the JS
     * is assigned. If not provided then a randomly generated name will be used.
     *
     * @param rootVariableName the name to use when declaring the root variable
     */
    public void setRootVariableName(final String rootVariableName) {
        this.rootVariableName = rootVariableName;
    }

    /**
     * Returns the name used to declare the root variable to which the built
     * object is assigned.
     *
     * @return The root variable name for the object which is being converted
     */
    public String getRootVariableName() {
        return rootVariableName;
    }

    /**
     * Causes the JavaScriptBuilder to navigate the properties of the supplied
     * object and convert them to JavaScript.
     *
     * @return String a fragment of JavaScript that will define and return the
     * JavaScript equivalent of the Java object supplied to the builder.
     * @throws java.lang.Exception If something goes wrong when creating the
     * javascript object.
     */
    public String build() throws Exception {
        Writer writer = new StringWriter();
        build(writer);
        return writer.toString();
    }

    /**
     * Causes the ObjectOutputBuilder to navigate the properties of the supplied
     * object and convert them to the desired format, writing them to the
     * supplied writer as it goes.
     *
     * @param writer Instance of the writer that the converted object should be
     * written to.
     * @throws java.lang.Exception If something goes wrong when creating the
     * javascript object.
     */
    public abstract void build(Writer writer) throws Exception;

    /**
     * Returns true if the supplied type should be excluded from conversion,
     * otherwise returns false. A class should be excluded if it is assignable
     * to one of the types listed for exclusion, or, it is an array of such a
     * type.
     *
     * @param type - Class to check to see if it is excluded for this builder.
     * @return Whether or not the passed class is targeted for exclusion by the
     * builder.
     */
    public boolean isExcludedType(Class<?> type) {
        for (Class<?> excludedType : this.excludeClasses) {
            if (excludedType.isAssignableFrom(type)) {
                return true;
            } else if (type.isArray() && excludedType.isAssignableFrom(type.getComponentType())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if the object is of a type that can be converted to a simple
     * scalar, and false otherwise.
     *
     * @param in Object to check to see if it is a scalar
     * @return Whether or not the passed object is a scalar object.
     */
    public boolean isScalarType(Object in) {
        if (in == null) {
            return true; // Though not strictly scalar, null can be treated as such
        }
        Class<?> type = in.getClass();
        return simpleTypes.contains(type)
                || Number.class.isAssignableFrom(type)
                || String.class.isAssignableFrom(type)
                || Boolean.class.isAssignableFrom(type)
                || Character.class.isAssignableFrom(type)
                || Date.class.isAssignableFrom(type);
    }

    /**
     * Returns the root object being built.
     *
     * @return Root object being built.
     */
    public Object getRootObject() {
        return this.rootObject;
    }

    /**
     * Returns the properties that should be excluded from this object out.
     *
     * @return Set of excluded properties
     */
    public Set<String> getExcludedProperties() {
        return this.excludeProperties;
    }

}
