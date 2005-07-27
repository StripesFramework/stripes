package net.sourceforge.stripes.validation;

import java.util.Collection;

/**
 * Basic type converter for converting strings to integers.
 *
 * @author Tim Fennell
 */
public class LongTypeConverter extends NumberTypeConverterSupport implements TypeConverter<Long> {
    /**
     *
     * @param input
     * @param errors
     * @return Integer an Integer object if one can be parsed from the input
     */
    public Long convert(String input,
                        Class<? extends Long> targetType,
                        Collection<ValidationError> errors) {

        Number number = parse(input, errors);
        Long retval = null;
        if (errors.size() == 0) {
            retval = new Long(number.longValue());
        }

        return retval;
    }
}
