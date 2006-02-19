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
