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
 * Basic type converter for converting strings to short integers.
 *
 * @author Tim Fennell
 */
public class ShortTypeConverter extends NumberTypeConverterSupport implements TypeConverter<Short> {
    /**
     *
     * @param input
     * @param errors
     * @return Integer an Integer object if one can be parsed from the input
     */
    public Short convert(String input,
                         Class<? extends Short> targetType,
                         Collection<ValidationError> errors) {

        Number number = parse(input, errors);
        Short retval = null;

        if (errors.size() == 0) {
            long output = number.longValue();

            if (output < Short.MIN_VALUE || output > Short.MAX_VALUE) {
                errors.add( new ScopedLocalizableError("converter.short", "outOfRange",
                                                       Short.MIN_VALUE, Short.MAX_VALUE) );
            }
            else {
                retval = new Short((short) output);
            }
        }

        return retval;
    }
}
