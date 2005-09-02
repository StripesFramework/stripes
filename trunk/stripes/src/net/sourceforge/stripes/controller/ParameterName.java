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
package net.sourceforge.stripes.controller;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Encapsulates the name of a parameter in the HttpServletRequest. Detects whether or
 * not the name refers to an indexed or mapped property.
 *
 * @author Tim Fennell
 */
public class ParameterName {
    /** Stores the regular expression that will remove all [] segments. */
    public static final Pattern pattern = Pattern.compile("\\[.*\\]");

    /** Stores the name passed in at construction time. */
    private String name;

    /** Stores the name with all indexing and mapping stripped out of it. */
    private String strippedName;

    /** True if the name has indexing or mapping in it. */
    private boolean indexed;

    /**
     * Constructs a ParameterName for a given name from the HttpServletRequest. As it is
     * constructed, detects whether or not the name contains indexing or mapping components,
     * and if it does, also creates and stores the stripped name.
     *
     * @param name a name that may or may not contain indexing or mapping
     */
    public ParameterName(String name) {
        this.name = name;
        Matcher matcher = pattern.matcher(this.name);
        this.indexed = matcher.find();

        if (this.indexed) {
            this.strippedName = matcher.replaceAll("");
        }
        else {
            this.strippedName = this.name;
        }
    }

    /** Returns true if the name has indexing or mapping components, otherwise false. */
    public boolean isIndexed() {
        return this.indexed;
    }

    /**
     * Always returns the parameter name as passed in to the constructor. If it contained
     * indexing or mapping components (e.g. [3] or (foo)) they will be present in the
     * String returned.
     *
     * @return String the name as supplied in the request
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the name with all indexing and mapping components stripped.  E.g. if the name
     * in the request was 'foo[1].bar', this method will return 'foo.bar'.
     *
     * @return String the name minus indexing and mapping
     */
    public String getStrippedName() {
        return this.strippedName;
    }

    /**
     * Checks for equality as efficiently as possible.  First checks for JVM equality to
     * see if we can short circuit, and then checks for equality of the name attribute for
     * a real test.
     */
    public boolean equals(Object obj) {
        return (obj instanceof ParameterName) &&
                (this == obj || this.name.equals(((ParameterName) obj).name) );
    }

    /**
     * Uses the original name as the string representation of the class. This is probably
     * the most useful thing to see in log messages, which is the only place toString will
     * be used.
     */
    public String toString() {
        return this.name;
    }
}
