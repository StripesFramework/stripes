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
 * Interface which is implemented by classes capable of acting as accessors for certain
 * types of classes. Currently this is tied very tightly to the {@link NodeType} enum. Each
 * PropertyAccessor must be able to store and retrieve values from the target type.
 *
 * @author Tim Fennell
 * @since Stripes 1.4
 */
public interface PropertyAccessor<T> {
    /**
     * Gets the value specified by the NodeEvaluation in the target object.
     *
     * @param evaluation provides access to the name and type of the desired property
     *        as well as to prior and subsequent nodes in the expression
     * @param target the target object from which the value is to be retrieved
     * @return the value as determined by the accessor, may be null
     */
    public Object getValue(NodeEvaluation evaluation, T target);

    /**
     * Gets the value specified by the NodeEvaluation in the target object.
     *
     * @param evaluation provides access to the name and type of the desired property
     *        as well as to prior and subsequent nodes in the expression
     * @param target the target object in/on to which the value is to be stored
     * @param value the value to be set, may be null
     */
    public void setValue(NodeEvaluation evaluation, T target, Object value);
}
