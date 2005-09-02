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
package net.sourceforge.stripes.validation;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * <p>A TypeConverter that aggressively attempts to convert a String to a java.util.Date object.
 * Under the cover it uses DateFormat instances to do the heavy lifting, but since
 * SimpleDateFormat is quite picky about its input a couple of measures are taken to improve our
 * chances of parsing a Date.  First the String is pre-processed to collapse white spaces into
 * single spaces, and to remove commas from the String.  Then an array of DateFormat instances
 * are used in turn to attempt to parse the input.  If any DateFormat succeeds and returns a
 * Date, that Date will be returned as the result of the conversion.  If all DateFormats fail,
 * a validation error will be produced.</p>
 *
 * <p>This class is designed to be overridden in order to change its behaviour.  Subclasses can
 * override the preProcessInput() method to change the pre-processing behavior if desired. Similarly,
 * subclasses can override getDateFormats() to return a different set of DateFormats as applicable
 * for the given application.</p>
 *
 * <p>The default set of patterns used (on the processed input) are:
 *   <ul>
 *     <li>M d yy</li>
 *     <li>M/d/yy</li>
 *     <li>M.d.yy</li>
 *     <li>MMM d yy</li>
 *     <li>d MMM yy</li>
 *     <li>MMMM d yy</li>
 *     <li>d MMMM yy</li>
 *   </ul>
 * </p>
 */
public class DateTypeConverter implements TypeConverter<Date> {
    private Locale locale;
    private DateFormat[] formats;

    /**
     * Used by Stripes to set the input locale.  Once the locale is set a number of DateFormat
     * instances are created ready to convert any input.
     */
    public void setLocale(Locale locale) {
        this.locale = locale;

        this.formats = new DateFormat[formatStrings.length];

        for (int i=0; i<formatStrings.length; ++i) {
            this.formats[i] = new SimpleDateFormat(formatStrings[i], locale);
            this.formats[i].setLenient(false);
        }
    }

    /**
     * A pattern used to pre-process Strings before the parsing attempt is made.  Since even the
     * &quot;lenient&quot; mode of SimpleDateFormat is not very lenient, this patter is used to
     * collapse multiple white-space characters into a single space and remove commas from the
     * String (replacing them with a space if one is not present already).
     */
    public static final Pattern pattern = Pattern.compile("(\\s{2,}|\\s*,\\s)");

    /** The default set of date patterns used to parse dates with SimpleDateFormat. */
    public static final String[] formatStrings = new String[] {
        "M d yy",
        "M/d/yy",
        "M.d.yy",
        "MMM d yy",
        "d MMM yy",
        "MMMM d yy",
        "d MMMM yy",
    };

    /**
     * Attempts to convert a String to a Date object.  Pre-processes the input by invoking the
     * method preProcessInput(), then uses an ordered list of DateFormat objects (supplied by
     * getDateFormats()) to try and parse the String into a Date.
     */
    public Date convert(String input,
                        Class<? extends Date> targetType,
                        Collection<ValidationError> errors) {

        // Step 1: pre process the input to make it more palatable
        String parseable = preProcessInput(input);

        // Step 2: try really hard to parse the input
        Date date = null;

        for (DateFormat format : this.formats) {
            try {
                if (date == null) { date = format.parse(parseable); }
            }
            catch (ParseException pe) { /* Do nothing, we'll get lots of these. */ }
        }

        // Step 3: If we successfully parsed, return a date, otherwise send back an error
        if (date != null) {
            return date;
        }
        else {
            errors.add( new ScopedLocalizableError("converter.date", "invalidDate") );
            return null;
        }
    }

    /**
     * Pre-processes the input Strnig using the regular expression Pattern stored on this class. Is
     * called by the convert() method prior to attempting date parsing.
     */
    protected String preProcessInput(String input) {
        return DateTypeConverter.pattern.matcher(input).replaceAll(" ");
    }
}
