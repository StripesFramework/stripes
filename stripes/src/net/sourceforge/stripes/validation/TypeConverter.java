package net.sourceforge.stripes.validation;

import java.util.Collection;

/**
 * Interface for all type converters in the validation system that provide facilities for
 * converting from String to a specific object type.
 *
 * @author Tim Fennell
 */
public interface TypeConverter<T> {
    /**
     * Convert a String to the target type supported by this converter.
     *
     * @param input the String being converted
     * @param targetType the Class representing the type of the property to which the return
     *        value of the conversion will be assigned.  In many cases this can be ignored as
     *        converters will return a single type more often than not.
     * @param errors an empty collection of validation errors that should be populated by the
     *        converter for any errors that occur during validation that are user input related.
     * @return T an instance of the converted type
     */
    T convert(String input, Class<? extends T> targetType, Collection<ValidationError> errors);
}
