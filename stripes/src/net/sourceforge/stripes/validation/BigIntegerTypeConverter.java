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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.text.NumberFormat;
import java.text.DecimalFormat;

/**
 * Type converter for converting localized strings into BigInteger numbers without
 * any loss of magnitude or precision. Relies on NumberFormat.getInstance() returning
 * a decimal format, and will warn if that is not the case, as it will likely lead to
 * loss of information.
 *
 * @author Tim Fennell
 * @since Stripes 1.1.2
 */
public class BigIntegerTypeConverter extends NumberTypeConverterSupport
        implements TypeConverter<BigInteger> {

    /**
     * Uses the parent implementation to fetch a number format, but then downcasts it
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
     * Implemented to parse a BigDecimal and then remove any fractional part and
     * return a BigInteger.
     */
    public BigInteger convert(String input,
                              Class<? extends BigInteger> targetType,
                              Collection<ValidationError> errors) {

        BigDecimal decimal = (BigDecimal) parse(input, errors);

        if (errors.size() == 0) {
            return decimal.toBigInteger();
        }
        else {
            return null;
        }
    }
}
