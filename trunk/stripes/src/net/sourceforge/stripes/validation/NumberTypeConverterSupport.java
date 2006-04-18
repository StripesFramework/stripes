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
import java.text.ParseException;
import java.util.Collection;
import java.util.Locale;
import java.util.Currency;

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
        this.currencySymbol = Currency.getInstance(getLocale()).getSymbol(getLocale());
    }

    /** Returns the Locale set on the object using setLocale(). */
    public Locale getLocale() {
        return locale;
    }


    /**
     * Fetches a NumberFormat that can be used to parse non-decimal numbers.
     * @deprecated Use getNumberFormats() instead which is capable of returning multiple
     *            formats for parsing. This method will be removed in Stripes 1.3.
     */
    @Deprecated()
    protected NumberFormat getNumberFormat() {
        return NumberFormat.getInstance(this.locale);
    }

    /**
     * Fetches one or more NumberFormat instances that can be used to parse numbers
     * for the current locale. The default implementation returns two instances, one
     * regular NumberFormat and a currency instance of NumberFormat.
     *
     * @return one or more NumberFormats to use in parsing numbers
     */
    protected NumberFormat[] getNumberFormats() {
        return new NumberFormat[] {
                getNumberFormat() // TODO: inline and remove getNumberFormat() in 1.3
        };
    }

    /**
     * Parse the input using a NumberFormatter.  If the number cannot be parsed, the error key
     * <em>number.invalidNumber</em> will be added to the errors.
     */
    protected Number parse(String input, Collection<ValidationError> errors) {
        input = preprocess(input);

        for (NumberFormat format : this.formats) {
            try { return format.parse(input); }
            catch (ParseException pe) { /* Do nothing. */ }
        }

        // If we've gotten here we could not parse the number
        errors.add( new ScopedLocalizableError("converter.number", "invalidNumber"));
        return null;
    }

    /**
     * Pre-processes the String to give the NumberFormats a better shot at parsing the
     * input. The default implementation trims the String for whitespace and then looks to
     * see if the number is surrounded by parentheses, e.g. (800), and if so removes the
     * parentheses and prepends a minus sign.  Lastly it will remove the currency symbol
     * from the String so that we don't have to use too many NumberFormats!
     *
     * @param input the String as input by the user
     * @return the result of preprocessing the String
     */
    protected String preprocess(String input) {
        // Step 1: trim whitespace
        String output = input.trim();

        // Step 2: remove the currency symbol
        // The casts are to make sure we don't call replace(String regex, String replacement)
        output = output.replace((CharSequence) currencySymbol, (CharSequence) "");

        // Step 3: replace parentheses with negation
        if (output.startsWith("(") && output.endsWith(")")) {
            output = "-" + output.substring(1, output.length() - 1);
        }

        return output;
    }
}
