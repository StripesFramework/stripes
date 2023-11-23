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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.controller.ActionResolver;
import net.sourceforge.stripes.controller.AnnotatedClassActionResolver;
import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.controller.UrlBinding;
import net.sourceforge.stripes.controller.UrlBindingParameter;
import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.exception.UrlBindingConflictException;
import net.sourceforge.stripes.format.Formatter;
import net.sourceforge.stripes.format.FormatterFactory;
import net.sourceforge.stripes.validation.ValidationMetadata;
import net.sourceforge.stripes.validation.ValidationMetadataProvider;

/**
 * Simple class that encapsulates the process of building up a URL from a path fragment and a zero
 * or more parameters. Parameters can be single valued, array valued or collection valued. In the
 * case of arrays and collections, each value in the array or collection will be added as a separate
 * URL parameter (with the same name). The assembled URL can then be retrieved by calling
 * toString().
 *
 * <p>While not immediately obvious, it is possible to add a single parameter with multiple values
 * by invoking the addParameter() method that uses varargs, and supplying a Collection as the single
 * parameter value to the method.
 *
 * @author Tim Fennell
 * @since Stripes 1.1.2
 */
public class UrlBuilder {
  /** Holds the name and value of a parameter to be appended to the URL. */
  private static class Parameter {
    String name;
    Object value;

    Parameter(String name, Object value) {
      this.name = name;
      this.value = value;
    }

    public boolean isEvent() {
      return UrlBindingParameter.PARAMETER_NAME_EVENT.equals(name);
    }

    @Override
    public String toString() {
      return this.name + "=" + this.value;
    }
  }

  private String baseUrl;
  private String anchor;
  private final Locale locale;
  private String parameterSeparator;
  private Parameter event;
  private final List<Parameter> parameters = new ArrayList<>();
  private String url;

  /**
   * Constructs a UrlBuilder with the path to a resource. Parameters can be added later using
   * addParameter(). If the link is to be used in a page then the ampersand character usually used
   * to separate parameters will be escaped using the XML entity for ampersand.
   *
   * @param locale the locale to use when formatting parameters with a {@link Formatter}
   * @param url the path part of the URL
   * @param isForPage true if the URL is to be embedded in a page (e.g. in an anchor of img tag),
   *     false if for some other purpose.
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
   * using addParameter(). If the link is to be used in a page then the ampersand character usually
   * used to separate parameters will be escaped using the XML entity for ampersand.
   *
   * @param locale the locale to use when formatting parameters with a {@link Formatter}
   * @param beanType {@link ActionBean} class for which the URL will be built
   * @param isForPage true if the URL is to be embedded in a page (e.g. in an anchor of img tag),
   *     false if for some other purpose.
   */
  public UrlBuilder(Locale locale, Class<? extends ActionBean> beanType, boolean isForPage) {
    this(locale, isForPage);
    Configuration configuration = StripesFilter.getConfiguration();
    if (configuration != null) {
      this.baseUrl = configuration.getActionResolver().getUrlBinding(beanType);
    } else {
      throw new StripesRuntimeException(
          "Unable to lookup URL binding for ActionBean class "
              + "because there is no Configuration object available.");
    }
  }

  /**
   * Sets the locale and sets the parameter separator based on the value of <code>isForPage</code>.
   *
   * @param locale the locale to use when formatting parameters with a {@link Formatter}
   * @param isForPage true if the URL is to be embedded in a page (e.g. in an anchor of img tag),
   *     false if for some other purpose.
   */
  protected UrlBuilder(Locale locale, boolean isForPage) {
    this.locale = locale;
    if (isForPage) {
      this.parameterSeparator = "&amp;";
    } else {
      this.parameterSeparator = "&";
    }
  }

  /** Get the name of the event to be executed by this URL. */
  public String getEvent() {
    return (String) (event == null ? null : event.value);
  }

  /**
   * Set the name of the event to be executed by this URL. A default event name can be defined for
   * an {@link ActionBean} using either {@link DefaultHandler} or by assigning a default value to
   * the $event parameter in a clean URL. If {@code event} is null then the default event name will
   * be ignored and no event parameter will be added to the URL.
   *
   * @param event the event name
   */
  public UrlBuilder setEvent(String event) {
    this.event = new Parameter(UrlBindingParameter.PARAMETER_NAME_EVENT, event);
    this.url = null;
    return this;
  }

  /**
   * Returns the string that will be used to separate parameters in the query string. Will usually
   * be either {@code '&amp;'} for query strings that will be embedded in HTML pages and {@code '&'}
   * otherwise.
   */
  public String getParameterSeparator() {
    return parameterSeparator;
  }

  /**
   * Sets the string that will be used to separate parameters. By default, the values is a single
   * ampersand character. If the URL is to be embedded in a page the value should be set to the XML
   * ampersand entity.
   */
  public void setParameterSeparator(String parameterSeparator) {
    this.parameterSeparator = parameterSeparator;
  }

  /**
   * Appends one or more values of a parameter to the URL. Checks to see if each value is null, and
   * if so generates a parameter with no value. URL Encodes the parameter values to make sure that
   * it is safe to insert into the URL.
   *
   * <p>If any parameter value passed is a Collection or an Array then this method is called
   * recursively with the contents of the collection or array. As a result you can pass arbitrarily
   * nested arrays and collections to this method, and it will recurse through them adding all
   * scalar values as parameters to the URL.
   *
   * @param name the name of the request parameter being added
   * @param values one or more values for the parameter supplied
   * @return this UrlBuilder, for chaining
   */
  public UrlBuilder addParameter(String name, Object... values) {
    // If values is null or empty, then simply sub in a single empty string
    if (values == null || values.length == 0) {
      values = Literal.array("");
    }

    for (Object v : values) {
      // Special case: recurse for nested collections and arrays!
      if (v instanceof Collection<?>) {
        addParameter(name, ((Collection<?>) v).toArray());
      } else if (v != null && v.getClass().isArray()) {
        addParameter(name, CollectionUtil.asObjectArray(v));
      } else {
        parameters.add(new Parameter(name, v));
        url = null;
      }
    }

    return this;
  }

  /**
   * Appends one or more parameters to the URL. Various assumptions are made about the Map
   * parameter. Firstly, that the keys are all either Strings, or objects that can be safely
   * toString()'d to yield parameter names. Secondly that the values either toString() to form a
   * single parameter value, or are arrays or collections that contain objects with toString()
   * representations.
   *
   * @param parameters a non-null Map as described above
   */
  public UrlBuilder addParameters(Map<?, ?> parameters) {
    for (Map.Entry<?, ?> parameter : parameters.entrySet()) {
      String name = parameter.getKey().toString();
      Object valueOrValues = parameter.getValue();

      if (valueOrValues == null) {
        addParameter(name, (Object) null);
      } else if (valueOrValues.getClass().isArray()) {
        addParameter(name, CollectionUtil.asObjectArray(valueOrValues));
      } else if (valueOrValues instanceof Collection<?>) {
        addParameter(name, valueOrValues);
      } else {
        addParameter(name, valueOrValues);
      }
    }

    return this;
  }

  /**
   * Gets the anchor, if any, that will be appended to the URL. E.g. if this method returns 'input'
   * then the URL will be terminated with '#input' in order to instruct the browser to navigate to
   * the HTML anchor called 'input' when accessing the URL.
   *
   * @return the anchor (if any) without the leading pound sign, or null
   */
  public String getAnchor() {
    return anchor;
  }

  /**
   * Sets the anchor, if any, that will be appended to the URL. E.g. if supplied with 'input' then
   * the URL will be terminated with '#input' in order to instruct the browser to navigate to the
   * HTML anchor called 'input' when accessing the URL.
   *
   * @param anchor the anchor with or without the leading pound sign, or null to disable
   */
  public UrlBuilder setAnchor(String anchor) {
    if (anchor != null && anchor.startsWith("#") && anchor.length() > 1) {
      this.anchor = anchor.substring(1);
    } else {
      this.anchor = anchor;
    }
    return this;
  }

  /**
   * Returns the URL composed thus far as a String. All parameter values will have been URL encoded
   * and appended to the URL before returning it.
   */
  @Override
  public String toString() {
    if (url == null) {
      url = build();
    }
    if (this.anchor != null && !this.anchor.isEmpty()) {
      return url + "#" + StringUtil.uriFragmentEncode(this.anchor);
    } else {
      return url;
    }
  }

  /**
   * Attempts to format an object using an appropriate {@link Formatter}. If no formatter is
   * available for the object, then this method will call <code>toString()</code> on the object. A
   * null <code>value</code> will be formatted as an empty string.
   *
   * @param value the object to be formatted
   * @return the formatted value
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  protected String format(Object value) {
    if (value == null) {
      return "";
    } else {
      Formatter formatter = getFormatter(value);
      if (formatter == null) return value.toString();
      else return formatter.format(value);
    }
  }

  /**
   * Tries to get a formatter for the given value using the {@link FormatterFactory}. Returns null
   * if there is no {@link Configuration} or {@link FormatterFactory} available (e.g. in a test
   * environment) or if there is no {@link Formatter} configured for the value's type.
   *
   * @param value the object to be formatted
   * @return a formatter, if one can be found; null otherwise
   */
  @SuppressWarnings("rawtypes")
  protected Formatter getFormatter(Object value) {
    Configuration configuration = StripesFilter.getConfiguration();
    if (configuration == null) return null;

    FormatterFactory factory = configuration.getFormatterFactory();
    if (factory == null) return null;

    return factory.getFormatter(value.getClass(), locale, null, null);
  }

  /**
   * Get a map of property names to {@link ValidationMetadata} for the {@link ActionBean} class
   * bound to the URL being built. If the URL does not point to an ActionBean class or no validation
   * metadata exists for the ActionBean class then an empty map will be returned.
   *
   * @return a map of ActionBean property names to their validation metadata
   * @see ValidationMetadataProvider#getValidationMetadata(Class)
   */
  protected Map<String, ValidationMetadata> getValidationMetadata() {
    Map<String, ValidationMetadata> validations = null;
    Configuration configuration = StripesFilter.getConfiguration();
    if (configuration != null) {
      Class<? extends ActionBean> beanType = null;
      try {
        beanType = configuration.getActionResolver().getActionBeanType(this.baseUrl);
      } catch (UrlBindingConflictException e) {
        // This can be safely ignored
      }

      if (beanType != null) {
        validations = configuration.getValidationMetadataProvider().getValidationMetadata(beanType);
      }
    }

    if (validations == null) validations = Collections.emptyMap();

    return validations;
  }

  /** Build and return the URL */
  protected String build() {
    // special handling for event parameter
    List<Parameter> parameters = new ArrayList<>(this.parameters.size() + 1);
    if (this.event != null) {
      parameters.add(this.event);
    }
    parameters.addAll(this.parameters);

    // lookup validation info for the bean class to find encrypted properties
    Map<String, ValidationMetadata> validations = getValidationMetadata();

    StringBuilder buffer = new StringBuilder(256);
    buffer.append(getBaseURL(this.baseUrl, parameters));
    boolean seenQuestionMark = buffer.indexOf("?") != -1;
    for (Parameter param : parameters) {
      // special handling for event parameter
      if (param == this.event) {
        if (param.value == null) continue;
        else param = new Parameter((String) this.event.value, "");
      }

      // Figure out whether we already have params or not
      if (!seenQuestionMark) {
        buffer.append('?');
        seenQuestionMark = true;
      } else {
        buffer.append(getParameterSeparator());
      }
      buffer.append(StringUtil.urlEncode(param.name)).append('=');
      if (param.value != null) {
        ValidationMetadata validation = validations.get(param.name);
        String formatted = format(param.value);
        if (validation != null && validation.encrypted()) formatted = CryptoUtil.encrypt(formatted);
        buffer.append(StringUtil.urlEncode(formatted));
      }
    }
    return buffer.toString();
  }

  /**
   * Get the base URL (without a query string). If a {@link UrlBinding} exists for the URL or {@link
   * ActionBean} type that was passed into the constructor, then this method will return the base
   * URL after appending any URI parameters that have been added with a call to {@link
   * #addParameter(String, Object[])} or {@link #addParameters(Map)}. Otherwise, it returns the
   * original base URL.
   *
   * @param baseUrl The base URL to start with. In many cases, this value will be returned
   *     unchanged.
   * @param parameters The query parameters. Any parameters that should not be appended to the query
   *     string by {@link #build()} (e.g., because they are embedded in the URL) should be removed
   *     from the collection before this method returns.
   * @return the base URL, without a query string
   * @see #UrlBuilder(Locale, Class, boolean)
   * @see #UrlBuilder(Locale, String, boolean)
   */
  @SuppressWarnings("ClassEscapesDefinedScope")
  protected String getBaseURL(String baseUrl, Collection<Parameter> parameters) {
    ActionResolver resolver = StripesFilter.getConfiguration().getActionResolver();
    if (!(resolver instanceof AnnotatedClassActionResolver)) return baseUrl;

    UrlBinding binding = null;
    try {
      binding =
          ((AnnotatedClassActionResolver) resolver)
              .getUrlBindingFactory()
              .getBindingPrototype(baseUrl);
    } catch (UrlBindingConflictException e) {
      // This can be safely ignored
    }

    if (binding == null || binding.getParameters().size() == 0) {
      return baseUrl;
    }

    // if we have a parameterized binding then we need to trim it down to the path
    if (baseUrl.equals(binding.toString())) {
      baseUrl = binding.getPath();
    }

    // if any extra path info is present then do not add URI parameters
    if (binding.getPath().length() < baseUrl.length()) {
      return baseUrl;
    }

    // lookup validation info for the bean class to find encrypted properties
    Map<String, ValidationMetadata> validations = getValidationMetadata();

    // map the declared URI parameter names to values
    Map<String, Parameter> map = new HashMap<>();
    for (Parameter p : parameters) {
      if (!map.containsKey(p.name)) map.put(p.name, p);
    }

    StringBuilder buf = new StringBuilder(256);
    buf.append(baseUrl);

    String nextLiteral = null;
    for (Object component : binding.getComponents()) {
      if (component instanceof String) {
        nextLiteral = (String) component;
      } else if (component instanceof UrlBindingParameter parameter) {
        boolean ok = false;

        // get the value for the parameter, falling back to default value if present
        Parameter assigned = map.get(parameter.getName());
        Object value;
        if (assigned != null && (assigned.value != null || assigned.isEvent()))
          value = assigned.value;
        else value = parameter.getDefaultValue();

        if (value != null) {
          // format (and maybe encrypt) the value as a string
          String formatted = format(value);
          ValidationMetadata validation = validations.get(parameter.getName());
          if (validation != null && validation.encrypted())
            formatted = CryptoUtil.encrypt(formatted);

          // if after formatting we still have a value then embed it in the URI
          if (formatted != null && !formatted.isEmpty()) {
            if (nextLiteral != null) {
              buf.append(nextLiteral);
            }

            buf.append(StringUtil.urlEncode(formatted));
            parameters.remove(assigned);
            ok = true;
          }
        } else if (assigned != null && assigned.isEvent()) {
          // remove event parameter even if value is null
          parameters.remove(assigned);
        }

        nextLiteral = null;
        if (!ok) break;
      }
    }

    // always append trailing literal if one is present
    if (nextLiteral != null) {
      buf.append(nextLiteral);
    } else if (binding.getSuffix() != null) {
      buf.append(binding.getSuffix());
    }

    // Test the URL to make sure it won't throw an exception when Stripes tries to dispatch it
    String url = buf.toString();
    try {
      StripesFilter.getConfiguration().getActionResolver().getActionBeanType(url);
    } catch (UrlBindingConflictException e) {
      UrlBindingConflictException tmp =
          new UrlBindingConflictException(binding.getBeanType(), e.getPath(), e.getMatches());
      tmp.setStackTrace(e.getStackTrace());
      throw tmp;
    }
    return url;
  }
}
