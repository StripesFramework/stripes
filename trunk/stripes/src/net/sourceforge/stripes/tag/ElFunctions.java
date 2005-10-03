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
package net.sourceforge.stripes.tag;

/**
 * A collection of static functions that are included in the Stripes tag library.  In most
 * cases these are not functions that are specific to Stripes, but simply functions that
 * make doing web development (especially with Java 5 constructs) easier.
 *
 * @author Tim Fennell
 * @since Stripes 1.1
 */
public class ElFunctions {

    /** Gets the name of the supplied enumerated value. */
    public static String name(Enum e) {
        return e.name();
    }
}
