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

import net.sourceforge.stripes.config.ConfigurableComponent;

import java.util.ResourceBundle;
import java.util.Locale;
import java.util.MissingResourceException;

/**
 * <p>Extremely simple interface that is implemented to resolve the ResourceBundles from which
 * various Strings are pulled at runtime.  Can be implemented using property resource bundles, class
 * resource bundles or anything else a developer can dream up.</p>
 *
 * <p>The bundles returned from each method do not need to be discrete, and may all be the same
 * bundle.
 */
public interface LocalizationBundleFactory extends ConfigurableComponent {

    /**
     * Returns the ResourceBundle from which to draw error messages for the specified locale.
     *
     * @param locale the locale that is in use for the current request
     * @throws MissingResourceException when a bundle that is expected to be present cannot
     *         be located.
     */
    ResourceBundle getErrorMessageBundle(Locale locale) throws MissingResourceException;

    /**
     * Returns the ResourceBundle from which to draw the names of form fields for the
     * specified locale.
     *
     * @param locale the locale that is in use for the current request
     * @throws MissingResourceException when a bundle that is expected to be present cannot
     *         be located.
     */
    ResourceBundle getFormFieldBundle(Locale locale) throws MissingResourceException;
}
