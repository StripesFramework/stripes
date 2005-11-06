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
package net.sourceforge.stripes.localization;

import net.sourceforge.stripes.controller.ParameterName;
import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.util.Log;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Provides simple localization utility methods that are used in multiple places in the Stripes
 * code base.
 *
 * @author Tim Fennell
 * @since Stripes 1.1
 */
public class LocalizationUtility {
    private static Log log = Log.getInstance(LocalizationUtility.class);

    /**
     * <p>Fetches the localized name for a form field if one exists in the form field resource bundle.
     * If for any reason a localized value cannot be found (e.g. the bundle cannot be found, or
     * does not contain the required properties) then null will be returned.</p>
     *
     * <p>Looks first for a property called {@code actionPath.fieldName} in the resource bundle, and
     * if not defined, looks for a property called {@code fieldName}.  Will Strip any indexing
     * from the field name prior to using it to construct property names (e.g. foo[12] will become
     * simply foo).</p>
     *
     * @param fieldName The name of the field whose localized name to look up
     * @param actionPath The action path of the form in which the field is nested. If for some
     *        reason this is not available, null may be passed without causing errors.
     * @param locale The desired locale of the looked up name.
     * @return a localized field name if one can be found, or null otherwise.
     */
    public static String getLocalizedFieldName(String fieldName,
                                               String actionPath,
                                               Locale locale) {

        String name = new ParameterName(fieldName).getStrippedName();
        String localizedValue = null;
        ResourceBundle bundle = null;

        try {
            bundle = StripesFilter.getConfiguration().getLocalizationBundleFactory()
                    .getFormFieldBundle(locale);
        }
        catch (MissingResourceException mre) {
            log.error(mre);
            return null;
        }

        try {
            localizedValue = bundle.getString(actionPath + "." + name);
        }
        catch (MissingResourceException mre) {
            try {
                localizedValue = bundle.getString(name);
            }
            catch (MissingResourceException mre2) {
                return null;
            }
        }

        return localizedValue;
    }


    /**
     * Makes a half hearted attempt to convert the property name of a field into a human
     * friendly name by breaking it on periods and capitablizing each word.  This is only used
     * when developers do not provide names for their fields.
     *
     * @param fieldNameKey the programmatic name of a form field
     * @return String a more user friendly name for the field in the absence of anything better
     */
    public static String makePseudoFriendlyName(String fieldNameKey) {
        String[] words = fieldNameKey.split("\\.");

        String friendlyName = words[0].substring(0,1).toUpperCase() + words[0].substring(1);
        for (int i=1; i<words.length; ++i) {
            friendlyName += " " + words[i].substring(0,1).toUpperCase() + words[i].substring(1);
        }

        return friendlyName;
    }
}
