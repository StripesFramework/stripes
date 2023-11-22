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

import java.util.Locale;
import net.sourceforge.stripes.config.ConfigurableComponent;

/**
 * Provides access to a set of TypeConverters for converting Strings to various types.
 * Implementations may use any mechanism desired to map a type to a TypeConverter, and may
 * optionally choose to cache TypeConverter instances. The behaviour of the type conversion lookups
 * in Stripes can be altered either by directly implementing this interface, or by subclassing
 * DefaultTypeConverterFactory.
 *
 * @author Tim Fennell
 */
public interface TypeConverterFactory extends ConfigurableComponent {
  /**
   * Gets the applicable type converter for the class passed in. The TypeConverter returned must
   * create objects of the type supplied, or possibly a suitable derived type.
   *
   * @param forType the type/Class that is the target type of the conversion. It is assumed that the
   *     input type is String, so to convert a String to a Date object you would supply
   *     java.util.Date.class.
   * @param locale the locale of the Strings to be converted with the returned converter
   * @return an instance of a TypeConverter which will convert Strings to the desired type
   * @throws Exception if the TypeConverter cannot be instantiated
   */
  @SuppressWarnings("unchecked")
  TypeConverter getTypeConverter(Class forType, Locale locale) throws Exception;

  /**
   * Gets an instance of the TypeConverter class specified.
   *
   * @param clazz the Class object representing the desired TypeConverter
   * @param locale the locale of the Strings to be converted with the returned converter
   * @return an instance of the TypeConverter specified
   * @throws Exception if the TypeConverter cannot be instantiated
   */
  @SuppressWarnings("unchecked")
  TypeConverter getInstance(Class<? extends TypeConverter> clazz, Locale locale) throws Exception;

  /**
   * Adds a type converter to the set of registered type converters, overriding an existing
   * converter if one was already registered for the type. This is an optional operation.
   * Implementations that do not support adding type converters at runtime must throw {@link
   * UnsupportedOperationException}.
   *
   * @param targetType the type for which the converter will handle conversions
   * @param converterClass the implementation class that will handle the conversions
   * @throws UnsupportedOperationException if the implementation does not support adding type
   *     converters at runtime
   */
  public void add(Class<?> targetType, Class<? extends TypeConverter<?>> converterClass);
}
