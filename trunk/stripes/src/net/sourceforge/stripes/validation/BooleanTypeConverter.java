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
package net.sourceforge.stripes.validation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;

/**
 * Performs a fairly aggressive conversion of a String to a boolean. The String will be deemd to be
 * equivelant to true if it meets any of the following conditions:
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
    private static final Collection<String> truths = new HashSet();

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
            catch (NumberFormatException nfe) {/* Ingore the exception */ }
        }

        return retval;
    }
}
