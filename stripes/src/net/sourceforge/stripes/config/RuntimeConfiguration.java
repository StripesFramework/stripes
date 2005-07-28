package net.sourceforge.stripes.config;

import net.sourceforge.stripes.exception.StripesServletException;
import net.sourceforge.stripes.controller.ActionResolver;
import net.sourceforge.stripes.controller.ActionBeanPropertyBinder;
import net.sourceforge.stripes.util.Log;

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


    /**
     * Checks for a configured class name, an if one exists tries to instantiate it. If the
     * value is present, but the class cannot be instantiated then an exception is thrown and
     * no ActionResolver is returned.  If the configured value is not present the default
     * will be used.
     */
    public ActionResolver getActionResolver() throws StripesServletException {
        String className = getBootstrapPropertyResolver().getProperty(ACTION_RESOLVER);

        if (className != null) {
            log.info("Found configured ActionResolver class [", className, "], attempting ",
                     "to instantiate.");

            try {
                ActionResolver resolver = (ActionResolver) Class.forName(className).newInstance();
                resolver.init(this);
                return resolver;
            }
            catch (Exception e) {
                throw new StripesServletException("Could not instantiate configured ActionResolver "
                        + " of type [" + className + "]. Please check the configuration "
                        + "parameters specified in your web.xml.", e);
            }
        }
        else {
            return super.getActionResolver();
        }
    }

    /**
     * Checks for a configured class name, an if one exists tries to instantiate it. If the
     * value is present, but the class cannot be instantiated then an exception is thrown and
     * no ActionBeanPropertyBinder is returned.  If the configured value is not present the default
     * will be used.
     */
    public ActionBeanPropertyBinder getActionBeanPropertyBinder() throws StripesServletException {
        String className = getBootstrapPropertyResolver().getProperty(ACTION_BEAN_PROPERTY_BINDER);

        if (className != null) {
            log.info("Found configured ActionBeanPropertyBinder class [", className, "], attempting ",
                     "to instantiate.");

            try {
                ActionBeanPropertyBinder binder =
                        (ActionBeanPropertyBinder) Class.forName(className).newInstance();
                binder.init(this);
                return binder;
            }
            catch (Exception e) {
                throw new StripesServletException("Could not instantiate configured "
                        + "ActionBeanPropertyBinder of type [" + className + "]. Please check the "
                        + "configuration parameters specified in your web.xml.", e);
            }
        }
        else {
            return super.getActionBeanPropertyBinder();
        }
    }
}

