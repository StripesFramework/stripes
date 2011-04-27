package net.sourceforge.stripes.extensions;

import java.util.Collection;
import java.util.Locale;

import net.sourceforge.stripes.validation.TypeConverter;
import net.sourceforge.stripes.validation.ValidationError;

/**
 * Converts the input string to upper case. This type converter does not extend a stock type
 * converter.
 *
 * @author Freddy Daoud
 */
public class MyStringTypeConverter implements TypeConverter<String> {
    public String convert(String input, Class<? extends String> targetType,
        Collection<ValidationError> errors)
    {
        return input.toUpperCase();
    }
    public void setLocale(Locale locale) { }
}
