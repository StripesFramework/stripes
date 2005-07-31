package net.sourceforge.stripes.validation;

import java.util.Collection;
import java.util.Locale;

/**
 * Converts the String form of an Enumerated type into the Enum value that it represents. If the
 * String does not represent one of the values in the Enum a validation error will be set.
 *
 * @author Tim Fennell
 */
public class EnumeratedTypeConverter implements TypeConverter<Enum> {

    /**
     * Does nothing at present due to the fact that enumerated types don't support localization
     * all that well. 
     */
    public void setLocale(Locale locale) {
        // Do nothing
    }

    public Enum convert(String input,
                        Class<? extends Enum> targetType,
                        Collection<ValidationError> errors) {

        try {
            return Enum.valueOf(targetType, input);
        }
        catch (IllegalArgumentException iae) {
            errors.add(new ScopedLocalizableError("converter.enum", "notAnEnumeratedValue"));
            return null;
        }
    }
}
