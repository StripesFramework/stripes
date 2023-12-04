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

import java.util.Collection;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * A faux TypeConverter that validates that the String supplied is a valid email address. Relies on
 * javax.mail.internet.InternetAddress for the bulk of the work (note that this means in order to
 * use this type converter you must have JavaMail available in your classpath).
 *
 * <p>If the String cannot be parsed, or it represents a "local" address (one with no @domain) a
 * single error message will be generated. The error message is a scoped message with a default
 * scope of <tt>converter.email</tt> and name <tt>invalidEmail</tt>. As a result error messages will
 * be looked for in the following order:
 *
 * <ul>
 *   <li>beanClassFQN.fieldName.invalidEmail
 *   <li>actionPath.fieldName.invalidEmail
 *   <li>fieldName.invalidEmail
 *   <li>beanClassFQN.invalidEmail
 *   <li>actionPath.invalidEmail
 *   <li>converter.email.invalidEmail
 * </ul>
 *
 * @author Tim Fennell
 * @since Stripes 1.2
 */
public class EmailTypeConverter implements TypeConverter<String> {

  /**
   * This RegEx is taken from the HTML5 <a
   * href="https://html.spec.whatwg.org/multipage/input.html#valid-e-mail-address">spec</a>.
   */
  private static final Pattern EMAIL_VALIDATION_REGEX =
      Pattern.compile(
          "[ \\a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[[\\[]a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[]a-zA-Z0-9])?)*");
  /** Accepts the Locale provided, but does nothing with it since emails are Locale-less. */
  public void setLocale(Locale locale) {
    /** Doesn't matter for email. */
  }

  /**
   * Validates the user input to ensure that it is a valid email address.
   *
   * @param input the String input, always a non-null non-empty String
   * @param targetType realistically always String since java.lang.String is final
   * @param errors a non-null collection of errors to populate in case of error
   * @return the parsed address, or null if there are no errors. Note that the parsed address may be
   *     different from the input if extraneous characters were removed.
   */
  public String convert(
      String input, Class<? extends String> targetType, Collection<ValidationError> errors) {

    // Used to test for more than one @ symbol
    int atSymbolOccurrences = (input.length() - input.replace("@", "").length());

    // Used to test for no dots after the last @ sign
    boolean noDotsAfterLastAtSign =
        input.contains("@") && !input.substring(input.lastIndexOf("@")).contains(".");

    // Used to test for starts with a dot
    boolean startsWithDot = input.split("@")[0].startsWith(".");

    // Used to test for ends with a dot
    boolean endsWithDot = input.split("@")[0].endsWith(".");

    // Used to test for two consecutive dots
    boolean twoConsecutiveDots = input.split("@")[0].contains("..");

    if (atSymbolOccurrences > 1
        || noDotsAfterLastAtSign
        || startsWithDot
        || endsWithDot
        || twoConsecutiveDots
        || !EMAIL_VALIDATION_REGEX.matcher(input).matches()) {
      errors.add(new ScopedLocalizableError("converter.email", "invalidEmail"));
    }

    return input;
  }
}
