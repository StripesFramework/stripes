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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.sourceforge.stripes.controller.ObjectPostProcessor;
import net.sourceforge.stripes.format.Formatter;
import net.sourceforge.stripes.validation.TypeConverter;

/**
 * <p>
 * Provides an efficient way to map "handlers" to classes. There are two types of mappings: direct
 * and indirect. A direct mapping is one that is explicitly created by a call to
 * {@link #add(Class, Object)}. An indirect mapping is one that is discovered by examining a target
 * type's implemented interfaces and superclasses. If {@code searchHierarchy} is set to false, then
 * only direct mappings will be considered and the class hierarchy will not be searched.
 * </p>
 * <p>
 * For example, let's assume a direct mapping is created for type {@code A} to handler {@code H}. A
 * request for a handler for type {@code A} returns {@code H} due to the direct mapping. If {@code
 * searchHierarchy} is true and a handler is requested later for type {@code B}, which implements
 * {@code A}, then an indirect mapping will be created that maps the handler {@code H} to type
 * {@code B}. (If {@code A} were a superclass of {@code B}, it would behave likewise.) However, if
 * {@code searchHierarchy} is false then a request for a handler for type {@code B} would return
 * {@link #getDefaultHandler()}.
 * </p>
 * <p>
 * This class is used within Stripes to map {@link Formatter}s, {@link TypeConverter}s and
 * {@link ObjectPostProcessor}s to specific classes and interfaces.
 * </p>
 * 
 * @author Ben Gunter
 */
public class TypeHandlerCache<T> {
    private static final Log log = Log.getInstance(TypeHandlerCache.class);

    /** A direct map of target types to handlers. */
    private Map<Class<?>, T> handlers = new ConcurrentHashMap<Class<?>, T>();

    /**
     * Cache of indirect type handler results, determined by examining a target type's implemented
     * interfaces and superclasses.
     */
    private Map<Class<?>, T> indirectCache = new ConcurrentHashMap<Class<?>, T>();

    /**
     * Cache of classes that have been searched, yet no handler (besides the default one) could be
     * found for them.
     */
    private Set<Class<?>> negativeCache = new ConcurrentHashSet<Class<?>>();

    private T defaultHandler;
    private boolean searchHierarchy = true, searchAnnotations = true;

    /** Get the default handler to return if no handler is found for a requested target type. */
    public T getDefaultHandler() {
        return defaultHandler;
    }

    /** Set the default handler to return if no handler is found for a requested target type. */
    public void setDefaultHandler(T defaultHandler) {
        this.defaultHandler = defaultHandler;
    }

    /**
     * Indicates if the class hierarchy will be searched to find the best available handler in case
     * a direct mapping is not available for a given target type. Defaults to true.
     */
    public boolean isSearchHierarchy() {
        return searchHierarchy;
    }

    /**
     * Set the flag that enables or disables searching of the class hierarchy to find the best
     * available handler in case a direct mapping is not available for a given target type.
     * 
     * @param searchHierarchy True to enable hierarchy search; false to disable it.
     */
    public void setSearchHierarchy(boolean searchHierarchy) {
        this.searchHierarchy = searchHierarchy;
    }

    /**
     * Indicates if the target type's annotations will be examined to find a handler registered for
     * the annotation class. Defaults to true.
     */
    public boolean isSearchAnnotations() {
        return searchAnnotations;
    }

    /**
     * Set the flag that enables or disables searching for handlers for a target type's annotations.
     */
    public void setSearchAnnotations(boolean searchAnnotations) {
        this.searchAnnotations = searchAnnotations;
    }

    /**
     * Gets the (rather confusing) map of handlers. The map uses the target type as the key in the
     * map, and the handler as the value.
     * 
     * @return the map of classes to their handlers
     */
    public Map<Class<?>, T> getHandlers() {
        return handlers;
    }

    /**
     * Adds a handler to the set of registered handlers, overriding an existing handler if one was
     * already registered for the target type. Calls {@link #clearCache()} because a new direct
     * mapping can affect the indirect search results.
     * 
     * @param targetType The type for which a handler is requested.
     * @param handler The handler for the target type.
     */
    public void add(Class<?> targetType, T handler) {
        handlers.put(targetType, handler);
        clearCache();
    }

    /**
     * Check to see if the there is a handler for the specified target type.
     * 
     * @param targetType The type for which a handler is requested.
     * @return An appropriate handler, if one is found. Otherwise, whatever is returned from a call
     *         to {@link #getDefaultHandler()}.
     */
    public T getHandler(Class<?> targetType) {
        T handler = findHandler(targetType);

        if (handler == null) {
            handler = getDefaultHandler();
            log.trace("Couldn't find a handler for ", targetType, ". Using default handler ",
                    getDefaultHandler(), " instead.");
        }

        return handler;
    }

    /**
     * Search for a handler class that best matches the requested class, first checking the
     * specified class, then all the interfaces it implements, then all its superclasses and the
     * interfaces they implement, and finally all the superclasses of the interfaces implemented by
     * {@code targetClass}.
     * 
     * @param targetType The type for which a handler is requested.
     * @return the best applicable handler
     */
    protected T findHandler(Class<?> targetType) {
        T handler = findInSuperclasses(targetType);

        if (handler == null) {
            if (isSearchHierarchy())
                handler = findInInterfaces(targetType, targetType.getInterfaces());
            else
                handler = cacheHandler(targetType, null);
        }

        return handler;
    }

    /**
     * Called first by {@link #findHandler(Class)}. Search for a handler class that best matches the
     * requested class, first checking the specified class, second all the interfaces it implements,
     * third annotations. If no match is found, repeat the process for each superclass.
     * 
     * @param targetType The type for which a handler is requested.
     * @return the first applicable handler found or null if no match could be found
     */
    protected T findInSuperclasses(Class<?> targetType) {
        // Check for a known handler for the class
        T handler;
        if ((handler = handlers.get(targetType)) != null) {
            return handler;
        }
        else if ((handler = indirectCache.get(targetType)) != null) {
            return handler;
        }
        else if (negativeCache.contains(targetType)) {
            return null;
        }
        else if (targetType.isEnum()) {
            handler = findInSuperclasses(Enum.class);
            if (handler != null)
                return cacheHandler(targetType, handler);
        }

        // Check directly implemented interfaces
        if (isSearchHierarchy()) {
            for (Class<?> iface : targetType.getInterfaces()) {
                if ((handler = handlers.get(iface)) != null)
                    return cacheHandler(targetType, handler);
                else if ((handler = indirectCache.get(iface)) != null)
                    return cacheHandler(targetType, handler);
            }
        }

        // Check for annotations
        if (isSearchAnnotations()) {
            for (Annotation annotation : targetType.getAnnotations()) {
                Class<? extends Annotation> annotationType = annotation.annotationType();
                if (handlers.containsKey(annotationType))
                    return cacheHandler(targetType, handlers.get(annotationType));
            }
        }

        // Check superclasses
        if (isSearchHierarchy()) {
            Class<?> parent = targetType.getSuperclass();
            if (parent != null) {
                if ((handler = findInSuperclasses(parent)) != null) {
                    return cacheHandler(targetType, handler);
                }
            }
        }

        // Nothing found, so return null
        return null;
    }

    /**
     * Called second by {@link #findHandler(Class)}, after {@link #findInSuperclasses(Class)} .
     * Search for a handler that best matches the requested class by checking the superclasses of
     * every interface implemented by {@code targetClass}.
     * 
     * @param targetType The type for which a handler is requested.
     * @param ifaces An array of interfaces to search
     * @return The first applicable handler found or null if no match could be found
     */
    protected T findInInterfaces(Class<?> targetType, Class<?>... ifaces) {
        T handler = null;
        for (Class<?> iface : ifaces) {
            if ((handler = handlers.get(iface)) != null) {
                return cacheHandler(targetType, handler);
            }
            else if ((handler = indirectCache.get(iface)) != null) {
                return cacheHandler(targetType, handler);
            }
            else if ((handler = findInInterfaces(targetType, iface.getInterfaces())) != null) {
                return cacheHandler(targetType, handler);
            }
        }

        // Nothing found, so return null
        return null;
    }

    /**
     * Cache an indirect handler mapping for the given target type.
     * 
     * @param targetType The type for which a handler is requested.
     * @param handler The handler
     * @return The {@code targetType} parameter
     */
    protected T cacheHandler(Class<?> targetType, T handler) {
        if (handler == null) {
            log.debug("Caching no handler for ", targetType);
            negativeCache.add(targetType);
        }
        else {
            log.debug("Caching handler for ", targetType, " => ", handler);
            indirectCache.put(targetType, handler);
        }

        return handler;
    }

    /** Clear the indirect cache. This is called by {@link #add(Class, Object)}. */
    public void clearCache() {
        log.debug("Clearing indirect cache and negative cache");
        indirectCache.clear();
        negativeCache.clear();
    }
}
