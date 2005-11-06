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
package net.sourceforge.stripes.format;

import net.sourceforge.stripes.config.ConfigurableComponent;

import java.util.Locale;

/**
 * Interface for creating instances of formatter classes that are capable of formatting
 * the types specified into Strings.
 *
 * @see Formatter
 * @author Tim Fennell
 */
public interface FormatterFactory extends ConfigurableComponent {

    /**
     * Returns a configured formatter that meets the criteria specified.  The formatter is ready
     * for use as soon as it is returned from this method.
     *
     * @param clazz the type of object being formatted
     * @param locale the Locale into which the object should be formatted
     * @param formatType the manner in which the object should be formatted (allows nulls)
     * @param formatPattern the named format, or format pattern to be applied (allows nulls)
     * @return Formatter an instance of a Formatter, or null if no Formatter is available for
     *         the type specified
     */
    Formatter getFormatter(Class clazz, Locale locale, String formatType, String formatPattern);
}
