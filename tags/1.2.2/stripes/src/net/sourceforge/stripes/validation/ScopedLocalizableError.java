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

import net.sourceforge.stripes.controller.StripesFilter;

import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.util.Locale;

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
     *
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
    @Override
    protected String getMessageTemplate(Locale locale) {
        ResourceBundle bundle = null;
        try {
            bundle = StripesFilter.getConfiguration().
                    getLocalizationBundleFactory().getErrorMessageBundle(locale);

            return bundle.getString(getActionPath() + "." + getFieldName() + "." + key);
        }
        catch (MissingResourceException mre) {
            try {
                return bundle.getString(getActionPath() + "." + key);
            }
            catch (MissingResourceException mre2) {
                return super.getMessageTemplate(locale);
            }
        }
    }

    /** Generated equals method that checks all fields and super.equals(). */
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

        final ScopedLocalizableError that = (ScopedLocalizableError) o;

        if (defaultScope != null ? !defaultScope.equals(that.defaultScope) : that.defaultScope != null) {
            return false;
        }
        if (key != null ? !key.equals(that.key) : that.key != null) {
            return false;
        }

        return true;
    }

    /** Generated hashCode() method. */
    public int hashCode() {
        int result = super.hashCode();
        result = 29 * result + (defaultScope != null ? defaultScope.hashCode() : 0);
        result = 29 * result + (key != null ? key.hashCode() : 0);
        return result;
    }
}
