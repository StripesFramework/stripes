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
package net.sourceforge.stripes.util;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Set;
import java.util.Collection;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * <p>ResolverUtil is used to locate classes that implement an interface or extend a given base
 * class. It does this in two different ways.  The first way is by accessing the
 * {@link Thread#getContextClassLoader() Context ClassLoader} and attempting to discover the set
 * of URLs that are used for classloading.  The second mechanism uses the {@link ServletContext}
 * to discover classes under {@code /WEB-INF/classes/} and jar files under {@code /WEB-INF/lib/}</p>.
 *
 * <p>The first mechanism is generally preferred since it can usually discover classes in more
 * locations, but it requires that the context class loader be a subclass of {@link URLClassLoader}.
 * Most containers use class loaders that extend URLClassloader, but not all do.  Since accessing
 * resources through the ServletContext is mandated to work in the Servlet specification this should
 * work in all containers.</p>
 *
 * <p>Since scanning all classpath entries and/or jars under {@code /WEB-INF/lib/} can take a
 * non-trivial amount of time, it is possible to filter the set of locations and packages that
 * are examined.  This is done by supplying Collections of filter patterns.  The
 * {@code locationFilters} are used to match the locations (directories, jar files, etc.) examined.
 * The {@code packageFilters} restricts the set of classes loaded by package.  In both cases a
 * simple sub-string match is used.  For example if location patterns of ["project1", project2"] are
 * supplied, you would see the following:</p>
 *
 *<pre>
 *lib/project1/dependencies/dep1.jar  -> scanned
 *lib/project3/dependencies/dep79.jar -> not scanned
 *WEB-INF/lib/project1-web.jar        -> scanned
 *WEB-INF/classes                     -> not scanned
 *lib/project2/project2-business.jar  -> scanned
 *</pre>
 *
 * <p>If no location filters are supplied, all discovered locations will be scanned for classes.
 * If no package filters are supplied, all classes discovered will be checked.</p>
 *
 * <p>At first glance it may seem redundant to provide the class type being searched for at
 * instantiation time, and again when invoking one of the {@code load()} methods.  However,
 * this allows for certain usages that would not otherwise be possible.  For example, the
 * following is used to find all collections that support ordering of some kind:</p>
 *
 *<pre>
 *ResolverUtil&lt;Collection&gt; resolver = new ResolverUtil&lt;Collection&gt;();
 *resolver.loadImplementationsFromContextClassloader(List.class);
 *resolver.loadImplementationsFromContextClassloader(SortedSet.class);
 *Set&lt;Class&lt;? extends Collection&gt;&gt; classes = resolver.getClasses();
 *</pre>
 * @author Tim Fennell
 */
public class ResolverUtil<T> {
    /** An instance of Log to use for logging in this class. */
    private static final Log log = Log.getInstance(ResolverUtil.class);

    /** Set of filter strings used to match URLs to check for classes. */
    private Set<String> locationFilters = new HashSet<String>();

    /** Set of filter strings used to match package names of classes to load and check. */
    private Set<String> packageFilters = new HashSet<String>();

    /** The set of implementations being accumulated. */
    private Set<Class<? extends T>> implementations = new HashSet<Class<?extends T>>();

    /**
     * Sets the collection of location filter patterns to use when deciding whether to check
     * a given location for classes.  Removes any "*" wildcards from the String just in case.
     *
     * @param patterns a set of patterns used to match locations for finding classes
     */
    public void setLocationFilters(Collection<String> patterns) {
        // Try and bullet proof this a little by removing any * characters folks
        // might have added, thinking we actually support wild-carding ;)
        this.locationFilters.clear();
        for (String pattern : patterns) {
            locationFilters.add( pattern.replace("*", "") );
        }
    }

    /**
     * Sets the collection of package filter patterns to use when deciding whether to load
     * and examine classes.
     *
     * @param patterns a set of patterns to match against fully qualified class names
     */
    public void setPackageFilters(Collection<String> patterns) {
        this.packageFilters.clear();
        for (String pattern : patterns) {
            packageFilters.add( pattern.replace("*", "").replace(".", "/") );
        }
    }

    /**
     * Provides access to the classes discovered so far. If neither of
     * {@link #loadImplementationsFromContextClassloader(Class)} or
     * {@link #loadImplementationsFromServletContext(Class, javax.servlet.ServletContext)} have
     * been called, this set will be empty.
     *
     * @return the set of classes that have been discovered.
     */
    public Set<Class<? extends T>> getClasses() {
        return implementations;
    }

    /**
     * <p>Attempts to locate, load and examine classes using the ServletContext to load resources
     * from {@code /WEB-INF/}. While dependent on the Servlet API and restricted to looking for
     * classes in {@code /WEB-INF/classes} and libraries in {@code /WEB-INF/lib}, this method
     * should work in all servlet containers regardless of classloading implementation.</p>
     *
     * <p>Locations and classes are examined with respect to any filters set.  Classes are
     * stored internally and may be accessed (along with any other previously resolved classes)
     * by calling {@link #getClasses()}.</p>
     *
     * @param parentType an interface or class to find implementations or subclasses of.
     * @param context a ServletContext from which to load resources
     */
    public void loadImplementationsFromServletContext(Class<? extends T> parentType,
                                                      ServletContext context) {
        // Always scan WEB-INF/classes
        log.info("Checking for classes in /WEB-INF/classes using ServletContext resources.");
        loadImplementationsFromServletContext(parentType, "/WEB-INF/classes/", context);

        // Now scan WEB-INF/lib
        Set<String> jars = context.getResourcePaths("/WEB-INF/lib/");
        if (jars != null) {
            for (String jarName : jars) {
                if (matchesAny(jarName, locationFilters)) {
                    log.info("Checking web application library '", jarName,
                             "' for instances of ", parentType.getName());

                    loadImplementationsInJar(parentType,
                                             context.getResourceAsStream(jarName),
                                             jarName);
                }
            }
        }
    }

    /**
     * Internal method that will find any classes in the supplied sub-directory of
     * {@code /WEB-INF/classes} and then recurse for any directories found within the
     * current directory.
     *
     * @param parentType an interface or class to find implementations or subclasses of.
     * @param context a ServletContext from which to load resources
     * @param path the path within /WEB-INF/classes to be checked
     */
    private void loadImplementationsFromServletContext(Class<? extends T> parentType,
                                                         String path,
                                                         ServletContext context) {
        Set<String> paths = context.getResourcePaths(path);
        if (paths != null) {
            for (String subPath : paths) {
                // Recurse for directories
                if (subPath.endsWith("/")) {
                    loadImplementationsFromServletContext(parentType, subPath, context);
                }
                else if (subPath.endsWith(".class")) {
                    addIfAssignableTo(parentType, subPath.replace("/WEB-INF/classes/", ""));
                }
            }
        }
    }


    /**
     * <p>Locates all implementations of an interface in the classloader being used by this thread.
     * Does not scan the full chain of classloaders.  Scans only in the URLs in the ClassLoader
     * which match the filters provided, and within those URLs only checks classes within the
     * packages defined by the package filters provided.</p>
     *
     * <p>This method relies on the fact that most ClassLoaders in the wild extend the built-in
     * {@link URLClassLoader}. This is relied upon because there is no standard way to discover
     * the set of locations from which a ClassLoader is loading classes.  The URLClassLoader
     * exposes methods to discover this, and those are made use of to within this method.</p>
     *
     * @param parentType an interface or class to find implementations or subclasses of.
     * @return true if the classloader was a subclass of {@link URLClassLoader} and was scanned,
     *         false if the classloader could not be scanned.
     */
    public boolean loadImplementationsFromContextClassloader(Class<? extends T> parentType) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        // If it's not a URLClassLoader, we can't deal with it!
        if (!(loader instanceof URLClassLoader)) {
            log.error("The current ClassLoader is not castable to a URLClassLoader. ClassLoader ",
                    "is of type [", loader.getClass().getName(), "]. Cannot scan ClassLoader for ",
                    "implementations of ", parentType.getClass().getName(), ".");
            return false;
        }
        else {
            URLClassLoader urlLoader = (URLClassLoader) loader;
            URL[] urls = urlLoader.getURLs();

            for (URL url : urls) {
                String path = url.getFile();
                try { path = URLDecoder.decode(path, "UTF-8"); }
                catch (UnsupportedEncodingException e) { /* UTF-8 is a required encoding */ }
                File location = new File(path);

                // Manage what happens when Resin decides to return URLs that do not
                // match reality!  For whatever reason, Resin decides to return some JAR
                // URLs with an extra '!/' on the end of the jar file name and a file:
                // in front even though that's the protocol spec, not the path!
                if (!location.exists()) {
                    if (path.endsWith("!/")) path = path.substring(0, path.length() - 2);
                    if (path.startsWith("file:")) path = path.substring(5);
                    location = new File(path);
                }

                // Only process the URL if it matches one of our filter strings
                if ( matchesAny(path, locationFilters) ) {
                    log.info("Checking URL '", path, "' for instances of ", parentType.getName());
                    if (location.isDirectory()) {
                        loadImplementationsInDirectory(parentType, null, location);
                    }
                    else {
                        loadImplementationsInJar(parentType, null, path);
                    }
                }
            }

            return true;
        }
    }

    /**
     * Checks to see if one or more of the filter strings occurs within the string specified. If
     * so, returns true.  Otherwise returns false.
     *
     * @param text the text within which to look for the filter strings
     * @param filters a set of substrings to look for in the text
     */
    private boolean matchesAny(String text, Set<String> filters) {
        if (filters.size() == 0) {
            return true;
        }
        for (String filter : filters) {
            if (text.indexOf(filter) != -1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds implementations of an interface in a physical directory on a filesystem.  Examines all
     * files within a directory - if the File object is not a directory, and ends with <i>.class</i>
     * the file is loaded and tested to see if it is an implemenation of the interface.  Operates
     * recursively to find classes within a folder structure matching the package structure.
     *
     * @param parentType an interface or class to find implementations or subclasses of.
     * @param parent the package name up to this directory in the package hierarchy.  E.g. if
     *        /classes is in the classpath and we wish to examine files in /classes/org/apache then
     *        the values of <i>parent</i> would be <i>org/apache</i>
     * @param location a File object representing a directory
     */
    private void loadImplementationsInDirectory(Class<? extends T> parentType,
                                                  String parent, File location) {
        File[] files = location.listFiles();
        StringBuilder builder = null;

        for (File file : files) {
            builder = new StringBuilder(100);
            builder.append(parent).append("/").append(file.getName());
            String packageOrClass = ( parent == null ? file.getName() : builder.toString() );

            if (file.isDirectory()) {
                loadImplementationsInDirectory(parentType, packageOrClass, file);
            }
            else if (file.getName().endsWith(".class")) {
                if (matchesAny(packageOrClass, packageFilters)) {
                    addIfAssignableTo(parentType, packageOrClass);
                }
            }
        }
    }

    /**
     * Finds implementations of an interface within a jar files that contains a folder structure
     * matching the package structure.  If the File is not a JarFile or does not exist a warning
     * will be logged, but no error will be raised.  In this case an empty Set will be returned.
     *
     * @param parentType an interface or class to find implementations or subclasses of.
     * @param inputStream a regular (non-jar/non-zip) input stream from which to read the
     *        jar file in question
     * @param location the location of the jar file being examined. Used to create the input
     *        stream if the input stream is null, and to log appropriate error messages
     */
    private void loadImplementationsInJar(Class<? extends T> parentType,
                                            InputStream inputStream,
                                            String location) {

        try {
            JarEntry entry;
            if (inputStream == null) inputStream = new FileInputStream(location);
            JarInputStream jarStream = new JarInputStream(inputStream);

            while ( (entry = jarStream.getNextJarEntry() ) != null) {
                String name = entry.getName();
                if (!entry.isDirectory() && name.endsWith(".class")) {
                    if (matchesAny(name, this.packageFilters)) {
                        addIfAssignableTo(parentType, name);
                    }
                }
            }
        }
        catch (IOException ioe) {
            log.error("Could not search jar file '", location, "' for implementations of ",
                      parentType.getName(), "due to an IOException: ", ioe.getMessage());
        }
    }

    /**
     * Add the class designated by the fully qualified class name provided to the set of
     * resolved classes if and only if it extends/implements the parent type supplied.
     *
     * @param parentType the interface or class to add implementations or subclasses of.
     * @param fqn the fully qualified name of a class
     */
    private void addIfAssignableTo(Class<? extends T> parentType, String fqn) {
        try {
            log.trace("Checking to see if class '", fqn, "' implements ", parentType.getName());
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            String externalName = fqn.substring(0, fqn.indexOf('.')).replace('/', '.');

            Class type = loader.loadClass(externalName);
            if (parentType.isAssignableFrom(type) ) {
                implementations.add( (Class<T>) type);
            }
        }
        catch (Throwable t) {
            log.warn("Could not examine class '", fqn, "'", " due to a ",
                     t.getClass().getName(), " with message: ", t.getMessage());
        }
    }
}