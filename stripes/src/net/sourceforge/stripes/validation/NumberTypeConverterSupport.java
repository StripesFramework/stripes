package net.sourceforge.stripes.validation;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Locale;

/**
 * Provides the basic support for converting Strings to non-floating point numbers (i.e. shorts,
 * integers, and longs).
 *
 * @author Tim Fennell
 */
public class NumberTypeConverterSupport {
    private Locale locale;

    /** Used by Stripes to tell the converter what locale the incoming text is in. */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /** Returns the Locale set on the object using setLocale(). */
    public Locale getLocale() {
        return locale;
    }


    /** Fetches a NumberFormat that can be used to parse non-decimal numbers. */
    protected NumberFormat getNumberFormat() {
        //TODO: add thread local pooling of NumberFormats
        NumberFormat format = NumberFormat.getInstance(this.locale);
        return format;
    }

    /**
     * Parse the input using a NumberFormatter.  If the number cannot be parsed, the error key
     * <em>number.invalidNumber</em> will be added to the errors.
     */
    protected Number parse(String input, Collection<ValidationError> errors) {
        try {
            return getNumberFormat().parse(input);
        }
        catch (ParseException pe) {
            errors.add( new ScopedLocalizableError("converter.number", "invalidNumber"));
            return null;
        }
    }
}
