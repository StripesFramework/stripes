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

import java.math.BigDecimal;
import java.util.Collection;
import java.text.NumberFormat;
import java.text.DecimalFormat;

/**
 * Type converter for converting localized strings into BigDecimal numbers without
 * any loss of magnitude or precision. Relies on NumberFormat.getInstance() returning
 * a decimal format, and will warn if that is not the case, as it will likely lead to
 * loss of information.
 *
 * @author Tim Fennell
 * @since Stripes 1.1.2
 */
public class BigDecimalTypeConverter extends NumberTypeConverterSupport
                                     implements TypeConverter<BigDecimal> {

    /**
     * Uses the parent implementation to fetch number formats, but then downcasts it
     * to a decimal format and ensures that a BigDecimal is parsed instead of a Long or
     * Double.
     */
    protected NumberFormat[] getNumberFormats() {
        NumberFormat[] formats = super.getNumberFormats();
        for (NumberFormat format : formats) {
            ((DecimalFormat) format).setParseBigDecimal(true);
        }

        return formats;
    }

    /**
     * Implemented to parse a BigDecimal using the parse() support method.
     */
    public BigDecimal convert(String input,
                              Class<? extends BigDecimal> targetType,
                              Collection<ValidationError> errors) {

        return (BigDecimal) parse(input, errors);
    }
}
