package net.sourceforge.stripes.validation;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
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
        DateFormat[] formats = getDateFormats();

        for (DateFormat format : formats) {
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
     * Gets an instance of DateFormat to use to parse the date.  This can be used to override the
     * format mask used in parsing by sublassing this class and overriding this method.
     */
    protected DateFormat[] getDateFormats() {
        //TODO: implement thread local caching
        DateFormat[] formats = new DateFormat[formatStrings.length];

        for (int i=0; i<formatStrings.length; ++i) {
            formats[i] = new SimpleDateFormat(formatStrings[i]);
            formats[i].setLenient(true);
        }

        return formats;
    }

    /**
     * Pre-processes the input Strnig using the regular expression Pattern stored on this class. Is
     * called by the convert() method prior to attempting date parsing.
     */
    protected String preProcessInput(String input) {
        return DateTypeConverter.pattern.matcher(input).replaceAll(" ");
    }
}
