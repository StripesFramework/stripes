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
package net.sourceforge.stripes.config;

import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.tag.TagErrorRendererFactory;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.controller.ActionResolver;
import net.sourceforge.stripes.controller.ActionBeanPropertyBinder;
import net.sourceforge.stripes.controller.ActionBeanContextFactory;
import net.sourceforge.stripes.validation.TypeConverterFactory;
import net.sourceforge.stripes.localization.LocalizationBundleFactory;
import net.sourceforge.stripes.localization.LocalePicker;
import net.sourceforge.stripes.format.FormatterFactory;

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
 *   <li>If the value does not exist, the default from DefaultConfiguration will be used.</li>
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

    /** The Configuration Key for looking up the name of an ActionBeanContextFactory class. */
    public static final String ACTION_BEAN_CONTEXT_FACTORY = "ActionBeanContextFactory.Class";

    /** The Configuration Key for looking up the name of the TypeConverterFactory class. */
    public static final String TYPE_CONVERTER_FACTORY = "TypeConverterFactory.Class";

    /** The Configuration Key for looking up the name of the LocalizationBundleFactory class. */
    public static final String LOCALIZATION_BUNDLE_FACTORY = "LocalizationBundleFactory.Class";

    /** The Configuration Key for looking up the name of the LocalizationBundleFactory class. */
    public static final String LOCALE_PICKER = "LocalePicker.Class";

    /** The Configuration Key for looking up the name of the FormatterFactory class. */
    public static final String FORMATTER_FACTORY = "FormatterFactory.Class";

    /** The Configuration Key for looking up the name of the TagErrorRendererFactory class */
    public static final String TAG_ERROR_RENDERER_FACTORY = "TagErrorRendererFactory.Class";

    /** Looks for a class name in config and uses that to create the component. */
    @Override protected ActionResolver initActionResolver() {
        return initializeComponent(ActionResolver.class, ACTION_RESOLVER);
    }

    /** Looks for a class name in config and uses that to create the component. */
    @Override protected ActionBeanPropertyBinder initActionBeanPropertyBinder() {
        return initializeComponent(ActionBeanPropertyBinder.class, ACTION_BEAN_PROPERTY_BINDER);
    }

    /** Looks for a class name in config and uses that to create the component. */
    @Override protected ActionBeanContextFactory initActionBeanContextFactory() {
        return initializeComponent(ActionBeanContextFactory.class, ACTION_BEAN_CONTEXT_FACTORY);
    }

    /** Looks for a class name in config and uses that to create the component. */
    @Override protected TypeConverterFactory initTypeConverterFactory() {
        return initializeComponent(TypeConverterFactory.class, TYPE_CONVERTER_FACTORY);
    }

    /** Looks for a class name in config and uses that to create the component. */
    @Override protected LocalizationBundleFactory initLocalizationBundleFactory() {
        return initializeComponent(LocalizationBundleFactory.class, LOCALIZATION_BUNDLE_FACTORY);
    }

    /** Looks for a class name in config and uses that to create the component. */
    @Override protected LocalePicker initLocalePicker() {
        return initializeComponent(LocalePicker.class, LOCALE_PICKER);
    }

    /** Looks for a class name in config and uses that to create the component. */
    @Override protected FormatterFactory initFormatterFactory() {
        return initializeComponent(FormatterFactory.class, FORMATTER_FACTORY);
    }

    /** Looks for a class name in config and uses that to create the component. */
    @Override protected TagErrorRendererFactory initTagErrorRendererFactory() {
        return initializeComponent(TagErrorRendererFactory.class, TAG_ERROR_RENDERER_FACTORY);
    }

    /**
     * Internal utility method that is used to implement the main pattern of this class: lookup
     * the name of a class based on a property name, instantiate the named class and initialize it.
     *
     * @param componentType a Class object representing a subclass of ConfigurableComponent
     * @param propertyName the name of the property to look up for the class name
     * @return an instance of the component, or null if one was not configured.
     */
    protected <T extends ConfigurableComponent> T initializeComponent(Class<T> componentType,
                                                                      String propertyName) {
        String className = getBootstrapPropertyResolver().getProperty(propertyName);

        if (className != null) {
            String componentTypeName = componentType.getSimpleName();
            try {
                log.info("Found configured ", componentTypeName, " class [", className,
                         "], attempting to instantiate and initialize.");

                T component = (T) Class.forName(className).newInstance();
                component.init(this);
                return component;
            }
            catch (Exception e) {
                throw new StripesRuntimeException("Could not instantiate configured "
                        + componentTypeName + " of type [" + className + "]. Please check "
                        + "the configuration parameters specified in your web.xml.", e);
            }
        }
        else {
            return null;
        }
    }
}

