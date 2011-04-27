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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

import net.sourceforge.stripes.controller.ActionBeanContextFactory;
import net.sourceforge.stripes.controller.ActionBeanPropertyBinder;
import net.sourceforge.stripes.controller.ActionResolver;
import net.sourceforge.stripes.controller.BeforeAfterMethodInterceptor;
import net.sourceforge.stripes.controller.DefaultActionBeanContextFactory;
import net.sourceforge.stripes.controller.DefaultActionBeanPropertyBinder;
import net.sourceforge.stripes.controller.DefaultObjectFactory;
import net.sourceforge.stripes.controller.HttpCacheInterceptor;
import net.sourceforge.stripes.controller.Interceptor;
import net.sourceforge.stripes.controller.Intercepts;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.controller.NameBasedActionResolver;
import net.sourceforge.stripes.controller.ObjectFactory;
import net.sourceforge.stripes.controller.ObjectPostProcessor;
import net.sourceforge.stripes.controller.multipart.DefaultMultipartWrapperFactory;
import net.sourceforge.stripes.controller.multipart.MultipartWrapperFactory;
import net.sourceforge.stripes.exception.DefaultExceptionHandler;
import net.sourceforge.stripes.exception.ExceptionHandler;
import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.format.DefaultFormatterFactory;
import net.sourceforge.stripes.format.FormatterFactory;
import net.sourceforge.stripes.localization.DefaultLocalePicker;
import net.sourceforge.stripes.localization.DefaultLocalizationBundleFactory;
import net.sourceforge.stripes.localization.LocalePicker;
import net.sourceforge.stripes.localization.LocalizationBundleFactory;
import net.sourceforge.stripes.tag.BeanFirstPopulationStrategy;
import net.sourceforge.stripes.tag.DefaultTagErrorRendererFactory;
import net.sourceforge.stripes.tag.PopulationStrategy;
import net.sourceforge.stripes.tag.TagErrorRendererFactory;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.validation.DefaultTypeConverterFactory;
import net.sourceforge.stripes.validation.DefaultValidationMetadataProvider;
import net.sourceforge.stripes.validation.TypeConverterFactory;
import net.sourceforge.stripes.validation.ValidationMetadataProvider;

/**
 * <p>Centralized location for defaults for all Configuration properties.  This implementation does
 * not lookup configuration information anywhere!  It returns hard-coded defaults that will result
 * in a working system without any user intervention.</p>
 *
 * <p>Despite it's name the DefaultConfiguration is not in fact the default Configuration
 * implementation in Stripes!  Instead it is the retainer of default configuration values. The
 * Configuration implementation that is used when no alternative is configured is the
 * {@link RuntimeConfiguration}, which is a direct subclass of DefaultConfiguration, and when no
 * further configuration properties are supplied behaves identically to the DefaultConfiguration.</p>
 *
 * <p>The DefaultConfiguration is designed to be easily extended as needed.  The init() method
 * ensures that components are initialized in the correct order (taking dependencies into account),
 * and should generally not be overridden. It invokes a number of initXXX() methods, one per
 * configurable component. Subclasses should override any of the initXXX() methods desirable to
 * return a fully initialized instance of the relevant component type, or null if the default is
 * desired.</p>
 *
 * @author Tim Fennell
 */
public class DefaultConfiguration implements Configuration {
    /** Log implementation for use within this class. */
    private static final Log log = Log.getInstance(DefaultConfiguration.class);

    private boolean debugMode;
    private BootstrapPropertyResolver resolver;
    private ObjectFactory objectFactory;
    private ActionResolver actionResolver;
    private ActionBeanPropertyBinder actionBeanPropertyBinder;
    private ActionBeanContextFactory actionBeanContextFactory;
    private TypeConverterFactory typeConverterFactory;
    private LocalizationBundleFactory localizationBundleFactory;
    private LocalePicker localePicker;
    private FormatterFactory formatterFactory;
    private TagErrorRendererFactory tagErrorRendererFactory;
    private PopulationStrategy populationStrategy;
    private Map<LifecycleStage,Collection<Interceptor>> interceptors;
    private ExceptionHandler exceptionHandler;
    private MultipartWrapperFactory multipartWrapperFactory;
    private ValidationMetadataProvider validationMetadataProvider;

    /** Gratefully accepts the BootstrapPropertyResolver handed to the Configuration. */
    public void setBootstrapPropertyResolver(BootstrapPropertyResolver resolver) {
        this.resolver = resolver;
    }

    /**
     * Creates and stores instances of the objects of the type that the Configuration is
     * responsible for providing, except where subclasses have already provided instances.
     */
    @SuppressWarnings("unchecked")
    public void init() {
        try {
        	Boolean debugMode = initDebugMode();
            if (debugMode != null) {
                this.debugMode = debugMode;
            }
            else {
                this.debugMode = false;
            }

            this.objectFactory = initObjectFactory();
            if (this.objectFactory == null) {
                this.objectFactory = new DefaultObjectFactory();
                this.objectFactory.init(this);
            }
            if (this.objectFactory instanceof DefaultObjectFactory) {
                List<Class<? extends ObjectPostProcessor>> classes = getBootstrapPropertyResolver()
                        .getClassPropertyList(ObjectPostProcessor.class);
                List<ObjectPostProcessor> instances = new ArrayList<ObjectPostProcessor>();
                for (Class<? extends ObjectPostProcessor> clazz : classes) {
                    log.debug("Instantiating object post-processor ", clazz);
                    instances.add(this.objectFactory.newInstance(clazz));
                }
                for (ObjectPostProcessor pp : instances) {
                    ((DefaultObjectFactory) this.objectFactory).addPostProcessor(pp);
                }
            }

            this.actionResolver = initActionResolver();
            if (this.actionResolver == null) {
                this.actionResolver = new NameBasedActionResolver();
                this.actionResolver.init(this);
            }

            this.actionBeanPropertyBinder = initActionBeanPropertyBinder();
            if (this.actionBeanPropertyBinder == null) {
                this.actionBeanPropertyBinder = new DefaultActionBeanPropertyBinder();
                this.actionBeanPropertyBinder.init(this);
            }

            this.actionBeanContextFactory = initActionBeanContextFactory();
            if (this.actionBeanContextFactory == null) {
                this.actionBeanContextFactory = new DefaultActionBeanContextFactory();
                this.actionBeanContextFactory.init(this);
            }

            this.typeConverterFactory = initTypeConverterFactory();
            if (this.typeConverterFactory == null) {
                this.typeConverterFactory = new DefaultTypeConverterFactory();
                this.typeConverterFactory.init(this);
            }

            this.localizationBundleFactory = initLocalizationBundleFactory();
            if (this.localizationBundleFactory == null) {
                this.localizationBundleFactory = new DefaultLocalizationBundleFactory();
                this.localizationBundleFactory.init(this);
            }

            this.localePicker = initLocalePicker();
            if (this.localePicker == null) {
                this.localePicker = new DefaultLocalePicker();
                this.localePicker.init(this);
            }

            this.formatterFactory = initFormatterFactory();
            if (this.formatterFactory == null) {
                this.formatterFactory = new DefaultFormatterFactory();
                this.formatterFactory.init(this);
            }

            this.tagErrorRendererFactory = initTagErrorRendererFactory();
            if (this.tagErrorRendererFactory == null) {
                this.tagErrorRendererFactory = new DefaultTagErrorRendererFactory();
                this.tagErrorRendererFactory.init(this);
            }

            this.populationStrategy = initPopulationStrategy();
            if (this.populationStrategy == null) {
                this.populationStrategy = new BeanFirstPopulationStrategy();
                this.populationStrategy.init(this);
            }

            this.exceptionHandler = initExceptionHandler();
            if (this.exceptionHandler == null) {
                this.exceptionHandler = new DefaultExceptionHandler();
                this.exceptionHandler.init(this);
            }

            this.multipartWrapperFactory = initMultipartWrapperFactory();
            if (this.multipartWrapperFactory == null) {
                this.multipartWrapperFactory = new DefaultMultipartWrapperFactory();
                this.multipartWrapperFactory.init(this);
            }

            this.validationMetadataProvider = initValidationMetadataProvider();
            if (this.validationMetadataProvider == null) {
                this.validationMetadataProvider = new DefaultValidationMetadataProvider();
                this.validationMetadataProvider.init(this);
            }

            this.interceptors = new HashMap<LifecycleStage, Collection<Interceptor>>();
            Map<LifecycleStage, Collection<Interceptor>> map = initCoreInterceptors();
            if (map != null) {
                mergeInterceptorMaps(this.interceptors, map);
            }
            map = initInterceptors();
            if (map != null) {
                mergeInterceptorMaps(this.interceptors, map);
            }

            // do a quick check to see if any interceptor classes are configured more than once
            for (Map.Entry<LifecycleStage, Collection<Interceptor>> entry : this.interceptors.entrySet()) {
                Set<Class<? extends Interceptor>> classes = new HashSet<Class<? extends Interceptor>>();
                Collection<Interceptor> interceptors = entry.getValue();
                if (interceptors == null)
                    continue;

                for (Interceptor interceptor : interceptors) {
                    Class<? extends Interceptor> clazz = interceptor.getClass();
                    if (classes.contains(clazz)) {
                        log.warn("Interceptor ", clazz,
                                " is configured to run more than once for ", entry.getKey());
                    }
                    else {
                        classes.add(clazz);
                    }
                }
            }
        }
        catch (Exception e) {
            throw new StripesRuntimeException
                    ("Problem instantiating default configuration objects.", e);
        }
    }

    /** Returns a reference to the resolver supplied at initialization time. */
    public BootstrapPropertyResolver getBootstrapPropertyResolver() {
        return this.resolver;
    }

    /**
     * Retrieves the ServletContext for the context within which the Stripes application is
     * executing.
     *
     * @return the ServletContext in which the application is running
     */
    public ServletContext getServletContext() {
        return getBootstrapPropertyResolver().getFilterConfig().getServletContext();
    }

	/** Enable or disable debug mode. */
	public void setDebugMode(boolean debugMode) {
		this.debugMode = debugMode;
	}

	/** Returns true if the Stripes application is running in debug mode. */
	public boolean isDebugMode() {
		return debugMode;
	}

	/** Allows subclasses to initialize a non-default debug mode value. */
	protected Boolean initDebugMode() {
		return null;
	}

    /**
     * Returns an instance of {@link ObjectFactory} that is used throughout Stripes to instantiate
     * classes.
     * 
     * @return an instance of {@link ObjectFactory}.
     */
    public ObjectFactory getObjectFactory() {
        return this.objectFactory;
    }

    /** Allows subclasses to initialize a non-default {@link ObjectFactory}. */
    protected ObjectFactory initObjectFactory() { return null; }

    /**
     * Returns an instance of {@link NameBasedActionResolver} unless a subclass has
     * overridden the default.
     * @return ActionResolver an instance of the configured resolver
     */
    public ActionResolver getActionResolver() {
        return this.actionResolver;
    }

    /** Allows subclasses to initialize a non-default ActionResovler. */
    protected ActionResolver initActionResolver() { return null; }

    /**
     * Returns an instance of {@link DefaultActionBeanPropertyBinder} unless a subclass has
     * overridden the default.
     * @return ActionBeanPropertyBinder an instance of the configured binder
     */
    public ActionBeanPropertyBinder getActionBeanPropertyBinder() {
        return this.actionBeanPropertyBinder;
    }

    /** Allows subclasses to initialize a non-default ActionBeanPropertyBinder. */
    protected ActionBeanPropertyBinder initActionBeanPropertyBinder() { return null; }

    /**
     * Returns the configured ActionBeanContextFactory. Unless a subclass has configured a custom
     * one, the instance will be a DefaultActionBeanContextFactory.
     *
     * @return ActionBeanContextFactory an instance of a factory for creating ActionBeanContexts
     */
    public ActionBeanContextFactory getActionBeanContextFactory() {
        return this.actionBeanContextFactory;
    }

    /** Allows subclasses to initialize a non-default ActionBeanContextFactory. */
    protected ActionBeanContextFactory initActionBeanContextFactory() { return null; }

    /**
     * Returns an instance of {@link DefaultTypeConverterFactory} unless a subclass has
     * overridden the default..
     * @return TypeConverterFactory an instance of the configured factory.
     */
    public TypeConverterFactory getTypeConverterFactory() {
        return this.typeConverterFactory;
    }

    /** Allows subclasses to initialize a non-default TypeConverterFactory. */
    protected TypeConverterFactory initTypeConverterFactory() { return null; }

    /**
     * Returns an instance of a LocalizationBundleFactory.  By default this will be an instance of
     * DefaultLocalizationBundleFactory unless another type has been configured.
     */
    public LocalizationBundleFactory getLocalizationBundleFactory() {
        return this.localizationBundleFactory;
    }

    /** Allows subclasses to initialize a non-default LocalizationBundleFactory. */
    protected LocalizationBundleFactory initLocalizationBundleFactory() { return null; }

    /**
     * Returns an instance of a LocalePicker. Unless a subclass has picked another implementation
     * will return an instance of DefaultLocalePicker.
     */
    public LocalePicker getLocalePicker() { return this.localePicker; }

    /** Allows subclasses to initialize a non-default LocalePicker. */
    protected LocalePicker initLocalePicker() { return null; }

    /**
     * Returns an instance of a FormatterFactory. Unless a subclass has picked another implementation
     * will return an instance of DefaultFormatterFactory.
     */
    public FormatterFactory getFormatterFactory() { return this.formatterFactory; }

    /** Allows subclasses to initialize a non-default FormatterFactory. */
    protected FormatterFactory initFormatterFactory() { return null; }

    /**
     * Returns an instance of a TagErrorRendererFactory. Unless a subclass has picked another
     * implementation,  will return an instance of DefaultTagErrorRendererFactory.
     */
    public TagErrorRendererFactory getTagErrorRendererFactory() {
        return tagErrorRendererFactory;
    }

    /** Allows subclasses to initialize a non-default TagErrorRendererFactory instance to be used. */
    protected TagErrorRendererFactory initTagErrorRendererFactory() { return null; }

    /**
     * Returns an instance of a PopulationsStrategy.  Unless a subclass has picked another
     * implementation, will return an instance of
     * {@link net.sourceforge.stripes.tag.BeanFirstPopulationStrategy}.
     * @since Stripes 1.6
     */
    public PopulationStrategy getPopulationStrategy() { return this.populationStrategy; }

    /** Allows subclasses to initialize a non-default PopulationStrategy instance to be used. */
    protected PopulationStrategy initPopulationStrategy() { return null; }

    /**
     * Returns an instance of an ExceptionHandler.  Unless a subclass has picked another
     * implementation, will return an instance of
     * {@link net.sourceforge.stripes.exception.DefaultExceptionHandler}.
     */
    public ExceptionHandler getExceptionHandler() { return this.exceptionHandler; }

    /** Allows subclasses to initialize a non-default ExceptionHandler instance to be used. */
    protected ExceptionHandler initExceptionHandler() { return null; }

    /**
     * Returns an instance of MultipartWrapperFactory that can be used by Stripes to construct
     * MultipartWrapper instances for dealing with multipart requests (those containing file
     * uploads).
     *
     * @return MultipartWrapperFactory an instance of the wrapper factory
     */
    public MultipartWrapperFactory getMultipartWrapperFactory() {
        return this.multipartWrapperFactory;
    }


    /** Allows subclasses to initialize a non-default MultipartWrapperFactory. */
    protected MultipartWrapperFactory initMultipartWrapperFactory() { return null; }

    /**
     * Returns an instance of {@link ValidationMetadataProvider} that can be used by Stripes to
     * determine what validations need to be applied during
     * {@link LifecycleStage#BindingAndValidation}.
     * 
     * @return an instance of {@link ValidationMetadataProvider}
     */
    public ValidationMetadataProvider getValidationMetadataProvider() {
        return this.validationMetadataProvider;
    }

    /** Allows subclasses to initialize a non-default {@link ValidationMetadataProvider}. */
    protected ValidationMetadataProvider initValidationMetadataProvider() { return null; }

    /**
     * Returns a list of interceptors that should be executed around the lifecycle stage
     * indicated.  By default returns a single element list containing the 
     * {@link BeforeAfterMethodInterceptor}.
     */
    public Collection<Interceptor> getInterceptors(LifecycleStage stage) {
        Collection<Interceptor> interceptors = this.interceptors.get(stage);
        if (interceptors == null) {
            interceptors = Collections.emptyList();
        }
        return interceptors;
    }
    
    /**
     * Merges the two {@link Map}s of {@link LifecycleStage} to {@link Collection} of
     * {@link Interceptor}. A simple {@link Map#putAll(Map)} does not work because it overwrites
     * the collections in the map instead of adding to them.
     */
    protected void mergeInterceptorMaps(Map<LifecycleStage, Collection<Interceptor>> dst,
            Map<LifecycleStage, Collection<Interceptor>> src) {
        for (Map.Entry<LifecycleStage, Collection<Interceptor>> entry : src.entrySet()) {
            Collection<Interceptor> collection = dst.get(entry.getKey());
            if (collection == null) {
                collection = new LinkedList<Interceptor>();
                dst.put(entry.getKey(), collection);
            }
            collection.addAll(entry.getValue());
        }
    }
    
    /**
     * Adds the interceptor to the map, associating it with the {@link LifecycleStage}s indicated
     * by the {@link Intercepts} annotation. If the interceptor implements
     * {@link ConfigurableComponent}, then its init() method will be called.
     */
    protected void addInterceptor(Map<LifecycleStage, Collection<Interceptor>> map,
            Interceptor interceptor) {
        Class<? extends Interceptor> type = interceptor.getClass();
        Intercepts intercepts = type.getAnnotation(Intercepts.class);
        if (intercepts == null) {
            log.error("An interceptor of type ", type.getName(), " was configured ",
                    "but was not marked with an @Intercepts annotation. As a ",
                    "result it is not possible to determine at which ",
                    "lifecycle stages the interceptor should be applied. This ",
                    "interceptor will be ignored.");
            return;
        }
        else {
            log.debug("Configuring interceptor '", type.getSimpleName(),
                    "', for lifecycle stages: ", intercepts.value());
        }

        // call init() if the interceptor implements ConfigurableComponent
        if (interceptor instanceof ConfigurableComponent) {
            try {
                ((ConfigurableComponent) interceptor).init(this);
            }
            catch (Exception e) {
                log.error("Error initializing interceptor of type " + type.getName(), e);
            }
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

    /** Instantiates the core interceptors, allowing subclasses to override the default behavior */
    protected Map<LifecycleStage, Collection<Interceptor>> initCoreInterceptors() {
        Map<LifecycleStage, Collection<Interceptor>> interceptors = new HashMap<LifecycleStage, Collection<Interceptor>>();
        addInterceptor(interceptors, new BeforeAfterMethodInterceptor());
        addInterceptor(interceptors, new HttpCacheInterceptor());
        return interceptors;
    }

    /** Allows subclasses to initialize a non-default Map of Interceptor instances. */
    protected Map<LifecycleStage,Collection<Interceptor>> initInterceptors() { return null; }
}
