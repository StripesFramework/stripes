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
import net.sourceforge.stripes.util.ReflectUtil;

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
import net.sourceforge.stripes.action.ObjectOutputBuilder;

/**
 * <p>
 * Builds a set of JavaScript statements that will re-construct the value of a
 * Java object, including all Number, String, Enum, Boolean, Collection, Map and
 * Array properties. Safely handles object graph circularities - each object
 * will be translated only once, and all references will be valid.</p>
 *
 * <p>
 * The JavaScript created by the builder can be evaluated in JavaScript
 * using:</p>
 *
 * <pre>
 * var myObject = eval(generatedFragment);
 * </pre>
 *
 * @author Tim Fennell
 * @author Rick Grashel
 * @since Stripes 1.1
 */
public class JavaScriptBuilder extends ObjectOutputBuilder<JavaScriptBuilder> {

    /**
     * Log instance used to log messages.
     */
    private static final Log log = Log.getInstance(JavaScriptBuilder.class);

    /**
     * Holds the set of objects that have been visited during conversion.
     */
    private Set<Integer> visitedIdentities = new HashSet<Integer>();

    /**
     * Holds a map of name to JSON value for JS Objects and Arrays.
     */
    private Map<String, String> objectValues = new HashMap<String, String>();

    /**
     * Holds a map of object.property = object.
     */
    private Map<String, String> assignments = new HashMap<String, String>();

    /**
     * Constructs a new JavaScriptBuilder to build JS for the root object
     * supplied.
     *
     * @param root The root object from which to being translation into
     * JavaScript
     * @param objectsToExclude Zero or more Strings and/or Classes to be
     * excluded from translation.
     */
    public JavaScriptBuilder(Object root, Object... objectsToExclude) {
        super(root, objectsToExclude);
        setRootVariableName("_sj_root_" + new Random().nextInt(Integer.MAX_VALUE));
    }

    /**
     * Causes the JavaScriptBuilder to navigate the properties of the supplied
     * object and convert them to JavaScript, writing them to the supplied
     * writer as it goes.
     */
    @Override
    public void build(Writer writer) {
        try {
            // If for some reason a caller provided us with a simple scalar object, then
            // convert it and short-circuit return
            if (isScalarType(getRootObject())) {
                writer.write(getScalarAsString(getRootObject()));
                writer.write(";\n");
                return;
            }

            buildNode(getRootVariableName(), getRootObject(), "");

            writer.write("var ");
            writer.write(getRootVariableName());
            writer.write(";\n");

            for (Map.Entry<String, String> entry : objectValues.entrySet()) {
                writer.append("var ");
                writer.append(entry.getKey());
                writer.append(" = ");
                writer.append(entry.getValue());
                writer.append(";\n");
            }

            for (Map.Entry<String, String> entry : assignments.entrySet()) {
                writer.append(entry.getKey());
                writer.append(" = ");
                writer.append(entry.getValue());
                writer.append(";\n");
            }

            writer.append(getRootVariableName()).append(";\n");
        } catch (Exception e) {
            throw new StripesRuntimeException("Could not build JavaScript for object. An "
                    + "exception was thrown while trying to convert a property from Java to "
                    + "JavaScript. The object being converted is: " + getRootObject(), e);
        }

    }

    /**
     * Quotes the supplied String and escapes all characters that could be
     * problematic when eval()'ing the String in JavaScript.
     *
     * @param string a String to be escaped and quoted
     * @return the escaped and quoted String
     * @since Stripes 1.2 (thanks to Sergey Pariev)
     */
    public static String quote(String string) {
        if (string == null || string.length() == 0) {
            return "\"\"";
        }

        char c ;
        int len = string.length();
        StringBuilder sb = new StringBuilder(len + 10);

        sb.append('"');
        for (int i = 0; i < len; ++i) {
            c = string.charAt(i);
            switch (c) {
                case '\\':
                case '"':
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
                        for (int j = 0; j < pad; ++j) {
                            sb.append("0");
                        }
                        sb.append(hex);
                    } else {
                        sb.append(c);
                    }
            }
        }

        sb.append('"');
        return sb.toString();
    }

    /**
     * Determines the type of the object being translated and dispatches to the
     * build*Node() method. Generates the temporary name of the object being
     * translated, checks to ensure that the object has not already been
     * translated, and ensure that the object is correctly inserted into the set
     * of assignments.
     *
     * @param name The name that should appear on the left hand side of the
     * assignment statement once a value for the object has been generated.
     * @param in The object being translated.
     */
    void buildNode(String name, Object in, String propertyPrefix) throws Exception {
        int systemId = System.identityHashCode(in);
        String targetName = "_sj_" + systemId;

        if (this.visitedIdentities.contains(systemId)) {
            this.assignments.put(name, targetName);
        } else if (isExcludedType(in.getClass())) {
            // Do nothing, it's being excluded!!
        } else {
            this.visitedIdentities.add(systemId);

            if (Collection.class.isAssignableFrom(in.getClass())) {
                buildCollectionNode(targetName, (Collection<?>) in, propertyPrefix);
            } else if (in.getClass().isArray()) {
                buildArrayNode(targetName, in, propertyPrefix);
            } else if (Map.class.isAssignableFrom(in.getClass())) {
                buildMapNode(targetName, (Map<?, ?>) in, propertyPrefix);
            } else {
                buildObjectNode(targetName, in, propertyPrefix);
            }

            this.assignments.put(name, targetName);
        }
    }

    /**
     * <p>
     * Processes a Java Object that conforms to JavaBean conventions. Scalar
     * properties of the object are converted to a JSON format object
     * declaration which is inserted into the "objectValues" instance level map.
     * Nested non-scalar objects are processed separately and then setup for
     * re-attachment using the instance level "assignments" map.</p>
     *
     * <p>
     * In most cases just the JavaBean properties will be translated. In the
     * case of Java 5 enums, two additional properties will be translated, one
     * each for the enum's 'ordinal' and 'name' properties.</p>
     *
     * @param targetName The generated name assigned to the Object being
     * translated
     * @param in The Object who's JavaBean properties are to be translated
     */
    void buildObjectNode(String targetName, Object in, String propertyPrefix) throws Exception {
        StringBuilder out = new StringBuilder();
        out.append("{");
        PropertyDescriptor[] props = ReflectUtil.getPropertyDescriptors(in.getClass());

        for (PropertyDescriptor property : props) {
            try {
                Method readMethod = property.getReadMethod();
                String fullPropertyName = (propertyPrefix != null && propertyPrefix.length() > 0 ? propertyPrefix + '.' : "")
                        + property.getName();
                if ((readMethod != null) && !getExcludedProperties().contains(fullPropertyName)) {
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
                        out.append(getScalarAsString(value));
                    } else {
                        buildNode(targetName + "." + property.getName(), value, fullPropertyName);
                    }
                }
            } catch (Exception e) {
                log.warn(e, "Could not translate property [", property.getName(), "] of type [",
                        property.getPropertyType().getName(), "] due to an exception.");
            }
        }

        // Do something a little extra for enums
        if (Enum.class.isAssignableFrom(in.getClass())) {
            Enum<?> e = (Enum<?>) in;

            if (out.length() > 1) {
                out.append(", ");
            }
            out.append("ordinal:").append(getScalarAsString(e.ordinal()));
            out.append(", name:").append(getScalarAsString(e.name()));
        }

        out.append("}");
        this.objectValues.put(targetName, out.toString());
    }

    /**
     * Builds a JavaScript object node from a java Map. The keys of the map are
     * used to define the properties of the JavaScript object. As such it is
     * assumed that the keys are either primitives, Strings or toString()
     * cleanly. The values of the map are used to generate the values of the
     * object properties. Scalar values are inserted directly into the JSON
     * representation, while complex types are converted separately and then
     * attached using assignments.
     *
     * @param targetName The generated name assigned to the Map being translated
     * @param in The Map being translated
     */
    void buildMapNode(String targetName, Map<?, ?> in, String propertyPrefix) throws Exception {
        StringBuilder out = new StringBuilder();
        out.append("{");

        for (Map.Entry<?, ?> entry : in.entrySet()) {
            String propertyName = getScalarAsString(entry.getKey());
            Object value = entry.getValue();

            if (getExcludedProperties().contains(propertyPrefix + '[' + propertyName + ']')) {
                // Do nothing, it's being excluded!!
            } else if (isScalarType(value)) {
                if (out.length() > 1) {
                    out.append(", ");
                }
                out.append(propertyName);
                out.append(":");
                out.append(getScalarAsString(value));
            } else {
                buildNode(targetName + "[" + propertyName + "]", value, propertyPrefix + "[" + propertyName + "]");
            }
        }

        out.append("}");
        this.objectValues.put(targetName, out.toString());
    }

    /**
     * Builds a JavaScript array node from a Java array. Scalar values are
     * inserted directly into the array definition. Complex values are processed
     * separately - they are inserted into the JSON array as null to maintain
     * ordering, and re-attached later using assignments.
     *
     * @param targetName The generated name of the array node being translated.
     * @param in The Array being translated.
     */
    void buildArrayNode(String targetName, Object in, String propertyPrefix) throws Exception {
        StringBuilder out = new StringBuilder();
        out.append("[");

        int length = Array.getLength(in);
        for (int i = 0; i < length; i++) {
            Object value = Array.get(in, i);

            if (getExcludedProperties().contains(propertyPrefix + '[' + i + ']')) {
                // It's being excluded but we should leave a placeholder in the array
                out.append("null");
            } else if (isScalarType(value)) {
                out.append(getScalarAsString(value));
            } else {
                out.append("null");
                buildNode(targetName + "[" + i + "]", value, propertyPrefix + "[" + i + "]");
            }

            if (i != length - 1) {
                out.append(", ");
            }
        }

        out.append("]");
        this.objectValues.put(targetName, out.toString());
    }

    /**
     * Builds an object node that is of type collection. Simply converts the
     * collection to an array, and delegates to buildArrayNode().
     */
    void buildCollectionNode(String targetName, Collection<?> in, String propertyPrefix) throws Exception {
        buildArrayNode(targetName, in.toArray(), propertyPrefix);
    }

    /**
     * Fetches the value of a scalar type as a String. The input to this method
     * may not be null, and must be a of a type that will return true when
     * supplied to isScalarType().
     */
    public String getScalarAsString(Object in) {
        if (in == null) {
            return "null";
        }

        Class<? extends Object> type = in.getClass();

        if (String.class.isAssignableFrom(type)) {
            return quote((String) in);
        } else if (Character.class.isAssignableFrom(type)) {
            return quote(((Character) in).toString());
        } else if (Date.class.isAssignableFrom(type)) {
            return "new Date(" + ((Date) in).getTime() + ")";
        } else {
            return in.toString();
        }
    }
}
