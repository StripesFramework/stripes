package net.sourceforge.stripes.validation;

import net.sourceforge.stripes.controller.StripesFilter;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.text.MessageFormat;

/**
 * <p>Validation error message that allows for supplying the error message at the time of
 * creation - i.e. not through a resource bundle or other external mechanism.  SimpleError
 * will still attempt to lookup the field name in the field name bundle, but as with other
 * errors, it will fall back to a prettified version of the field name that is used in the
 * input tag.</p>
 *
 * <p>Messages may contain one or more &quot;replacement parameters &quot;.  Two replacement
 * parameters are provided by default, they are the field name and field value, and are
 * indices 0 and 1 respectively.  To use replacement parameters a message must contain the
 * replacement token {#} where # is the numeric index of the replacement parameter.</p>
 *
 * <p>For example, to construct an error message with one additional replacement parameter which is
 * the action the user was trying to perform, you might supply a message like:</p>
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
 */
public abstract class SimpleError implements ValidationError {
    private String fieldNameKey;
    private String actionPath;
    private String message;

    /**
     * The set of replacement parameters that will be used to create the message from the message
     * template.  Note that position 0 is reserved for the field name and position 1 is reserved
     * for the field value.
     */
    protected Object[] replacementParameters;

    public SimpleError(String message, Object... parameter) {
        this(parameter);
        this.message = message;
    }

    protected SimpleError(Object... parameter) {
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
        resolveFieldName(locale);

        // Now get the message itself
        String messageTemplate = getMessageTemplate(locale);
        return MessageFormat.format(messageTemplate, this.replacementParameters);
    }

    /**
     * Simply returns the message passed in at Construction time. Designed to be overridded by
     * subclasses to lookup messages from resource bundles.
     *
     * @param locale the Locale of the message template desired
     * @return the message (potentially with TextFormat replacement tokens).
     */
    protected String getMessageTemplate(Locale locale) {
        return this.message;
    }

    /**
     * Takes the form field name supplied and tries first to resolve a String in the locale
     * specific bundle for the field name, and if that fails, will try to make a semi-friendly
     * name by parsing the form field name.  The result is stored in replacementParameters[0]
     * for message template parameter replacement.
     */
    protected void resolveFieldName(Locale locale) {
        LocalizableError.log.debug("Looking up localized field name with messageKey: ", this.fieldNameKey);
        try {
            ResourceBundle bundle = StripesFilter.getConfiguration().
                    getLocalizationBundleFactory().getFormFieldBundle(locale);

            this.replacementParameters[0] =
                bundle.getString(this.actionPath + "." + this.fieldNameKey);
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
    public String getFieldName() {
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

    /** Sets the binding path of the ActionBean on which the errored field occurs. */
    public void setActionPath(String actionPath) {
        this.actionPath = actionPath;
    }

    /** Provides subclasses access to the name of the form on which the errored field occurs. */
    public String getActionPath() {
        return actionPath;
    }
}
