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

import net.sourceforge.stripes.controller.StripesFilter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.regex.Pattern;

/**
 * <p>A TypeConverter that aggressively attempts to convert a String to a java.util.Date object.
 * Under the covers it uses DateFormat instances to do the heavy lifting, but since
 * SimpleDateFormat is quite picky about its input a couple of measures are taken to improve our
 * chances of parsing a Date.</p>
 *
 * <p>First the String is pre-processed to replace commas, slashes, hyphens and periods with spaces
 * and to collapse multiple white spaces into a single space.  Then, to ensure that input like
 * "Jan 1" and "3/19" are parseable a check is performed to see if there are only two segments
 * in the input string (e.g. "Jan" and "1" but no "2007").  If that is the case then the current
 * four digit year is appended to improve chances or parsing because a two segment date is not
 * parsable by a DateFormat that expects a year.</p>
 *
 * <p>Then an array of DateFormat instances are used in turn to attempt to parse the input. If any
 * DateFormat succeeds and returns a Date, that Date will be returned as the result of the
 * conversion.  If all DateFormats fail, a validation error will be produced.</p>
 *
 * <p>The set of formats is obtained from getFormatStrings(). The default set of formats used is
 * constructed by taking the default SHORT, MEDIUM and LONG formats for the input locale (and
 * replacing all non-space separator characters with spaces), and adding formats to obtain the
 * following patterns:</p>
 *
 *   <ul>
 *     <li>SHORT  (e.g. 'M d yy' for Locale.US)</li>
 *     <li>MEDIUM (e.g. 'MMM d yyyy' for Locale.US)</li>
 *     <li>LONG   (e.g. 'MMMM d yyyy' for Locale.US)</li>
 *     <li>d MMM yy (note that for parsing MMM and MMMM are interchangeable)</li>
 *     <li>yyyy M d (note that for parsing M and MM are interchangeable)</li>
 *     <li>yyyy MMM d</li>
 *     <li>EEE MMM dd HH:mm:ss zzz yyyy (the format created by Date.toString())</li>
 *   </ul>
 * </p>
 *
 * <p>This default set of formats can be changed by providing a different set of format strings in
 * the Stripes resource bundle, or by subclassing and overriding getFormatStrings().  In all cases
 * patterns should be specified using single spaces as separators instead of slashes, dashes
 * or other characters.</p>
 *
 * <p>The regular expression pattern used in the pre-process method can also be changed in the
 * Stripes resource bundle, or by subclassing and overriding the getPreProcessPattern() method.</p>
 *
 * <p>The keys used in the resource bundle to specify the format strings and the pre-process
 * pattern are:
 *   <ul>
 *     <li>stripes.dateTypeConverter.formatStrings</li>
 *     <li>stripes.dateTypeConverter.preProcessPattern</li>
 *   </ul>
 * </p>
 *
 * <p>DateTypeConverter can also be overridden in order to change its behaviour. Subclasses can
 * override the preProcessInput() method to change the pre-processing behavior if desired. Similarly,
 * subclasses can override getDateFormats() to change how the DateFormat objects get constructed.
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
     * @return the current input locale.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * <p>A pattern used to pre-process Strings before the parsing attempt is made.  Since
     * SimpleDateFormat strictly enforces that the separator characters in the input are the same
     * as those in the pattern, this regular expression is used to remove commas, slashes, hyphens
     * and periods from the input String (replacing them with spaces) and to collapse multiple
     * white-space characters into a single space.</p>
     *
     * <p>This pattern can be changed by providing a different value under the
     * <code>'stripes.dateTypeConverter.preProcessPattern'</code> key in the resource
     * bundle. The default value is <code>(?&lt;!GMT)[\\s,-/\\.]+</code>.</p>
     */
    public static final Pattern PRE_PROCESS_PATTERN = Pattern.compile("(?<!GMT)[\\s,/\\.-]+");

    /** The default set of date patterns used to parse dates with SimpleDateFormat. */
    public static final String[] formatStrings = new String[] {
            "d MMM yy",
            "yyyy M d",
            "yyyy MMM d",
            "EEE MMM dd HH:mm:ss zzz yyyy"
    };

    /** The key used to look up the localized format strings from the resource bundle. */
    public static final String KEY_FORMAT_STRINGS = "stripes.dateTypeConverter.formatStrings";

    /** The key used to look up the pre-process pattern from the resource bundle. */
    public static final String KEY_PRE_PROCESS_PATTERN = "stripes.dateTypeConverter.preProcessPattern";

    /**
     * Returns an array of format strings that will be used, in order, to try and parse the date.
     * This method can be overridden to make DateTypeConverter use a different set of format
     * Strings. Given that pre-processing converts most common separator characters into spaces,
     * patterns should be expressed with spaces as separators, not slashes, hyphens etc.
     */
    protected String[] getFormatStrings() {
        try {
            return getResourceString(KEY_FORMAT_STRINGS).split(", *");
        }
        catch (MissingResourceException mre) {
            // First get the locale specific date format patterns
            int[] dateFormats = { DateFormat.SHORT, DateFormat.MEDIUM, DateFormat.LONG };
            String[] formatStrings = new String[dateFormats.length + DateTypeConverter.formatStrings.length];

            for (int i=0; i<dateFormats.length; i++) {
                SimpleDateFormat dateFormat = (SimpleDateFormat) DateFormat.getDateInstance(dateFormats[i], locale);
                formatStrings[i] = preProcessInput(dateFormat.toPattern());
            }

            // Then copy the default format strings over too
            System.arraycopy(DateTypeConverter.formatStrings, 0,
                             formatStrings, dateFormats.length,
                             DateTypeConverter.formatStrings.length);

            return formatStrings;
        }
    }

    /**
     * Returns an array of DateFormat objects that will be used in sequence to try and parse the
     * date String. This method will be called once when the DateTypeConverter instance is
     * initialized. It first calls getFormatStrings() to obtain the format strings that are used to
     * construct SimpleDateFormat instances.
     */
    protected DateFormat[] getDateFormats() {
        String[] formatStrings = getFormatStrings();
        SimpleDateFormat[] dateFormats = new SimpleDateFormat[formatStrings.length];

        for (int i=0; i<formatStrings.length; i++) {
            dateFormats[i] = new SimpleDateFormat(formatStrings[i], locale);
            dateFormats[i].setLenient(false);
        }

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

        // Step 1: pre-process the input to make it more palatable
        String parseable = preProcessInput(input);

        // Step 2: try really hard to parse the input
        Date date = null;

        for (DateFormat format : this.formats) {
            try {
                date = format.parse(parseable);
                break;
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
     * Returns the regular expression pattern used in the pre-process method. Looks for a pattern in
     * the resource bundle under the key 'stripes.dateTypeConverter.preProcessPattern'. If no value
     * is found, the pattern <code>(?&lt;!GMT)[\\s,-/\\.]+</code> is used by default. The pattern is
     * used by preProcessInput() to replace all matches by single spaces.
     */
    protected Pattern getPreProcessPattern() {
        try {
            return Pattern.compile(getResourceString(KEY_PRE_PROCESS_PATTERN));
        }
        catch (MissingResourceException exc) {
            return DateTypeConverter.PRE_PROCESS_PATTERN;
        }
    }

    /**
     * Pre-processes the input String to improve the chances of parsing it. First uses the regular
     * expression Pattern returned by getPreProcessPattern() to remove all separator chars and
     * ensure that components are separated by single spaces.  Then invokes
     * {@link #checkAndAppendYear(String)} to append the year to the date in case the date
     * is in a format like "12/25" which would otherwise fail to parse.
     */
    protected String preProcessInput(String input) {
        input = getPreProcessPattern().matcher(input.trim()).replaceAll(" ");
        input = checkAndAppendYear(input);
        return input;
    }

    /**
     * Checks to see how many 'parts' there are to the date (separated by spaces) and
     * if there are only two parts it adds the current year to the end by geting the
     * Locale specific year string from a Calendar instance.
     *
     * @param input the date string after the pre-process pattern has been run against it
     * @return either the date string as is, or with the year appended to the end
     */
    protected String checkAndAppendYear(String input) {
        // Count the spaces, date components = spaces + 1
        int count = 0;
        for (char ch : input.toCharArray()) {
            if (ch == ' ') ++count;
        }

        // Looks like we probably only have a day and month component, that won't work!
        if (count == 1) {
            input += " " + Calendar.getInstance(locale).get(Calendar.YEAR);
        }
        return input;
    }

    /** Convenience method to fetch a property from the resource bundle. */
    protected String getResourceString(String key) throws MissingResourceException {
        return StripesFilter.getConfiguration().getLocalizationBundleFactory()
                .getErrorMessageBundle(locale).getString(key);

    }
}