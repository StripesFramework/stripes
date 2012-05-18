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

import java.lang.reflect.Array;

/**
 * Implementation of the {@link PropertyAccessor} interface for interacting with arrays.
 * Throws exceptions if the index is not an integer or if the index is out of range
 * for the current array.
 *
 * @author Tim Fennell
 * @since Stripes 1.4
 */
public class ArrayPropertyAccessor implements PropertyAccessor<Object>{
    /**
     * Gets the index specified by the current node from the target array.
     * @param evaluation the current NodeEvaluation containing the array index
     * @param target the target array
     * @return the element stored at that index
     */
    public Object getValue(NodeEvaluation evaluation, Object target) {
        int index = getKey(evaluation);
        return Array.get(target, index);
    }

    /**
     * Sets the value at the index specified by the current node to the supplied value.
     * @param evaluation the current NodeEvaluation containing the array index
     * @param target the target array
     * @param value the value to set, possibly null
     */
    public void setValue(NodeEvaluation evaluation, Object target, Object value) {
        int index = getKey(evaluation);
        Array.set(target, index, value);
    }

    /**
     * Fetches the key and casts/unboxes it to an int. If the key is not an int, it
     * will throw an Evaluation Exception.
     */
    private int getKey(NodeEvaluation eval) {
        Object key = eval.getNode().getTypedValue();
        if (key.getClass() != Integer.class) {
            throw new EvaluationException
                    ("Attempting to index into an array using a non-integer index: " + key);
        }

        return (Integer) key;
    }
}
