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
package net.sourceforge.stripes.ajax;

import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.util.Log;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.lang.reflect.Method;
import java.lang.reflect.Array;

/**
 * <p>Builds a set of JavaScript statements that will re-construct the value of a Java object,
 * including all Number, String, Enum, Boolean, Collection, Map and Array properties.  Safely handles
 * object graph circularities - each object will be translated only once, and all references will
 * be valid.</p>
 *
 * <p>The JavaScript created by the builder can be evaluated in JavaScript using:</p>
 *
 *<pre>
 *var myObject = eval(generatedFragment);
 *</pre>
 *
 * @author Tim Fennell
 * @since Stripes 1.1
 */
public class JavaScriptBuilder {
    /** Log instance used to log messages. */
    private static final Log log = Log.getInstance(JavaScriptBuilder.class);

    /** Holds the set of classes representing the primitive types in Java. */
    static Set<Class<?>> simpleTypes = new HashSet<Class<?>>();

    /** Holds the set of types that will be skipped over by default. */
    static Set<Class<?>> ignoredTypes = new HashSet<Class<?>>();

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

    /** Holds the set of objects that have been visited during conversion. */
    private Set<Integer> visitedIdentities = new HashSet<Integer>();

    /** Holds a map of name to JSON value for JS Objects and Arrays. */
    private Map<String,String> objectValues = new HashMap<String,String>();

    /** Holds a map of object.property = object. */
    private Map<String,String> assignments = new HashMap<String,String>();

    /** Holds the root object which is to be converted to JavaScript. */
    private Object rootObject;

    /** Holds the (potentially empty) set of user classes that should be skipped over. */
    private Set<Class<?>> excludeClasses;
    
    /** Holds the (potentially empty) set of properties that should be skipped over. */
    private Set<String> excludeProperties;

    /** Holds an optional user-supplied name for the root property. */
    private String rootVariableName = "_sj_root_" + new Random().nextInt(Integer.MAX_VALUE);

    /**
     * Constructs a new JavaScriptBuilder to build JS for the root object supplied.
     *
     * @param root The root object from which to being translation into JavaScript
     * @param objectsToExclude Zero or more Strings and/or Classes to be excluded
     * from translation.
     */
    public JavaScriptBuilder(Object root, Object... objectsToExclude) {
        this.rootObject = root;
        this.excludeClasses = new HashSet<Class<?>>();
        this.excludeProperties = new HashSet<String>();

        for (Object object : objectsToExclude) {
            if (object instanceof Class<?>)
                addClassExclusion((Class<?>) object);
            else if (object instanceof String)
                addPropertyExclusion((String) object);
            else
                log.warn("Don't know to determine exclusion for objects of type ", object.getClass().getName(), ". You may only pass in instances of Class and/or String.");
        }

        this.excludeClasses.addAll(ignoredTypes);
    }

    /**
     * Adds one or more properties to the list of property to exclude when translating
     * to JavaScript.
     *
     * @param property one or more property names to be excluded
     * @return the JavaScripBuilder instance to simplify method chaining
     */
    public JavaScriptBuilder addPropertyExclusion(String... property) {
        for (String prop : property) {
            this.excludeProperties.add(prop);
        }
        return this;
    }

    /**
     * Adds one or more properties to the list of properties to exclude when translating
     * to JavaScript.
     *
     * @param clazz one or more classes to exclude
     * @return the JavaScripBuilder instance to simplify method chaining
     */
    public JavaScriptBuilder addClassExclusion(Class<?>... clazz) {
        for (Class<?> c : clazz) {
            this.excludeClasses.add(c);
        }
        return this;
    }

    /**
     * Sets an optional user-supplied root variable name. If set this name will be used
     * by the building when declarind the root variable to which the JS is assigned. If
     * not provided then a randomly generated name will be used.
     *
     * @param rootVariableName the name to use when declaring the root variable
     */
    public void setRootVariableName(final String rootVariableName) {
        this.rootVariableName = rootVariableName;
    }

    /**
     * Returns the name used to declare the root variable to which the built
     * JavaScript object is assigned.
     */
    public String getRootVariableName() {
        return rootVariableName;
    }

    /**
     * Causes the JavaScriptBuilder to navigate the properties of the supplied object and
     * convert them to JavaScript.
     *
     * @return String a fragment of JavaScript that will define and return the JavaScript
     *         equivalent of the Java object supplied to the builder.
     */
    public String build() {
        Writer writer = new StringWriter();
        build(writer);
        return writer.toString();
    }

    /**
     * Causes the JavaScriptBuilder to navigate the properties of the supplied object and
     * convert them to JavaScript, writing them to the supplied writer as it goes.
     */
    public void build(Writer writer) {
        try {
            // If for some reason a caller provided us with a simple scalar object, then
            // convert it and short-circuit return
            if (isScalarType(this.rootObject)) {
                writer.write(getScalarAsString(this.rootObject));
                writer.write(";\n");
                return;
            }

            buildNode(this.rootVariableName, this.rootObject, "");

            writer.write("var ");
            writer.write(rootVariableName);
            writer.write(";\n");

            for (Map.Entry<String,String> entry : objectValues.entrySet()) {
                writer.append("var ");
                writer.append(entry.getKey());
                writer.append(" = ");
                writer.append(entry.getValue());
                writer.append(";\n");
            }

            for (Map.Entry<String,String> entry : assignments.entrySet()) {
                writer.append(entry.getKey());
                writer.append(" = ");
                writer.append(entry.getValue());
                writer.append(";\n");
            }

            writer.append(rootVariableName).append(";\n");
        }
        catch (Exception e) {
            throw new StripesRuntimeException("Could not build JavaScript for object. An " +
                    "exception was thrown while trying to convert a property from Java to " +
                    "JavaScript. The object being converted is: " + this.rootObject, e);
        }

    }

    /**
     * Returns true if the supplied type should be excluded from conversion, otherwise
     * returns false.  A class should be excluded if it is assignable to one of the types
     * listed for exclusion, or, it is an array of such a type.
     */
    public boolean isExcludedType(Class<?> type) {
        for (Class<?> excludedType : this.excludeClasses) {
            if (excludedType.isAssignableFrom(type)) {
                return true;
            }
            else if (type.isArray() && excludedType.isAssignableFrom(type.getComponentType())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if the object is of a type that can be converted to a simple JavaScript
     * scalar, and false otherwise.
     */
    public boolean isScalarType(Object in) {
        if (in == null) return true; // Though not strictly scalar, null can be treated as such

        Class<?> type = in.getClass();
        return simpleTypes.contains(type)
            || Number.class.isAssignableFrom(type)
            || String.class.isAssignableFrom(type)
            || Boolean.class.isAssignableFrom(type)
            || Character.class.isAssignableFrom(type)
            || Date.class.isAssignableFrom(type);
    }

    /**
     * Fetches the value of a scalar type as a String. The input to this method may not be null,
     * and must be a of a type that will return true when supplied to isScalarType().
     */
    public String getScalarAsString(Object in) {
        if (in == null) return "null";

        Class<? extends Object> type = in.getClass();

        if (String.class.isAssignableFrom(type)) {
            return quote((String) in);
        }
        else if (Character.class.isAssignableFrom(type)) {
            return quote(((Character) in).toString());
        }
        else if(Date.class.isAssignableFrom(type)) {
            return "new Date(" + ((Date) in).getTime() + ")";
        }
        else {
            return in.toString();
        }
    }

    /**
     * Quotes the supplied String and escapes all characters that could be problematic
     * when eval()'ing the String in JavaScript.
     *
     * @param string a String to be escaped and quoted
     * @return the escaped and quoted String
     * @since Stripes 1.2 (thanks to Sergey Pariev)
     */
    public static String quote(String string) {
        if (string == null || string.length() == 0) {
            return "\"\"";
        }

        char c = 0;
        int len = string.length();
        StringBuilder sb = new StringBuilder(len + 10);

        sb.append('"');
        for (int i = 0; i < len; ++i) {
            c = string.charAt(i);
            switch (c) {
                case '\\':
                case  '"':
                    sb.append('\\').append(c);
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                default:
                    if (c < ' ') {
                        // The following takes lower order chars and creates unicode style
                        // char literals for them (e.g. \u00F3)
                        sb.append("\\u");
                        String hex = Integer.toHexString(c);
                        int pad = 4 - hex.length();
                        for (int j=0; j<pad; ++j) {
                            sb.append("0");
                        }
                        sb.append(hex);
                    }
                    else {
                        sb.append(c);
                    }
            }
        }

        sb.append('"');
        return sb.toString();
    }

    /**
     * Determines the type of the object being translated and dispatches to the
     * build*Node() method.  Generates the temporary name of the object being translated,
     * checks to ensure that the object has not already been translated, and ensure that
     * the object is correctly inserted into the set of assignments.
     *
     * @param name The name that should appear on the left hand side of the assignment
     *        statement once a value for the object has been generated.
     * @param in The object being translated.
     */
    void buildNode(String name, Object in, String propertyPrefix) throws Exception {
        int systemId = System.identityHashCode(in);
        String targetName = "_sj_" + systemId;
        
        if (this.visitedIdentities.contains(systemId)) {
            this.assignments.put(name, targetName);
        }
        else if (isExcludedType(in.getClass())) {
            // Do nothing, it's being excluded!!
        }
        else {
            this.visitedIdentities.add(systemId);

            if (Collection.class.isAssignableFrom(in.getClass())) {
                buildCollectionNode(targetName, (Collection<?>) in, propertyPrefix);
            }
            else if (in.getClass().isArray()) {
                buildArrayNode(targetName, in, propertyPrefix);
            }
            else if (Map.class.isAssignableFrom(in.getClass())) {
                buildMapNode(targetName, (Map<?, ?>) in, propertyPrefix);
            }
            else {
                buildObjectNode(targetName, in, propertyPrefix);
            }

            this.assignments.put(name, targetName);
        }
    }

    /**
     * <p>Processes a Java Object that conforms to JavaBean conventions. Scalar properties of the
     * object are converted to a JSON format object declaration which is inserted into the
     * "objectValues" instance level map. Nested non-scalar objects are processed separately and
     * then setup for re-attachment using the instance level "assignments" map.</p>
     *
     * <p>In most cases just the JavaBean properties will be translated.  In the case of Java 5
     * enums, two additional properties will be translated, one each for the enum's 'ordinal'
     * and 'name' properties.</p>
     *
     * @param targetName The generated name assigned to the Object being translated
     * @param in The Object who's JavaBean properties are to be translated
     */
    void buildObjectNode(String targetName, Object in, String propertyPrefix) throws Exception {
        StringBuilder out = new StringBuilder();
        out.append("{");
        PropertyDescriptor[] props = Introspector.getBeanInfo(in.getClass()).getPropertyDescriptors();

        for (PropertyDescriptor property : props) {
            try {
                Method readMethod = property.getReadMethod();
                String fullPropertyName = (propertyPrefix != null && propertyPrefix.length() > 0 ? propertyPrefix + '.' : "") +
                        property.getName();
                if ((readMethod != null) && !this.excludeProperties.contains(fullPropertyName)) {
                    Object value = property.getReadMethod().invoke(in);

                    if (isExcludedType(property.getPropertyType())) {
                        continue;
                    }

                    if (isScalarType(value)) {
                        if (out.length() > 1) {
                            out.append(", ");
                        }
                        out.append(property.getName());
                        out.append(":");
                        out.append( getScalarAsString(value) );
                    }
                    else {
                        buildNode(targetName + "." + property.getName(), value, fullPropertyName);
                    }
                }
            }
            catch (Exception e) {
                log.warn(e, "Could not translate property [", property.getName(), "] of type [",
                         property.getPropertyType().getName(), "] due to an exception.");
            }
        }

        // Do something a little extra for enums
        if (Enum.class.isAssignableFrom(in.getClass())) {
            Enum<?> e = (Enum<?>) in;

            if (out.length() > 1) { out.append(", "); }
            out.append("ordinal:").append( getScalarAsString(e.ordinal()) );
            out.append(", name:").append( getScalarAsString(e.name()) );
        }

        out.append("}");
        this.objectValues.put(targetName, out.toString());
    }

    /**
     * Builds a JavaScript object node from a java Map. The keys of the map are used to
     * define the properties of the JavaScript object.  As such it is assumed that the keys
     * are either primitives, Strings or toString() cleanly.  The values of the map are used
     * to generate the values of the object properties.  Scalar values are inserted directly
     * into the JSON representation, while complex types are converted separately and then
     * attached using assignments.
     *
     * @param targetName The generated name assigned to the Map being translated
     * @param in The Map being translated
     */
    void buildMapNode(String targetName, Map<?,?> in, String propertyPrefix) throws Exception {
        StringBuilder out = new StringBuilder();
        out.append("{");

        for (Map.Entry<?,?> entry : in.entrySet()) {
            String propertyName = getScalarAsString(entry.getKey());
            Object value = entry.getValue();
            
            if (this.excludeProperties.contains(propertyPrefix + '[' + propertyName + ']')) {
                // Do nothing, it's being excluded!!
            }
            else if (isScalarType(value)) {
                if (out.length() > 1) {
                    out.append(", ");
                }
                out.append(propertyName);
                out.append(":");
                out.append( getScalarAsString(value) );
            }
            else {
                buildNode(targetName + "[" + propertyName + "]", value, propertyPrefix + "[" + propertyName + "]");
            }
        }

        out.append("}");
        this.objectValues.put(targetName, out.toString());
    }

    /**
     * Builds a JavaScript array node from a Java array.  Scalar values are inserted directly
     * into the array definition. Complex values are processed separately - they are inserted
     * into the JSON array as null to maintain ordering, and re-attached later using assignments.
     *
     * @param targetName The generated name of the array node being translated.
     * @param in The Array being translated.
     */
    void buildArrayNode(String targetName, Object in, String propertyPrefix) throws Exception {
        StringBuilder out = new StringBuilder();
        out.append("[");

        int length = Array.getLength(in);
        for (int i=0; i<length; i++) {
            Object value = Array.get(in, i);

            if (this.excludeProperties.contains(propertyPrefix + '[' + i + ']')) {
                // It's being excluded but we should leave a placeholder in the array
                out.append("null");
            }
            else if (isScalarType(value)) {
                out.append( getScalarAsString(value) );
            }
            else {
                out.append("null");
                buildNode(targetName + "[" + i + "]", value, propertyPrefix + "[" + i + "]");
            }

            if (i != length-1) {
                out.append(", ");
            }
        }

        out.append("]");
        this.objectValues.put(targetName, out.toString());
    }

    /**
     * Builds an object node that is of type collection.  Simply converts the collection
     * to an array, and delegates to buildArrayNode().
     */
    void buildCollectionNode(String targetName, Collection<?> in, String propertyPrefix) throws Exception {
        buildArrayNode(targetName, in.toArray(), propertyPrefix);
    }
}
