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
package net.sourceforge.stripes.validation;

import java.util.Locale;
import java.util.MissingResourceException;
import net.sourceforge.stripes.localization.LocalizationUtility;

/**
 * Provides a slightly more customizable approach to error messages. Where the LocalizedError class
 * looks for an error message in a single place based on the key provided, ScopedLocalizableError
 * performs a scoped search for an error message.
 *
 * <p>As an example, let's say that the IntegerConverter raises an error message with the values
 * defaultScope=<em>converter.integer</em> and key=<em>outOfRange</em>, for a field called
 * <em>age</em> on an ActionBean bound to <em>/cats/KittenDetail.action</em>. Based on this
 * information an instance of ScopedLocalizableError would fetch the resource bundle and look for
 * error message templates in the following order:
 *
 * <ul>
 *   <li>com.myco.KittenDetailActionBean.age.outOfRange
 *   <li>com.myco.KittenDetailActionBean.age.errorMessage
 *   <li>age.outOfRange
 *   <li>age.errorMessage
 *   <li>com.myco.KittenDetailActionBean.outOfRange
 *   <li>com.myco.KittenDetailActionBean.errorMessage
 *   <li>converter.integer.outOfRange
 * </ul>
 *
 * <p>Using ScopingLocalizedErrors provides application developers with the flexibility to provide
 * as much or as little specificity in error messages as desired. The scope and ordering of the
 * messages is designed to allow developers to specify default messages at several levels, and then
 * override those as needed for specific circumstances.
 *
 * @author Tim Fennell
 */
public class ScopedLocalizableError extends LocalizableError {
  private static final long serialVersionUID = 1L;

  /** Default key that is used for looking up error messages. */
  public static final String DEFAULT_NAME = "errorMessage";

  private String defaultScope;
  private String key;

  /**
   * Constructs a ScopedLocalizableError with the supplied information.
   *
   * @param defaultScope the default scope under which to look for the error message should more
   *     specifically scoped lookups fail
   * @param key the name of the message to lookup
   * @param parameters an optional number of replacement parameters to be used in the message
   */
  public ScopedLocalizableError(String defaultScope, String key, Object... parameters) {
    super(defaultScope + "." + key, parameters);
    this.defaultScope = defaultScope;
    this.key = key;
  }

  /**
   * Get the default scope that was passed into the constructor.
   *
   * @see #ScopedLocalizableError(String, String, Object...)
   */
  public String getDefaultScope() {
    return defaultScope;
  }

  /**
   * Get the key that was passed into the constructor.
   *
   * @see #ScopedLocalizableError(String, String, Object...)
   */
  public String getKey() {
    return key;
  }

  /**
   * Overrides getMessageTemplate to perform a scoped search for a message template as defined in
   * the class level javadoc.
   */
  @Override
  protected String getMessageTemplate(Locale locale) {
    String name1 = null,
        name2 = null,
        name3 = null,
        name4 = null,
        name5 = null,
        name6 = null,
        name7 = null,
        name8 = null,
        name9 = null,
        name10 = null,
        name11 = null;

    final String actionPath = getActionPath();
    final String fqn = getBeanclass().getName();
    final String fieldName = getFieldName();

    // 1. com.myco.KittenDetailActionBean.age.outOfRange
    name1 = fqn + "." + fieldName + "." + this.key;
    String template = LocalizationUtility.getErrorMessage(locale, name1);

    if (template == null) {
      // 2. com.myco.KittenDetailActionBean.age.errorMessage
      name2 = fqn + "." + fieldName + "." + DEFAULT_NAME;
      template = LocalizationUtility.getErrorMessage(locale, name2);
    }
    if (template == null) {
      // 3. /cats/KittenDetail.action.age.outOfRange
      name3 = actionPath + "." + fieldName + "." + this.key;
      template = LocalizationUtility.getErrorMessage(locale, name3);
    }
    if (template == null) {
      // 4. /cats/KittenDetail.action.age.errorMessage
      name4 = actionPath + "." + fieldName + "." + DEFAULT_NAME;
      template = LocalizationUtility.getErrorMessage(locale, name4);
    }
    if (template == null) {
      // 5. age.outOfRange
      name5 = fieldName + "." + this.key;
      template = LocalizationUtility.getErrorMessage(locale, name5);
    }
    if (template == null) {
      // 6. age.errorMessage
      name6 = fieldName + "." + DEFAULT_NAME;
      template = LocalizationUtility.getErrorMessage(locale, name6);
    }
    if (template == null) {
      // 7. com.myco.KittenDetailActionBean.outOfRange
      name7 = fqn + "." + this.key;
      template = LocalizationUtility.getErrorMessage(locale, name7);
    }
    if (template == null) {
      // 8. com.myco.KittenDetailActionBean.outOfRange
      name8 = fqn + "." + DEFAULT_NAME;
      template = LocalizationUtility.getErrorMessage(locale, name8);
    }
    if (template == null) {
      // 9. /cats/KittenDetail.action.outOfRange
      name9 = actionPath + "." + this.key;
      template = LocalizationUtility.getErrorMessage(locale, name9);
    }
    if (template == null) {
      // 10. /cats/KittenDetail.action.errorMessage
      name10 = actionPath + "." + DEFAULT_NAME;
      template = LocalizationUtility.getErrorMessage(locale, name10);
    }
    if (template == null) {
      // 11. converter.integer.outOfRange
      name11 = this.defaultScope + "." + this.key;
      template = LocalizationUtility.getErrorMessage(locale, name11);
    }

    if (template == null) {
      throw new MissingResourceException(
          "Could not find an error message with any of the following keys: "
              + "'"
              + name1
              + "', '"
              + name2
              + "', '"
              + name3
              + "', '"
              + name4
              + "', '"
              + name5
              + "', '"
              + name6
              + "', '"
              + name7
              + "'."
              + name8
              + "', '"
              + name9
              + "', '"
              + name10
              + "', '"
              + name11
              + "'.",
          null,
          null);
    }

    return template;
  }

  /** Generated equals method that checks all fields and super.equals(). */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    final ScopedLocalizableError that = (ScopedLocalizableError) o;

    if (defaultScope != null
        ? !defaultScope.equals(that.defaultScope)
        : that.defaultScope != null) {
      return false;
    }
    if (key != null ? !key.equals(that.key) : that.key != null) {
      return false;
    }

    return true;
  }

  /** Generated hashCode() method. */
  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 29 * result + (defaultScope != null ? defaultScope.hashCode() : 0);
    result = 29 * result + (key != null ? key.hashCode() : 0);
    return result;
  }
}
