package net.sourceforge.stripes.validation;

import java.util.Collection;

/**
 *
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
