/* Copyright 2009 Ben Gunter
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
package net.sourceforge.stripes.util;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sourceforge.stripes.validation.TypeConverter;
import net.sourceforge.stripes.validation.TypeConverterFactory;

/**
 * Provides an efficient way to map "handler" classes to other classes, while taking into
 * consideration the target type's implemented interfaces and superclasses. For example,
 * {@link TypeConverterFactory} uses this class to map an implementation of {@link TypeConverter} to
 * classes.
 * 
 * @author Ben Gunter
 */
public class TypeHandlerCache<T> {
    private static final Log log = Log.getInstance(TypeHandlerCache.class);

    /** A direct map of target types to handlers. */
    private Map<Class<?>, Class<? extends T>> handlers = new ConcurrentHashMap<Class<?>, Class<? extends T>>();

    /**
     * Cache of indirect type handler results, determined by examining a target type's implemented
     * interfaces and superclasses.
     */
    private Map<Class<?>, Class<? extends T>> indirectCache = new ConcurrentHashMap<Class<?>, Class<? extends T>>();

    /**
     * Gets the (rather confusing) Map of handler classes. The Map uses the target class as the key
     * in the Map, and the Class object representing the handler as the value.
     * 
     * @return the Map of classes to their handlers
     */
    public Map<Class<?>, Class<? extends T>> getHandlers() {
        return handlers;
    }

    /**
     * Adds a handler to the set of registered handlers, overriding an existing handler if one was
     * already registered for the target type. Calls {@link #clearIndirectCache()} because a new
     * direct mapping can affect the indirect search results.
     * 
     * @param targetType the type for which the handler will handle conversions
     * @param handlerClass the implementation class that will handle the conversions
     */
    public void add(Class<?> targetType, Class<? extends T> handlerClass) {
        handlers.put(targetType, handlerClass);
        clearIndirectCache();
    }

    /**
     * Gets the applicable type handler for the class passed in.
     * 
     * @param forType The target type
     * @return The handler class associated with the target type, or null if none is found.
     */
    public Class<? extends T> getHandlerClass(Class<?> forType) {
        Class<? extends T> handlerClass = findHandlerClass(forType);
        if (handlerClass != null) {
            return handlerClass;
        }
        else {
            log.trace("Couldn't find a type handler for ", forType);
            return null;
        }
    }

    /**
     * Search for a type handler class that best matches the requested class.
     * 
     * @param targetType The target type
     * @return The first applicable type handler found or null if no match could be found
     */
    protected Class<? extends T> findHandlerClass(Class<?> targetType) {
        if (handlers.containsKey(targetType))
            return handlers.get(targetType);
        else if (indirectCache.containsKey(targetType))
            return indirectCache.get(targetType);
        else if (targetType.isEnum()) {
            Class<? extends T> handlerClass = findHandlerClass(Enum.class);
            if (handlerClass != null)
                return cacheHandlerClass(targetType, handlerClass);
        }
        else {
            for (Annotation annotation : targetType.getAnnotations()) {
                Class<? extends Annotation> annotationType = annotation.annotationType();
                if (handlers.containsKey(annotationType))
                    return cacheHandlerClass(targetType, handlers.get(annotationType));
            }
        }

        return null;
    }

    /**
     * Add handler class {@code handlerClass} for converting objects of type {@code clazz}.
     * 
     * @param clazz The type of object being converted
     * @param handlerClass The class of the handler
     * @return The {@code targetType} parameter
     */
    protected Class<? extends T> cacheHandlerClass(Class<?> clazz, Class<? extends T> handlerClass) {
        log.debug("Caching type handler for ", clazz, " => ", handlerClass);
        indirectCache.put(clazz, handlerClass);
        return handlerClass;
    }

    /** Clear the indirect cache. This is called by {@link #add(Class, Class)}. */
    public void clearIndirectCache() {
        log.debug("Clearing indirect type handler cache");
        indirectCache.clear();
    }
}
