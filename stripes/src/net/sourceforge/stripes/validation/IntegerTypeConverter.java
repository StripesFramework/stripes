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

/**
 * Basic type converter for converting strings to integers.
 *
 * @author Tim Fennell
 */
public class IntegerTypeConverter extends NumberTypeConverterSupport implements TypeConverter<Integer> {
    /**
     *
     * @param input
     * @param errors
     * @return Integer an Integer object if one can be parsed from the input
     */
    public Integer convert(String input,
                           Class<? extends Integer> targetType,
                           Collection<ValidationError> errors) {

        Number number = parse(input, errors);
        Integer retval = null;

        if (errors.size() == 0) {
            long output = number.longValue();

            if (output < Integer.MIN_VALUE || output > Integer.MAX_VALUE) {
                errors.add( new ScopedLocalizableError("converter.integer", "outOfRange",
                                                       Integer.MIN_VALUE, Integer.MAX_VALUE) );
            }
            else {
                retval = new Integer((int) output);
            }
        }

        return retval;
    }
}
