/* Copyright (C) 2005 Tim Fennell
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the license with this software. If not,
 * it can be found online at http://www.fsf.org/licensing/licenses/lgpl.html
 */
package net.sourceforge.stripes.validation;

import net.sourceforge.stripes.action.SimpleMessage;
import net.sourceforge.stripes.localization.LocalizationUtility;
import net.sourceforge.stripes.util.Log;

import java.util.Locale;

/**
 * <p>Validation error message that allows for supplying the error message at the time of
 * creation - i.e. not through a resource bundle or other external mechanism.  SimpleError
 * will still attempt to lookup the field name in the field name bundle, but as with other
 * errors, it will fall back to a prettified version of the field name that is used in the
 * input tag.</p>
 *
 * <p>Messages may contain one or more &quot;replacement parameters&quot;.  Two replacement
 * parameters are provided by default, they are the field name and field value, and are
 * indices 0 and 1 respectively.  To use replacement parameters a message must contain the
 * replacement token {#} where # is the numeric index of the replacement parameter.</p>
 *
 * <p>For example, to construct an error message with one additional replacement parameter which is
 * the action the user was trying to perform, you might supply a message like:</p>
 *
 * <pre>{1} is not a valid {0} when trying to {2}</pre>
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
public class SimpleError extends SimpleMessage implements ValidationError {
    private static Log log = Log.getInstance(SimpleError.class);

    private String fieldNameKey;
    private String actionPath;

    /**
     * Constructs a simple error message.
     *
     * @param message the String message (template) to display
     * @param parameter zero or more parameters for replacement into the message
     */
    public SimpleError(String message, Object... parameter) {
        super(message, processReplacementParameters(parameter));
    }

    /**
     * Helper method that is used to widen the replacement parameter array to make
     * room for the two standard parameters, the field name and field value.
     *
     * @param parameter zero or more replacement parmeters
     */
    static Object[] processReplacementParameters(Object... parameter) {
        if (parameter == null) {
            return new Object[2];
        }
        else {
            Object[] out = new Object[parameter.length + 2];
            System.arraycopy(parameter, 0, out, 2, parameter.length);
            return out;
        }
    }

    /**
     * Looks up the field name in the resource bundle (if it exists) so that it can be used
     * in the message, and then defers to it's super class to combine the message template
     * with the replacement parameters provided.
     *
     * @param locale the locale of the current request
     * @return String the message stored under the messageKey supplied
     */
    public String getMessage(Locale locale) {
        resolveFieldName(locale);
        return super.getMessage(locale);
    }

    /**
     * Takes the form field name supplied and tries first to resolve a String in the locale
     * specific bundle for the field name, and if that fails, will try to make a semi-friendly
     * name by parsing the form field name.  The result is stored in replacementParameters[0]
     * for message template parameter replacement.
     */
    protected void resolveFieldName(Locale locale) {
        log.debug("Looking up localized field name with messageKey: ", this.fieldNameKey);

        if (this.fieldNameKey == null) {
            getReplacementParameters()[0] = "FIELD NAME NOT SUPPLIED IN CODE";
        }
        else {
            getReplacementParameters()[0] =
                    LocalizationUtility.getLocalizedFieldName(this.fieldNameKey,
                                                              this.actionPath,
                                                              locale);

            if (getReplacementParameters()[0] == null) {
                getReplacementParameters()[0] =
                        LocalizationUtility.makePseudoFriendlyName(this.fieldNameKey);
            }
        }
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
        getReplacementParameters()[1] = value;
    }

    /** Provides subclasses with access to the value of the field that is in error. */
    public String getFieldValue() {
        return (String) getReplacementParameters()[1];
    }

    /** Sets the binding path of the ActionBean on which the errored field occurs. */
    public void setActionPath(String actionPath) {
        this.actionPath = actionPath;
    }

    /** Provides subclasses access to the name of the form on which the errored field occurs. */
    public String getActionPath() {
        return actionPath;
    }

    /** Generated equals method that checks all fields for equality. */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final SimpleError that = (SimpleError) o;

        if (actionPath != null ? !actionPath.equals(that.actionPath) : that.actionPath != null) {
            return false;
        }
        if (fieldNameKey != null ? !fieldNameKey.equals(that.fieldNameKey) : that.fieldNameKey != null) {
            return false;
        }

        return true;
    }

    /** Generated hashCode() method. */
    public int hashCode() {
        int result = super.hashCode();
        result = (fieldNameKey != null ? fieldNameKey.hashCode() : 0);
        result = 29 * result + (actionPath != null ? actionPath.hashCode() : 0);
        return result;
    }
}
