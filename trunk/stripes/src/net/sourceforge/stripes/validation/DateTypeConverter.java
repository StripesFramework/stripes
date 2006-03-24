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
 * Under the covers it uses DateFormat instances to do the heavy lifting, but since
 * SimpleDateFormat is quite picky about its input a couple of measures are taken to improve our
 * chances of parsing a Date.  First the String is pre-processed to replace commas, slashes,
 * hyphens and periods with spaces and to collapse multiple white spaces into a single space. Then
 * an array of DateFormat instances are used in turn to attempt to parse the input. If any DateFormat
 * succeeds and returns a Date, that Date will be returned as the result of the conversion.  If
 * all DateFormats fail, a validation error will be produced.</p>
 *
 * <p>This class is designed to be overridden in order to change its behaviour.  Subclasses can
 * override the preProcessInput() method to change the pre-processing behavior if desired. Similarly,
 * subclasses can override getFormatStrings() to change the set of format strings used in parsing,
 * or even override getDateFormats() to change how the DateFormat objects get constructed.</p>
 *
 * <p>The set of formats used is constructed by taking the default SHORT, MEDIUM and LONG formats
 * for the input locale (and replacing all non-space separator characters with spaces) and adding
 * formats based on the format strings supplied by getFormatStrings().  This results in a list of
 * DateFormats with the following patterns:</p>
 *
 *   <ul>
 *     <li>SHORT  (e.g. 'M d yy' for Locale.US)</li>
 *     <li>MEDIUM (e.g. 'MMM d yyyy' for Locale.US)</li>
 *     <li>LONG   (e.g. 'MMMM d yyyy' for Locale.US)</li>
 *     <li>d MMM yy</li>
 *     <li>d MMMM yy</li>
 *     <li>yyyy M d</li>
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
        this.formats = getDateFormats();
    }

    /**
     * A pattern used to pre-process Strings before the parsing attempt is made.  Since even the
     * &quot;lenient&quot; mode of SimpleDateFormat is not very lenient, this pattern is used to
     * remove commas, slashes, hyphens and periods from the input String (replacing them with spaces)
     * and to collapse multiple white-space characters into a single space.
     */
    public static final Pattern pattern = Pattern.compile("[\\s,-/\\.]+");

    /** The default set of date patterns used to parse dates with SimpleDateFormat. */
    public static final String[] formatStrings = new String[] {
        "d MMM yy",
        "d MMMM yy",
        "yyyy M d"
    };

    /**
     * <p>Returns an array of format strings that will be used, in order, to try and parse the date.
     * This method can be overridden to make DateTypeConverter use a different set of format
     * Strings.  Two caveats apply:</p>
     *
     * <ol>
     *   <li>
     *       Given that pre-processing converts most common separator characters into spaces,
     *       patterns should be expressed with spaces as separators, not slashes, hyphens etc.
     *   </li>
     *   <li>
     *       In all cases getDateFormats() will return formats based on this list <b>preceeded</b>
     *       by the standard SHORT, MEDIUM and LONG formats for the input locale.
     *   </li>
     * </ol>
     */
    protected String[] getFormatStrings() {
        return DateTypeConverter.formatStrings;
    }

    /**
     * Returns an array of DateFormat objects that will be used in sequence to try and parse the
     * date String. This method will be called once when the DateTypeConverter instance is
     * initialized.  By default it returns an array three larger than the list of format
     * strings returned by getFormatStrings().  The first three slots in the array are filled
     * with DateFormats which are based upon the standard SHORT, MEDIUM and LONG formats for
     * the input locale, with all separator characters replaced with spaces.
     */
    protected DateFormat[] getDateFormats() {
        String[] formatStrings = getFormatStrings();
        SimpleDateFormat[] dateFormats = new SimpleDateFormat[formatStrings.length + 3];

        // First create the ones from the format Strings
        for (int i=0; i<formatStrings.length; ++i) {
            dateFormats[i+3] = new SimpleDateFormat(formatStrings[i], locale);
            dateFormats[i+3].setLenient(false);
        }

        // And then create the ones from the default input locale
        dateFormats[0] = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT, locale);
        dateFormats[0].applyPattern( preProcessInput(dateFormats[0].toPattern()) );
        dateFormats[0].setLenient(false);

        dateFormats[1] = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
        dateFormats[1].applyPattern( preProcessInput(dateFormats[1].toPattern()) );
        dateFormats[1].setLenient(false);

        dateFormats[2] = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.LONG, locale);
        dateFormats[2].applyPattern( preProcessInput(dateFormats[2].toPattern()) );
        dateFormats[2].setLenient(false);

        return dateFormats;
    }

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
