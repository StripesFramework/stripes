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
package net.sourceforge.stripes.format;

import net.sourceforge.stripes.config.Configuration;

import java.util.Date;
import java.util.Locale;

/**
 * Very simple default implementation of a formatter factory that is aware of how to format
 * dates, numbers and enums.
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
        else if (Enum.class.isAssignableFrom(clazz)) {
            formatter = new EnumFormatter();
            formatter.init();
            return formatter;
        }
        else {
            return null;
        }
    }
}
