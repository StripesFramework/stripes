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
package net.sourceforge.stripes.config;

import net.sourceforge.stripes.controller.ActionBeanContextFactory;
import net.sourceforge.stripes.controller.ActionBeanPropertyBinder;
import net.sourceforge.stripes.controller.ActionResolver;
import net.sourceforge.stripes.controller.Interceptor;
import net.sourceforge.stripes.controller.Intercepts;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.controller.multipart.MultipartWrapperFactory;
import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.exception.ExceptionHandler;
import net.sourceforge.stripes.format.FormatterFactory;
import net.sourceforge.stripes.localization.LocalePicker;
import net.sourceforge.stripes.localization.LocalizationBundleFactory;
import net.sourceforge.stripes.tag.PopulationStrategy;
import net.sourceforge.stripes.tag.TagErrorRendererFactory;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.util.ReflectUtil;
import net.sourceforge.stripes.validation.TypeConverterFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

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

    /** The Configuration Key for looking up the name of the PopulationStrategy class */
    public static final String POPULATION_STRATEGY = "PopulationStrategy.Class";

    /** The Configuration Key for looking up the name of the ExceptionHandler class */
    public static final String EXCEPTION_HANDLER = "ExceptionHandler.Class";

    /** The Configuration Key for looking up the name of the MultipartWrapperFactory class */
    public static final String MULTIPART_WRAPPER_FACTORY = "MultipartWrapperFactory.Class";

    /** The Configuration Key for looking up the comma separated list of interceptor classes. */
    public static final String INTERCEPTOR_LIST = "Interceptor.Classes";


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

    /** Looks for a class name in config and uses that to create the component. */
    @Override protected PopulationStrategy initPopulationStrategy() {
        return initializeComponent(PopulationStrategy.class, POPULATION_STRATEGY);
    }

    /** Looks for a class name in config and uses that to create the component. */
    @Override protected ExceptionHandler initExceptionHandler() {
        return initializeComponent(ExceptionHandler.class, EXCEPTION_HANDLER);
    }

    /** Looks for a class name in config and uses that to create the component. */
    @Override protected MultipartWrapperFactory initMultipartWrapperFactory() {
        return initializeComponent(MultipartWrapperFactory.class, MULTIPART_WRAPPER_FACTORY);
    }

    /**
     * Looks for a list of class names separated by commas under the configuration key
     * {@link #INTERCEPTOR_LIST}.  White space surrounding the class names is trimmed,
     * the classes instantiated and then stored under the lifecycle stage(s) they should
     * intercept.
     *
     * @return a Map of {@link LifecycleStage} to Collection of {@link Interceptor}
     */
    @Override
    protected Map<LifecycleStage, Collection<Interceptor>> initInterceptors() {
        String classList = getBootstrapPropertyResolver().getProperty(INTERCEPTOR_LIST);
        if (classList == null) {
            return null;
        }
        else {
            String[] classNames = classList.split(",");
            Map<LifecycleStage, Collection<Interceptor>> map =
                    new HashMap<LifecycleStage, Collection<Interceptor>>();

            for (String className : classNames) {
                try {
                    Class<? extends Interceptor> type = ReflectUtil.findClass(className.trim());
                    Intercepts intercepts = type.getAnnotation(Intercepts.class);
                    if (intercepts == null) {
                        log.error("An interceptor of type ", type.getName(), " was configured ",
                                  "but was not marked with an @Intercepts annotation. As a ",
                                  "result it is not possible to determine at which ",
                                  "lifecycle stages the interceprot should be applied. This ",
                                  "interceptor will be ignored.");
                    }
                    else {
                        log.debug("Configuring interceptor '", type.getSimpleName(),
                                  "', for lifecycle stages: ", intercepts.value());
                    }

                    // Instantiate it and optionally call init() if the interceptor
                    // implements ConfigurableComponent
                    Interceptor interceptor = type.newInstance();
                    if (interceptor instanceof ConfigurableComponent) {
                        ((ConfigurableComponent) interceptor).init(this);
                    }

                    for (LifecycleStage stage : intercepts.value()) {
                        Collection<Interceptor> stack = map.get(stage);
                        if (stack == null) {
                            stack = new LinkedList<Interceptor>();
                            map.put(stage, stack);
                        }

                        stack.add(interceptor);
                    }
                }
                catch (Exception e) {
                    throw new StripesRuntimeException(
                            "Could not instantiate one or more configured Interceptors. The " +
                            "property '" + INTERCEPTOR_LIST + "' contained [" + classList +
                            "]. This value must contain fully qualified class names separated " +
                            "by commas.", e);
                }
            }

            return map;
        }
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

                T component = (T) ReflectUtil.findClass(className).newInstance();
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

