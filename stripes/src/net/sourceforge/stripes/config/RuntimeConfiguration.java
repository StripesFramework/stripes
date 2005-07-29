package net.sourceforge.stripes.config;

import net.sourceforge.stripes.exception.StripesServletException;
import net.sourceforge.stripes.controller.ActionResolver;
import net.sourceforge.stripes.controller.ActionBeanPropertyBinder;
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

    /** The Configuration Key for lookup up the name of the ActionResolver class. */
    public static final String ACTION_RESOLVER = "ActionResolver.Class";

    /** The Configuration Key for lookup up the name of the ActionResolver class. */
    public static final String ACTION_BEAN_PROPERTY_BINDER = "ActionBeanPropertyBinder.Class";

    /** The Configuration Key for lookup up the name of the TypeConverterFactory class. */
    public static final String TYPE_CONVERTER_FACTORY = "TypeConverterFactory.Class";


    /**
     * Attempts to find names of implementation classes using the BootstrapPropertyResolver and
     * instantiates any configured classes found.  Then delegates to the DefaultConfiguration to
     * fill in the blanks.
     * @throws StripesServletException
     */
    public void init() throws StripesServletException {

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
            throw new StripesServletException("Could not instantiate configured ActionResolver "
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
            throw new StripesServletException("Could not instantiate configured "
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
            throw new StripesServletException("Could not instantiate configured "
                    + "ActionBeanPropertyBinder of type [" + binderName + "]. Please check the "
                    + "configuration parameters specified in your web.xml.", e);
        }

        // And now call super.init to fill in any blanks
        super.init();
    }
}

