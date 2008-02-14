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

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.stripes.controller.ActionBeanContextFactory;
import net.sourceforge.stripes.controller.ActionBeanPropertyBinder;
import net.sourceforge.stripes.controller.ActionResolver;
import net.sourceforge.stripes.controller.Interceptor;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.controller.multipart.MultipartWrapperFactory;
import net.sourceforge.stripes.exception.ExceptionHandler;
import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.format.Formatter;
import net.sourceforge.stripes.format.FormatterFactory;
import net.sourceforge.stripes.localization.LocalePicker;
import net.sourceforge.stripes.localization.LocalizationBundleFactory;
import net.sourceforge.stripes.tag.PopulationStrategy;
import net.sourceforge.stripes.tag.TagErrorRendererFactory;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.util.ReflectUtil;
import net.sourceforge.stripes.validation.TypeConverter;
import net.sourceforge.stripes.validation.TypeConverterFactory;
import net.sourceforge.stripes.validation.ValidationMetadataProvider;

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

    /** The Configuration Key for enabling debug mode. */
    public static final String DEBUG_MODE = "Stripes.DebugMode";

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

    /** The Configuration Key for looking up the name of the ValidationMetadataProvider class */
    public static final String VALIDATION_METADATA_PROVIDER = "ValidationMetadataProvider.Class";

    /** The Configuration Key for looking up the comma separated list of core interceptor classes. */
    public static final String CORE_INTERCEPTOR_LIST = "CoreInterceptor.Classes";

    /** The Configuration Key for looking up the comma separated list of interceptor classes. */
    public static final String INTERCEPTOR_LIST = "Interceptor.Classes";
    
    /** Looks for a true/false value in config. */
    @Override protected Boolean initDebugMode() {
        try {
            return Boolean.valueOf(getBootstrapPropertyResolver().getProperty(DEBUG_MODE)
                    .toLowerCase());
        }
        catch (Exception e) {
            return null;
        }
    }

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

    /** Looks for a class name in config and uses that to create the component. */
    @Override protected ValidationMetadataProvider initValidationMetadataProvider() {
        return initializeComponent(ValidationMetadataProvider.class, VALIDATION_METADATA_PROVIDER);
    }

    /**
     * Looks for a list of class names separated by commas under the configuration key
     * {@link #CORE_INTERCEPTOR_LIST}.  White space surrounding the class names is trimmed,
     * the classes instantiated and then stored under the lifecycle stage(s) they should
     * intercept.
     * 
     * @return a Map of {@link LifecycleStage} to Collection of {@link Interceptor}
     */
    @Override
    protected Map<LifecycleStage, Collection<Interceptor>> initCoreInterceptors() {
        String classList = getBootstrapPropertyResolver().getProperty(CORE_INTERCEPTOR_LIST);
        if (classList == null)
            return super.initCoreInterceptors();
        else
            return initInterceptors(getBootstrapPropertyResolver().getClassPropertyList(CORE_INTERCEPTOR_LIST));
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
        return initInterceptors(getBootstrapPropertyResolver().getClassPropertyList(INTERCEPTOR_LIST, Interceptor.class));
    }

    /**
     * Splits a comma-separated list of class names and maps each {@link LifecycleStage} to the
     * interceptors in the list that intercept it. Also automatically finds Interceptors in
     * packages listed in {@link BootstrapPropertyResolver#PACKAGES} if searchExtensionPackages is true.
     * 
     * @return a Map of {@link LifecycleStage} to Collection of {@link Interceptor}
     */
    @SuppressWarnings("unchecked")
    protected Map<LifecycleStage, Collection<Interceptor>> initInterceptors(List classes) {

        Map<LifecycleStage, Collection<Interceptor>> map = new HashMap<LifecycleStage, Collection<Interceptor>>();

        for (Object type : classes) {
            try {
                Interceptor interceptor = (Interceptor) ((Class) type).newInstance();
                addInterceptor(map, interceptor);
            }
            catch (Exception e) {
                throw new StripesRuntimeException("Could not instantiate configured Interceptor ["
                        + type.getClass().getName() + "].", e);
            }
        }

        return map;
    }

    /**
     * Internal utility method that is used to implement the main pattern of this class: lookup the
     * name of a class based on a property name, instantiate the named class and initialize it.
     * 
     * @param componentType a Class object representing a subclass of ConfigurableComponent
     * @param propertyName the name of the property to look up for the class name
     * @return an instance of the component, or null if one was not configured.
     */
    @SuppressWarnings("unchecked")
	protected <T extends ConfigurableComponent> T initializeComponent(Class<T> componentType,
                                                                      String propertyName) {
        Class clazz = getBootstrapPropertyResolver().getClassProperty(propertyName, componentType);
        if (clazz != null) {
            try {
                T component = (T) clazz.newInstance();
                component.init(this);
                return component;
            }
            catch (Exception e) {
                throw new StripesRuntimeException("Could not instantiate configured "
                        + componentType.getSimpleName() + " of type [" + clazz.getSimpleName()
                        + "]. Please check "
                        + "the configuration parameters specified in your web.xml.", e);

            }
        }
        else {
            return null;
        }
    }

    /**
     * Calls super.init() then adds Formatters and TypeConverters found in
     * packages listed in {@link BootstrapPropertyResolver#PACKAGES} to their respective factories.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void init() {
        super.init();
        
        List<Class<? extends Formatter>> formatters = getBootstrapPropertyResolver().getClassPropertyList(Formatter.class);
        for (Class<? extends Formatter> formatter : formatters) {
            Type[] typeArguments = ReflectUtil.getActualTypeArguments(formatter, Formatter.class);
            log.trace("Found Formatter [", formatter, "] - type parameters: ", typeArguments);
            if ((typeArguments != null) && (typeArguments.length == 1)
                    && !typeArguments[0].equals(Object.class)) {
                log.debug("Adding auto-discovered Formatter [", formatter, "] for [", typeArguments[0], "] (from type parameter)");
                getFormatterFactory().add((Class<?>) typeArguments[0], (Class<? extends Formatter<?>>) formatter);
            }
            
            TargetTypes targetTypes = formatter.getAnnotation(TargetTypes.class);
            if (targetTypes != null) {
                for (Class<?> targetType : targetTypes.value()) {
                    log.debug("Adding auto-discovered Formatter [", formatter, "] for [", targetType, "] (from TargetTypes annotation)");
                    getFormatterFactory().add(targetType, (Class<? extends Formatter<?>>) formatter);
                }
            }
        }

        List<Class<? extends TypeConverter>> typeConverters = getBootstrapPropertyResolver().getClassPropertyList(TypeConverter.class);
        for (Class<? extends TypeConverter> typeConverter : typeConverters) {
            Type[] typeArguments = ReflectUtil.getActualTypeArguments(typeConverter, TypeConverter.class);
            log.trace("Found TypeConverter [", typeConverter, "] - type parameters: ", typeArguments);
            if ((typeArguments != null) && (typeArguments.length == 1)
                    && !typeArguments[0].equals(Object.class)) {
                log.debug("Adding auto-discovered TypeConverter [", typeConverter, "] for [", typeArguments[0], "] (from type parameter)");
                getTypeConverterFactory().add((Class<?>) typeArguments[0], (Class<? extends TypeConverter<?>>) typeConverter);
            }
            
            TargetTypes targetTypes = typeConverter.getAnnotation(TargetTypes.class);
            if (targetTypes != null) {
                for (Class<?> targetType : targetTypes.value()) {
                    log.debug("Adding auto-discovered TypeConverter [", typeConverter, "] for [", targetType, "] (from TargetTypes annotation)");
                    getTypeConverterFactory().add(targetType, (Class<? extends TypeConverter<?>>) typeConverter);
                }
            }
        }
    }
}
