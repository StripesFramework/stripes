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
package net.sourceforge.stripes.util;

import java.lang.reflect.Array;

/**
 * Utility methods for working with Collections and Arrays.
 *
 * @author Tim Fennell
 * @since Stripes 1.4
 */
public class CollectionUtil {
    /**
     * Checks to see if an array contains an item. Works on unsorted arrays. If the array is
     * null this method will always return false.  If the item is null, will return true if the
     * array contains a null entry, false otherwise.  In all other cases, item.equals() is used
     * to determine equality.
     *
     * @param arr the array to scan for the item.
     * @param item the item to be looked for
     * @return true if item is contained in the array, false otherwise
     */
    public static boolean contains(Object[] arr, Object item) {
        if (arr == null) return false;

        for (int i=0; i<arr.length; ++i) {
            if (item == null && arr[i] == null) return true;
            if (item != null && item.equals(arr[i])) return true;
        }

        return false;
    }

    /**
     * Checks to see if the array contains any values that are non-null non empty-string values.
     * If it does, returns false.  Returns true for null arrays and zero length arrays, as well
     * as for arrays consisting only of nulls and empty strings.
     */
    public static boolean empty(String[] arr) {
        if (arr == null || arr.length == 0) return true;
        for (String s : arr) {
            if (s != null && !"".equals(s)) return false;
        }

        return true;
    }

    /**
     * Converts an Object reference that is known to be an array into an Object[]. If the array
     * is assignable to Object[], the array passed in is simply cast and returned. Otherwise a
     * new Object[] of equal size is constructed and the elements are wrapped and inserted into
     * the new array before being returned.
     *
     * @param in an array of Objects or primitives
     * @return an Object[], either the array passed in, or in the case of primitives, a new
     *         Object[] containing a wrapper for each element in the input array
     * @throws IllegalArgumentException thrown if the in parameter is null or not an array
     */
    public static Object[] asObjectArray(Object in) {
        if (in == null || !in.getClass().isArray()) {
            throw new IllegalArgumentException("Parameter to asObjectArray must be a non-null array.");
        }
        else if (in instanceof Object[]) {
            return (Object[]) in;
        }
        else {
            int length = Array.getLength(in);
            Object[] out = new Object[length];
            for (int i=0; i<length; ++i) {
                out[i] = Array.get(in, i);
            }

            return out;
        }
    }
}
