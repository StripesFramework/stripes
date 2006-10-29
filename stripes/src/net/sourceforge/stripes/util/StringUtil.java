package net.sourceforge.stripes.util;

import java.util.regex.Pattern;

/**
 * Provies utility methods for manipulating and parsing Strings.
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
}
