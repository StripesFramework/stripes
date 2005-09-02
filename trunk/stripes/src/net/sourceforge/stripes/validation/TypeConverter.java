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

import java.util.Collection;
import java.util.Locale;

/**
 * Interface for all type converters in the validation system that provide facilities for
 * converting from String to a specific object type.
 *
 * @author Tim Fennell
 */
public interface TypeConverter<T> {

    /**
     * Sets the locale that the TypeConverter can expect incoming Strings to be in. This method will
     * only be called once during a TypeConverter's lifetime, and will be called prior to any
     * invocations of convert().
     *
     * @param locale the locale that the TypeConverter will be converting from.
     */
    void setLocale(Locale locale);

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
