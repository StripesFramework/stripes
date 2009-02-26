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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.util.TypeHandlerCache;

/**
 * Default TypeConverterFactory implementation that simply creates an instance level map of all the
 * TypeConverters included in the Stripes distribution, and their applicable classes.  Can handle
 * all the primitive and wrapper types as well as the rich types for which type converters exist.
 *
 * @author Tim Fennell
 */
public class DefaultTypeConverterFactory implements TypeConverterFactory {
    private static final Log log = Log.getInstance(DefaultTypeConverterFactory.class);

    /** Caches {@link TypeConverter} to {@link Class} mappings. */
    private TypeHandlerCache<Class<? extends TypeConverter<?>>> cache;

    /** Stores a reference to the Configuration passed in at initialization time. */
    private Configuration configuration;

    /**
     * Places all the known convertible types and type converters into an instance level Map.
     */
    public void init(final Configuration configuration) {
        this.configuration = configuration;
        this.cache = new TypeHandlerCache<Class<? extends TypeConverter<?>>>();
        this.cache.setSearchHierarchy(false);

        cache.add(Boolean.class,    BooleanTypeConverter.class);
        cache.add(Boolean.TYPE,     BooleanTypeConverter.class);
        cache.add(Byte.class,       ByteTypeConverter.class);
        cache.add(Byte.TYPE,        ByteTypeConverter.class);
        cache.add(Short.class,      ShortTypeConverter.class);
        cache.add(Short.TYPE,       ShortTypeConverter.class);
        cache.add(Integer.class,    IntegerTypeConverter.class);
        cache.add(Integer.TYPE,     IntegerTypeConverter.class);
        cache.add(Long.class,       LongTypeConverter.class);
        cache.add(Long.TYPE,        LongTypeConverter.class);
        cache.add(Float.class,      FloatTypeConverter.class);
        cache.add(Float.TYPE,       FloatTypeConverter.class);
        cache.add(Double.class,     DoubleTypeConverter.class);
        cache.add(Double.TYPE,      DoubleTypeConverter.class);
        cache.add(Date.class,       DateTypeConverter.class);
        cache.add(BigInteger.class, BigIntegerTypeConverter.class);
        cache.add(BigDecimal.class, BigDecimalTypeConverter.class);
        cache.add(Enum.class,       EnumeratedTypeConverter.class);

        // Now some less useful, but still helpful converters
        cache.add(String.class,     StringTypeConverter.class);
        cache.add(Object.class,     ObjectTypeConverter.class);
        cache.add(Character.class,  CharacterTypeConverter.class);
        cache.add(Character.TYPE,   CharacterTypeConverter.class);
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
        return this.cache.getHandlers();
    }

    /**
     * Adds a TypeConverter to the set of registered TypeConverters, overriding an existing
     * converter if one was registered for the type.
     *
     * @param targetType the type for which the converter will handle conversions
     * @param converterClass the implementation class that will handle the conversions
     */
    public void add(Class<?> targetType, Class<? extends TypeConverter<?>> converterClass) {
        cache.add(targetType, converterClass);
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
        Class<? extends TypeConverter<?>> converterClass = cache.getHandler(forType);
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
     * Gets an instance of the TypeConverter class specified.
     *
     * @param clazz the TypeConverter type that is desired
     * @return an instance of the TypeConverter specified
     * @throws Exception if there is a problem instantiating the TypeConverter
     */
    @SuppressWarnings("unchecked")
    public TypeConverter getInstance(Class<? extends TypeConverter> clazz, Locale locale) throws Exception {
        TypeConverter converter = getConfiguration().getObjectFactory().newInstance(clazz);
        converter.setLocale(locale);
        return converter;
    }
}
