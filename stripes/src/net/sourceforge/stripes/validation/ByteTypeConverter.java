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
