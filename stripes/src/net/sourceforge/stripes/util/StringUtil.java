package net.sourceforge.stripes.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.stripes.exception.StripesRuntimeException;

/**
 * Provides utility methods for manipulating and parsing Strings.
 *
 * @author Tim Fennell
 * @since Stripes 1.4.2
 */
public class StringUtil {
    /**
     * A regular expression for splitting apart a String where individual parts are
     * separated by any whitespace (including new lines) and/or a comma.
     */
    private static final Pattern STANDARD_SPLIT = Pattern.compile("[\\s,]+");

    /**
     * A regular expression that matches characters that are not explicitly allowed in the fragment
     * part of a URI according to RFC 3986. This does not include the percent sign (%), which is
     * actually allowed but only as an escape character for percent-encoded characters.
     */
    private static final Pattern URI_FRAGMENT_DISALLOWED_CHARACTERS = Pattern
            .compile("[^\\p{Alnum}._~!$&'()*+,;=:@/?-]");

    /**
     * Splits apart the input String on any whitespace and/or commas. Leading and trailing
     * whitespace are ignored. If a null String is provided as input a zero length array
     * will be returned.
     *
     * @param input the String to split apart
     * @return an array of substrings of the input String based on the split
     */
    public static String[] standardSplit(String input) {
        if (input == null) {
            return new String[0];
        }
        else {
            return STANDARD_SPLIT.split(input.trim());
        }
    }
    
    /**
     * Combines a bunch of objects into a single String. Array contents get converted nicely.
     */
    public static String combineParts(Object... messageParts) {
        StringBuilder builder = new StringBuilder(128);
        for (Object part : messageParts) {
            if (part != null && part.getClass().isArray()) {
                builder.append( Arrays.toString(CollectionUtil.asObjectArray(part) ));
            }
            else {
                builder.append(part);
            }
        }

        return builder.toString();
    }

    /**
     * URL-encodes {@code value} using the UTF-8 charset. Using this method eliminates the need for
     * a try/catch since UTF-8 is guaranteed to exist.
     * 
     * @see URLEncoder#encode(String, String)
     */
    public static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new StripesRuntimeException("Unsupported encoding?  UTF-8?  That's unpossible.");
        }
    }

    /**
     * URL-decodes {@code value} using the UTF-8 charset. Using this method eliminates the need for
     * a try/catch since UTF-8 is guaranteed to exist.
     * 
     * @see URLDecoder#decode(String, String)
     */
    public static String urlDecode(String value) {
        try {
            return URLDecoder.decode(value, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new StripesRuntimeException("Unsupported encoding?  UTF-8?  That's unpossible.");
        }
    }

    /**
     * Encode a URI fragment as required by RFC 3986. The fragment is allowed to contain a different
     * set of characters than other parts of the URI, and characters that are allowed in the
     * fragment must not be encoded.
     * 
     * @param value The string to encode
     * @return The encoded string
     */
    public static String uriFragmentEncode(String value) {
        Matcher matcher = URI_FRAGMENT_DISALLOWED_CHARACTERS.matcher(value);

        StringBuilder buf = null;
        int end = 0;
        while (matcher.find()) {
            if (buf == null) {
                buf = new StringBuilder(value.length() * 2);
            }

            buf.append(value.substring(end, matcher.start())).append(
                    String.format("%%%02X", (int) matcher.group().charAt(0)));
            end = matcher.end();
        }

        // No match, return input unchanged
        if (buf == null)
            return value;

        // Append tail
        if (end < value.length())
            buf.append(value.substring(end));

        return buf.toString();
    }
}
