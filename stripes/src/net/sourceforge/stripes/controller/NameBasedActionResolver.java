/* Copyright (C) 2006 Tim Fennell
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
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.util.Literal;

import java.lang.reflect.Modifier;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.Collections;

/**
 * <p>An ActionResolver that uses the names of classes and methods to generate sensible default
 * URL bindings and event names respectively. Extends the default
 * {@link AnnotatedClassActionResolver}, and is fully backward compatible.  Any classes and
 * methods that are annotated with {@link net.sourceforge.stripes.action.UrlBinding} and
 * {@link net.sourceforge.stripes.action.HandlesEvent} will retain the bindings specified in
 * those annotations.  In the case when an annotation is absent then a default binding is
 * generated.</p>
 *
 * <p>The generation of ActionBean URL bindings is done by taking the class name and removing
 * any extraneous packages at the front of the name, removing the strings "Action" and "Bean"
 * from the end of the name, substituting slashes for periods and appending a suffix (.action by
 * default).  The set of packages that are trimmed is specified by the
 * {@code getBasePackages()} method.  By default this method returns the set
 * [web, www, stripes, action].  These packages (and their parents) are removed from the
 * class name. E.g. {@code com.myco.web.foo.BarActionBean} would become {@code foo.BarActionBean}.
 * Continuing on, we would trim this further to {@code foo.Bar} and then translate it to
 * {@code /foo/Bar}.  Lastly the suffix returned by {@code getBindingSuffix()} is appended
 * giving the binding {@code /foo/Bar.action}.</p>
 *
 * <p>The translation of class names into URL bindings is designed to be easy to override and
 * customize.  To that end you can easiliy change how this translation is done by overriding
 * {@code getBasePackages()} and/or {@code getBindingSuffix()}, or completely customize the
 * behaviour by overriding {@code getUrlBinding(String)}.</p>
 *
 * <p>Mapping of method names to event names is simpler.  Again the parent class is delegated to
 * in case the method is annotated. If it is not, and the method is a concrete public method that
 * returns a Resolution (or subclass thereof) it is mapped to an event of the same name as the
 * method.  So an un-annotated method "{@code public Resolution view()}" is mapped to an event
 * called "view".  It should be noted that there is no special method name that signifies the
 * default handler.  If there is more than one handler and you require a default handler you
 * must still mark the default method with {@code @DefaultHandler}.</p>
 *
 * @author Tim Fennell
 * @since Stripes 1.2
 */
public class NameBasedActionResolver extends AnnotatedClassActionResolver {
    /**
     * Default set of packages (web, www, stripes, action) to be removed from the front
     * of class names when translating them to URL bindings.
     */
    public static final Set<String> BASE_PACKAGES =
            Collections.unmodifiableSet(Literal.set("web", "www", "stripes", "action"));

    /** Default suffix (.action) to add to URL bindings.*/
    public static final String DEFAULT_BINDING_SUFFIX = ".action";

    /**
     * <p>Finds or generates the URL binding for the class supplied. First delegates to the parent
     * class to see if an annotated url binding is present. If not, the class name is taken
     * and translated into a URL binding using {@code getUrlBinding(String name)}.</p>
     *
     * @param clazz a Class represenging an ActionBean
     * @return the String URL binding for the ActionBean
     */
    @Override
    public String getUrlBinding(Class<? extends ActionBean> clazz) {
        String binding = super.getUrlBinding(clazz);

        // If there's no annotated binding, and the class is concrete
        if (binding == null && !Modifier.isAbstract(clazz.getModifiers())) {
            binding = getUrlBinding(clazz.getName());
        }

        return binding;
    }

    /**
     * Takes a class name and translats it into a URL binding by removing extraneous package names,
     * removing Action, Bean, or ActionBean from the end of the class name if present, replacing
     * periods with slashes, and appending a standard suffix as supplied by
     * {@link net.sourceforge.stripes.controller.NameBasedActionResolver#getBindingSuffix()}.</p>
     *
     * <p>For example the class {@code com.myco.web.action.user.RegisterActionBean} would be
     * translated to {@code /user/Register.action}.  The behaviour of this method can be
     * overridden either directly or by overriding the methods {@code getBindingSuffix()} and
     * {@code getBasePackages()} which are used by this method.</p>
     *
     * @param name the name of the class to create a binding for
     * @return a String URL binding for the class
     *
     */
    protected String getUrlBinding(String name) {
        // Chop off the packages up until (and including) any base package
        for (String base : getBasePackages()) {
            int i = name.indexOf(base);
            if (i != -1) {
                name = name.substring(i + base.length());
            }
        }

        // If it ends in Action or Bean (or ActionBean) take that off
        if (name.endsWith("Bean")) {
            name = name.substring(0, name.length() - 4);
        }
        if (name.endsWith("Action")) {
            name = name.substring(0, name.length() - 6);
        }

        // Replace periods with slashes and make sure it starts with one
        name = name.replace('.', '/');
        if (!name.startsWith("/")) {
            name = "/" + name;
        }

        // Lastly add the suffix
        name += getBindingSuffix();
        return name;
    }

    /**
     * Returns a set of package names (fully qualified or not) that should be removed
     * from the start of a classname before translating the name into a URL Binding. By default
     * returns "web", "www", "stripes" and "action".
     *
     * @return a non-null set of String package names.
     */
    protected Set<String> getBasePackages() {
        return BASE_PACKAGES;
    }

    /**
     * Returns a non-null String suffix to be used when constructing URL bindings. The
     * default is ".action".
     */
    protected String getBindingSuffix() {
        return DEFAULT_BINDING_SUFFIX;
    }

    @Override
    protected String getEventName(Method handler) {
        String name = super.getEventName(handler);

        // If the method isn't annotated, but does return a resolution and is
        // not abstract (we already know it's public) then use the method name
        if ( name == null && !Modifier.isAbstract(handler.getModifiers())
                && Resolution.class.isAssignableFrom(handler.getReturnType()) ) {

            name = handler.getName();
        }

        return name;
    }
}
