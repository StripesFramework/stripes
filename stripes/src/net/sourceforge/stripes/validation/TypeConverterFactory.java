package net.sourceforge.stripes.validation;

import net.sourceforge.stripes.config.ConfigurableComponent;

/**
 * Provides access to a set of TypeConverters for converting Strings to various types.
 * Implementations may use any mechanism desired to map a type to a TypeConverter, and may
 * optionally choose to cache TypeConverter instances.  The behaviour of the type conversion
 * lookups in Stripes can be altered either by directly implementing this interface, or by
 * subclassing DefaultTypeConverterFactory.
 *
 * @author Tim Fennell
 */
public interface TypeConverterFactory extends ConfigurableComponent {
    /**
     * Gets the applicable type converter for the class passed in.  The TypeConverter retuned must
     * create objects of the type supplied, or possibly a suitable derived type.
     *
     * @param forType the type/Class that is the target type of the conversion.  It is assumed that
     *        the input type is String, so to convert a String to a Date object you would supply
     *        java.util.Date.class.
     * @return an instance of a TypeConverter which will convert Strings to the desired type
     * @throws Exception if the TypeConverter cannot be instantiated
     */
    TypeConverter getTypeConverter(Class forType) throws Exception;

    /**
     * Gets an instance of the TypeConverter class specified.
     *
     * @param clazz the Class object representing the desired TypeConverter
     * @return an instance of the TypeConverter specified
     * @throws Exception if the TypeConverter cannot be instantiated
     */
    TypeConverter getInstance(Class<? extends TypeConverter> clazz) throws Exception;
}
