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

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Collection;
import java.util.Currency;
import java.util.Locale;

/**
 * Provides the basic support for converting Strings to non-floating point numbers (i.e. shorts,
 * integers, and longs).
 *
 * @author Tim Fennell
 */
public class NumberTypeConverterSupport {
  private Locale locale;
  private NumberFormat[] formats;
  private String currencySymbol;

  /** Used by Stripes to tell the converter what locale the incoming text is in. */
  public void setLocale(Locale locale) {
    this.locale = locale;
    this.formats = getNumberFormats();

    // Use the appropriate currency symbol if our locale has a country, otherwise try the dollar
    // sign!
    this.currencySymbol = "$";
    if (locale.getCountry() != null && !"".equals(locale.getCountry())) {
      try {
        this.currencySymbol = Currency.getInstance(locale).getSymbol(locale);
      } catch (IllegalArgumentException exc) {
        // use dollar sign as default value
      }
    }
  }

  /** Returns the Locale set on the object using setLocale(). */
  public Locale getLocale() {
    return locale;
  }

  /**
   * Fetches one or more NumberFormat instances that can be used to parse numbers for the current
   * locale. The default implementation returns two instances, one regular NumberFormat and a
   * currency instance of NumberFormat.
   *
   * @return one or more NumberFormats to use in parsing numbers
   */
  protected NumberFormat[] getNumberFormats() {
    return new NumberFormat[] {NumberFormat.getInstance(this.locale)};
  }

  /**
   * Parse the input using a NumberFormatter. If the number cannot be parsed, the error key
   * <em>number.invalidNumber</em> will be added to the errors.
   */
  protected Number parse(String input, Collection<ValidationError> errors) {
    input = preprocess(input);
    ParsePosition pp = new ParsePosition(0);

    for (NumberFormat format : this.formats) {
      pp.setIndex(0);
      Number number = format.parse(input, pp);
      if (number != null && input.length() == pp.getIndex()) return number;
    }

    // If we've gotten here we could not parse the number
    errors.add(new ScopedLocalizableError("converter.number", "invalidNumber"));
    return null;
  }

  /**
   * Pre-processes the String to give the NumberFormats a better shot at parsing the input. The
   * default implementation trims the String for whitespace and then looks to see if the number is
   * surrounded by parentheses, e.g. (800), and if so removes the parentheses and prepends a minus
   * sign. Lastly it will remove the currency symbol from the String so that we don't have to use
   * too many NumberFormats!
   *
   * @param input the String as input by the user
   * @return the result of preprocessing the String
   */
  protected String preprocess(String input) {
    // Step 1: trim whitespace
    String output = input.trim();

    // Step 2: remove the currency symbol
    output = output.replace(currencySymbol, "");

    // Step 3: trim whitespace that might precede or follow currency symbol
    output = output.trim();

    // Step 4: replace parentheses with negation
    if (output.startsWith("(") && output.endsWith(")")) {
      output = "-" + output.substring(1, output.length() - 1);
    }

    return output;
  }
}
