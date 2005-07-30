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
