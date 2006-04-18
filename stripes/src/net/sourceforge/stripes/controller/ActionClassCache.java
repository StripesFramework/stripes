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
