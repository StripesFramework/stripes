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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

import net.sourceforge.stripes.validation.ScopedLocalizableError;
import net.sourceforge.stripes.validation.TypeConverter;
import net.sourceforge.stripes.validation.ValidationError;

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
 * <p>DateTypeConverter is designed to be overridden in order to change its behaviour. Subclasses can
 * override the preProcessInput() method to change the pre-processing behavior if desired. Similarly,
 * subclasses can override getFormatStrings() to change the set of format strings used in parsing,
 * or even override getDateFormats() to change how the DateFormat objects get constructed.</p>
 *
 * <p>The set of formats used is constructed by taking the default SHORT, MEDIUM and LONG formats
 * for the input locale (and replacing all non-space separator characters with spaces), supplied by
 * getDefaultLocalizedFormats(), and adding formats based on the format strings supplied by getFormatStrings().  
 * This results in a list of DateFormats with the following patterns:</p>
 *
 *   <ul>
 *     <li>SHORT  (e.g. 'M d yy' for Locale.US)</li>
 *     <li>MEDIUM (e.g. 'MMM d yyyy' for Locale.US)</li>
 *     <li>LONG   (e.g. 'MMMM d yyyy' for Locale.US)</li>
 *     <li>d MMM yy (note that for parsing MMM and MMMM are interchangable)</li>
 *     <li>yyyy M d (note that for parsing M and MM are interchangable)</li>
 *     <li>yyyy MMM d</li>
 *     <li>EEE MMM dd HH:mm:ss zzz yyyy (the format created by Date.toString())</li>
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
     * @return the current input locale.
     */
    public Locale getLocale() {
    	return locale;
    }
    
    /**
     * A pattern used to pre-process Strings before the parsing attempt is made.  Since
     * SimpleDateFormat strictly enforces that the separator characters in the input are the same
     * as those in the pattern, this regular expression is used to remove commas, slashes, hyphens
     * and periods from the input String (replacing them with spaces) and to collapse multiple
     * white-space characters into a single space.
     */
    public static final Pattern pattern = Pattern.compile("(?<!GMT)[\\s,-/\\.]+");

    /** The default set of date patterns used to parse dates with SimpleDateFormat. */
    public static final String[] formatStrings = new String[] {
        "d MMM yy",
        "yyyy M d",
        "yyyy MMM d",
        "EEE MMM dd HH:mm:ss zzz yyyy"
    };
    
    /**
     * Returns an array of format strings that will be used, in order, to try and parse the date.
     * This method can be overridden to make DateTypeConverter use a different set of format
     * Strings. Given that pre-processing converts most common separator characters into spaces,
     * patterns should be expressed with spaces as separators, not slashes, hyphens etc.
     */
    protected String[] getFormatStrings() {
        return DateTypeConverter.formatStrings;
    }
    
    /**
     * Returns an array of 3 DateFormats which are based upon the standard SHORT, MEDIUM and LONG
     * formats for the input locale, with all separator characters replaced with spaces.
     */
    protected SimpleDateFormat[] getDefaultLocalizedFormats() {
    	SimpleDateFormat[] dateFormats = new SimpleDateFormat[3];
        
    	dateFormats[0] = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT, locale);
        dateFormats[0].applyPattern( preProcessInput(dateFormats[0].toPattern()) );

        dateFormats[1] = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
        dateFormats[1].applyPattern( preProcessInput(dateFormats[1].toPattern()) );

        dateFormats[2] = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.LONG, locale);
        dateFormats[2].applyPattern( preProcessInput(dateFormats[2].toPattern()) );
        
        return dateFormats;
    }
    

    /**
     * Returns an array of DateFormat objects that will be used in sequence to try and parse the
     * date String. This method will be called once when the DateTypeConverter instance is
     * initialized. It returns an array that combines the dateformats returned by
     * getDefaultLocalizedFormats() and getFormatStrings(), in that order.
     */
    protected DateFormat[] getDateFormats() {
        String[] formatStrings = getFormatStrings();
        SimpleDateFormat[] defaultLocalizedFormats = getDefaultLocalizedFormats();
        
        SimpleDateFormat[] dateFormats = 
        	new SimpleDateFormat[defaultLocalizedFormats.length + formatStrings.length];

        // Put the default localized dateformats in the aggregated array
        for (int i=0; i<defaultLocalizedFormats.length; i++) {
            dateFormats[i] = defaultLocalizedFormats[i];
            dateFormats[i].setLenient(false);
        }
        
        // Create the dateFormats from the format Strings
        for (int i=0; i<formatStrings.length; i++) {
            dateFormats[i+defaultLocalizedFormats.length] = new SimpleDateFormat(formatStrings[i], locale);
            dateFormats[i+defaultLocalizedFormats.length].setLenient(false);
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

        // Step 1: pre process the input to make it more palatable
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
     * Pre-processes the input String to improve the chances of parsing it. First uses the regular
     * expression Pattern stored on this class to remove all separator chars and ensure that
     * components are separated by single spaces.  Then, if the string contains only two components
     * the current year is appended to ensure that dates like "12/25" parse to the current year
     * instead of failing altogether.
     */
    protected String preProcessInput(String input) {
        input = DateTypeConverter.pattern.matcher(input.trim()).replaceAll(" ");

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
}
