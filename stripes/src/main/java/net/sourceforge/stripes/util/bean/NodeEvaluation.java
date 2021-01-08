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

import java.lang.reflect.Type;


/**
 * Represents a Node in an expression which has been evaluated against a specific bean in
 * order to determine additional type information etc.  NodeEvaluation is to {@link Node}, what
 * {@link PropertyExpressionEvaluation} is to {@link PropertyExpression}.
 *
 * @author Tim Fennell
 * @since Stripes 1.4
 */
public class NodeEvaluation {

   private Node                         _node;
   private NodeType                     _type;
   private Type                         _valueType;
   private Class<?>                     _keyType;
   private PropertyExpressionEvaluation _expressionEvaluation;
   private NodeEvaluation               _next;
   private NodeEvaluation               _previous;

   /**
    * Constructs a new NodeEvaluation for the specified part of an expression evaluation.
    * @param expressionEvaluation the parent expression evaluation
    * @param node the node that this evaluation is a mirror for
    */
   public NodeEvaluation( PropertyExpressionEvaluation expressionEvaluation, Node node ) {
       _expressionEvaluation = expressionEvaluation;
       _node = node;
   }

   /** Gets the PropertyExpressionEvaluation that this NodeEvaluation is a part of. */
   public PropertyExpressionEvaluation getExpressionEvaluation() { return _expressionEvaluation; }

   /**
    * Gets the class object which represents the key type determined during evaluation
    * of the expression against the provided bean. Only usually relevant for Map entries,
    * when it stores the Class specified by the generic signature of the Map.
    *
    * @return a Class object or null
    */
   public Class<?> getKeyType() { return _keyType; }

   /** Gets the next NodeEvaluation in the chain, or null if this is the terminal node. */
   public NodeEvaluation getNext() { return _next; }

   /** Gets the Node that is represented in the evaluation by this NodeEvaluation. */
   public Node getNode() { return _node; }

   /** Gets the previous NodeEvaluation in the chain, or null if this is the first node. */
   public NodeEvaluation getPrevious() { return _previous; }

   /** Gets the type of the node (bean property, list item etc.). */
   public NodeType getType() { return _type; }

   /** Gets the Type object which represents the type returned by evaluating up to this node. */
   public Type getValueType() { return _valueType; }

   /** Sets the PropertyExpressionEvaluation that this NodeEvaluation is a part of. */
   public void setExpressionEvaluation( PropertyExpressionEvaluation expressionEvaluation ) {
       _expressionEvaluation = expressionEvaluation;
   }

   /** Gets the class object which represents the key type for this node if applicable. */
   public void setKeyType( Class<?> keyType ) { _keyType = keyType; }

   /** Sets the next NodeEvaluation in the chain. */
   public void setNext( NodeEvaluation next ) { _next = next; }

   /** Sets the Node that is represented in the evaluation by this NodeEvaluation. */
   public void setNode( Node node ) { _node = node; }

   /** Sets the previous NodeEvaluation in the chain. */
   public void setPrevious( NodeEvaluation previous ) { _previous = previous; }

   /** Gets the type of the node (bean property, list item etc.). */
   public void setType( NodeType type ) { _type = type; }

   /** Sets the Type object which represents the type returned by evaluating up to this node. */
   public void setValueType( Type valueType ) { _valueType = valueType; }
}
