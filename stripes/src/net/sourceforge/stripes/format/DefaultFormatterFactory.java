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

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.util.Log;

/**
 * Very simple default implementation of a formatter factory that is aware of how to format
 * dates, numbers and enums.
 *
 * @author Tim Fennell
 */
public class DefaultFormatterFactory implements FormatterFactory {
    /** A rather generic-heavy Map that maps target type to Formatter. */
    private Map<Class, Class<? extends Formatter>> formatters =
        new HashMap<Class, Class<? extends Formatter>>();

    /** Stores a reference to the Configuration passed in at initialization time. */
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
     * Gets the (rather confusing) Map of Formatter objects.  The Map uses the target class
     * as the key in the Map, and the Class object representing the Formatter as the value.
     *
     * @return the Map of Formatter classes
     */
    protected Map<Class,Class<? extends Formatter>> getFormatters() {
        return this.formatters;
    }

    /**
     * Adds a Formatter to the set of registered Formatters, overriding an existing
     * formatter if one was registered for the type.
     *
     * @param targetType the type for which the formatter will handle formatting
     * @param formatterClass the implementation class that will handle the formatting
     */
    protected void add(Class targetType, Class<? extends Formatter> formatterClass) {
        this.formatters.put(targetType, formatterClass);
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
        if (formatters.containsKey(clazz)) {
            Class<? extends Formatter> formatterClass = formatters.get(clazz);
            try {
                return getInstance(formatterClass, formatType, formatPattern, locale);
            }
            catch (Exception e) {
                Log.getInstance(getClass()).error(e, "Unable to instantiate Formatter ", formatterClass);
                return null;
            }
        }
        else if (Date.class.isAssignableFrom(clazz)) {
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

    /**
     * Gets an instance of the Formatter class specified.
     *
     * @param clazz the Formatter type that is desired
     * @return an instance of the Formatter specified
     * @throws Exception if there is a problem instantiating the Formatter
     */
    public Formatter getInstance(Class<? extends Formatter> clazz,
            String formatType, String formatPattern, Locale locale)
            throws Exception {
        // TODO: add thread local caching of formatter classes
        Formatter formatter = clazz.newInstance();
        formatter.setFormatType(formatType);
        formatter.setFormatPattern(formatPattern);
        formatter.setLocale(locale);
        formatter.init();
        return formatter;
    }
}
