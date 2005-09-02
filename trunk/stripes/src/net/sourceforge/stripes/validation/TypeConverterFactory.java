/* Copyright (C) 2005 Tim Fennell
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the license with this software. If not,
 * it can be found online at http://www.fsf.org/licensing/licenses/lgpl.html
 */
package net.sourceforge.stripes.validation;

import net.sourceforge.stripes.config.ConfigurableComponent;

import java.util.Locale;

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
     * @param locale the locale of the Strings to be converted with the returned converter
     * @return an instance of a TypeConverter which will convert Strings to the desired type
     * @throws Exception if the TypeConverter cannot be instantiated
     */
    TypeConverter getTypeConverter(Class forType, Locale locale) throws Exception;

    /**
     * Gets an instance of the TypeConverter class specified.
     *
     * @param clazz the Class object representing the desired TypeConverter
     * @param locale the locale of the Strings to be converted with the returned converter
     * @return an instance of the TypeConverter specified
     * @throws Exception if the TypeConverter cannot be instantiated
     */
    TypeConverter getInstance(Class<? extends TypeConverter> clazz, Locale locale) throws Exception;
}
