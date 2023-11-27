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
package net.sourceforge.stripes.localization;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.controller.ParameterName;
import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.validation.ValidationMetadata;

/**
 * Provides simple localization utility methods that are used in multiple places in the Stripes code
 * base.
 *
 * @author Tim Fennell
 * @since Stripes 1.1
 */
public class LocalizationUtility {
  private static final Log log = Log.getInstance(LocalizationUtility.class);

  /**
   * Fetches the localized name for a form field if one exists in the form field resource bundle. If
   * for any reason a localized value cannot be found (e.g. the bundle cannot be found, or does not
   * contain the required properties) then null will be returned.
   *
   * <p>Looks first for a property called {@code beanClassFQN.fieldName} in the resource bundle. If
   * that is undefined, it next looks for {@code actionPath.fieldName} and if not defined, looks for
   * a property called {@code fieldName}. Will strip any indexing from the field name prior to using
   * it to construct property names (e.g. foo[12] will become simply foo).
   *
   * @param fieldName The name of the field whose localized name to look up
   * @param actionPath The action path of the form in which the field is nested. If for some reason
   *     this is not available, null may be passed without causing errors.
   * @param locale The desired locale of the looked up name.
   * @return a localized field name if one can be found, or null otherwise.
   */
  public static String getLocalizedFieldName(
      String fieldName, String actionPath, Class<? extends ActionBean> beanclass, Locale locale) {

    ParameterName parameterName = new ParameterName(fieldName);
    String strippedName = parameterName.getStrippedName();
    String localizedValue = null;
    ResourceBundle bundle = null;

    try {
      bundle =
          StripesFilter.getConfiguration()
              .getLocalizationBundleFactory()
              .getFormFieldBundle(locale);
    } catch (MissingResourceException mre) {
      log.error(mre);
      return null;
    }

    // First with the bean class
    if (beanclass != null) {
      try {
        localizedValue = bundle.getString(beanclass.getName() + "." + strippedName);
      } catch (MissingResourceException mre) {
        /* do nothing */
      }
    }

    // Then all by itself
    if (localizedValue == null) {
      try {
        localizedValue = bundle.getString(strippedName);
      } catch (MissingResourceException mre2) {
        /* do nothing */
      }
    }

    // Lastly, check @Validate on the ActionBean property
    if (localizedValue == null && beanclass != null) {
      ValidationMetadata validate =
          StripesFilter.getConfiguration()
              .getValidationMetadataProvider()
              .getValidationMetadata(beanclass, parameterName);
      if (validate != null && validate.label() != null && !"".equals(validate.label())) {
        localizedValue = validate.label();
      }
    }

    return localizedValue;
  }

  /**
   * Makes a half hearted attempt to convert the property name of a field into a human friendly name
   * by breaking it on periods and upper case letters and capitalizing each word. This is only used
   * when developers do not provide names for their fields.
   *
   * @param fieldNameKey the programmatic name of a form field
   * @return String a more user friendly name for the field in the absence of anything better
   */
  public static String makePseudoFriendlyName(String fieldNameKey) {
    StringBuilder builder = new StringBuilder(fieldNameKey.length() + 10);
    char[] characters = fieldNameKey.toCharArray();
    builder.append(Character.toUpperCase(characters[0]));
    boolean upcaseNextChar = false;

    for (int i = 1; i < characters.length; ++i) {
      if (characters[i] == '.') {
        builder.append(' ');
        upcaseNextChar = true;
      } else if (Character.isUpperCase(characters[i])) {
        builder.append(' ').append(characters[i]);
        upcaseNextChar = false;
      } else if (upcaseNextChar) {
        builder.append(Character.toUpperCase(characters[i]));
        upcaseNextChar = false;
      } else {
        builder.append(characters[i]);
        upcaseNextChar = false;
      }
    }

    return builder.toString();
  }

  /**
   * Looks up the specified key in the error message resource bundle. If the bundle is missing or if
   * the resource cannot be found, will return null instead of throwing an exception.
   *
   * @param locale the locale in which to lookup the resource
   * @param key the exact resource key to lookup
   * @return the resource String or null
   */
  public static String getErrorMessage(Locale locale, String key) {
    try {
      Configuration config = StripesFilter.getConfiguration();
      ResourceBundle bundle = config.getLocalizationBundleFactory().getErrorMessageBundle(locale);
      return bundle.getString(key);
    } catch (MissingResourceException mre) {
      return null;
    }
  }

  /**
   * Gets the simple name of a class for use as a key to look up a resource. This is usually the
   * same as {@link Class#getSimpleName()}, but static inner classes are handled such that the
   * simple name is {@code OuterClass.InnerClass}. Multiple layers of nesting are supported.
   *
   * @param c The class whose simple name is requested.
   * @return The simple name of the class.
   */
  public static String getSimpleName(Class<?> c) {
    if (c.getEnclosingClass() == null) return c.getSimpleName();
    else return prefixSimpleName(new StringBuilder(), c).toString();
  }

  /** A recursive method used by {@link #getSimpleName(Class)}. */
  private static StringBuilder prefixSimpleName(StringBuilder s, Class<?> c) {
    if (c.getEnclosingClass() != null) prefixSimpleName(s, c.getEnclosingClass()).append('.');
    return s.append(c.getSimpleName());
  }
}
