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
import java.util.List;
import java.util.LinkedList;

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
     * <p>Checks to see if an event is applicable given an array of event names. The array is
     * usually derived from the <tt>on</tt> attribute of one of the Stripes annotations
     * (e.g. {@link net.sourceforge.stripes.validation.ValidationMethod}). The array can
     * be composed of <i>positive</i> event names (e.g. {"foo", "bar"}) in which case the event
     * must be contained in the array, or negative event names (e.g. {"!splat", "!whee"}) in
     * which case the event must not be contained in the array.</p>
     *
     * <p>Calling this method with a null or zero length array will always return true.</p>
     *
     * @param events an array containing event names or event names prefixed with bangs
     * @param event the event name to check for applicability given the array
     * @return true if the array indicates the event is applicable, false otherwise
     */
    public static boolean applies(String events[], String event) {
        if (events == null || events.length == 0) return true;
        boolean isPositive = events[0].charAt(0) != '!';

        if (isPositive) return contains(events, event);
        else return !contains(events, "!" + event);
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

    /**
     * <p>Converts an Object reference that is known to be an array into a List. Semantically
     * very similar to {@link java.util.Arrays#asList(Object[])} except that this method
     * can deal with arrays of primitives in the same manner as arrays as objects.</p>
     *
     * <p>A new List is created of the same size as the array, and elements are copied from
     * the array into the List. If elements are primitives then they are converted to the
     * appropriate wrapper types in order to return a List.</p>
     *
     * @param in an array of Objects or primitives (null values are not allowed)
     * @return a List containing an element for each element in the input array
     * @throws IllegalArgumentException thrown if the in parameter is null or not an array
     */
    public static List<Object> asList(Object in) {
        if (in == null || !in.getClass().isArray()) {
            throw new IllegalArgumentException("Parameter to asObjectArray must be a non-null array.");
        }
        else {
            int length = Array.getLength(in);
            LinkedList<Object> list = new LinkedList<Object>();
            for (int i=0; i<length; ++i) {
                list.add(i, Array.get(in, i));
            }

            return list;
        }
    }

    /**
     * Converts an Iterable into a List that can be navigated in ways other than simple
     * iteration. If the underlying implementation of the Iterable is a List, it is cast
     * to List and returned. Othewise it is iterated and the items placed, in order,
     * into a new List.
     *
     * @param in an Iterable to serve as the source for a List
     * @return either the Iterable itself if it is a List, or a new List with the same elements
     */
    public static <T> List<T> asList(Iterable<T> in) {
        if (in instanceof List) return (List<T>) in;
        else {
            LinkedList<T> list = new LinkedList<T>();
            for (T item : in) {
                list.add(item);
            }

            return list;
        }
    }
}
