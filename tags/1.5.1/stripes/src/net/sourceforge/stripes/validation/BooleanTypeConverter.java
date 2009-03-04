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
package net.sourceforge.stripes.validation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;

/**
 * Performs a fairly aggressive conversion of a String to a boolean. The String will be deemed to be
 * equivalent to true if it meets any of the following conditions:
 *
 * <ul>
 *   <li>Equals, ignoring case, true</li>
 *   <li>Equals, ignoring case, t</li>
 *   <li>Equals, ignoring case, yes</li>
 *   <li>Equals, ignoring case, y</li>
 *   <li>Equals, ignoring case, on</li>
 *   <li>Is parseable as a number and yields a number greater than zero</li>
 * </ul>
 *
 * If none of the above conditions are met, the return value is false.  This type converter does
 * not produce any validation errors - it always returns either true or false.
 *
 * @author Tim Fennell
 */
public class BooleanTypeConverter implements TypeConverter<Boolean> {
    private static final Collection<String> truths = new HashSet<String>();

    static {
        truths.add("true");
        truths.add("t");
        truths.add("yes");
        truths.add("y");
        truths.add("on");
    }

    /**
     * Does nothing currently due to the fact that there is no localization support for
     * Booleans in Java.
     */
    public void setLocale(Locale locale) {
        // Do nothing
    }

    /**
     * Converts a String to a Boolean in accordance with the specification laid out in the
     * class level javadoc.
     */
    public Boolean convert(String input,
                           Class<? extends Boolean> targetType,
                           Collection<ValidationError> errors) {

        boolean retval = false;

        for (String truth : truths) {
            retval = retval || truth.equalsIgnoreCase(input);
        }

        if (retval == false) {
            try {
                long number = Long.parseLong(input);
                retval =  (number > 0);
            }
            catch (NumberFormatException nfe) {/* Ignore the exception */ }
        }

        return retval;
    }
}
