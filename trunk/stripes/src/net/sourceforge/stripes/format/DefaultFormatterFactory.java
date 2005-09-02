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

import net.sourceforge.stripes.config.Configuration;

import java.util.Date;
import java.util.Locale;

/**
 * Very simple default implementation of a formatter factory that is aware of how to format
 * dates and numbers.
 *
 * @author Tim Fennell
 */
public class DefaultFormatterFactory implements FormatterFactory {
    private Configuration configuration;

    /** Stores a reference to the configuration and returns. */
    public void init(Configuration configuration) throws Exception {
        this.configuration = configuration;
    }

    /** Allows subclasses to access the stored configuration if needed. */
    protected Configuration getConfiguration() {
        return this.configuration;
    }

    /**
     * Does a simple check to see if the clazz specified is equal to or a subclass of
     * java.util.Date or java.lang.Number, and if so, creates a formatter instance. Otherwise
     * returns null.
     *
     * @param clazz the type of object being formatted
     * @param locale the Locale into which the object should be formatted
     * @param formatType the type of output to produce (e.g. date, time etc.)
     * @param formatPattern a named format string, or a format pattern
     * @return Formatter an instance of a Formatter, or null
     */
    public Formatter getFormatter(Class clazz, Locale locale, String formatType, String formatPattern) {
        Formatter formatter = null;

        // Figure out if we have a type we can format
        if (Date.class.isAssignableFrom(clazz)) {
            formatter = new DateFormatter();
            formatter.setFormatType(formatType);
            formatter.setFormatPattern(formatPattern);
            formatter.setLocale(locale);
            formatter.init();
            return formatter;
        }
        else if (Number.class.isAssignableFrom(clazz)) {
            formatter = new NumberFormatter();
            formatter.setFormatType(formatType);
            formatter.setFormatPattern(formatPattern);
            formatter.setLocale(locale);
            formatter.init();
            return formatter;
        }
        else {
            return null;
        }
    }
}
