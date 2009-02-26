/* Copyright 2009 Ben Gunter
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

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A set based on {@link ConcurrentHashMap}. The Javadoc for the constructors in this class were
 * copied from the Java 1.5 Javadoc for {@link ConcurrentHashMap} and changed to reflect that this
 * is a Set and not a Map. See the Javadoc for {@link ConcurrentHashMap} for information on
 * performance characteristics, etc.
 * 
 * @author Ben Gunter
 */
public class ConcurrentHashSet<T> implements Set<T> {
    /** The value object that will be put in the map since it does not accept null values. */
    private static final Object VALUE = new Object();

    /** The map that backs this set. */
    private ConcurrentMap<T, Object> map;

    /**
     * Creates a new, empty map with a default initial capacity, load factor, and concurrencyLevel.
     */
    public ConcurrentHashSet() {
        map = new ConcurrentHashMap<T, Object>();
    }

    /**
     * Creates a new, empty map with the specified initial capacity, and with default load factor
     * and concurrencyLevel.
     * 
     * @param initialCapacity the initial capacity. The implementation performs internal sizing to
     *            accommodate this many elements.
     * @throws IllegalArgumentException if the initial capacity of elements is negative.
     */
    public ConcurrentHashSet(int initialCapacity) {
        map = new ConcurrentHashMap<T, Object>(initialCapacity);
    }

    /**
     * Creates a new, empty map with the specified initial capacity, load factor, and concurrency
     * level.
     * 
     * @param initialCapacity the initial capacity. The implementation performs internal sizing to
     *            accommodate this many elements.
     * @param loadFactor the load factor threshold, used to control resizing. Resizing may be
     *            performed when the average number of elements per bin exceeds this threshold.
     * @param concurrencyLevel - the estimated number of concurrently updating threads. The
     *            implementation performs internal sizing to try to accommodate this many threads.
     * @throws IllegalArgumentException if the initial capacity is negative or the load factor or
     *             concurrencyLevel are nonpositive.
     */
    public ConcurrentHashSet(int initialCapacity, float loadFactor, int concurrencyLevel) {
        map = new ConcurrentHashMap<T, Object>(initialCapacity, loadFactor, concurrencyLevel);
    }

    /**
     * Creates a new set with the same elements as the given set. The set is created with a capacity
     * of twice the number of elements in the given set or 11 (whichever is greater), and a default
     * load factor and concurrencyLevel.
     * 
     * @param set The set
     */
    public ConcurrentHashSet(Set<? extends T> set) {
        this(Math.max(set.size() * 2, 11));
        addAll(set);
    }

    public boolean add(T e) {
        return map.putIfAbsent(e, VALUE) == null;
    }

    public boolean addAll(Collection<? extends T> c) {
        boolean b = false;
        for (T t : c) {
            b = b || map.putIfAbsent(t, VALUE) == null;
        }
        return b;
    }

    public void clear() {
        map.clear();
    }

    public boolean contains(Object o) {
        return map.keySet().contains(o);
    }

    public boolean containsAll(Collection<?> c) {
        return map.keySet().containsAll(c);
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public Iterator<T> iterator() {
        return map.keySet().iterator();
    }

    public boolean remove(Object o) {
        return map.remove(o) != null;
    }

    public boolean removeAll(Collection<?> c) {
        return map.keySet().removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return map.keySet().retainAll(c);
    }

    public int size() {
        return map.size();
    }

    public Object[] toArray() {
        return map.keySet().toArray();
    }

    public <E> E[] toArray(E[] a) {
        return map.keySet().toArray(a);
    }
}
