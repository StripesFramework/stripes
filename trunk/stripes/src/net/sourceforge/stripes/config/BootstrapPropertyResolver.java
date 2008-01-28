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
package net.sourceforge.stripes.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.servlet.FilterConfig;

import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.util.ReflectUtil;
import net.sourceforge.stripes.util.ResolverUtil;
import net.sourceforge.stripes.util.StringUtil;

/**
 * <p>Resolves configuration properties that are used to bootstrap the system.  Essentially this boils
 * down to a handful of properties that are needed to figure out which configuration class should
 * be instantiated, and any values needed by that configuration class to locate configuration
 * information.</p>
 *
 * <p>Properties are looked for in the following order:
 *  <ul>
 *      <li>Initialization Parameters for the Dispatcher servlet</li>
 *      <li>Initialization Parameters for the Servlet Context</li>
 *      <li>Java System Properties</li>
 *  </ul>
 * </p>
 *
 * @author Tim Fennell
 */
public class BootstrapPropertyResolver {
    private static final Log log = Log.getInstance(BootstrapPropertyResolver.class);
    
    private FilterConfig filterConfig;

    /** The Configuration Key for looking up the comma separated list of extension packages. */
    public static final String EXTENSION_LIST = "Extension.Packages";

    /** Constructs a new BootstrapPropertyResolver with the given ServletConfig. */
    public BootstrapPropertyResolver(FilterConfig filterConfig) {
        setFilterConfig(filterConfig);
    }

    /** Stores a reference to the filter's FilterConfig object. */
    public void setFilterConfig(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

    /** Returns a reference to the StripesFilter's FilterConfig object. */
    public FilterConfig getFilterConfig() {
        return this.filterConfig;
    }

    /**
     * Fetches a configuration property in the manner described in the class level javadoc for
     * this class.
     *
     * @param key the String name of the configuration value to be looked up
     * @return String the value of the configuration item or null
     */
    public String getProperty(String key) {
        String value = this.filterConfig.getInitParameter(key);

        if (value == null) {
            value = this.filterConfig.getServletContext().getInitParameter(key);
        }

        if (value == null) {
            value = System.getProperty(key);
        }

        return value;
    }
    
    /**
     * Attempts to find a class the user has specified in web.xml or by auto-discovery in packages
     * listed in web.xml under Extension.Packages. Classes specified in web.xml take precedence.
     * 
     * @param paramName the parameter to look for in web.xml
     * @param targetType the type that we're looking for
     * @return the Class that was found
     */
    @SuppressWarnings("unchecked")
    public <T> Class<? extends T> getClassProperty(String paramName, Class<T> targetType)
    {
        Class<? extends T> clazz = null;

        String className = getProperty(paramName);

        if (className != null) {
            // web.xml takes precedence
            try {
                clazz = (Class<? extends T>) ReflectUtil.findClass(className);
                log.info("Class implementing/extending ", targetType.getSimpleName(),
                        " found in web.xml: ", className);
            }
            catch (ClassNotFoundException e) {
                log.error("Couldn't find class specified in web.xml under param ", paramName, ": ",
                        className);
            }
        }
        else {
            // we didn't find it in web.xml so now we check any extension packages
            ResolverUtil<T> resolver = new ResolverUtil<T>();
            String[] packages = StringUtil.standardSplit(getProperty(EXTENSION_LIST));
            resolver.findImplementations(targetType, packages);
            Set<Class<? extends T>> classes = resolver.getClasses();
            if (classes.size() == 1) {
                clazz = classes.iterator().next();
                className = clazz.getName();
                log.info("Class implementing/extending  ", targetType.getSimpleName(),
                        " found via auto-discovery: ", className);
            }
            else if (classes.size() > 1) {
                throw new StripesRuntimeException(StringUtil.combineParts(
                        "Found too many classes implementing/extending ", targetType
                                .getSimpleName(), ": ", classes));
            }
        }

        return clazz;
    }
    
    /**
     * Attempts to find all classes the user has specified in web.xml.
     * 
     * @param paramName the parameter to look for in web.xml
     * @return a List of classes found
     */
    @SuppressWarnings("unchecked")
    public List<Class> getClassPropertyList(String paramName)
    {
        List<Class> classes = new ArrayList<Class>();

        String classList = getProperty(paramName);

        if (classList != null) {
            String[] classNames = StringUtil.standardSplit(classList);
            for (String className : classNames) {
                className = className.trim();
                try {
                    classes.add(ReflectUtil.findClass(className));
                }
                catch (ClassNotFoundException e) {
                    throw new StripesRuntimeException("Could not find configured Interceptor ["
                            + className + "]. The " + "property '" + paramName
                            + "' contained [" + classList
                            + "]. This value must contain fully qualified class names separated "
                            + "by commas.");
                }
            }
        }

        return classes;
    }
    
    /**
     * Attempts to find classes by auto-discovery in packages listed in web.xml under
     * Extension.Packages.
     * 
     * @param targetType the type that we're looking for
     * @return a List of classes found
     */
    public <T> List<Class<? extends T>> getClassPropertyList(Class<T> targetType)
    {
        List<Class<? extends T>> classes = new ArrayList<Class<? extends T>>();

        ResolverUtil<T> resolver = new ResolverUtil<T>();
        String[] packages = StringUtil.standardSplit(getProperty(EXTENSION_LIST));
        resolver.findImplementations(targetType, packages);
        classes.addAll(resolver.getClasses());

        return classes;
    }

    /**
     * Attempts to find all matching classes the user has specified in web.xml or by auto-discovery
     * in packages listed in web.xml under Extension.Packages.
     * 
     * @param paramName the parameter to look for in web.xml
     * @param targetType the type that we're looking for
     * @return the Class that was found
     */
    @SuppressWarnings("unchecked")
    public <T> List<Class<? extends T>> getClassPropertyList(String paramName, Class<T> targetType)
    {
        List<Class<? extends T>> classes = new ArrayList<Class<? extends T>>();

        for (Class<?> clazz : getClassPropertyList(paramName)) {
            // can't use addAll :(
            classes.add((Class<? extends T>) clazz);
        }

        classes.addAll(getClassPropertyList(targetType));

        return classes;
    }
}
