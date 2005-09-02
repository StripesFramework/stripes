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
 * Basic TypeConverter that converts Strings to Numbers of type Float.  If the
 * String is a valid number, but the number is out of the range representable
 * by the Float class, a validation error of 'converter.float.outOfRange' will
 * be produced.
 *
 * @author Tim Fennell
 */
public class FloatTypeConverter extends NumberTypeConverterSupport implements TypeConverter<Float> {

    /**
     * Converts the input to an object of type Double.
     */
    public Float convert(String input,
                          Class<? extends Float> targetType,
                          Collection<ValidationError> errors) {

        Number number = parse(input, errors);
        Float retval = null;

        if (errors.size() == 0) {
            double output = number.doubleValue();
            if (output > Float.MAX_VALUE || output < Float.MIN_VALUE) {
                errors.add( new ScopedLocalizableError("converter.float", "outOfRange",
                                                       Float.MIN_VALUE, Float.MAX_VALUE));
            }
            else {
                retval = new Float(number.floatValue());
            }
        }

        return retval;
    }
}
