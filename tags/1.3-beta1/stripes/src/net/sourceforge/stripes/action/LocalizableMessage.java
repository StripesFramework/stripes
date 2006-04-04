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
package net.sourceforge.stripes.action;

import net.sourceforge.stripes.controller.StripesFilter;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * A non-error message class that can localize (or at least externalize) the message String
 * in a resource bundle.  The bundle used is the Stripes error message bundle, which can be
 * conifgured but by default is called 'StripesResources.properties'.  In all other ways
 * this class behaves like it's parent {@link SimpleMessage}.
 *
 * @author Tim Fennell 
 */
public class LocalizableMessage extends SimpleMessage {
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
    public LocalizableMessage(String messageKey, Object... parameter) {
        super((String) null, parameter);
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

    /**
     * Generated equals method which will return true if the other object is of the same
     * type as this instance, and would produce the same user message.
     *
     * @param o an instance of LocalizableMessage or subclass thereof
     * @return true if the two messages would produce the same user message, false otherwise
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final LocalizableMessage that = (LocalizableMessage) o;

        if (messageKey != null ? !messageKey.equals(that.messageKey) : that.messageKey != null) {
            return false;
        }

        return true;
    }

    /** Generated hashCode method. */
    public int hashCode() {
        int result = super.hashCode();
        result = 29 * result + (messageKey != null ? messageKey.hashCode() : 0);
        return result;
    }
}
