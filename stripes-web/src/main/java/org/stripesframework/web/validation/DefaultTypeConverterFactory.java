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
package org.stripesframework.web.validation;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.stripesframework.web.config.Configuration;
import org.stripesframework.web.util.Log;
import org.stripesframework.web.util.TypeHandlerCache;


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
   private TypeHandlerCache<Class<? extends TypeConverter<?>>> _cache;

   /** Stores a reference to the Configuration passed in at initialization time. */
   private Configuration _configuration;

   /**
    * Adds a TypeConverter to the set of registered TypeConverters, overriding an existing
    * converter if one was registered for the type.
    *
    * @param targetType the type for which the converter will handle conversions
    * @param converterClass the implementation class that will handle the conversions
    */
   @Override
   public void add( Class<?> targetType, Class<? extends TypeConverter<?>> converterClass ) {
      _cache.add(targetType, converterClass);
   }

   /**
    * Gets an instance of the TypeConverter class specified.
    *
    * @param clazz the TypeConverter type that is desired
    * @return an instance of the TypeConverter specified
    * @throws Exception if there is a problem instantiating the TypeConverter
    */
   @Override
   @SuppressWarnings("unchecked")
   public TypeConverter getInstance( Class<? extends TypeConverter> clazz, Locale locale ) throws Exception {
      TypeConverter converter = getConfiguration().getObjectFactory().newInstance(clazz);
      converter.setLocale(locale);
      return converter;
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
   @Override
   @SuppressWarnings("unchecked")
   public TypeConverter getTypeConverter( Class forType, Locale locale ) throws Exception {
      Class<? extends TypeConverter<?>> converterClass = _cache.getHandler(forType);
      if ( converterClass != null ) {
         try {
            return getInstance(converterClass, locale);
         }
         catch ( Exception e ) {
            log.error(e, "Unable to instantiate type converter ", converterClass);
            return null;
         }
      } else {
         log.trace("Couldn't find a type converter for ", forType);
         return null;
      }
   }

   /**
    * Places all the known convertible types and type converters into an instance level Map.
    */
   @Override
   public void init( final Configuration configuration ) {
      _configuration = configuration;
      _cache = new TypeHandlerCache<>();
      _cache.setSearchHierarchy(false);

      _cache.add(Boolean.class, BooleanTypeConverter.class);
      _cache.add(Boolean.TYPE, BooleanTypeConverter.class);
      _cache.add(Byte.class, ByteTypeConverter.class);
      _cache.add(Byte.TYPE, ByteTypeConverter.class);
      _cache.add(Short.class, ShortTypeConverter.class);
      _cache.add(Short.TYPE, ShortTypeConverter.class);
      _cache.add(Integer.class, IntegerTypeConverter.class);
      _cache.add(Integer.TYPE, IntegerTypeConverter.class);
      _cache.add(Long.class, LongTypeConverter.class);
      _cache.add(Long.TYPE, LongTypeConverter.class);
      _cache.add(Float.class, FloatTypeConverter.class);
      _cache.add(Float.TYPE, FloatTypeConverter.class);
      _cache.add(Double.class, DoubleTypeConverter.class);
      _cache.add(Double.TYPE, DoubleTypeConverter.class);
      _cache.add(Date.class, DateTypeConverter.class);
      _cache.add(BigInteger.class, BigIntegerTypeConverter.class);
      _cache.add(BigDecimal.class, BigDecimalTypeConverter.class);
      _cache.add(Enum.class, EnumeratedTypeConverter.class);

      // Now some less useful, but still helpful converters
      _cache.add(String.class, StringTypeConverter.class);
      _cache.add(Object.class, ObjectTypeConverter.class);
      _cache.add(Character.class, CharacterTypeConverter.class);
      _cache.add(Character.TYPE, CharacterTypeConverter.class);
      _cache.add(LocalDate.class, LocalDateTypeConverter.class);
   }

   /** Provides subclasses with access to the configuration provided at initialization. */
   protected Configuration getConfiguration() {
      return _configuration;
   }

   /**
    * Gets the (rather confusing) Map of TypeConverter objects.  The Map uses the target class
    * as the key in the Map, and the Class object representing the TypeConverter as the value.
    *
    * @return the Map of TypeConverter classes
    */
   protected Map<Class<?>, Class<? extends TypeConverter<?>>> getTypeConverters() {
      return _cache.getHandlers();
   }
}
