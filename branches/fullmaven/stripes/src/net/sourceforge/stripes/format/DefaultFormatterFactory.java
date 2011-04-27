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
import java.util.Locale;
import java.util.Map;

import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.util.TypeHandlerCache;

/**
 * Implementation of {@link FormatterFactory} that contains a set of built-in formatters. Additional
 * formatters can be registered by calling {@link #add(Class, Class)}. If there is no registered
 * formatter for a specific class, then it attempts to find the best available formatter by
 * searching for a match against the target implemented interfaces, class's superclasses, and
 * interface superclasses.
 * 
 * @author Tim Fennell
 */
public class DefaultFormatterFactory implements FormatterFactory {
    private static final Log log = Log.getInstance(DefaultFormatterFactory.class);

    /** Cache target type to Formatter class mappings. */
    private TypeHandlerCache<Class<? extends Formatter<?>>> cache;

    /** Stores a reference to the Configuration passed in at initialization time. */
    private Configuration configuration;

    /** Stores a reference to the configuration and configures the default formatters. */
    public void init(Configuration configuration) throws Exception {
        this.configuration = configuration;
        this.cache = new TypeHandlerCache<Class<? extends Formatter<?>>>();
        this.cache.setDefaultHandler(ObjectFormatter.class);

        add(Date.class, DateFormatter.class);
        add(Number.class, NumberFormatter.class);
        add(Enum.class, EnumFormatter.class);
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
    protected Map<Class<?>,Class<? extends Formatter<?>>> getFormatters() {
        return cache.getHandlers();
    }

    /**
     * Adds a Formatter to the set of registered Formatters, overriding an existing
     * formatter if one was registered for the type.
     *
     * @param targetType the type for which the formatter will handle formatting
     * @param formatterClass the implementation class that will handle the formatting
     */
    public void add(Class<?> targetType, Class<? extends Formatter<?>> formatterClass) {
        cache.add(targetType, formatterClass);
    }

    /**
     * Check to see if the there is a Formatter for the specified clazz. If a Formatter is found an
     * instance is created, configured and returned. Otherwise returns null.
     * 
     * @param clazz the type of object being formatted
     * @param locale the Locale into which the object should be formatted
     * @param formatType the type of output to produce (e.g. date, time etc.)
     * @param formatPattern a named format string, or a format pattern
     * @return Formatter an instance of a Formatter, or null
     */
    public Formatter<?> getFormatter(Class<?> clazz, Locale locale, String formatType, String formatPattern) {
        Class<? extends Formatter<?>> formatterClass = cache.getHandler(clazz);
        if (formatterClass != null) {
            try {
                return getInstance(formatterClass, formatType, formatPattern, locale);
            }
            catch (Exception e) {
                log.error(e, "Unable to instantiate Formatter ", formatterClass);
                return null;
            }
        }
        else {
            log.trace("Couldn't find a formatter for ", clazz);
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
    public Formatter<?> getInstance(Class<? extends Formatter<?>> clazz,
            String formatType, String formatPattern, Locale locale)
            throws Exception {

        Formatter<?> formatter = getConfiguration().getObjectFactory().newInstance(clazz);
        formatter.setFormatType(formatType);
        formatter.setFormatPattern(formatPattern);
        formatter.setLocale(locale);
        formatter.init();
        return formatter;
    }
}
