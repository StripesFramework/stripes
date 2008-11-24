/* Copyright 2007 Ben Gunter
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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.exception.UrlBindingConflictException;
import net.sourceforge.stripes.util.HttpUtil;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.util.bean.ParseException;

/**
 * <p>
 * Provides access to {@link UrlBinding} objects. Bindings are used in two contexts:
 * <ul>
 * <li><strong>As a prototype:</strong> Binding prototypes provide static information about the
 * binding, such as the URI path, string literals, parameter names and default values. However, the
 * parameters associated with a prototype do not have a value since they are not evaluated against a
 * live request.</li>
 * <li><strong>"Live":</strong> Bindings that have been evaluated against a live servlet request
 * or request URI are exactly like their prototypes except that the parameter values associated with
 * them contain the values (if any) that were extracted from the URI.</li>
 * </ul>
 * </p>
 * 
 * @author Ben Gunter
 * @since Stripes 1.5
 * @see UrlBinding
 * @see UrlBindingParameter
 */
public class UrlBindingFactory {
    private static final Log log = Log.getInstance(UrlBindingFactory.class);

    /** Singleton instance */
    private static final UrlBindingFactory instance = new UrlBindingFactory();

    /**
     * Get the singleton instance.
     * 
     * @return an instance of this class
     */
    public static UrlBindingFactory getInstance() {
        return instance;
    }

    /** Maps {@link ActionBean} classes to {@link UrlBinding}s */
    private final Map<Class<? extends ActionBean>, UrlBinding> classCache = new HashMap<Class<? extends ActionBean>, UrlBinding>();

    /** Maps simple paths to {@link UrlBinding}s */
    private final Map<String, UrlBinding> pathCache = new HashMap<String, UrlBinding>();

    /** Keeps a list of all the paths that could not be cached due to conflicts between URL bindings */
    private final Map<String, List<String>> pathConflicts = new HashMap<String, List<String>>();

    /** Holds the set of paths that are cached, sorted from longest to shortest */
    private final Map<String, Set<UrlBinding>> prefixCache = new TreeMap<String, Set<UrlBinding>>(
            new Comparator<String>() {
                public int compare(String a, String b) {
                    int cmp = b.length() - a.length();
                    return cmp == 0 ? a.compareTo(b) : cmp;
                }
            });

    /** Don't want the constructor to be public */
    protected UrlBindingFactory() {
        // do nothing
    }

    /**
     * Get all the classes implementing {@link ActionBean}
     */
    public Collection<Class<? extends ActionBean>> getActionBeanClasses() {
        return Collections.unmodifiableSet(classCache.keySet());
    }

    /**
     * Get the {@link UrlBinding} prototype associated with the given {@link ActionBean} type. This
     * method may return null if no binding is associated with the given type.
     * 
     * @param type a class that implements {@link ActionBean}
     * @return a binding object if one is defined or null if not
     */
    public UrlBinding getBindingPrototype(Class<? extends ActionBean> type) {
        UrlBinding binding = classCache.get(type);
        if (binding != null)
            return binding;

        binding = parseUrlBinding(type);
        if (binding != null)
            addBinding(type, binding);
        return binding;
    }

    /**
     * Examines a URI (as returned by {@link HttpUtil#getRequestedPath(HttpServletRequest)}) and
     * returns the associated binding prototype, if any. No attempt is made to extract parameter
     * values from the URI. This is intended as a fast means to get static information associated
     * with a given request URI.
     * 
     * @param uri a request URI
     * @return a binding prototype, or null if the URI does not match
     */
    public UrlBinding getBindingPrototype(String uri) {
        // Look for an exact match to the URI first
        UrlBinding prototype = pathCache.get(uri);
        if (prototype != null) {
            log.debug("Matched ", uri, " to ", prototype);
            return prototype;
        }
        else if (pathConflicts.containsKey(uri)) {
            throw new UrlBindingConflictException(uri, pathConflicts.get(uri));
        }

        // Get all the bindings whose prefix matches the URI
        Set<UrlBinding> candidates = null;
        for (Entry<String, Set<UrlBinding>> entry : prefixCache.entrySet()) {
            if (uri.startsWith(entry.getKey())) {
                candidates = entry.getValue();
                break;
            }
        }

        // If none matched or exactly one matched then return now
        if (candidates == null) {
            log.debug("No URL binding matches ", uri);
            return null;
        }
        else if (candidates.size() == 1) {
            log.debug("Matched ", uri, " to ", candidates);
            return candidates.iterator().next();
        }

        // Now find the one that matches deepest into the URI with the fewest components
        int maxIndex = 0, minComponents = Integer.MAX_VALUE;
        List<String> conflicts = null;
        for (UrlBinding binding : candidates) {
            int idx = binding.getPath().length();
            List<Object> components = binding.getComponents();
            int componentCount = components.size();

            for (Object component : components) {
                if (!(component instanceof String))
                    continue;

                int at = uri.indexOf((String) component, idx);
                if (at >= 0) {
                    idx = at + ((String) component).length();
                }
                else {
                    break;
                }
            }

            if (idx == maxIndex) {
                if (componentCount < minComponents) {
                    conflicts = null;
                    minComponents = componentCount;
                    prototype = binding;
                }
                else if (componentCount == minComponents) {
                    if (conflicts == null) {
                        conflicts = new ArrayList<String>(candidates.size());
                        conflicts.add(prototype.toString());
                    }
                    conflicts.add(binding.toString());
                    prototype = null;
                }
            }
            else if (idx > maxIndex) {
                conflicts = null;
                minComponents = componentCount;
                prototype = binding;
                maxIndex = idx;
            }
        }

        log.debug("Matched @", maxIndex, " ", uri, " to ", prototype == null ? conflicts : prototype);
        if (prototype == null) {
            throw new UrlBindingConflictException(uri, conflicts);
        }

        return prototype;
    }

    /**
     * Examines a servlet request and returns the associated binding prototype, if any. No attempt
     * is made to extract parameter values from the URI. This is intended as a fast means to get
     * static information associated with a given request.
     * 
     * @param request a servlet request
     * @return a binding prototype, or null if the request URI does not match
     */
    public UrlBinding getBindingPrototype(HttpServletRequest request) {
        return getBindingPrototype(HttpUtil.getRequestedPath(request));
    }

    /**
     * Examines a URI (as returned by {@link HttpUtil#getRequestedPath(HttpServletRequest)}) and
     * returns the associated binding, if any. Parameters will be extracted from the URI, and the
     * {@link UrlBindingParameter} objects returned by {@link UrlBinding#getParameters()} will
     * contain the values that are present in the URI.
     * 
     * @param uri a request URI
     * @return a binding prototype, or null if the URI does not match
     */
    public UrlBinding getBinding(String uri) {
        UrlBinding prototype = getBindingPrototype(uri);
        if (prototype == null)
            return null;

        // ignore trailing slashes in the URI
        int length = uri.length();
        while (length > 0 && uri.charAt(length - 1) == '/')
            --length;

        // check for literal suffix in prototype and ignore it if found
        String suffix = prototype.getSuffix();
        if (suffix != null && uri.endsWith(suffix)) {
            length -= suffix.length();
        }

        // extract the request parameters and add to new binding object
        ArrayList<Object> components = new ArrayList<Object>(prototype.getComponents().size());
        int index = prototype.getPath().length();
        UrlBindingParameter current = null;
        String value = null;
        Iterator<Object> iter = prototype.getComponents().iterator();
        while (index < length && iter.hasNext()) {
            Object component = iter.next();
            if (component instanceof String) {
                // extract the parameter value from the URI
                String literal = (String) component;
                int end = uri.indexOf(literal, index);
                if (end >= 0) {
                    value = uri.substring(index, end);
                    index = end + literal.length();
                }
                else {
                    value = uri.substring(index, length);
                    index = length;
                }

                // add to the binding
                if (current != null && value != null && value.length() > 0) {
                    components.add(new UrlBindingParameter(current, value));
                    components.add(component);
                    current = null;
                    value = null;
                }
            }
            else if (component instanceof UrlBindingParameter) {
                current = (UrlBindingParameter) component;
            }
        }

        // if component iterator ended before end of string, then grab remainder of string
        if (index < length) {
            value = uri.substring(index, length);
        }

        // parameter was last component in list
        if (current != null && value != null && value.length() > 0) {
            components.add(new UrlBindingParameter(current, value));
        }

        // ensure all components are included so default parameter values are available
        while (iter.hasNext()) {
            Object component = iter.next();
            if (component instanceof UrlBindingParameter) {
                components.add(new UrlBindingParameter((UrlBindingParameter) component));
            }
            else {
                components.add(component);
            }
        }

        return new UrlBinding(prototype.getBeanType(), prototype.getPath(), components);
    }

    /**
     * Examines a servlet request and returns the associated binding, if any. Parameters will be
     * extracted from the request, and the {@link UrlBindingParameter} objects returned by
     * {@link UrlBinding#getParameters()} will contain the values that are present in the request.
     * 
     * @param request a servlet request
     * @return if the request matches a defined binding, then this method should return that
     *         binding. Otherwise, this method should return null.
     */
    public UrlBinding getBinding(HttpServletRequest request) {
        return getBinding(HttpUtil.getRequestedPath(request));
    }

    /**
     * Get all the {@link ActionBean}s classes that have been found.
     * 
     * @return an immutable collection of {@link ActionBean} classes
     */
    public HashMap<String, Class<? extends ActionBean>> getPathMap() {
        HashMap<String, Class<? extends ActionBean>> map = new HashMap<String, Class<? extends ActionBean>>();
        for (Entry<String, UrlBinding> entry : pathCache.entrySet()) {
            map.put(entry.getKey(), entry.getValue().getBeanType());
        }
        return map;
    }

    /**
     * Map an {@link ActionBean} to a URL.
     * 
     * @param beanType the {@link ActionBean} class
     * @param binding the URL binding
     */
    public void addBinding(Class<? extends ActionBean> beanType, UrlBinding binding) {
        // The binding path with trailing slash, used several times below
        String pathPlusSlash = binding.getPath();
        if (!pathPlusSlash.endsWith("/"))
            pathPlusSlash = pathPlusSlash + '/';

        // Wire some paths directly to the ActionBean (path, path + /, path + suffix, etc.)
        cachePath(binding.getPath(), binding);
        if (!binding.getPath().equals(pathPlusSlash))
            cachePath(pathPlusSlash, binding);
        if (binding.getSuffix() != null)
            cachePath(binding.getPath() + binding.getSuffix(), binding);
        if (!binding.toString().equals(binding.getPath()))
            cachePath(binding.toString(), binding);

        // Pick out the first component if it is a literal
        String leadingLiteral = null;
        List<Object> components = binding.getComponents();
        if (components != null && !components.isEmpty() && components.get(0) instanceof String)
            leadingLiteral = (String) components.get(0);

        // Map some prefixes to the binding
        String pathPlusLiteral = binding.getPath() + leadingLiteral;
        if (leadingLiteral != null)
            cachePrefix(pathPlusLiteral, binding);
        if (!pathPlusSlash.equals(pathPlusLiteral))
            cachePrefix(pathPlusSlash, binding);

        // Map the ActionBean to its binding
        classCache.put(beanType, binding);
    }

    /**
     * Map a path directly to a binding. If the path matches more than one binding, then a warning
     * will be logged indicating such a condition, and the path will not be cached for any binding.
     * 
     * @param path The path to cache
     * @param binding The binding to which the path should map
     */
    protected void cachePath(String path, UrlBinding binding) {
        if (pathCache.containsKey(path)) {
            UrlBinding conflict = pathCache.put(path, null);
            List<String> list = pathConflicts.get(path);
            if (list == null) {
                list = new ArrayList<String>();
                list.add(conflict.toString());
                pathConflicts.put(path, list);
            }
            log.warn("The path ", path, " for ", binding.getBeanType().getName(), " @ ", binding,
                    " conflicts with ", list);
            list.add(binding.toString());
        }
        else {
            log.debug("Wiring path ", path, " to ", binding.getBeanType().getName(), " @ ", binding);
            pathCache.put(path, binding);
        }
    }

    /**
     * Add a binding to the set of bindings associated with a prefix.
     * 
     * @param prefix The prefix to cache
     * @param binding The binding to map to the prefix
     */
    protected void cachePrefix(String prefix, UrlBinding binding) {
        log.debug("Wiring prefix ", prefix, "* to ", binding.getBeanType().getName(), " @ ", binding);

        // Look up existing set of bindings to which the prefix maps
        Set<UrlBinding> bindings = prefixCache.get(prefix);

        // If necessary, create and store a new set of bindings
        if (bindings == null) {
            bindings = new TreeSet<UrlBinding>(new Comparator<UrlBinding>() {
                public int compare(UrlBinding o1, UrlBinding o2) {
                    int cmp = o1.getComponents().size() - o2.getComponents().size();
                    if (cmp == 0)
                        cmp = o1.toString().compareTo(o2.toString());
                    return cmp;
                }
            });
            prefixCache.put(prefix, bindings);
        }

        // Add the binding to the set
        bindings.add(binding);
    }

    /**
     * Parse a binding pattern and create a {@link UrlBinding} object.
     * 
     * @param beanType the {@link ActionBean} type whose binding is to be parsed
     * @return a {@link UrlBinding}
     * @throws ParseException if the pattern cannot be parsed
     */
    protected static UrlBinding parseUrlBinding(Class<? extends ActionBean> beanType) {
        // check that class is annotated
        net.sourceforge.stripes.action.UrlBinding annotation = beanType
                .getAnnotation(net.sourceforge.stripes.action.UrlBinding.class);
        if (annotation == null)
            return null;

        // check that value is not null or empty
        String pattern = annotation.value();
        if (pattern == null || pattern.length() < 1)
            return null;

        // parse the pattern
        String path = null;
        List<Object> components = new ArrayList<Object>();
        int braceLevel = 0;
        boolean escape = false;
        char[] chars = pattern.toCharArray();
        StringBuilder buf = new StringBuilder(pattern.length());
        char c = 0;
        for (int i = 0; i < chars.length; i++) {
            c = chars[i];
            if (!escape) {
                switch (c) {
                case '{':
                    ++braceLevel;
                    if (braceLevel == 1) {
                        if (path == null) {
                            // extract trailing non-alphanum chars as a literal to trim the path
                            int end = buf.length() - 1;
                            while (end >= 0 && !Character.isJavaIdentifierPart(buf.charAt(end)))
                                --end;
                            if (end < 0) {
                                path = buf.toString();
                            }
                            else {
                                ++end;
                                path = buf.substring(0, end);
                                components.add(buf.substring(end));
                            }
                        }
                        else {
                            components.add(buf.toString());
                        }
                        buf.setLength(0);
                        continue;
                    }
                    break;
                case '}':
                    if (braceLevel > 0) {
                        --braceLevel;
                    }
                    if (braceLevel == 0) {
                        components.add(parseUrlBindingParameter(beanType, buf.toString()));
                        buf.setLength(0);
                        continue;
                    }
                    break;
                case '\\':
                    escape = true;
                    continue;
                }
            }

            // append the char
            buf.append(c);
            escape = false;
        }

        // handle whatever is left
        if (buf.length() > 0) {
            if (escape)
                throw new ParseException(pattern, "Expression must not end with escape character");
            else if (braceLevel > 0)
                throw new ParseException(pattern, "Unterminated left brace ('{') in expression");
            else if (path == null)
                path = buf.toString();
            else if (c == '}')
                components.add(parseUrlBindingParameter(beanType, buf.toString()));
            else
                components.add(buf.toString());
        }

        return new UrlBinding(beanType, path, components);
    }

    /**
     * Parses a parameter specification into name and default value and returns a
     * {@link UrlBindingParameter} with the corresponding name and default value properties set
     * accordingly.
     * 
     * @param beanClass the bean class to which the binding applies
     * @param string the parameter string
     * @return a parameter object
     * @throws ParseException if the pattern cannot be parsed
     */
    protected static UrlBindingParameter parseUrlBindingParameter(
            Class<? extends ActionBean> beanClass, String string) {
        char[] chars = string.toCharArray();
        char c = 0;
        boolean escape = false;
        StringBuilder name = new StringBuilder();
        StringBuilder defaultValue = new StringBuilder();
        StringBuilder current = name;
        for (int i = 0; i < chars.length; i++) {
            c = chars[i];
            if (!escape) {
                switch (c) {
                case '\\':
                    escape = true;
                    continue;
                case '=':
                    current = defaultValue;
                    continue;
                }
            }

            current.append(c);
            escape = false;
        }

        String dflt = defaultValue.length() < 1 ? null : defaultValue.toString();
        if (dflt != null && UrlBindingParameter.PARAMETER_NAME_EVENT.equals(name.toString())) {
            throw new ParseException(string, "In ActionBean class " + beanClass.getName()
                    + ", the " + UrlBindingParameter.PARAMETER_NAME_EVENT
                    + " parameter may not be assigned a default value. Its default value is"
                    + " determined by the @DefaultHandler annotation.");
        }
        return new UrlBindingParameter(beanClass, name.toString(), null, dflt) {
            @Override
            public String getValue() {
                throw new UnsupportedOperationException(
                        "getValue() is not implemented for URL parameter prototypes");
            }
        };
    }

    /**
     * Returns the URI of the given {@code request} with the context path trimmed from the
     * beginning. I.e., the request URI relative to the context.
     * 
     * @param request a servlet request
     * @return the context-relative request URI
     * @deprecated Use {@link HttpUtil#getRequestedPath(HttpServletRequest)} instead.
     */
    @Deprecated
    protected String trimContextPath(HttpServletRequest request) {
        // Trim context path from beginning of URI
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath.length() > 1)
            uri = uri.substring(contextPath.length());

        // URL decode
        try {
            String encoding = request.getCharacterEncoding();
            uri = URLDecoder.decode(uri, encoding != null ? encoding : "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new StripesRuntimeException(e);
        }

        return uri;
    }

    @Override
    public String toString() {
        return String.valueOf(classCache);
    }
}
