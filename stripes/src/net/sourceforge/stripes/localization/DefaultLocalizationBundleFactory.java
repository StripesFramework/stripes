package net.sourceforge.stripes.localization;

import net.sourceforge.stripes.config.Configuration;

import java.util.ResourceBundle;
import java.util.Locale;
import java.util.MissingResourceException;

/**
 * Very simple default implementation of a bundle factory.  Looks for configuration parameters in
 * the bootstrap properties called "LocalizationBundleFactory.ErrorMessageBundle" and
 * "LocalizationBundleFactory.FieldNameBundle".  If one or both of these values is not specified
 * the default bundle name of "StripesResources" will be used in its place.
 *
 * @see net.sourceforge.stripes.config.BootstrapPropertyResolver
 * @author Tim Fennell
 */
public class DefaultLocalizationBundleFactory implements LocalizationBundleFactory {

    /** The name of the default resource bundle for Stripes. */
    public static final String BUNDLE_NAME = "StripesResources";

    /** Holds the configuration passed in at initialization time. */
    private Configuration configuration;
    private String errorBundleName;
    private String fieldBundleName;

    /**
     * Uses the BootstrapPropertyResolver attached to the Configuration in order to look for
     * configured bundle names in the servlet init parameters etc.  If those can't be found then
     * the default bundle names are put in place.
     */
    public void init(Configuration configuration) throws Exception {
        this.configuration = configuration;

        this.errorBundleName = configuration.getBootstrapPropertyResolver().
                getProperty("LocalizationBundleFactory.ErrorMessageBundle");
        if (this.errorBundleName == null) {
            this.errorBundleName = BUNDLE_NAME;
        }

        this.fieldBundleName = configuration.getBootstrapPropertyResolver().
                getProperty("LocalizationBundleFactory.FieldNameBundle");
        if (this.fieldBundleName == null) {
            this.fieldBundleName = BUNDLE_NAME;
        }
    }

    /**
     * Looks for a bundle called StripesResources with the supplied locale if one is provided,
     * or with the default locale if the locale provided is null.
     *
     * @param locale an optional locale, may be null.
     * @return ResourceBundle a bundle in which to look for localized error messages
     * @throws MissingResourceException if a suitable bundle cannot be found
     */
    public ResourceBundle getErrorMessageBundle(Locale locale) throws MissingResourceException {
        if (locale == null) {
            return ResourceBundle.getBundle(this.errorBundleName);
        }
        else {
            return ResourceBundle.getBundle(this.errorBundleName, locale);
        }
    }

    /**
     * Looks for a bundle called StripesResources with the supplied locale if one is provided,
     * or with the default locale if the locale provided is null.
     *
     * @param locale an optional locale, may be null.
     * @return ResourceBundle a bundle in which to look for localized field names
     * @throws MissingResourceException if a suitable bundle cannot be found
     */
    public ResourceBundle getFormFieldBundle(Locale locale) throws MissingResourceException {
        if (locale == null) {
            return ResourceBundle.getBundle(this.fieldBundleName);
        }
        else {
            return ResourceBundle.getBundle(this.fieldBundleName, locale);
        }
    }
}
