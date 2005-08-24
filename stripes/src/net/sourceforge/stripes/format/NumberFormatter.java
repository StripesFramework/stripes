/* Copyright (C) 2005 Tim Fennell
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the license with this software. If not,
 * it can be found online at http://www.fsf.org/licensing/licenses/lgpl.html
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

    /** Sets the named format string or number format pattern to use to format the number. */
    public void setFormatPattern(String formatPattern) {
        this.formatPattern = formatPattern;
    }

    /** Sets the locale that output String should be in. */
    public void setLocale(Locale locale) {
        this.locale = locale;
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
