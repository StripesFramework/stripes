package net.sourceforge.stripes.validation;

import java.util.Collection;

/**
 * Converts the String form of an Enumerated type into the Enum value that it represents. If the
 * String does not represent one of the values in the Enum a validation error will be set.
 *
 * @author Tim Fennell
 */
public class EnumeratedTypeConverter implements TypeConverter<Enum> {
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
