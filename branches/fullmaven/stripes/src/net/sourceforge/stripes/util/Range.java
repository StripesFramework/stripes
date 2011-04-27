/* Copyright 2010 Ward van Wanrooij
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
 * Utility class for working with ranges, ranging from start to end (both inclusive).
 * 
 * @author Ward van Wanrooij
 * @since Stripes 1.6
 */
public class Range<T extends Comparable<T>> implements Comparable<Range<T>> {
    private T start, end;

    /**
     * Constructor for range from start to end (both inclusive). Start and end may not be null.
     * 
     * @param start Start of the range
     * @param end End of the range
     */
    public Range(T start, T end) {
        setStart(start);
        setEnd(end);
    }

    /**
     * Retrieves start of the range.
     * 
     * @return Start of the range
     */
    public T getStart() {
        return start;
    }

    /**
     * Sets start of the range. Start may not be null.
     * 
     * @param start Start of the range
     */
    public void setStart(T start) {
        if (start == null)
            throw new NullPointerException();
        this.start = start;
    }

    /**
     * Retrieves end of the range.
     * 
     * @return End of the range
     */
    public T getEnd() {
        return end;
    }

    /**
     * Sets end of the range. End may not be null.
     * 
     * @param end End of the range
     */
    public void setEnd(T end) {
        if (end == null)
            throw new NullPointerException();
        this.end = end;
    }

    /**
     * Checks whether an item is contained in this range.
     * 
     * @param item Item to check
     * @return True if item is in range
     */
    public boolean contains(T item) {
        return (start.compareTo(item) <= 0) && (end.compareTo(item) >= 0);
    }

    public int compareTo(Range<T> o) {
        int res;

        if ((res = start.compareTo(o.getStart())) == 0)
            res = end.compareTo(o.getEnd());
        return res;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object o) {
        return (o instanceof Range) && ((this == o) || (compareTo((Range<T>) o) == 0));
    }

    @Override
    public int hashCode() {
        return start.hashCode() ^ end.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getName() + " { type: " + start.getClass().getName() + ", start: "
                + start.toString() + ", end: " + end.toString() + " }";
    }
}
