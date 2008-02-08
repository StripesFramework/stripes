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
package net.sourceforge.stripes.format;

import net.sourceforge.stripes.exception.StripesRuntimeException;

import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * <p>Formats numbers into localized Strings for display.  This class relies heavily on the
 * NumberFormat and DecimalFormat classes in the java.text package, and it is suggested that you
 * become familiar with those classes before using custom formats.</p>
 *
 * <p>Accepts the following named formatTypes (not case sensitive):</p>
 * <ul>
 *   <li>number</li>
 *   <li>currency</li>
 *   <li>percentage</li>
 * </ul>
 *
 * <p>If a format type is not supplied the default value of "number" will be used. Format String
 * can be either a custom pattern as used by NumberFormat, or one of the following named formats
 * (not case sensitive):</p>
 * <ul>
 *   <li>plain - Outputs text in a manner similar to toString(), but appropriate to a locale.</li>
 *   <li>integer - Outputs text with grouping characters and no decimals.</li>
 *   <li>decimal - Outputs text with grouping characters and 2-6 decimal positions as needed.</li>
 * </ul>
 *
 * @author Tim Fennell
 */
public class NumberFormatter implements Formatter<Number> {

    /** Maintains a set of named formats that can be used instead of patterns. */
    protected static final Set<String> namedPatterns = new HashSet<String>();

    static {
        namedPatterns.add("plain");
        namedPatterns.add("integer");
        namedPatterns.add("decimal");
    }

    private String formatType;
    private String formatPattern;
    private Locale locale;
    private NumberFormat format;

    /** Sets the format type to be used to render numbers as Strings. */
    public void setFormatType(String formatType) {
        this.formatType = formatType;
    }

    /** Gets the format type to be used to render numbers as Strings. */
    public String getFormatType() {
        return formatType;
    }

    /** Sets the named format string or number format pattern to use to format the number. */
    public void setFormatPattern(String formatPattern) {
        this.formatPattern = formatPattern;
    }

    /** Gets the named format string or number format pattern to use to format the number. */
    public String getFormatPattern() {
        return formatPattern;
    }

    /** Sets the locale that output String should be in. */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /** Gets the locale that output String should be in. */
    public Locale getLocale() {
        return locale;
    }

    /** Instantiates the NumberFormat based on the information provided through setter methods. */
    public void init() {
        // Set some sensible defaults if things are null
        if (this.formatType == null) {
            this.formatType = "number";
        }

        // Figure out which kind of number formatter to get
        if (this.formatPattern == null) {
            this.formatPattern = "plain";
        }

        if (this.formatType.equalsIgnoreCase("number")) {
            this.format = NumberFormat.getInstance(locale);
        }
        else if (this.formatType.equalsIgnoreCase("currency")) {
            this.format = NumberFormat.getCurrencyInstance(locale);
        }
        else if (this.formatType.equalsIgnoreCase("percentage")) {
            this.format = NumberFormat.getPercentInstance(locale);
        }
        else {
            throw new StripesRuntimeException("Invalid format type supplied for formatting a " +
                "number: " + this.formatType + ". Valid values are 'number', 'currency' " +
                "and 'percentage'.");
        }

        // Do any extra configuration
        if (this.formatPattern.equalsIgnoreCase("plain")) {
            this.format.setGroupingUsed(false);
        }
        else if (this.formatPattern.equalsIgnoreCase("integer")) {
            this.format.setMaximumFractionDigits(0);
        }
        else if (this.formatPattern.equalsIgnoreCase(("decimal"))) {
            this.format.setMinimumFractionDigits(2);
            this.format.setMaximumFractionDigits(6);
        }
        else {
            try {
                ((DecimalFormat) this.format).applyPattern(this.formatPattern);
            }
            catch (Exception e) {
                throw new StripesRuntimeException("Custom pattern could not be applied to " +
                    "NumberFormat instance.  Pattern was: " + this.formatPattern,  e);
            }
        }
    }

    /** Formats the number supplied as a String. */
    public String format(Number input) {
        return this.format.format(input);
    }
}
