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
import java.util.concurrent.ConcurrentHashMap;

import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.tag.EncryptedValue;
import net.sourceforge.stripes.util.Log;

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

    /** A rather generic-heavy Map that maps target type to Formatter. */
    private Map<Class<?>, Class<? extends Formatter<?>>> formatters = new ConcurrentHashMap<Class<?>, Class<? extends Formatter<?>>>();

    /** Cache of indirect formatter results. */
    private Map<Class<?>, Class<? extends Formatter<?>>> classCache = new ConcurrentHashMap<Class<?>, Class<? extends Formatter<?>>>();

    /** Stores a reference to the Configuration passed in at initialization time. */
    private Configuration configuration;

    /** Stores a reference to the configuration and configures the default formatters. */
    public void init(Configuration configuration) throws Exception {
        this.configuration = configuration;

        add(Date.class, DateFormatter.class);
        add(Number.class, NumberFormatter.class);
        add(Enum.class, EnumFormatter.class);
        add(EncryptedValue.class, EncryptedValueFormatter.class);
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
        return this.formatters;
    }

    /**
     * Adds a Formatter to the set of registered Formatters, overriding an existing
     * formatter if one was registered for the type.
     *
     * @param targetType the type for which the formatter will handle formatting
     * @param formatterClass the implementation class that will handle the formatting
     */
    public void add(Class<?> targetType, Class<? extends Formatter<?>> formatterClass) {
        if (classCache.size() > 0)
            clearCache();
        this.formatters.put(targetType, formatterClass);
    }

    /** Clear the class and instance caches. This is called by {@link #add(Class, Class)}. */
    protected void clearCache() {
        log.debug("Clearing formatter cache");
        classCache.clear();
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
        Class<? extends Formatter<?>> formatterClass = findFormatterClass(clazz);
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
            log.debug("Couldn't find a Formatter for ", clazz);
            return null;
        }
    }

    /**
     * Search for a formatter class that best matches the requested class, first checking the
     * specified class, then it's interfaces, then superclasses, and then the superclasses of its
     * interfaces.
     * 
     * @param targetClass the class of the object that needs to be formatted
     * @return the first applicable formatter found or null if no match could be found
     */
    protected Class<? extends Formatter<?>> findFormatterClass(Class<?> targetClass) {
        // Check for a known formatter for the class
        if (formatters.containsKey(targetClass))
            return formatters.get(targetClass);
        else if (classCache.containsKey(targetClass))
            return classCache.get(targetClass);

        // Check directly implemented interfaces
        for (Class<?> iface : targetClass.getInterfaces()) {
            if (formatters.containsKey(iface))
                return cacheFormatterClass(targetClass, formatters.get(iface));
            else if (classCache.containsKey(iface))
                return cacheFormatterClass(targetClass, classCache.get(iface));
        }

        // Check superclasses
        Class<?> parent = targetClass;
        while ((parent = parent.getSuperclass()) != null) {
            if (formatters.containsKey(parent))
                return cacheFormatterClass(targetClass, formatters.get(parent));
            else if (classCache.containsKey(parent))
                return cacheFormatterClass(targetClass, classCache.get(parent));
        }

        // Check superclasses of implemented interfaces
        for (Class<?> iface : targetClass.getInterfaces()) {
            for (Class<?> superiface : iface.getInterfaces()) {
                if (formatters.containsKey(superiface))
                    return cacheFormatterClass(targetClass, formatters.get(superiface));
                else if (classCache.containsKey(superiface))
                    return cacheFormatterClass(targetClass, classCache.get(superiface));
            }
        }

        // Nothing found, so cache null
        return cacheFormatterClass(targetClass, null);
    }

    /**
     * Add formatter class {@code formatterClass} for formatting objects of type {@code clazz}.
     * 
     * @param clazz the type of object being formatted
     * @param formatterClass the class of the formatter
     * @return the {@code targetType} parameter
     */
    protected Class<? extends Formatter<?>> cacheFormatterClass(Class<?> clazz,
            Class<? extends Formatter<?>> formatterClass) {
        log.debug("Caching Formatter for ", clazz, " => ", formatterClass);
        classCache.put(clazz, formatterClass);
        return formatterClass;
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
        // TODO: add thread local caching of formatter classes
        Formatter<?> formatter = clazz.newInstance();
        formatter.setFormatType(formatType);
        formatter.setFormatPattern(formatPattern);
        formatter.setLocale(locale);
        formatter.init();
        return formatter;
    }
}
