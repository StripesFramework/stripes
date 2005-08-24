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

/**
 * Provides simple utility methods for dealing with HTML.
 *
 * @author Tim Fennell
 */
public class HtmlUtil {

    /**
     * Replaces special HTML characters from the set {@literal [<, >, ", ', &]} with their HTML
     * escape codes.  Note that because the escape codes are multi-character that the returned
     * String could be longer than the one passed in.
     *
     * @param fragment a String fragment that might have HTML special characters in it
     * @return the fragment with special characters escaped
     */
    public static String encode(String fragment) {
        StringBuilder builder = new StringBuilder(fragment.length() + 10); // a little wiggle room
        char[] characters = fragment.toCharArray();

        for (int i=0; i<characters.length; ++i) {
            switch (characters[i]) {
                case '<'  : builder.append("&lt;"); break;
                case '>'  : builder.append("&gt;"); break;
                case '"'  : builder.append("&quot;"); break;
                case '\'' : builder.append("&apos;"); break;
                case '&'  : builder.append("&amp;"); break;
                default: builder.append(characters[i]);
            }
        }

        return builder.toString();
    }
}
