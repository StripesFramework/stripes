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

    /** Thread local cache of formatter instances. */
    private ThreadLocal<Map<Class<? extends Formatter<?>>, Formatter<?>>> instanceCache = new ThreadLocal<Map<Class<? extends Formatter<?>>, Formatter<?>>>() {
        @Override
        protected Map<Class<? extends Formatter<?>>, Formatter<?>> initialValue() {
            return new HashMap<Class<? extends Formatter<?>>, Formatter<?>>();
        }
    };

    /** Thread local flag that, if true, causes the instance cache to be reset in each thread. */
    private ThreadLocal<Boolean> clearInstanceCache = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

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
        this.formatters.put(targetType, formatterClass);
        clearCache();
    }

    /** Clear the class and instance caches. This is called by {@link #add(Class, Class)}. */
    protected synchronized void clearCache() {
        log.debug("Clearing formatter cache");
        classCache.clear();
        clearInstanceCache = new ThreadLocal<Boolean>() {
            @Override
            protected Boolean initialValue() {
                return true;
            }
        };
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
            log.trace("Couldn't find a formatter for ", clazz);
            return null;
        }
    }

    /**
     * Search for a formatter class that best matches the requested class, first checking the
     * specified class, then all the interfaces it implements, then all its superclasses and the
     * interfaces they implement, and finally all the superclasses of the interfaces implemented by
     * {@code targetClass}.
     * 
     * @param targetClass the class of the object that needs to be formatted
     * @return the best applicable formatter
     */
    protected Class<? extends Formatter<?>> findFormatterClass(Class<?> targetClass) {
        Class<? extends Formatter<?>> formatterClass = findInSuperclasses(targetClass);
        if (formatterClass != null)
            return formatterClass;

        formatterClass = findInInterfaces(targetClass, targetClass.getInterfaces());
        if (formatterClass != null)
            return formatterClass;

        return cacheFormatterClass(targetClass, ObjectFormatter.class);
    }

    /**
     * Called first by {@link #findFormatterClass(Class)}. Search for a formatter class that best
     * matches the requested class, first checking the specified class, then all the interfaces it
     * implements. If no match is found, repeat the process for each superclass.
     * 
     * @param targetClass the class of the object that needs to be formatted
     * @return the first applicable formatter found or null if no match could be found
     */
    protected Class<? extends Formatter<?>> findInSuperclasses(Class<?> targetClass) {
        // Check for a known formatter for the class
        Class<? extends Formatter<?>> formatterClass;
        if ((formatterClass = formatters.get(targetClass)) != null)
            return formatterClass;
        else if ((formatterClass = classCache.get(targetClass)) != null)
            return formatterClass;

        // Check directly implemented interfaces
        for (Class<?> iface : targetClass.getInterfaces()) {
            if ((formatterClass = formatters.get(iface)) != null)
                return cacheFormatterClass(targetClass, formatterClass);
            else if ((formatterClass = classCache.get(iface)) != null)
                return cacheFormatterClass(targetClass, formatterClass);
        }

        // Check superclasses
        Class<?> parent = targetClass.getSuperclass();
        if (parent != null) {
            if ((formatterClass = findInSuperclasses(parent)) != null) {
                return cacheFormatterClass(targetClass, formatterClass);
            }
        }

        // Nothing found, so return null
        return null;
    }

    /**
     * Called second by {@link #findFormatterClass(Class)}, after
     * {@link #findInSuperclasses(Class)}. Search for a formatter class that best matches the
     * requested class by checking the superclasses of every interface implemented by
     * {@code targetClass}.
     * 
     * @param targetClass the class of the object that needs to be formatted
     * @param ifaces an array of interfaces to search
     * @return the first applicable formatter found or null if no match could be found
     */
    protected Class<? extends Formatter<?>> findInInterfaces(Class<?> targetClass,
            Class<?>... ifaces) {
        Class<? extends Formatter<?>> formatterClass = null;
        for (Class<?> iface : ifaces) {
            if ((formatterClass = formatters.get(iface)) != null) {
                return cacheFormatterClass(targetClass, formatterClass);
            }
            else if ((formatterClass = classCache.get(iface)) != null) {
                return cacheFormatterClass(targetClass, formatterClass);
            }
            else if ((formatterClass = findInInterfaces(targetClass, iface.getInterfaces())) != null) {
                return cacheFormatterClass(targetClass, formatterClass);
            }
        }

        // Nothing found, so return null
        return null;
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
        // If the reset flag is turned on, then clear the cache and turn the flag off
        if (clearInstanceCache.get()) {
            log.debug("Clearing formatter instance cache for thread ",
                    Thread.currentThread().getName());
            instanceCache.get().clear();
            clearInstanceCache.set(false);
        }

        // Look for an instance in the cache. If none is found then create one and cache it.
        Formatter<?> formatter = instanceCache.get().get(clazz);
        if (formatter == null) {
            formatter = clazz.newInstance();
            log.debug("Caching instance of formatter ", clazz);
            instanceCache.get().put(clazz, formatter);
        }
        formatter.setFormatType(formatType);
        formatter.setFormatPattern(formatPattern);
        formatter.setLocale(locale);
        formatter.init();
        return formatter;
    }
}
