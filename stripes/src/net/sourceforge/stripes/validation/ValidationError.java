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

