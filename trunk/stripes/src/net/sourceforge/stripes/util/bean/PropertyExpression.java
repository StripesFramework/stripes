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

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>An expression representing a property, nested property or indexed property of a JavaBean, or
 * a combination of all three. Capable of parsing String property expressions into a series of
 * {@link Node}s representing each sub-property or indexed property.  Expression nodes can be
 * separated with periods, or square-bracket indexing.  Items inside square brackets can be
 * single or double quoted, or bare int/long/float/double/boolean literals in the same manner they
 * appear in Java source code (e.g. 123.6F for a float).</p>
 *
 * @author Tim Fennell
 * @since Stripes 1.4
 */
public class PropertyExpression {
    // Patterns used to identify the type of Nodes in expressions
    private static final Pattern REGEX_INTEGER = Pattern.compile("^-?\\d+$");
    private static final Pattern REGEX_LONG    = Pattern.compile("^(-?\\d+)L$", Pattern.CASE_INSENSITIVE);
    private static final Pattern REGEX_DOUBLE  = Pattern.compile("^-?\\d+\\.\\d+$");
    private static final Pattern REGEX_FLOAT   = Pattern.compile("^(-?\\d+\\.?\\d+)F$", Pattern.CASE_INSENSITIVE);
    private static final Pattern REGEX_BOOLEAN = Pattern.compile("^(true|false)$", Pattern.CASE_INSENSITIVE);

    /** The set of characters which can terminate an expression node in one way or another. */
    private static final String TERMINATOR_CHARS = ".[]";

    /** A static cache of parse expressions. */
    private static Map<String,PropertyExpression> expressions = new ConcurrentHashMap<String,PropertyExpression>();

    /** The original property string, or 'source' of the expression. */
    private String source;

    private Node root;
    private Node leaf;

    /** Constructs a new expression by parsing the supplied String. */
    private PropertyExpression(String expression) throws ParseException {
        this.source = expression;
        parse(expression);
    }

    /**
     * Fetches the root or first node in this expression.  In an expression like 'foo.bar' this
     * would return the node that contains 'foo'.
     * @return the first node in the expression
     */
    public Node getRootNode() {
        return this.root;
    }

    /**
     * Fetches the original 'source' of the expression - the String value that was parsed
     * to create the PropertyExpression object.
     * @return the String form of the expression that was parsed
     */
    public String getSource() {
        return source;
    }

    /**
     * Factory method for retrieving PropertyExpression objects for expression strings.
     *
     * @param expression the expression to fetch a PropertyExpression for
     * @return PropertyExpression the parsed form of the expression passed in
     */
    public static PropertyExpression getExpression(String expression) throws ParseException {
        PropertyExpression parsed = PropertyExpression.expressions.get(expression);
        if (parsed == null) {
            parsed = new PropertyExpression(expression);
            PropertyExpression.expressions.put(expression, parsed);
        }

        return parsed;
    }

    /**
     * Performs the internal parsing of the expression and stores the results in a chain
     * of nodes internally. Passes through the String a character at a time looking for
     * transitions between nodes and invalid states.
     *
     * @param expression the String expression to be parsed
     */
    protected void parse(String expression) throws ParseException {
        char[] chars = expression.toCharArray();
        StringBuilder builder = new StringBuilder();
        boolean inSingleQuotedString = false;
        boolean inDoubleQuotedString = false;
        boolean inSquareBrackets = false;
        boolean escapedChar = false;

        for (int i=0; i<chars.length; ++i) {
            char ch = chars[i];

            // If the previous char was an escape char, accept the next char no questions asked
            if (escapedChar) {
                builder.append(ch);
                escapedChar = false;
            }
            // If it's the escape char, record it and skip to the next char
            else if (ch == '\\') { escapedChar = true; }
            // Deal with single quotes
            else if (!inSingleQuotedString && ch == '\'') { inSingleQuotedString = true; }
            else if (inSingleQuotedString && ch == '\'') {
                inSingleQuotedString = false;
                // assert that we're at the end of the expression, or the next char is a [ or .
                if (i != chars.length -1 && TERMINATOR_CHARS.indexOf(chars[i+1]) == -1) {
                    throw new ParseException("A quoted String must be terminated by a matching " +
                            "quote followed by either the end of the expression, a period or a " +
                            "square bracket character.", expression);
                }
                else {
                    String value = builder.toString();
                    addNode(value, value.length() == 1 ? value.charAt(0) :  value);
                    builder = new StringBuilder();
                }
            }
            else if (inSingleQuotedString) { builder.append(ch); }
            // Deal with Double quotes
            else if (!inDoubleQuotedString && ch == '"') { inDoubleQuotedString = true; }
            else if (inDoubleQuotedString && ch == '"') {
                inDoubleQuotedString = false;
                // assert that we're at the end of the expression, or the next char is a [ or .
                if (i != chars.length -1 && TERMINATOR_CHARS.indexOf(chars[i+1]) == -1) {
                    throw new ParseException("A quoted String must be terminated by a matching " +
                            "quote followed by either the end of the expression, a period or a " +
                            "square bracket character.", expression);
                }
                else {
                    String value = builder.toString();
                    addNode(value, value);
                    builder = new StringBuilder();
                }
            }
            else if (inDoubleQuotedString) { builder.append(ch); }
            // Deal with square brackets
            else if (!inSquareBrackets && ch == '[') {
                if (builder.length() > 0) {
                    addNode(builder.toString(), null);
                    builder = new StringBuilder();
                }
                inSquareBrackets = true;
            }
            else if (inSquareBrackets) {
                // Using the nested IF allows us to consume periods in unquoted strings of digits
                if (ch == ']') {
                    inSquareBrackets = false;
                    if (builder.length() > 0) {
                        addNode(builder.toString(), null);
                        builder = new StringBuilder();
                    }
                }
                else {
                    builder.append(ch);
                }
            }
            // If it's a bare period, it's the end of the current node
            else if (ch == '.') {
                if (builder.length() < 1) {
                    // Ignore pseudo-zero-length nodes
                }
                else {
                    addNode(builder.toString(), null);
                    builder = new StringBuilder();
                }
            }
            else {
                builder.append(ch);
            }

            // If it's the last char and we have stuff in buffer, close out the last node
            if (i == chars.length - 1) {
                if (inSingleQuotedString) {
                    throw new ParseException(expression,
                                             "Expression appears to terminate inside of single quoted string.");
                }
                else if (inDoubleQuotedString) {
                    throw new ParseException(expression,
                                             "Expression appears to terminate inside of double quoted string.");
                }
                else if (inSquareBrackets) {
                    throw new ParseException(expression,
                                             "Expression appears to terminate inside of square bracketed sub-expression.");
                }
                else if (builder.length() > 0) {
                    addNode(builder.toString(), null);
                }
            }
        }
    }

    /**
     * Constructs a node and links it in to other nodes within the current expression.
     * @param nodeValue the String part of the expression that the node represents
     * @param typedValue a strongly typed value for the nodeValue if one is indicated by
     *        the expression String, otherwise null to automatically determine
     */
    private void addNode(String nodeValue, Object typedValue) {
        // Determine the primitive/wrapper type of the node
        if (typedValue != null) {
            // skip ahead
        }
        else if (REGEX_INTEGER.matcher(nodeValue).matches()) {
            typedValue = Integer.parseInt(nodeValue);
        }
        else if (REGEX_DOUBLE.matcher(nodeValue).matches()) {
            typedValue = Double.parseDouble(nodeValue);
        }
        else if (REGEX_LONG.matcher(nodeValue).matches()) {
            Matcher matcher = REGEX_LONG.matcher(nodeValue);
            matcher.matches();
            typedValue = Long.parseLong(matcher.group(1));
        }
        else if (REGEX_FLOAT.matcher(nodeValue).matches()) {
            Matcher matcher = REGEX_FLOAT.matcher(nodeValue);
            matcher.find();
            typedValue = Float.parseFloat(matcher.group(1));
        }
        else if (REGEX_BOOLEAN.matcher(nodeValue).matches()) {
            typedValue = Boolean.parseBoolean(nodeValue);
        }
        else {
            typedValue = nodeValue;
        }

        Node node = new Node(nodeValue, typedValue);

        // Attach the node at the appropriate point in the expression
        if (this.root == null) {
            this.root = this.leaf = node;
        }
        else {
            node.setPrevious(this.leaf);
            this.leaf.setNext(node);
            this.leaf = node;
        }
    }

    /** Returns the String expression that was parsed to create this PropertyExpression. */
    @Override
    public String toString() {
        return this.source;
    }
}
