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
import java.util.ArrayList;
import java.text.NumberFormat;
import java.math.BigDecimal;

/**
 * <p>A locale aware number converter that parses percentages. Consistent with other areas of
 * Java (and computing) values are divided by 100 before being returned.  For example "100%"
 * will return 1, "58%" will return 0.58 etc. The value returned is either a double, float,
 * or BigDecimal depending on the target type supplied (which is usually driven by the type
 * of the property being converted).</p>
 *
 * @author Tim Fennell
 */
public class PercentageTypeConverter extends NumberTypeConverterSupport
                                     implements TypeConverter<Number> {

    /** Returns a single percentage instance of NumberFormat. */
    @Override
    protected NumberFormat[] getNumberFormats() {
        return new NumberFormat[] { NumberFormat.getPercentInstance(getLocale()) };
    }

    /**
     * Converts the input to a subclass of Number based on the targetType provided. Uses
     * a NumberFormat Percentage instance to do the parsing, making sure that the number
     * is divided by 100 and any percent signs etc. are handled.
     */
    public Number convert(String input, Class<? extends Number> targetType, Collection<ValidationError> errors) {
        Number number = parse(input, errors);

        // Since NumberFormat's percentage instance is insistent that the % sign must be
        // present (how dumb), if there are errors, let's take a second shot at parsing
        if (errors.size() > 0) {
            Collection<ValidationError> errorsToo = new ArrayList<ValidationError>();
            number = parse(input + "%", errorsToo);

            // If we met with success, clear out the original errors
            if (errorsToo.size() == 0) {
                errors.clear();
            }
        }

        if (errors.size() == 0) {
            if (targetType.equals(Float.class) || targetType.equals(Float.TYPE)) {
                number = new Float(number.floatValue());
            }
            else if (targetType.equals(Double.class) || targetType.equals(Double.TYPE)) {
                number = new Double(number.doubleValue());
            }
            else if (targetType.equals(BigDecimal.class)) {
                number = new BigDecimal(number.doubleValue());
            }
            else {
                throw new IllegalArgumentException(
                        "PercentageTypeConverter only converts to float, double and BigDecimal. " +
                        "This is because the input number is always converted to a decimal value. " +
                         "E.g. 99% -> 0.99. Type specified was: " + targetType);
            }
        }

        return number;
    }
}
