package net.sourceforge.stripes.validation;

import net.sourceforge.stripes.controller.DispatcherServlet;
import net.sourceforge.stripes.util.Log;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

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
 * <pre>myForm.myErrorMessage={1} is not a valid {0} when trying to {2}</pre>
 *
 * <p>At runtime this might get replaced out to result in an error message for the user that looks
 * like &quot;<em>Fixed</em> is not a valid <em>status</em> when trying to create a new
 * <em>bug</em>&quot;.</p>
 *
 * <p>One last point of interest is where the user friendly field name comes from. Firstly an
 * attempt is made to look up the localized name in the applicable resource bundle using the
 * String <em>formName.fieldName</em> where formName is the name of the form in the JSP (or equally,
 * the name given in the @FormName annotation in the ActionBeanclass) and fieldName is the name
 * of the field on the form.</p>
 *
 * @see java.text.MessageFormat
 * @see java.util.ResourceBundle
 */
public class LocalizableError implements ValidationError {
    /** Log class used to log debugging information. */
    public static final Log log = Log.getInstance(ValidationError.class);

    private String messageKey;
    private String fieldNameKey;
    private String formName;

    /**
     * The set of replacement parameters that will be used to create the message from the message
     * template.  Note that position 0 is reserved for the field name and position 1 is reserved
     * for the field value.
     */
    private Object[] replacementParameters;

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
        this.messageKey = messageKey;

        if (parameter == null) {
            this.replacementParameters = new Object[2];
        }
        else {
            this.replacementParameters = new Object[parameter.length + 2];
            for (int i=0; i<parameter.length; ++i) {
                this.replacementParameters[i+2] = parameter[i];
            }
        }
    }

    /**
     * Gets a String message from the resource bundle for the locale specified. If Locale is null
     * then the message will be looked up in the default bundle.
     *
     * @param locale the locale of the current request
     * @return String the message stored under the messageKey supplied
     */
    public String getMessage(Locale locale) {
        try {
            ResourceBundle fieldBundle = DispatcherServlet.getConfiguration().
                    getLocalizationBundleFactory().getFormFieldBundle(locale);

            ResourceBundle messageBundle = DispatcherServlet.getConfiguration().
                    getLocalizationBundleFactory().getErrorMessageBundle(locale);
            resolveFieldName(fieldBundle);

            // Now get the message itself
            String messageTemplate = null;
            messageTemplate = getMessageTemplate(messageBundle);
            return MessageFormat.format(messageTemplate, this.replacementParameters);
        }
        catch (MissingResourceException mre) {
            return "One does not appear to have correctly configured an error message with key = " +
                messageKey + " and locale = " + locale.toString();
        }
    }

    /**
     * Method responsible for using the information supplied to the error object to find a
     * message template. In this class this is done simply by looking up the resource
     * corresponding to the messageKey supplied in the constructor.
     */
    protected String getMessageTemplate(ResourceBundle bundle) {
        return bundle.getString(messageKey);
    }

    /**
     * Takes the form field name supplied and tries first to resolve a String in the locale
     * specific bundle for the field name, and if that fails, will try to make a semi-friendly
     * name by parsing the form field name.  The result is stored in replacementParameters[0]
     * for message template parameter replacement.
     */
    protected void resolveFieldName(ResourceBundle bundle) {
        log.debug("Looking up localized field name with messageKey: ", this.fieldNameKey);
        try {
            this.replacementParameters[0] =
                bundle.getString(this.formName + "." + this.fieldNameKey);
        }
        catch (MissingResourceException mre) {
            this.replacementParameters[0] = tryToMakeFriendly(this.fieldNameKey);
        }
    }



    /**
     * Makes a half hearted attempt to convert the property name of the field into a human
     * friendly name by breaking it on periods and capitablizing each word.  This is only used
     * when developers do not provide names for their fields.
     */
    private String tryToMakeFriendly(String fieldNameKey) {
        String[] words = fieldNameKey.split("\\.");

        String friendlyName = words[0].substring(0,1).toUpperCase() + words[0].substring(1);
        for (int i=1; i<words.length; ++i) {
            friendlyName += " " + words[i].substring(0,1).toUpperCase() + words[i].substring(1);
        }

        return friendlyName;
    }

    /**
     * Sets the name of the form field in error.  This is the programmatic name, and hence probably
     * not the name that the user sees.
     */
    public void setFieldName(String name) {
        this.fieldNameKey = name;
    }

    /** Provides sublcasses access to the field name. */
    protected String getFieldName() {
        return this.fieldNameKey;
    }

    /** Sets the value of the field that is in error. */
    public void setFieldValue(String value) {
        this.replacementParameters[1] = value;
    }

    /** Provides subclasses with access to the value of the field that is in error. */
    public String getFieldValue() {
        return (String) this.replacementParameters[1];
    }

    /** Sets the name of the form on which the errored field occurs. */
    public void setFormName(String formName) {
        this.formName = formName;
    }

    /** Provides subclasses access to the name of the form on which the errored field occurs. */
    protected String getFormName() {
        return formName;
    }
}
