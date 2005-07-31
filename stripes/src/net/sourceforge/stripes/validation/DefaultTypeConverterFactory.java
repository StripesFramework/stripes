package net.sourceforge.stripes.validation;

import net.sourceforge.stripes.config.Configuration;

import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.Locale;

/**
 * Default TypeConverterFactory implementation that simply creates an instance level map of all the
 * TypeConverters included in the Stripes distribution, and their applicable classes.  Can handle
 * all the primitive and wrapper types as well as the rich types for which type converters exist.
 *
 * @author Tim Fennell
 */
public class DefaultTypeConverterFactory implements TypeConverterFactory {
    /** A rather generic-heavy Map that maps target type to TypeConverter. */
    private Map<Class, Class<? extends TypeConverter>> converters =
        new HashMap<Class, Class<? extends TypeConverter>>();

    /** Stores a reference to the Configuration passed in at initialization time. */
    protected Configuration configuration;

    /**
     * Places all the known convertible types and type converters into an instance level Map.
     */
    public void init(Configuration configuration) {
        converters.put(Boolean.class, BooleanTypeConverter.class);
        converters.put(Boolean.TYPE,  BooleanTypeConverter.class);
        converters.put(Byte.class,    ByteTypeConverter.class);
        converters.put(Byte.TYPE,     ByteTypeConverter.class);
        converters.put(Short.class,   ShortTypeConverter.class);
        converters.put(Short.TYPE,    ShortTypeConverter.class);
        converters.put(Integer.class, IntegerTypeConverter.class);
        converters.put(Integer.TYPE,  IntegerTypeConverter.class);
        converters.put(Long.class,    LongTypeConverter.class);
        converters.put(Long.TYPE,     LongTypeConverter.class);
        converters.put(Float.class,   FloatTypeConverter.class);
        converters.put(Float.TYPE,    FloatTypeConverter.class);
        converters.put(Double.class,  DoubleTypeConverter.class);
        converters.put(Double.TYPE,   DoubleTypeConverter.class);
        converters.put(Date.class,    DateTypeConverter.class);
    }

    /**
     * Gets the applicable type converter for the class passed in.  This is based on the default
     * set of type converters which are stored in a Map on this class.  Enums are a special case,
     * whereby if there is no converter in the map, the EnumeratedTypeConverter will be returned.
     *
     * @param forType the type/Class that is the target type of the conversion.  It is assumed that
     *        the input type is String, so to convert a String to a Date object you would supply
     *        java.util.Date.class.
     * @return an instance of a TypeConverter which will convert Strings to the desired type
     * @throws Exception if the TypeConverter cannot be instantiated
     */
    public TypeConverter getTypeConverter(Class forType, Locale locale) throws Exception {
        // First take a look in our map of Converters for one registered for this type.
        Class<? extends TypeConverter> clazz = this.converters.get(forType);

        if (clazz != null) {
            return getInstance(clazz, locale );
        }
        else if (forType.isEnum()) {
            // If we didn't find one, maybe this class is an enum?
            return getInstance(EnumeratedTypeConverter.class, locale);
        }
        else {
            return null;
        }
    }

    /**
     * Gets an instance of the TypeConverter class specified.
     *
     * @param clazz the TypeConverter type that is desired
     * @return an instance of the TypeConverter specified
     * @throws Exception if there is a problem instantiating the TypeConverter
     */
    public TypeConverter getInstance(Class<? extends TypeConverter> clazz, Locale locale)
    throws Exception {
        // TODO: add thread local caching of converter classes
        TypeConverter converter = clazz.newInstance();
        converter.setLocale(locale);
        return converter;
    }
}
