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
}
