/* Copyright 2008 Ben Gunter
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
package net.sourceforge.stripes.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.exception.StripesRuntimeException;

/**
 * <p>
 * An implementation of {@link ObjectFactory} that simply calls {@link Class#newInstance()} to
 * obtain a new instance.
 * </p>
 * 
 * @author Ben Gunter
 * @since Stripes 1.5.1
 */
public class DefaultObjectFactory implements ObjectFactory {
    /**
     * Holds a map of commonly used interface types (mostly collections) to a class that implements
     * the interface and will, by default, be instantiated when an instance of the interface is
     * needed.
     */
    protected static final Map<Class<?>, Class<?>> interfaceImplementations = new HashMap<Class<?>, Class<?>>();

    static {
        interfaceImplementations.put(Collection.class, ArrayList.class);
        interfaceImplementations.put(List.class,       ArrayList.class);
        interfaceImplementations.put(Set.class,        HashSet.class);
        interfaceImplementations.put(SortedSet.class,  TreeSet.class);
        interfaceImplementations.put(Queue.class,      LinkedList.class);
        interfaceImplementations.put(Map.class,        HashMap.class);
        interfaceImplementations.put(SortedMap.class,  TreeMap.class);
    }

    private Configuration configuration;

    /** Does nothing. */
    public void init(Configuration configuration) throws Exception {
        this.configuration = configuration;
    }

    /** Get the {@link Configuration} that was passed into {@link #init(Configuration)}. */
    protected Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Calls {@link Class#newInstance()} and returns the newly created object.
     * 
     * @param clazz The class to instantiate.
     * @return The new object
     */
    public <T> T newInstance(Class<T> clazz) {
        try {
            if (clazz.isInterface())
                return newInterfaceInstance(clazz);
            else
                return clazz.newInstance();
        }
        catch (InstantiationException e) {
            throw new StripesRuntimeException("Could not instantiate " + clazz, e);
        }
        catch (IllegalAccessException e) {
            throw new StripesRuntimeException("Could not instantiate " + clazz, e);
        }
    }

    /**
     * Attempts to determine an implementing class for the interface provided and instantiate it
     * using a default constructor.
     * 
     * @param interfaceType an interface (or abstract class) to make an instance of
     * @return an instance of the interface type supplied
     * @throws InstantiationException if no implementation type has been configured
     * @throws IllegalAccessException if thrown by the JVM during class instantiation
     */
    @SuppressWarnings("unchecked")
    public <T> T newInterfaceInstance(Class<T> interfaceType) throws InstantiationException,
            IllegalAccessException {
        Class impl = getImplementingClass(interfaceType);
        if (impl == null) {
            throw new InstantiationException(
                    "Stripes needed to instantiate a property who's declared type as an " +
                    "interface (which obviously cannot be instantiated. The interface is not " +
                    "one that Stripes is aware of, so no implementing class was known. The " +
                    "interface type was: '" + interfaceType.getName() + "'. To fix this " +
                    "you'll need to do one of three things. 1) Change the getter/setter methods " +
                    "to use a concrete type so that Stripes can instantiate it. 2) in the bean's " +
                    "setContext() method pre-instantiate the property so Stripes doesn't have to. " +
                    "3) Bug the Stripes author ;)  If the interface is a JDK type it can easily be " +
                    "fixed. If not, if enough people ask, a generic way to handle the problem " +
                    "might get implemented.");
        }
        else {
            return StripesFilter.getConfiguration().getObjectFactory().newInstance((Class<T>) impl);
        }
    }

    /**
     * Looks up the default implementing type for the supplied interface. This is done based on a
     * static map of known common interface types and implementing classes.
     * 
     * @param iface an interface for which an implementing class is needed
     * @return a Class object representing the implementing type, or null if one is not found
     */
    public Class<?> getImplementingClass(Class<?> iface) {
        return interfaceImplementations.get(iface);
    }
}
