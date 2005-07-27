package net.sourceforge.stripes.controller;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.util.ResolverUtil;

import java.util.Set;
import java.util.Collections;

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
    Set<Class<ActionBean>> beans;

    /**
     * Private construtor that initializes the cached set of bean Class objects. While
     * ResolverUtil does not appear to throw any exceptions, it can throw runtime exceptions which
     * would cause classloading to fail if this initalization were done statically.
     */
    private ActionClassCache() {
        beans = Collections.unmodifiableSet( ResolverUtil.getImplementations(ActionBean.class) );
    }

    /**
     * Gets access to the singleton instance of this class.
     */
    public static synchronized ActionClassCache getInstance() {
        if (ActionClassCache.cache == null) {
            ActionClassCache.cache = new ActionClassCache();
        }

        return ActionClassCache.cache;
    }

    /**
     * Returns the set of beans in the classpath that implement ActionBean. This set is cached
     * after the first lookup, so it can be accessed repeatedly without any performance impact.
     * The set is an un-modifiable set.
     */
    public Set<Class<ActionBean>> getActionBeanClasses() {
        return this.beans;
    }
}
