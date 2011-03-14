/* Copyright 2007 Aaron Porter
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
package net.sourceforge.stripes.format;

import java.util.Locale;

/**
 * This is the default formatter. It simply calls String.valueOf() on the
 * object being formatted.
 *
 * @author Aaron Porter
 * @since Stripes 1.5
 */
public class ObjectFormatter implements Formatter<Object> {

    /**
     * Converts the supplied parameter to a string using String.valueOf().
     * 
     * @param input an object of a type that the formatter knows how to format
     * @return String.valueOf(input)
     */
    public String format(Object input) {
        return String.valueOf(input);
    }

    /** Does nothing. */
    public void init() { /* unused */ }
    /** Does nothing. */
    public void setFormatPattern(String formatPattern) { /* unused */ }
    /** Does nothing. */
    public void setFormatType(String formatType) { /* unused */ }
    /** Does nothing. */
    public void setLocale(Locale locale) { /* unused */ }
}
