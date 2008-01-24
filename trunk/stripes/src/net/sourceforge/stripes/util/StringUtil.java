package net.sourceforge.stripes.util;

import java.util.Arrays;
import java.util.regex.Pattern;

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
}
