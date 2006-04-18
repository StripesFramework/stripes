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
