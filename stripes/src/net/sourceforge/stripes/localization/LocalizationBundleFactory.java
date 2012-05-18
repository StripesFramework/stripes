/* Copyright 2005-2006 Tim Fennell
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
