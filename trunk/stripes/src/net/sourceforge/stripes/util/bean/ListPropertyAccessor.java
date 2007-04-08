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

import java.util.List;

/**
 * Implementation of {@link PropertyAccessor} for interacting with Lists. Automatically
 * expands the list to make the supplied list index valid for set operations, and supresses
 * IndexOutOfBoundsExceptions during get operations.
 * 
 * @author Tim Fennell
 * @since Stripes 1.4
 */
public class ListPropertyAccessor implements PropertyAccessor<List> {
    /**
     * Fetches the value stored at the index specified by the current node. If the index is
     * out of bounds, will return null.
     *
     * @param evaluation the current node evaluation
     * @param list the target list
     * @return the corresponding item in the list, or null if the list is not long enough
     */
    public Object getValue(NodeEvaluation evaluation, List list) {
        int index = getKey(evaluation);
        try { return list.get(index); }
        catch (IndexOutOfBoundsException ioobe) { return null; }
    }

    /**
     * Sets the value at the index specified by the current node to the supplied value. If
     * the list is not long enough it is expanded (filling with nulls) until it is large
     * enough to accomodate the supplied index.
     *
     * @param evaluation the current node evaluation
     * @param list the target list
     * @param value the value to be stored at the specified index
     */
    @SuppressWarnings("unchecked")
	public void setValue(NodeEvaluation evaluation, List list, Object value) {
        int index = getKey(evaluation);
        for (int i=list.size(); i<=index; ++i) {
            list.add(null);
        }

        list.set(index, value);
    }

    /**
     * Fetches the key and casts/unboxes it to an int. If the key is not an int, it
     * will throw an Evaluation Exception.
     */
    private int getKey(NodeEvaluation eval) {
        Object key = eval.getNode().getTypedValue();
        if (key.getClass() != Integer.class) {
            throw new EvaluationException
                    ("Attempting to index into a List using a non-integer index: " + key);
        }

        return (Integer) key;
    }


}
