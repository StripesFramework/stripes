package net.sourceforge.stripes.validation;

import java.util.ResourceBundle;
import java.util.MissingResourceException;

/**
 * <p>Provides a slightly more customizable approach to error messages.  Where the LocalizedError
 * class looks for an error message in a single place based on the key provided,
 * ScopedLocalizableError performs a scoped search for an error message.</p>
 *
 * <p>As an example, let's say that the IntegerConverter raises an error messsage with the values
 * defaultScope=<em>converter.integer</em> and key=<em>outOfRange</em>, for a field called
 * <em>age</em> on a form called <em>KittenDetail</em>.  Based on this information an instance of
 * ScopedLocalizableError would fetch the resource bundle and look for error message templates in
 * the following order:</p>
 *
 * <ul>
 *   <li>KittenDetail.age.outOfRange</li>
 *   <li>KittenDetail.outOfRange</li>
 *   <li>converter.integer.outOfRange</li>
 * </ul>
 *
 * <p>Using ScopingLocalizedErrors provides application developers with the flexibility to provide
 * as much or as little specificity in error messages as desired.</p>
 *
 * @author Tim Fennell
 */
public class ScopedLocalizableError extends LocalizableError {

    private String defaultScope;
    private String key;

    /**
     * Constructs a ScopedLocalizableError with the supplied information.
     * @param defaultScope the default scope under which to look for the error message should more
     *        specificly scoped lookups fail
     * @param key the name of the message to lookup
     * @param parameters an optional number of replacement parameters to be used in the message
     */
    public ScopedLocalizableError(String defaultScope, String key, Object... parameters) {
        super(defaultScope + "." + key, parameters);
        this.defaultScope = defaultScope;
        this.key = key;
    }


    /**
     * Overrides getMessageTemplate to perform a scoped search for a message template as defined
     * in the class level javadoc.
     */
    protected String getMessageTemplate(ResourceBundle bundle) {
        try {
            return bundle.getString(getFormName() + "." + getFieldName() + "." + key);
        }
        catch (MissingResourceException mre) {
            try {
                return bundle.getString(getFormName() + "." + key);
            }
            catch (MissingResourceException mre2) {
                return super.getMessageTemplate(bundle);
            }
        }
    }
}
