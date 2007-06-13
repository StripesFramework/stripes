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
package net.sourceforge.stripes.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.controller.UrlBinding;
import net.sourceforge.stripes.controller.UrlBindingFactory;
import net.sourceforge.stripes.controller.UrlBindingParameter;
import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.format.Formatter;
import net.sourceforge.stripes.format.FormatterFactory;

/**
 * <p>Simple class that encapsulates the process of building up a URL from a path fragment
 * and a zero or more parameters.  Parameters can be single valued, array valued or
 * collection valued.  In the case of arrays and collections, each value in the array or
 * collection will be added as a separate URL parameter (with the same name). The assembled
 * URL can then be retrieved by calling toString().</p>
 *
 * <p>While not immediately obvious, it is possible to add a single parameter with multiple
 * values by invoking the addParameter() method that uses varargs, and supplying a Collection as
 * the single parameter value to the method.</p>
 *
 * @author Tim Fennell
 * @since Stripes 1.1.2
 */
public class UrlBuilder {
    /**
     * Holds the name and value of a parameter to be appended to the URL.
     */
    private static class Parameter {
        String name;
        Object value;
        boolean skip;

        Parameter(String name, Object value) {
            this.name = name;
            this.value = value;
        }
    }

    private Class<? extends ActionBean> beanType;
    private String baseUrl;
    private String anchor;
    private Locale locale;
    private String parameterSeparator;
    private List<Parameter> parameters = new ArrayList<Parameter>();
    private String url;

    /**
     * Constructs a UrlBuilder with the path to a resource. Parameters can be added
     * later using addParameter().  If the link is to be used in a page then the ampersand
     * character usually used to separate parameters will be escaped using the XML entity
     * for ampersand.
     *
     * @param url the path part of the URL
     * @param isForPage true if the URL is to be embedded in a page (e.g. in an anchor of img
     *        tag), false if for some other purpose.
     * @deprecated As of Stripes 1.5, this constructor has been replaced by
     *             {@link #UrlBuilder(Locale, String, boolean)}.
     */
    @Deprecated
    public UrlBuilder(String url, boolean isForPage) {
        this(Locale.getDefault(), url, isForPage);
    }

    /**
     * Constructs a UrlBuilder with the path to a resource. Parameters can be added
     * later using addParameter().  If the link is to be used in a page then the ampersand
     * character usually used to separate parameters will be escaped using the XML entity
     * for ampersand.
     *
     * @param locale the locale to use when formatting parameters with a {@link Formatter}
     * @param url the path part of the URL
     * @param isForPage true if the URL is to be embedded in a page (e.g. in an anchor of img
     *        tag), false if for some other purpose.
     */
    public UrlBuilder(Locale locale, String url, boolean isForPage) {
        this(locale, isForPage);
        if (url != null) {
            // Check to see if there is an embedded anchor, and strip it out for later
            int index = url.indexOf('#');
            if (index != -1) {
                if (index < url.length() - 1) {
                    this.anchor = url.substring(index + 1);
                }
                url = url.substring(0, index);
            }

            this.baseUrl = url;
        }
    }

    /**
     * Constructs a UrlBuilder that references an {@link ActionBean}. Parameters can be added later
     * using addParameter(). If the link is to be used in a page then the ampersand character
     * usually used to separate parameters will be escaped using the XML entity for ampersand.
     * 
     * @param locale the locale to use when formatting parameters with a {@link Formatter}
     * @param beanType {@link ActionBean} class for which the URL will be built
     * @param isForPage true if the URL is to be embedded in a page (e.g. in an anchor of img tag),
     *            false if for some other purpose.
     */
    public UrlBuilder(Locale locale, Class<? extends ActionBean> beanType, boolean isForPage) {
        this(locale, isForPage);
        this.beanType = beanType;
    }

    /**
     * Sets the locale and sets the parameter separator based on the value of <code>isForPage</code>.
     * 
     * @param locale the locale to use when formatting parameters with a {@link Formatter}
     * @param isForPage true if the URL is to be embedded in a page (e.g. in an anchor of img tag),
     *            false if for some other purpose.
     */
    protected UrlBuilder(Locale locale, boolean isForPage) {
        this.locale = locale;
        if (isForPage) {
            this.parameterSeparator = "&amp;";
        }
        else {
            this.parameterSeparator = "&";
        }
    }

    /**
     * Returns the string that will be used to separate parameters in the query string.
     * Will usually be either '&amp;' for query strings that will be embedded in HTML
     * pages and '&' otherwise.
     */
    public String getParameterSeparator() { return parameterSeparator; }

    /**
     * Sets the string that will be used to separate parameters. By default the values is a
     * single ampersand character. If the URL is to be embedded in a page the value should be
     * set to the XML ampersand entity.
     */
    public void setParameterSeparator(String parameterSeparator) {
        this.parameterSeparator = parameterSeparator;
    }

    /**
     * <p>Appends one or more values of a parameter to the URL. Checks to see if each value is
     * null, and if so generates a parameter with no value.  URL Encodes the parameter values
     * to make sure that it is safe to insert into the URL.</p>
     *
     * <p>If any parameter value passed is a Collection or an Array then this method is called
     * recursively with the contents of the collection or array. As a result you can pass
     * arbitrarily nested arrays and collections to this method and it will recurse through them
     * adding all scalar values as parameters to the URL.</p.
     *
     * @param name the name of the request parameter being added
     * @param values one or more values for the parameter supplied
     */
    public void addParameter(String name, Object... values) {
        // If values is null or empty, then simply sub in a single empty string
        if (values == null || values.length == 0) {
            values = Literal.array("");
        }

        for (Object v : values) {
            // Special case: recurse for nested collections and arrays!
            if (v instanceof Collection) {
                addParameter(name, ((Collection) v).toArray());
            }
            else if (v != null && v.getClass().isArray()) {
                addParameter(name, CollectionUtil.asObjectArray(v));
            }
            else {
                parameters.add(new Parameter(name, v));
                url = null;
            }
        }
    }

    /**
     * Appends one or more parameters to the URL.  Various assumptions are made about the Map
     * parameter. Firstly, that the keys are all either Strings, or objects that can be safely
     * toString()'d to yield parameter names.  Secondly that the values either toString() to form
     * a single parameter value, or are arrays or collections that contain toString()'able
     * objects.
     *
     * @param parameters a non-null Map as described above
     */
    public void addParameters(Map<? extends Object,? extends Object> parameters) {
        for (Map.Entry<? extends Object,? extends Object> parameter : parameters.entrySet()) {
            String name = parameter.getKey().toString();
            Object valueOrValues = parameter.getValue();

            if (valueOrValues == null) {
                addParameter(name, (Object) null);
            }
            else if (valueOrValues.getClass().isArray()) {
                addParameter(name, CollectionUtil.asObjectArray(valueOrValues));
            }
            else if (valueOrValues instanceof Collection) {
                addParameter(name, (Collection) valueOrValues);
            }
            else {
                addParameter(name, valueOrValues);
            }
        }
    }

    /**
     * Gets the anchor, if any, that will be appended to the URL. E.g. if this method
     * returns 'input' then the URL will be terminated with '#input' in order to instruct
     * the browser to navigate to the HTML anchor callled 'input' when accessing the URL.
     *
     * @return the anchor (if any) without the leading pound sign, or null
     */
    public String getAnchor() { return anchor; }

    /**
     * Sets the anchor, if any, that will be appended to the URL. E.g. if supplied with
     * 'input' then the URL will be terminated with '#input' in order to instruct
     * the browser to navigate to the HTML anchor callled 'input' when accessing the URL.
     *
     * @param anchor the anchor with or without the leading pound sign, or null to disable
     */
    public void setAnchor(String anchor) {
        if (anchor != null && anchor.startsWith("#") && anchor.length() > 1) {
            this.anchor = anchor.substring(1);
        }
        else {
            this.anchor = anchor;
        }
    }

    /**
     * Returns the URL composed thus far as a String.  All paramter values will have been
     * URL encoded and appended to the URL before returning it.
     */
    @Override
    public String toString() {
        if (url == null) {
            url = build();
        }
        if (this.anchor != null && this.anchor.length() > 0) {
            return url + "#" + this.anchor;
        }
        else {
            return url;
        }
    }

    /**
     * Attempts to format an object using an appropriate {@link Formatter}. If
     * no formatter is available for the object, then this method will call
     * <code>toString()</code> on the object. A null <code>value</code> will
     * be formatted as an empty string.
     * 
     * @param value
     *            the object to be formatted
     * @return the formatted value
     */
    @SuppressWarnings("unchecked")
    protected String format(Object value) {
        if (value == null) {
            return "";
        }
        else {
            Formatter formatter = getFormatter(value);
            if (formatter == null)
                return value.toString();
            else
                return formatter.format(value);
        }
    }

    /**
     * Tries to get a formatter for the given value using the {@link FormatterFactory}. Returns
     * null if there is no {@link Configuration} or {@link FormatterFactory} available (e.g. in a
     * test environment) or if there is no {@link Formatter} configured for the value's type.
     * 
     * @param value the object to be formatted
     * @return a formatter, if one can be found; null otherwise
     */
    protected Formatter getFormatter(Object value) {
        Configuration configuration = StripesFilter.getConfiguration();
        if (configuration == null)
            return null;

        FormatterFactory factory = configuration.getFormatterFactory();
        if (factory == null)
            return null;

        return factory.getFormatter(value.getClass(), locale, null, null);
    }

    /**
     * Build and return the URL
     */
    protected String build() {
        try {
            StringBuilder buffer = new StringBuilder(256);
            buffer.append(getBaseURL());
            boolean seenQuestionMark = buffer.indexOf("?") != -1;
            for (Parameter pair : parameters) {
                if (pair.skip)
                    continue;

                // Figure out whether we already have params or not
                if (!seenQuestionMark) {
                    buffer.append('?');
                    seenQuestionMark = true;
                }
                else {
                    buffer.append(getParameterSeparator());
                }
                buffer.append(pair.name);
                buffer.append('=');
                if (pair.value != null) {
                    buffer.append(URLEncoder.encode(format(pair.value), "UTF-8"));
                }
            }
            return buffer.toString();
        }
        catch (UnsupportedEncodingException uee) {
            throw new StripesRuntimeException("Unsupported encoding?  UTF-8?  That's unpossible.");
        }
    }

    /**
     * Get the base URL (without a query string). If an {@link ActionBean} class was passed to
     * {@link #UrlBuilder(Locale, Class, boolean)}, then this method will return the URL binding
     * that is mapped to that class, including any URI parameters that are available. Otherwise, it
     * returns the URL string with which this object was initialized.
     * 
     * @return the base URL, without a query string
     * @see #UrlBuilder(Locale, Class, boolean)
     * @see #UrlBuilder(Locale, String, boolean)
     */
    protected String getBaseURL() {
        if (beanType == null)
            return baseUrl;

        UrlBinding binding = UrlBindingFactory.getInstance().getBindingPrototype(beanType);
        if (binding == null) {
            return StripesFilter.getConfiguration().getActionResolver().getUrlBinding(beanType);
        }

        Map<String, Parameter> map = new HashMap<String, Parameter>();
        for (Parameter p : parameters) {
            p.skip = false;
            if (!map.containsKey(p.name))
                map.put(p.name, p);
        }

        StringBuilder buf = new StringBuilder(256);
        buf.append(binding.getPath());

        String nextLiteral = null;
        for (Object component : binding.getComponents()) {
            if (component instanceof String) {
                nextLiteral = (String) component;
            }
            else if (component instanceof UrlBindingParameter) {
                UrlBindingParameter parameter = (UrlBindingParameter) component;
                boolean ok = false;
                if (map.containsKey(parameter.getName())) {
                    Parameter assigned = map.get(parameter.getName());
                    String value = format(assigned.value);
                    if (value != null && value.length() > 0) {
                        if (nextLiteral != null) {
                            buf.append(nextLiteral);
                        }

                        buf.append(value);
                        assigned.skip = true;
                        ok = true;
                    }
                }
                nextLiteral = null;
                if (!ok)
                    break;
            }
        }
        if (nextLiteral != null) {
            buf.append(nextLiteral);
        }

        return buf.toString();
    }
}
