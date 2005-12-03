/* Copyright (C) 2005 Tim Fennell
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the license with this software. If not,
 * it can be found online at http://www.fsf.org/licensing/licenses/lgpl.html
 */
package net.sourceforge.stripes.util;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Utility class that makes it easy to construct Collection literals, and also provides
 * a nicer syntax for creating array literals.
 *
 * @author Tim Fennell
 * @since Stripes 1.1.2
 */
public class Literal {

    /** Returns an array containing all the elements supplied. */
    public static <T> T[] array(T... elements) { return elements; }

    /** Returns an array containing all the elements supplied. */
    public static boolean[] array(boolean... elements) { return elements; }

    /** Returns an array containing all the elements supplied. */
    public static byte[] array(byte... elements) { return elements; }

    /** Returns an array containing all the elements supplied. */
    public static char[] array(char... elements) { return elements; }

    /** Returns an array containing all the elements supplied. */
    public static short[] array(short... elements) { return elements; }

    /** Returns an array containing all the elements supplied. */
    public static int[] array(int... elements) { return elements; }

    /** Returns an array containing all the elements supplied. */
    public static long[] array(long... elements) { return elements; }

    /** Returns an array containing all the elements supplied. */
    public static float[] array(float... elements) { return elements; }

    /** Returns an array containing all the elements supplied. */
    public static double[] array(double... elements) { return elements; }

    /** Returns a new List instance containing the supplied elements. */
    public static <T> List<T> list(T... elements) {
        List<T> list = new ArrayList<T>();
        Collections.addAll(list, elements);
        return list;
    }

    /** Returns a new Set instance containing the supplied elements. */
    public static <T> Set<T> set(T... elements) {
        Set<T> set = new HashSet<T>();
        Collections.addAll(set, elements);
        return set;
    }

    /** Returns a new SortedSet instance containing the supplied elements. */
    public static <T extends Comparable> SortedSet<T> sortedSet(T... elements) {
        SortedSet<T> set = new TreeSet<T>();
        Collections.addAll(set, elements);
        return set;
    }
}