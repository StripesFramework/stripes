package net.sourceforge.stripes.validation;

import java.util.Collection;

/**
 * Basic type converter for converting strings to short integers.
 *
 * @author Tim Fennell
 */
public class ShortTypeConverter extends NumberTypeConverterSupport implements TypeConverter<Short> {
    /**
     *
     * @param input
     * @param errors
     * @return Integer an Integer object if one can be parsed from the input
     */
    public Short convert(String input,
                         Class<? extends Short> targetType,
                         Collection<ValidationError> errors) {

        Number number = parse(input, errors);
        Short retval = null;

        if (errors.size() == 0) {
            long output = number.longValue();

            if (output < Short.MIN_VALUE || output > Short.MAX_VALUE) {
                errors.add( new ScopedLocalizableError("converter.short", "outOfRange",
                                                       Short.MIN_VALUE, Short.MAX_VALUE) );
            }
            else {
                retval = new Short((short) output);
            }
        }

        return retval;
    }
}
