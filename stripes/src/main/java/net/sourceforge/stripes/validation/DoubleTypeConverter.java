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
 * Basic TypeConverter that will convert from Strings to Numbers of type Double.
 *
 * @author Tim Fennell
 */
public class DoubleTypeConverter extends NumberTypeConverterSupport implements TypeConverter<Double> {

    /**
     * Converts the input to an object of type Double.
     */
    public Double convert(String input,
                          Class<? extends Double> targetType,
                          Collection<ValidationError> errors) {

        Number number = parse(input, errors);
        Double retval = null;
        if (errors.size() == 0) {
            retval = new Double(number.doubleValue());
        }

        return retval;
    }
}
