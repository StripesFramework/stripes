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
