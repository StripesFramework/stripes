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
 * Enum representing the type of a node in a property expression. The primary purpose of
 * this enumeration is to provide a way to determine which property accessor to use when
 * attempting to get or set a particular node in an expression.
 *
 * @author Tim Fennell
 * @since Stripes 1.4
 */
public enum NodeType {
    /** Represents a regular JavaBean property accessed via getter/setter, or Field access. */
    BeanProperty(new JavaBeanPropertyAccessor()),
    /** Represents an entry in a class which implements the java.util.Map interface. */
        MapEntry(new MapPropertyAccessor()     ),
    /** Represents an entry in a class which implements the java.utilList interface. */
       ListEntry(new ListPropertyAccessor()    ),
    /** Represents an item stored in a Java array. */
      ArrayEntry(new ArrayPropertyAccessor()   );

    private PropertyAccessor propertyAccessor;

    /**
     * Private constructor which allows each enum to specify the type of property accessor
     * that should be used with nodes of this type.
     *
     * @param accessor an instance of a PropertyAccessor applicable for this node type
     */
    private NodeType(PropertyAccessor accessor) {
        this.propertyAccessor = accessor;
    }

    /**
     * Returns the PropertyAccessor instance that should be used to access properties
     * of this type.
     * @return an instance of PropertyAccessor
     */
    public PropertyAccessor getPropertyAccessor() {
        return propertyAccessor;
    }
}

