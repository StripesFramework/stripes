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
package net.sourceforge.stripes.controller;

import net.sourceforge.stripes.action.ActionBean;

import java.util.Set;

/**
 * Quick and dirty class which caches the Class objects representing ActionBeans since it is
 * expensive to look these up using the ResolverUtil.
 *
 * @author Tim Fennell
 */
public class ActionClassCache {
    /** The static singleton instance of this class. */
    private static ActionClassCache cache;

    /**
     * The caches set of ActionBeans so that they are available to all necessary classes without
     * repeating the classloader scanning that is necessary to find them.
     */
    Set<Class<? extends ActionBean>> beans;

    /**
     * Protected initializer that initializes the cached set of bean Class objects. While
     * ResolverUtil does not appear to throw any exceptions, it can throw runtime exceptions which
     * would cause classloading to fail if this initalization were done statically.
     */
    protected static synchronized void init(Set<Class<? extends ActionBean>> actionBeans) {
        ActionClassCache instance = new ActionClassCache();
        instance.beans = actionBeans;
        ActionClassCache.cache = instance;
    }

    /** Private constructor that stops anyone else from initialzing an ActionClassCache. */
    private ActionClassCache() {
        // Do nothing
    }

    /**
     * Gets access to the singleton instance of this class.
     */
    public static ActionClassCache getInstance() {
        if (ActionClassCache.cache == null) {
            throw new IllegalStateException
                    ("Attempt made to access ActionClassCache before it has been initialized.");
        }
        else {
            return ActionClassCache.cache;
        }
    }

    /**
     * Returns the set of beans in the classpath that implement ActionBean. This set is cached
     * after the first lookup, so it can be accessed repeatedly without any performance impact.
     * The set is an un-modifiable set.
     */
    public Set<Class<? extends ActionBean>> getActionBeanClasses() {
        return this.beans;
    }
}
