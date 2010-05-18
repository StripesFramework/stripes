package net.sourceforge.stripes.extensions;

import java.util.Collection;

import net.sourceforge.stripes.validation.IntegerTypeConverter;
import net.sourceforge.stripes.validation.ValidationError;

/**
 * Converts the input string to an Integer and doubles the value. This type converter extends the
 * stock IntegerTypeConverter.
 *
 * @author Freddy Daoud
 */
public class MyIntegerTypeConverter extends IntegerTypeConverter {
    @Override
    public Integer convert(String input, Class<? extends Integer> targetType,
        Collection<ValidationError> errors)
    {
        Integer result = super.convert(input, targetType, errors);

        if (result != null) {
            result = new Integer(result.intValue() * 2);
        }
        return result;
    }
}
