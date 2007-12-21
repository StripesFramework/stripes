/**
 * $Id$
 * $Name$
 *
 * Created on Jun 8, 2007 at 10:18:03 AM by Ben Gunter.
 * 
 * Copyright 2007 Cpons.com, Inc. All rights reserved.
 */
package net.sourceforge.stripes.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.exception.StripesRuntimeException;
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

    /** Holds the set of paths that are cached, sorted from longest to shortest */
    private final Set<String> pathSet = new TreeSet<String>(new Comparator<String>() {
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
     * Examines a URI (as might be returned by {@link HttpServletRequest#getRequestURI()}) and
     * returns the associated binding prototype, if any. No attempt is made to extract parameter
     * values from the URI. This is intended as a fast means to get static information associated
     * with a given request URI.
     * 
     * @param uri a request URI
     * @return a binding prototype, or null if the URI does not match
     */
    public UrlBinding getBindingPrototype(String uri) {
        // look up as a path first
        UrlBinding prototype = pathCache.get(uri);
        if (prototype != null)
            return prototype;

        // if not found, then find longest matching path
        for (String path : pathSet) {
            if (uri.startsWith(path)) {
                prototype = pathCache.get(path);
                break;
            }
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
        return getBindingPrototype(request.getRequestURI());
    }

    /**
     * Examines a URI (as might be returned by {@link HttpServletRequest#getRequestURI()}) and
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
        while (uri.charAt(length - 1) == '/')
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
        try {
            // get character encoding
            String charset = request.getCharacterEncoding();
            if (charset == null)
                charset = "UTF-8";

            // trim and decode the request URI
            String uri = request.getRequestURI();
            String contextPath = request.getContextPath();
            if (contextPath != null && contextPath.length() > 0)
                uri = uri.substring(contextPath.length());
            uri = URLDecoder.decode(uri, charset);

            // look up the binding by the URI
            return getBinding(uri);
        }
        catch (UnsupportedEncodingException e) {
            throw new StripesRuntimeException(e);
        }
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
        pathCache.put(binding.getPath(), binding);
        pathSet.add(binding.getPath());
        classCache.put(beanType, binding);
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
                throw new ParseException("Expression must not end with escape character", pattern);
            else if (braceLevel > 0)
                throw new ParseException("Unterminated left brace ('{') in expression", pattern);
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
        return new UrlBindingParameter(beanClass, name.toString(), null, dflt) {
            @Override
            public String getValue() {
                throw new UnsupportedOperationException(
                        "getValue() is not implemented for URL parameter prototypes");
            }
        };
    }

    @Override
    public String toString() {
        return String.valueOf(classCache);
    }
}
