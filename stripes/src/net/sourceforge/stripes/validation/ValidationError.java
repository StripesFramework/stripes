package net.sourceforge.stripes.validation;

import java.util.Locale;

/**
 * Interface to which all error objects in Stripes should conform.
 *
 * @author Tim Fennell
 */
public interface ValidationError {
    /**
     * The error message to display.  This object will be toString()&apos;d in order to display
     * it on the page.
     * @return Object an object containing the error message to display
     */
    String getMessage(Locale locale);

    /**
     * Provides the message with access to the name of the field in which the error occurred. This
     * is the name the system uses for the field (e.g. cat.name) and not necessarily something that
     * the user should see!
     */
    void setFieldName(String name);

    /**
     * Provides the message with access to the value of the field in which the error occurred
     */
    void setFieldValue(String value);

    /**
     * Provides the message with access to the unique action path associated with the
     * ActionBean bound to the current request.
     */
    void setActionPath(String actionPath);

    /** Returns the name of the field in error, if one was supplied. */
    String getFieldName();

    /** Returns the value that is in error, if one was supplied. */
    String getFieldValue();

    /** Returns the action path of the form/ActionBean, if one was supplied. */
    String getActionPath();
}

