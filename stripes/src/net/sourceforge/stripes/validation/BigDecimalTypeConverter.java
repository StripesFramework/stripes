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
     * Uses the parent implementation to fetch a number format, but then downcasts it
     * to a decimal format and ensures that a BigDecimal is parsed instead of a Long or
     * Double.
     */
    protected NumberFormat getNumberFormat() {
        DecimalFormat format = (DecimalFormat) super.getNumberFormat();
        format.setParseBigDecimal(true);
        return format;
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
