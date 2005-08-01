package net.sourceforge.stripes.config;

import net.sourceforge.stripes.controller.ActionBeanPropertyBinder;
import net.sourceforge.stripes.controller.ActionResolver;
import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.localization.LocalizationBundleFactory;
import net.sourceforge.stripes.localization.LocalePicker;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.validation.TypeConverterFactory;

/**
 * <p>Configuration class that uses the BootstrapPropertyResolver to look for configuration values,
 * and when it cannot find a value, falls back on the DefaultConfiguration to supply default
 * values.  In general, the RuntimeConfiguration will operate in the following pattern:</p>
 *
 * <ul>
 *   <li>Look for the value of a configuration property in the BootstrapProperties</li>
 *   <li>If the value exists, the configuration will attempt to use it (usually to instantiate
 *       a class). If an exception occurs, the RuntimeConfiguration will throw an exception and
 *       not provide a value.  In most cases this will be fatal!</li>
 *   <li>If the value does not exist, the default from DefaultConfiguration will be returned.</li>
 * </ul>
 *
 * @author Tim Fennell
 */
public class RuntimeConfiguration extends DefaultConfiguration {
    /** Log implementation for use within this class. */
    private static final Log log = Log.getInstance(RuntimeConfiguration.class);

    /** The Configuration Key for looking up the name of the ActionResolver class. */
    public static final String ACTION_RESOLVER = "ActionResolver.Class";

    /** The Configuration Key for looking up the name of the ActionResolver class. */
    public static final String ACTION_BEAN_PROPERTY_BINDER = "ActionBeanPropertyBinder.Class";

    /** The Configuration Key for looking up the name of the TypeConverterFactory class. */
    public static final String TYPE_CONVERTER_FACTORY = "TypeConverterFactory.Class";

    /** The Configuration Key for looking up the name of the LocalizationBundleFactory class. */
    public static final String LOCALIZATION_BUNDLE_FACTORY = "LocalizationBundleFactory.Class";

    /** The Configuration Key for looking up the name of the LocalizationBundleFactory class. */
    public static final String LOCALE_PICKER = "LocalePicker.Class";

    /**
     * Attempts to find names of implementation classes using the BootstrapPropertyResolver and
     * instantiates any configured classes found.  Then delegates to the DefaultConfiguration to
     * fill in the blanks.
     */
    public void init() {

        // Try instantiating the ActionResolver
        String resolverName = getBootstrapPropertyResolver().getProperty(ACTION_RESOLVER);
        try {
            if (resolverName != null) {
                log.info("Found configured ActionResolver class [", resolverName, "], attempting ",
                         "to instantiate.");

                    ActionResolver actionResolver
                            = (ActionResolver) Class.forName(resolverName).newInstance();
                    actionResolver.init(this);
                    setActionResolver(actionResolver);
                }
        }
        catch (Exception e) {
            throw new StripesRuntimeException("Could not instantiate configured ActionResolver "
                    + " of type [" + resolverName + "]. Please check the configuration "
                    + "parameters specified in your web.xml.", e);
        }

        // Try instantiating the ActionBeanPropertyBinder
        String binderName = getBootstrapPropertyResolver().getProperty(ACTION_BEAN_PROPERTY_BINDER);
        try {
            if (binderName != null) {
                log.info("Found configured ActionBeanPropertyBinder class [", binderName,
                         "], attempting to instantiate.");

                    ActionBeanPropertyBinder binder =
                            (ActionBeanPropertyBinder) Class.forName(binderName).newInstance();
                    binder.init(this);
                    setActionBeanPropertyBinder(binder);
            }
        }
        catch (Exception e) {
            throw new StripesRuntimeException("Could not instantiate configured "
                    + "ActionBeanPropertyBinder of type [" + binderName + "]. Please check the "
                    + "configuration parameters specified in your web.xml.", e);
        }

        // Try instantiating the TypeConverterFactory
        String converterName = getBootstrapPropertyResolver().getProperty(TYPE_CONVERTER_FACTORY);
        try {
            if (converterName != null) {
                log.info("Found configured TypeConverterFactory class [", converterName,
                         "], attempting to instantiate.");

                TypeConverterFactory converter =
                        (TypeConverterFactory) Class.forName(converterName).newInstance();
                converter.init(this);
                setTypeConverterFactory(converter);
            }
        }
        catch (Exception e) {
            throw new StripesRuntimeException("Could not instantiate configured "
                    + "ActionBeanPropertyBinder of type [" + binderName + "]. Please check the "
                    + "configuration parameters specified in your web.xml.", e);
        }

        // Try instantiating the LocalizationBundleFactory
        String bundleFactoryName =
                getBootstrapPropertyResolver().getProperty(LOCALIZATION_BUNDLE_FACTORY);
        try {
            if (bundleFactoryName != null) {
                log.info("Found configured LocalizationBundleFactory class [", bundleFactoryName,
                         "], attempting to instantiate.");

                LocalizationBundleFactory bundleFactory =
                        (LocalizationBundleFactory) Class.forName(bundleFactoryName).newInstance();
                bundleFactory.init(this);
                setLocalizationBundleFactory(bundleFactory);
            }
        }
        catch (Exception e) {
            throw new StripesRuntimeException("Could not instantiate configured "
                    + "LocalizationBundleFactory of type [" + bundleFactoryName + "]. Please check "
                    + "the configuration parameters specified in your web.xml.", e);
        }

        // Try instantiating the LocalePicker
        String localePickerName =
                getBootstrapPropertyResolver().getProperty(LOCALE_PICKER);
        try {
            if (localePickerName != null) {
                log.info("Found configured LocalePicker class [", localePickerName,
                         "], attempting to instantiate.");

                LocalePicker localePicker =
                        (LocalePicker) Class.forName(localePickerName).newInstance();
                localePicker.init(this);
                setLocalePicker(localePicker);
            }
        }
        catch (Exception e) {
            throw new StripesRuntimeException("Could not instantiate configured "
                    + "LocalePicker of type [" + localePickerName + "]. Please check "
                    + "the configuration parameters specified in your web.xml.", e);
        }

        // And now call super.init to fill in any blanks
        super.init();
    }
}

