/* Copyright 2005-2006 Tim Fennell
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sourceforge.stripes.validation;

import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.util.Log;

import java.util.Map;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.math.BigInteger;
import java.math.BigDecimal;

/**
 * Default TypeConverterFactory implementation that simply creates an instance level map of all the
 * TypeConverters included in the Stripes distribution, and their applicable classes.  Can handle
 * all the primitive and wrapper types as well as the rich types for which type converters exist.
 *
 * @author Tim Fennell
 */
public class DefaultTypeConverterFactory implements TypeConverterFactory {
    private static final Log log = Log.getInstance(DefaultTypeConverterFactory.class);

    /** A rather generic-heavy Map that maps target type to TypeConverter. */
    private Map<Class<?>, Class<? extends TypeConverter<?>>> converters =
        new ConcurrentHashMap<Class<?>, Class<? extends TypeConverter<?>>>();

    /** Cache of indirect type converter results. */
    private Map<Class<?>, Class<? extends TypeConverter<?>>> classCache =
            new ConcurrentHashMap<Class<?>, Class<? extends TypeConverter<?>>>();

    /** Stores a reference to the Configuration passed in at initialization time. */
    private Configuration configuration;

    /**
     * Places all the known convertible types and type converters into an instance level Map.
     */
    public void init(final Configuration configuration) {
        this.configuration = configuration;

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
        converters.put(BigInteger.class, BigIntegerTypeConverter.class);
        converters.put(BigDecimal.class, BigDecimalTypeConverter.class);

        // Now some less useful, but still helpful converters
        converters.put(String.class, StringTypeConverter.class);
        converters.put(Object.class, ObjectTypeConverter.class);
        converters.put(Character.class, CharacterTypeConverter.class);
        converters.put(Character.TYPE, CharacterTypeConverter.class); 
    }

    /** Provides subclasses with access to the configuration provided at initialization. */
    protected Configuration getConfiguration() {
        return this.configuration;
    }

    /**
     * Gets the (rather confusing) Map of TypeConverter objects.  The Map uses the target class
     * as the key in the Map, and the Class object representing the TypeConverter as the value.
     *
     * @return the Map of TypeConverter classes
     */
    protected Map<Class<?>,Class<? extends TypeConverter<?>>> getTypeConverters() {
        return this.converters;
    }

    /**
     * Adds a TypeConverter to the set of registered TypeConverters, overriding an existing
     * converter if one was registered for the type.
     *
     * @param targetType the type for which the converter will handle conversions
     * @param converterClass the implementation class that will handle the conversions
     */
    public void add(Class<?> targetType, Class<? extends TypeConverter<?>> converterClass) {
        this.converters.put(targetType, converterClass);
        clearCache();
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
    @SuppressWarnings("unchecked")
	public TypeConverter getTypeConverter(Class forType, Locale locale) throws Exception {
        Class<? extends TypeConverter<?>> converterClass = findTypeConverterClass(forType);
        if (converterClass != null) {
            try {
                return getInstance(converterClass, locale);
            }
            catch (Exception e) {
                log.error(e, "Unable to instantiate type converter ", converterClass);
                return null;
            }
        }
        else {
            log.trace("Couldn't find a type converter for ", forType);
            return null;
        }
    }
    
    /**
     * Search for a type converter class that best matches the requested class.
     * 
     * @param targetClass the class of the object that needs to be converted
     * @return the first applicable type converter found or null if no match could be found
     */
    @SuppressWarnings("unchecked")
    protected Class<? extends TypeConverter<?>> findTypeConverterClass(Class<?> targetClass) {
        if (converters.containsKey(targetClass))
            return converters.get(targetClass);
        else if (classCache.containsKey(targetClass))
            return classCache.get(targetClass);
        else if (targetClass.isEnum())
            return cacheTypeConverterClass(targetClass, EnumeratedTypeConverter.class);
        else
            return cacheTypeConverterClass(targetClass, null);
    }

    /**
     * Add converter class {@code converterClass} for converting objects of type {@code clazz}.
     * 
     * @param clazz the type of object being converted
     * @param converterClass the class of the converter
     * @return the {@code targetType} parameter
     */
    protected Class<? extends TypeConverter<?>> cacheTypeConverterClass(Class<?> clazz,
            Class<? extends TypeConverter<?>> converterClass) {
        log.debug("Caching type converter for ", clazz, " => ", converterClass);
        classCache.put(clazz, converterClass);
        return converterClass;
    }

    /** Clear the instance cache. This is called by {@link #add(Class, Class)}. */
    protected void clearCache() {
        log.debug("Clearing type converter cache");
        classCache.clear();
    }

    /**
     * Gets an instance of the TypeConverter class specified.
     *
     * @param clazz the TypeConverter type that is desired
     * @return an instance of the TypeConverter specified
     * @throws Exception if there is a problem instantiating the TypeConverter
     */
    @SuppressWarnings("unchecked")
    public TypeConverter getInstance(Class<? extends TypeConverter> clazz, Locale locale) throws Exception {
        TypeConverter converter = clazz.newInstance();
        converter.setLocale(locale);
        return converter;
    }
}
