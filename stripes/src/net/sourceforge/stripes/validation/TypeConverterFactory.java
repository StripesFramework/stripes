package net.sourceforge.stripes.validation;

import java.util.Map;
import java.util.HashMap;

/**
 *
 */
public class TypeConverterFactory {
    private static Map<Class, Class<? extends TypeConverter>> converters =
        new HashMap<Class, Class<? extends TypeConverter>>();

    static {
        converters.put(Byte.class, ByteTypeConverter.class);
        converters.put(Short.class, ShortTypeConverter.class);
        converters.put(Integer.class, IntegerTypeConverter.class);
        converters.put(Long.class, LongTypeConverter.class);
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
    public static TypeConverter getTypeConverter(Class forType) throws Exception {
        // First take a look in our map of Converters for one registered for this type.
        Class<TypeConverter> clazz = (Class<TypeConverter>) converters.get(forType);

        if (clazz != null) {
            return getInstance( clazz );
        }
        else if (forType.isEnum()) {
            // If we didn't find one, maybe this class is an enum?
            return getInstance(EnumeratedTypeConverter.class);
        }
        else {
            return null;
        }
    }

    /**
     * Gets an instance of the TypeConverter class specified.
     * @param clazz
     * @return an instance of the TypeConverter specified
     * @throws Exception
     */
    public static TypeConverter getInstance(Class<? extends TypeConverter> clazz)
    throws Exception {
        // TODO: add thread local caching of converter classes
        return clazz.newInstance();
    }
}
