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
