package net.sourceforge.stripes.validation;

import java.util.Collection;

/**
 *
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
