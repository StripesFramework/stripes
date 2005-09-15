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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Scans the classpath of its parent ClassLoader in order to locate all instances of a given
 * interface.  Loads the classes without invoking the static initalizers for the classes, thus
 * ensuring there are no negative side-affects ensue.  Since it scans the entire set of
 * URLs belonging to its parent ClassLoader it can take some time - call it only as often
 * as you need.
 *
 * @author Tim Fennell
 */
public class ResolverUtil {
    /** An instance of Log to use for logging in this class. */
    private static final Log log = Log.getInstance(ResolverUtil.class);


    /**
     * Locates all implementations of an interface, in all packages, in all URLs within the
     * current ClassLoader.  This can take a really long time, and it is usually sufficient to
     * limit the search to specific packages.
     *
     * @param anInterface class object representing the interface whose implementations to find
     * @return Set<Class> a set of Class objects, one for each implementation of anInterface
     */
    public static <T> Set<Class<T>> getImplementations(Class<T> anInterface) {
        return getImplementations(anInterface, Collections.EMPTY_SET, Collections.EMPTY_SET);
    }

    /**
     * Locates all implementations of an interface in the classloader being used by this thread.
     * Does not scan the full chain of classloaders.  Scans only in the URLs in the ClassLoader
     * which match the filters provided, and within those URLs only checks classes within the
     * packages defined by the package filters provided.
     *
     * @param anInterface class object representing the interface whose implementations to find
     * @param locationFilters restricts the locations in the classpath that will be searched to
     *        those who contain one of the specified filters as a substring.
     * @param packageFilters restricts the classes that will be checked to those whose name
     *        contains one of the specified filters as a substring
     * @return Set<Class> a set of Class objects, one for each implementation of anInterface
     */
    public static <T> Set<Class<T>> getImplementations(Class<T> anInterface,
                                                       Set<String> locationFilters,
                                                       Set<String> packageFilters) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Set<Class<T>> implementations = new HashSet<Class<T>>();

        // Try and bullet proof this a little by removing any * characters folks
        // might have added, thinking we actually support wild-carding ;)
        Set<String> locationPatterns = new HashSet<String>();
        for (String locationFilter : locationFilters) {
            locationPatterns.add( locationFilter.replace("*", "") );
        }

        Set<String> packagePatterns = new HashSet<String>();
        for (String packageFilter : packageFilters) {
            packagePatterns.add( packageFilter.replace("*", "").replace(".", "/") );
        }

        // If it's not a URLClassLoader, we can't deal with it!
        if (!(loader instanceof URLClassLoader)) {
            log.error("The current ClassLoader is not castable to a URLClassLoader. ClassLoader ",
                    "is of type [", loader.getClass().getName(), "]. Cannot scan ClassLoader for ",
                    "implementations of ", anInterface.getClass().getName(), ".");
        }
        else {
            URLClassLoader urlLoader = (URLClassLoader) loader;
            URL[] urls = urlLoader.getURLs();

            for (URL url : urls) {
                String path = url.getFile();
                File location = new File(path);

                // Only process the URL if it matches one of our filter strings
                if ( matchesAny(path, locationPatterns) ) {
                    log.info("Checking URL '", url, "' for instances of ", anInterface.getName());
                    if (location.isDirectory()) {
                        implementations.addAll(getImplementationsInDirectory(anInterface, null, location, packagePatterns));
                    }
                    else {
                        implementations.addAll(getImplementationsInJar(anInterface, location, packagePatterns));
                    }
                }
            }
        }

        return implementations;
    }

    /**
     * Checks to see if one or more of the filter strings occurs within the string specified. If
     * so, returns true.  Otherwise returns false.
     *
     * @param text the text within which to look for the filter strings
     * @param filters a set of substrings to look for in the text
     */
    static boolean matchesAny(String text, Set<String> filters) {
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
     * @param anInterface Class object of the interface whose implementations to return
     * @param parent the package name up to this directory in the package hierarchy.  E.g. if
     *        /classes is in the classpath and we wish to examine files in /classes/org/apache then
     *        the values of <i>parent</i> would be <i>org/apache</i>
     * @param location a File object representing a directory
     * @return Set the set of classes within the directory, and sub-directories, implementing
     *         anInterface
     */
    static <T> Set<Class<T>> getImplementationsInDirectory(Class<T> anInterface,
                                                           String parent,
                                                           File location,
                                                           Set<String> packagePatterns) {

        Set<Class<T>> implementations = new HashSet<Class<T>>();
        File[] files = location.listFiles();
        StringBuilder builder = null;

        for (File file : files) {
            builder = new StringBuilder(100);
            builder.append(parent).append("/").append(file.getName());
            String packageOrClass = ( parent == null ? file.getName() : builder.toString() );

            if (file.isDirectory()) {
                implementations.addAll(getImplementationsInDirectory(anInterface, packageOrClass, file, packagePatterns));
            }
            else if (file.getName().endsWith(".class")) {
                if (matchesAny(packageOrClass, packagePatterns)) {
                    addIfImplements(implementations, anInterface, packageOrClass);
                }
            }
        }

        return implementations;
    }

    /**
     * Finds implementations of an interface within a jar files that contains a folder structure
     * matching the package structure.  If the File is not a JarFile or does not exist a warning
     * will be logged, but no error will be raised.  In this case an empty Set will be returned.
     *
     * @param anInterface Class object of the interface whose implementations to return
     * @param location a File object representing a JarFile in the classpath
     * @return Set the set of classes within the JarFile, implementing anInterface
     */
    static <T> Set<Class<T>> getImplementationsInJar(Class<T> anInterface,
                                                     File location,
                                                     Set<String> packagePatterns) {

        Set<Class<T>> implementations = new HashSet<Class<T>>();

        try {
            JarFile jar = new JarFile(location);
            Enumeration entries = jar.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                String name = entry.getName();
                if (!entry.isDirectory() && name.endsWith(".class")) {
                    if (matchesAny(name, packagePatterns)) {
                        addIfImplements(implementations, anInterface, name);
                    }
                }
            }
        }
        catch (IOException ioe) {
            log.error(ioe, "Could not search jar file '", location, "' for implementations of ",
                      anInterface.getName());
        }
        return implementations;
    }

    /**
     * Adds a Class to a Set of Classes if it implements the specified interface.
     *
     * @param impls the set of Classes to add matching classes to
     * @param iface the Class object representing the interface classes should implement
     * @param name the fully qualified name of a class
     */
    static <T> void addIfImplements(Set<Class<T>> impls, Class<T> iface, String name) {
        try {
            log.trace("Checking to see if class '", name, "' implements ", iface.getName());
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            String externalName = name.substring(0, name.indexOf('.')).replace('/', '.');

            Class type = loader.loadClass(externalName);
            if (iface.isAssignableFrom(type) ) {
                impls.add( (Class<T>) type);
            }
        }
        catch (Throwable t) {
            log.warn(t, "Could not examine class '", name, "'");
        }
    }
}