package net.sourceforge.stripes.util;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Scans the classpath of its parent ClassLoader in order to locate all instances of a given
 * interface.  Uses Jakarta's BCEL to load and examine the classes thus ensuring that no
 * static initializers are called and no side-affects ensue.  Since it scans the entire set of
 * URLs belonging to its parent ClassLoader it can take some time - call it only as often
 * as you need.
 *
 * @author Tim Fennell
 */
public class ResolverUtil {
    private static final Log log = LogFactory.getLog(ResolverUtil.class);

    /**
     * Locates all implementations of an interface in the classloader being used by this thread.
     * Does not scan the full chain of classloaders.
     *
     * @param anInterface class object representing the interface whose implementations to find
     * @return Set<Class> a set of Class objects, one for each implementation of anInterface
     */
    public static <T> Set<Class<T>> getImplementations(Class<T> anInterface) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Set<Class<T>> implementations = new HashSet<Class<T>>();

        if (!(loader instanceof URLClassLoader)) {
            log.error("The current ClassLoader is not castable to a URLClassLoader. ClassLoader " +
                "is of type [" + loader.getClass().getName() + "]. Cannot scan ClassLoader for " +
                "implementations of " + anInterface.getClass().getName() + ".");
        }
        else {
            URLClassLoader urlLoader = (URLClassLoader) loader;
            URL[] urls = urlLoader.getURLs();

            for (URL url : urls) {
                log.info("Checking URL '" + url + "' for instances of " + anInterface.getName());
                String path = url.getPath();
                File location = new File(path);

                if (location.isDirectory()) {
                    implementations.addAll(getImplementationsInDirectory(anInterface, null, location));
                }
                else {
                    implementations.addAll(getImplementationsInJar(anInterface, location));
                }
            }
        }

        return implementations;
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
    static <T> Set<Class<T>> getImplementationsInDirectory(Class<T> anInterface, String parent, File location) {
        Set<Class<T>> implementations = new HashSet<Class<T>>();
        File[] files = location.listFiles();
        StringBuilder builder = null;

        for (File file : files) {
            builder = new StringBuilder(100);
            builder.append(parent).append(File.separator).append(file.getName());
            String packageOrClass = ( parent == null ? file.getName() : builder.toString() );

            if (file.isDirectory()) {
                implementations.addAll(getImplementationsInDirectory(anInterface, packageOrClass, file));
            }
            else if (file.getName().endsWith(".class")) {
                addIfImplements(implementations, anInterface, packageOrClass);
            }
        }

        return implementations;
    }

    /**
     * Finds implementations of an interface within a jar files that contains a folder structure
     * matching the package structure.  If the File is not a JarFile or does not exist a warning
     * will be logged, but no error will be raised.  In this case an empty Set will be returned.
     * @param anInterface Class object of the interface whose implementations to return
     * @param location a File object representing a JarFile in the classpath
     * @return Set the set of classes within the JarFile, implementing anInterface
     */
    static <T> Set<Class<T>> getImplementationsInJar(Class<T> anInterface, File location) {
        Set<Class<T>> implementations = new HashSet<Class<T>>();

        try {
            JarFile jar = new JarFile(location);
            Enumeration entries = jar.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                String name = entry.getName();
                if (!entry.isDirectory() && name.endsWith(".class")) {
                    addIfImplements(implementations, anInterface, name);
                }
            }
        }
        catch (IOException ioe) {
            log.error("Could not search jar file '" + location + "' for implementations of " +
                anInterface.getName(), ioe);
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
            log.trace("Checking to see if class '" + name + "' implements " + iface.getName());
            InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);

            if (stream == null) {
                log.warn("Input stream was null for class '" + name + "'");
            }
            else {
                JavaClass clazz = new ClassParser(stream, name).parse();
                String interfaceName = iface.getName();
                JavaClass[] interfaces = clazz.getAllInterfaces();

                for (JavaClass anInterface : interfaces) {
                    if (interfaceName.equals(anInterface.getClassName())) {
                        Class<T> type = iface.getClass().cast( Class.forName(clazz.getClassName()) );
                        impls.add(type);
                        break;
                    }
                }
            }
        }
        catch (Throwable t) {
            log.warn("Could not examine class '" + name + "'", t);
        }
    }
}
