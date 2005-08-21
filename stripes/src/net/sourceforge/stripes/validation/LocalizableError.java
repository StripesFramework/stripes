package net.sourceforge.stripes.validation;

import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.controller.StripesFilter;

import java.util.ResourceBundle;
import java.util.Locale;

/**
 * <p>Provides a mechanism for creating localizable error messages for presentation to the user.
 * Uses ResourceBundles to provide the localization of the error message.  Messages may contain one
 * or more &quot;replacement parameters &quot;.  Two replacement parameters are provided by default,
 * they are the field name and field value, and are indices 0 and 1 respectively.  To use
 * replacement parameters a message must contain the replacement token {#} where # is the numeric
 * index of the replacement parameter.</p>
 *
 * <p>For example, to construct an error message with one additional replacement parameter which is
 * the action the user was trying to perform, you might have a properties file entry like:</p>
 *
 * <pre>/action/MyAction.myErrorMessage={1} is not a valid {0} when trying to {2}</pre>
 *
 * <p>At runtime this might get replaced out to result in an error message for the user that looks
 * like &quot;<em>Fixed</em> is not a valid <em>status</em> when trying to create a new
 * <em>bug</em>&quot;.</p>
 *
 * <p>One last point of interest is where the user friendly field name comes from. Firstly an
 * attempt is made to look up the localized name in the applicable resource bundle using the
 * String <em>actionPath.fieldName</em> where actionPath is the action of the form in the JSP
 * (or equally, the path given in the @UrlBinding annotation in the ActionBean class),
 * and fieldName is the name of the field on the form.</p>
 *
 * @see java.text.MessageFormat
 * @see java.util.ResourceBundle
 */
public class LocalizableError extends SimpleError {
    /** Log class used to log debugging information. */
    public static final Log log = Log.getInstance(ValidationError.class);

    private String messageKey;

    /**
     * Creates a new LocalizableError with the message key provided, and optionally zero or more
     * replacement parameters to use in the message.  It should be noted that the replacement
     * parameters provided here can be referenced in the error message <b>starting with number
     * 2</b>.
     *
     * @param messageKey a key to lookup a message in the resource bundle
     * @param parameter one or more replacement parameters to insert into the message
     */
    public LocalizableError(String messageKey, Object... parameter) {
        super(parameter);
        this.messageKey = messageKey;

    }

    /**
     * Method responsible for using the information supplied to the error object to find a
     * message template. In this class this is done simply by looking up the resource
     * corresponding to the messageKey supplied in the constructor.
     */
    @Override
    protected String getMessageTemplate(Locale locale) {
        ResourceBundle bundle = StripesFilter.getConfiguration().
                getLocalizationBundleFactory().getErrorMessageBundle(locale);

        return bundle.getString(messageKey);
    }
}
