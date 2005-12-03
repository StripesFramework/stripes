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
 * Basic type converter for converting strings to bytes.  Will produce one error if the String
 * supplied is not a parsable number, and another error if the number is parseable but outside of
 * the range Byte.MIN_VALUE =< X =< Byte.MAX_VALUE.
 *
 * @author Tim Fennell
 */
public class ByteTypeConverter extends NumberTypeConverterSupport implements TypeConverter<Byte> {

    /**
     * Converts a String to a Byte in accordance with the rules laid out in the class level javadoc.
     */
    public Byte convert(String input,
                        Class<? extends Byte> targetType,
                        Collection<ValidationError> errors) {

        Number number = parse(input, errors);
        Byte retval = null;

        if (errors.size() == 0) {
            long output = number.longValue();

            if (output < Byte.MIN_VALUE || output > Byte.MAX_VALUE) {
                errors.add( new ScopedLocalizableError("converter.byte", "outOfRange",
                                                       Byte.MIN_VALUE, Byte.MAX_VALUE) );
            }
            else {
                retval = new Byte((byte) output);
            }
        }

        return retval;
    }
}
