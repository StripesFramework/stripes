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

/**
 * Represents a single node in a {@link PropertyExpression}. Note that Nodes are static
 * and are tied to an expression, <b>not</b> an expression evaluation. Each node stores
 * the original String value of the node as well as a typed value which can also be a String
 * or one of the built in types such as Integer, Long, Boolean.
 *
 * @author Tim Fennell
 * @since Stripes 1.4
 */
public class Node {
    private String stringValue;
    private Object typedValue;
    private Node next;
    private Node previous;

    /** Constructs a new node with the String value and typed value provided. */
    public Node(String value, Object typedValue) {
        this.stringValue = value;
        this.typedValue = typedValue;
    }

    /**
     * Returns the Java type of this node in the expression.  Specifically this is the
     * type determined for the text in this node, not the type of the property/sub-property
     * represented by this node in an evaluation against a specific bean.
     */
    public Class getExpresssionNodeType() {
        return typedValue.getClass();
    }

    /**
     * Returns the original String value of this expression node.
     * @return the original String value
     */
    public String getStringValue() { return stringValue; }

    /**
     * Returns the typed value for this node as determined when parsing the expression
     * @return the typed value (may also be a String)
     */
    public Object getTypedValue() { return typedValue; }

    /** Gets the next node in the expression. Returns null if this is the terminal node. */
    public Node getNext() { return next; }

    /** Sets the next node in the expression. */
    protected void setNext(Node next) { this.next = next; }

    /** Gets the prevoius node in the expression. Returns null if this is the first node. */
    public Node getPrevious() { return previous; }

    /** Sets the previous node in the expression. */
    protected void setPrevious(Node previous) { this.previous = previous; }

}
