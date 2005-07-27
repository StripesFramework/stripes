package net.sourceforge.stripes.validation;

import java.util.Collection;
import java.util.HashSet;

/**
 * Performs a fairly aggressive conversion of a String to a boolean. The String will be deemd to be
 * equivelant to true if it meets any of the following conditions:
 *
 * <ul>
 *   <li>Equals, ignoring case, true</li>
 *   <li>Equals, ignoring case, t</li>
 *   <li>Equals, ignoring case, yes</li>
 *   <li>Equals, ignoring case, y</li>
 *   <li>Equals, ignoring case, on</li>
 *   <li>Is parseable as a number and yields a number greater than zero</li>
 * </ul>
 *
 * If none of the above conditions are met, the return value is false.  This type converter does
 * not produce any validation errors - it always returns either true or false.
 *
 * @author Tim Fennell
 */
public class BooleanTypeConverter implements TypeConverter<Boolean> {
    private static final Collection<String> truths = new HashSet();

    static {
        truths.add("true");
        truths.add("t");
        truths.add("yes");
        truths.add("y");
        truths.add("on");
    }

    /**
     * Converts a String to a Boolean in accordance with the specification laid out in the
     * class level javadoc.
     */
    public Boolean convert(String input,
                           Class<? extends Boolean> targetType,
                           Collection<ValidationError> errors) {

        boolean retval = false;

        for (String truth : truths) {
            retval = retval || truth.equalsIgnoreCase(input);
        }

        if (retval == false) {
            try {
                long number = Long.parseLong(input);
                retval =  (number > 0);
            }
            catch (NumberFormatException nfe) {/* Ingore the exception */ }
        }

        return retval;
    }
}
