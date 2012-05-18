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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;

/**
 * Provides simple utility methods for dealing with HTML.
 *
 * @author Tim Fennell
 */
public class HtmlUtil {
    private static final String FIELD_DELIMITER_STRING = "||";
    private static final Pattern FIELD_DELIMITER_PATTERN = Pattern.compile("\\|\\|");

    /**
     * Replaces special HTML characters from the set {@literal [<, >, ", ', &]} with their HTML
     * escape codes.  Note that because the escape codes are multi-character that the returned
     * String could be longer than the one passed in.
     *
     * @param fragment a String fragment that might have HTML special characters in it
     * @return the fragment with special characters escaped
     */
    public static String encode(String fragment) {
        // If the input is null, then the output is null
        if (fragment == null) return null;

        StringBuilder builder = new StringBuilder(fragment.length() + 10); // a little wiggle room
        char[] characters = fragment.toCharArray();

        // This loop used to also look for and replace single ticks with &apos; but it
        // turns out that it's not strictly necessary since Stripes uses double-quotes
        // around all form fields, and stupid IE6 will render &apos; verbatim instead
        // of as a single quote.
        for (int i=0; i<characters.length; ++i) {
            switch (characters[i]) {
                case '<'  : builder.append("&lt;"); break;
                case '>'  : builder.append("&gt;"); break;
                case '"'  : builder.append("&quot;"); break;
                case '&'  : builder.append("&amp;"); break;
                default: builder.append(characters[i]);
            }
        }

        return builder.toString();
    }

    /**
     * One of a pair of methods (the other is splitValues) that is used to combine several
     * un-encoded values into a single delimited, encoded value for placement into a
     * hidden field.
     *
     * @param values One or more values which are to be combined
     * @return a single HTML-encoded String that contains all the values in such a way that
     *         they can be converted back into a Collection of Strings with splitValues().
     */
    public static String combineValues(Collection<String> values) {
        if (values == null || values.size() == 0) {
            return "";
        }
        else {
            StringBuilder builder = new StringBuilder(values.size() * 30);
            for (String value : values) {
                builder.append(value).append(FIELD_DELIMITER_STRING);
            }
            
            return encode(builder.toString());
        }
    }

    /**
     * Takes in a String produced by combineValues and returns a Collection of values that
     * contains the same values as originally supplied to combineValues.  Note that the order
     * or items in the collection (and indeed the type of Collection used) are not guaranteed
     * to be the same.
     *
     * @param value a String value produced by
     * @return a Collection of zero or more Strings
     */
    public static Collection<String> splitValues(String value) {
        if (value == null || value.length() == 0) {
            return Collections.emptyList();
        }
        else {
            String[] splits = FIELD_DELIMITER_PATTERN.split(value);
            return Arrays.asList(splits);
        }
    }
}
