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
package org.stripesframework.web.config;

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

import org.stripesframework.web.controller.ActionBeanContextFactory;
import org.stripesframework.web.controller.ActionBeanPropertyBinder;
import org.stripesframework.web.controller.ActionResolver;
import org.stripesframework.web.controller.BeforeAfterMethodInterceptor;
import org.stripesframework.web.controller.DefaultActionBeanContextFactory;
import org.stripesframework.web.controller.DefaultActionBeanPropertyBinder;
import org.stripesframework.web.controller.DefaultObjectFactory;
import org.stripesframework.web.controller.HttpCacheInterceptor;
import org.stripesframework.web.controller.Interceptor;
import org.stripesframework.web.controller.Intercepts;
import org.stripesframework.web.controller.LifecycleStage;
import org.stripesframework.web.controller.NameBasedActionResolver;
import org.stripesframework.web.controller.ObjectFactory;
import org.stripesframework.web.controller.ObjectPostProcessor;
import org.stripesframework.web.controller.multipart.DefaultMultipartWrapperFactory;
import org.stripesframework.web.controller.multipart.MultipartWrapperFactory;
import org.stripesframework.web.exception.DefaultExceptionHandler;
import org.stripesframework.web.exception.ExceptionHandler;
import org.stripesframework.web.exception.StripesRuntimeException;
import org.stripesframework.web.format.DefaultFormatterFactory;
import org.stripesframework.web.format.FormatterFactory;
import org.stripesframework.web.localization.DefaultLocalePicker;
import org.stripesframework.web.localization.DefaultLocalizationBundleFactory;
import org.stripesframework.web.localization.LocalePicker;
import org.stripesframework.web.localization.LocalizationBundleFactory;
import org.stripesframework.web.util.Log;
import org.stripesframework.web.validation.DefaultTypeConverterFactory;
import org.stripesframework.web.validation.DefaultValidationMetadataProvider;
import org.stripesframework.web.validation.TypeConverterFactory;
import org.stripesframework.web.validation.ValidationMetadataProvider;


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

   private boolean                                      _debugMode;
   private BootstrapPropertyResolver                    _resolver;
   private ObjectFactory                                _objectFactory;
   private ActionResolver                               _actionResolver;
   private ActionBeanPropertyBinder                     _actionBeanPropertyBinder;
   private ActionBeanContextFactory                     _actionBeanContextFactory;
   private TypeConverterFactory                         _typeConverterFactory;
   private LocalizationBundleFactory                    _localizationBundleFactory;
   private LocalePicker                                 _localePicker;
   private FormatterFactory                             _formatterFactory;
   private Map<LifecycleStage, Collection<Interceptor>> _interceptors;
   private ExceptionHandler                             _exceptionHandler;
   private MultipartWrapperFactory                      _multipartWrapperFactory;
   private ValidationMetadataProvider                   _validationMetadataProvider;

   /**
    * Returns the configured ActionBeanContextFactory. Unless a subclass has configured a custom
    * one, the instance will be a DefaultActionBeanContextFactory.
    *
    * @return ActionBeanContextFactory an instance of a factory for creating ActionBeanContexts
    */
   @Override
   public ActionBeanContextFactory getActionBeanContextFactory() {
      return _actionBeanContextFactory;
   }

   /**
    * Returns an instance of {@link DefaultActionBeanPropertyBinder} unless a subclass has
    * overridden the default.
    * @return ActionBeanPropertyBinder an instance of the configured binder
    */
   @Override
   public ActionBeanPropertyBinder getActionBeanPropertyBinder() {
      return _actionBeanPropertyBinder;
   }

   /**
    * Returns an instance of {@link NameBasedActionResolver} unless a subclass has
    * overridden the default.
    * @return ActionResolver an instance of the configured resolver
    */
   @Override
   public ActionResolver getActionResolver() {
      return _actionResolver;
   }

   /** Returns a reference to the resolver supplied at initialization time. */
   @Override
   public BootstrapPropertyResolver getBootstrapPropertyResolver() {
      return _resolver;
   }

   /**
    * Returns an instance of an ExceptionHandler.  Unless a subclass has picked another
    * implementation, will return an instance of
    * {@link org.stripesframework.web.exception.DefaultExceptionHandler}.
    */
   @Override
   public ExceptionHandler getExceptionHandler() { return _exceptionHandler; }

   /**
    * Returns an instance of a FormatterFactory. Unless a subclass has picked another implementation
    * will return an instance of DefaultFormatterFactory.
    */
   @Override
   public FormatterFactory getFormatterFactory() { return _formatterFactory; }

   /**
    * Returns a list of interceptors that should be executed around the lifecycle stage
    * indicated.  By default returns a single element list containing the
    * {@link BeforeAfterMethodInterceptor}.
    */
   @Override
   public Collection<Interceptor> getInterceptors( LifecycleStage stage ) {
      Collection<Interceptor> interceptors = _interceptors.get(stage);
      if ( interceptors == null ) {
         interceptors = Collections.emptyList();
      }
      return interceptors;
   }

   /**
    * Returns an instance of a LocalePicker. Unless a subclass has picked another implementation
    * will return an instance of DefaultLocalePicker.
    */
   @Override
   public LocalePicker getLocalePicker() { return _localePicker; }

   /**
    * Returns an instance of a LocalizationBundleFactory.  By default this will be an instance of
    * DefaultLocalizationBundleFactory unless another type has been configured.
    */
   @Override
   public LocalizationBundleFactory getLocalizationBundleFactory() {
      return _localizationBundleFactory;
   }

   /**
    * Returns an instance of MultipartWrapperFactory that can be used by Stripes to construct
    * MultipartWrapper instances for dealing with multipart requests (those containing file
    * uploads).
    *
    * @return MultipartWrapperFactory an instance of the wrapper factory
    */
   @Override
   public MultipartWrapperFactory getMultipartWrapperFactory() {
      return _multipartWrapperFactory;
   }

   /**
    * Returns an instance of {@link ObjectFactory} that is used throughout Stripes to instantiate
    * classes.
    *
    * @return an instance of {@link ObjectFactory}.
    */
   @Override
   public ObjectFactory getObjectFactory() {
      return _objectFactory;
   }

   /**
    * Retrieves the ServletContext for the context within which the Stripes application is
    * executing.
    *
    * @return the ServletContext in which the application is running
    */
   @Override
   public ServletContext getServletContext() {
      return getBootstrapPropertyResolver().getFilterConfig().getServletContext();
   }

   /**
    * Returns an instance of {@link DefaultTypeConverterFactory} unless a subclass has
    * overridden the default..
    * @return TypeConverterFactory an instance of the configured factory.
    */
   @Override
   public TypeConverterFactory getTypeConverterFactory() {
      return _typeConverterFactory;
   }

   /**
    * Returns an instance of {@link ValidationMetadataProvider} that can be used by Stripes to
    * determine what validations need to be applied during
    * {@link LifecycleStage#BindingAndValidation}.
    *
    * @return an instance of {@link ValidationMetadataProvider}
    */
   @Override
   public ValidationMetadataProvider getValidationMetadataProvider() {
      return _validationMetadataProvider;
   }

   /**
    * Creates and stores instances of the objects of the type that the Configuration is
    * responsible for providing, except where subclasses have already provided instances.
    */
   @SuppressWarnings("rawtypes")
   @Override
   public void init() {
      try {
         Boolean debugMode = initDebugMode();
         if ( debugMode != null ) {
            _debugMode = debugMode;
         } else {
            _debugMode = false;
         }

         _objectFactory = initObjectFactory();
         if ( _objectFactory == null ) {
            _objectFactory = new DefaultObjectFactory();
            _objectFactory.init(this);
         }
         if ( _objectFactory instanceof DefaultObjectFactory ) {
            List<Class<? extends ObjectPostProcessor>> classes = getBootstrapPropertyResolver().getClassPropertyList(ObjectPostProcessor.class);
            List<ObjectPostProcessor> instances = new ArrayList<>();
            for ( Class<? extends ObjectPostProcessor> clazz : classes ) {
               log.debug("Instantiating object post-processor ", clazz);
               instances.add(_objectFactory.newInstance(clazz));
            }
            for ( ObjectPostProcessor pp : instances ) {
               ((DefaultObjectFactory)_objectFactory).addPostProcessor(pp);
            }
         }

         _actionResolver = initActionResolver();
         if ( _actionResolver == null ) {
            _actionResolver = new NameBasedActionResolver();
            _actionResolver.init(this);
         }

         _actionBeanPropertyBinder = initActionBeanPropertyBinder();
         if ( _actionBeanPropertyBinder == null ) {
            _actionBeanPropertyBinder = new DefaultActionBeanPropertyBinder();
            _actionBeanPropertyBinder.init(this);
         }

         _actionBeanContextFactory = initActionBeanContextFactory();
         if ( _actionBeanContextFactory == null ) {
            _actionBeanContextFactory = new DefaultActionBeanContextFactory();
            _actionBeanContextFactory.init(this);
         }

         _typeConverterFactory = initTypeConverterFactory();
         if ( _typeConverterFactory == null ) {
            _typeConverterFactory = new DefaultTypeConverterFactory();
            _typeConverterFactory.init(this);
         }

         _localizationBundleFactory = initLocalizationBundleFactory();
         if ( _localizationBundleFactory == null ) {
            _localizationBundleFactory = new DefaultLocalizationBundleFactory();
            _localizationBundleFactory.init(this);
         }

         _localePicker = initLocalePicker();
         if ( _localePicker == null ) {
            _localePicker = new DefaultLocalePicker();
            _localePicker.init(this);
         }

         _formatterFactory = initFormatterFactory();
         if ( _formatterFactory == null ) {
            _formatterFactory = new DefaultFormatterFactory();
            _formatterFactory.init(this);
         }

         _exceptionHandler = initExceptionHandler();
         if ( _exceptionHandler == null ) {
            _exceptionHandler = new DefaultExceptionHandler();
            _exceptionHandler.init(this);
         }

         _multipartWrapperFactory = initMultipartWrapperFactory();
         if ( _multipartWrapperFactory == null ) {
            _multipartWrapperFactory = new DefaultMultipartWrapperFactory();
            _multipartWrapperFactory.init(this);
         }

         _validationMetadataProvider = initValidationMetadataProvider();
         if ( _validationMetadataProvider == null ) {
            _validationMetadataProvider = new DefaultValidationMetadataProvider();
            _validationMetadataProvider.init(this);
         }

         _interceptors = new HashMap<>();
         Map<LifecycleStage, Collection<Interceptor>> map = initCoreInterceptors();
         if ( map != null ) {
            mergeInterceptorMaps(_interceptors, map);
         }
         map = initInterceptors();
         if ( map != null ) {
            mergeInterceptorMaps(_interceptors, map);
         }

         // do a quick check to see if any interceptor classes are configured more than once
         for ( Map.Entry<LifecycleStage, Collection<Interceptor>> entry : _interceptors.entrySet() ) {
            Set<Class<? extends Interceptor>> classes = new HashSet<>();
            Collection<Interceptor> interceptors = entry.getValue();
            if ( interceptors == null ) {
               continue;
            }

            for ( Interceptor interceptor : interceptors ) {
               Class<? extends Interceptor> clazz = interceptor.getClass();
               if ( classes.contains(clazz) ) {
                  log.warn("Interceptor ", clazz, " is configured to run more than once for ", entry.getKey());
               } else {
                  classes.add(clazz);
               }
            }
         }
      }
      catch ( Exception e ) {
         throw new StripesRuntimeException("Problem instantiating default configuration objects.", e);
      }
   }

   /** Returns true if the Stripes application is running in debug mode. */
   @Override
   public boolean isDebugMode() {
      return _debugMode;
   }

   /** Gratefully accepts the BootstrapPropertyResolver handed to the Configuration. */
   @Override
   public void setBootstrapPropertyResolver( BootstrapPropertyResolver resolver ) {
      _resolver = resolver;
   }

   /** Enable or disable debug mode. */
   @Override
   public void setDebugMode( boolean debugMode ) {
      _debugMode = debugMode;
   }

   /**
    * Adds the interceptor to the map, associating it with the {@link LifecycleStage}s indicated
    * by the {@link Intercepts} annotation. If the interceptor implements
    * {@link ConfigurableComponent}, then its init() method will be called.
    */
   protected void addInterceptor( Map<LifecycleStage, Collection<Interceptor>> map, Interceptor interceptor ) {
      Class<? extends Interceptor> type = interceptor.getClass();
      Intercepts intercepts = type.getAnnotation(Intercepts.class);
      if ( intercepts == null ) {
         log.error("An interceptor of type ", type.getName(), " was configured ", "but was not marked with an @Intercepts annotation. As a ",
               "result it is not possible to determine at which ", "lifecycle stages the interceptor should be applied. This ", "interceptor will be ignored.");
         return;
      } else {
         log.debug("Configuring interceptor '", type.getSimpleName(), "', for lifecycle stages: ", intercepts.value());
      }

      // call init() if the interceptor implements ConfigurableComponent
      if ( interceptor instanceof ConfigurableComponent ) {
         try {
            ((ConfigurableComponent)interceptor).init(this);
         }
         catch ( Exception e ) {
            log.error("Error initializing interceptor of type " + type.getName(), e);
         }
      }

      for ( LifecycleStage stage : intercepts.value() ) {
         Collection<Interceptor> stack = map.computeIfAbsent(stage, k -> new LinkedList<>());

         stack.add(interceptor);
      }
   }

   /** Allows subclasses to initialize a non-default ActionBeanContextFactory. */
   protected ActionBeanContextFactory initActionBeanContextFactory() { return null; }

   /** Allows subclasses to initialize a non-default ActionBeanPropertyBinder. */
   protected ActionBeanPropertyBinder initActionBeanPropertyBinder() { return null; }

   /** Allows subclasses to initialize a non-default ActionResovler. */
   protected ActionResolver initActionResolver() { return null; }

   /** Instantiates the core interceptors, allowing subclasses to override the default behavior */
   protected Map<LifecycleStage, Collection<Interceptor>> initCoreInterceptors() {
      Map<LifecycleStage, Collection<Interceptor>> interceptors = new HashMap<>();
      addInterceptor(interceptors, new BeforeAfterMethodInterceptor());
      addInterceptor(interceptors, new HttpCacheInterceptor());
      return interceptors;
   }

   /** Allows subclasses to initialize a non-default debug mode value. */
   protected Boolean initDebugMode() {
      return null;
   }

   /** Allows subclasses to initialize a non-default ExceptionHandler instance to be used. */
   protected ExceptionHandler initExceptionHandler() { return null; }

   /** Allows subclasses to initialize a non-default FormatterFactory. */
   protected FormatterFactory initFormatterFactory() { return null; }

   /** Allows subclasses to initialize a non-default Map of Interceptor instances. */
   protected Map<LifecycleStage, Collection<Interceptor>> initInterceptors() { return null; }

   /** Allows subclasses to initialize a non-default LocalePicker. */
   protected LocalePicker initLocalePicker() { return null; }

   /** Allows subclasses to initialize a non-default LocalizationBundleFactory. */
   protected LocalizationBundleFactory initLocalizationBundleFactory() { return null; }

   /** Allows subclasses to initialize a non-default MultipartWrapperFactory. */
   protected MultipartWrapperFactory initMultipartWrapperFactory() { return null; }

   /** Allows subclasses to initialize a non-default {@link ObjectFactory}. */
   protected ObjectFactory initObjectFactory() { return null; }

   /** Allows subclasses to initialize a non-default TypeConverterFactory. */
   protected TypeConverterFactory initTypeConverterFactory() { return null; }

   /** Allows subclasses to initialize a non-default {@link ValidationMetadataProvider}. */
   protected ValidationMetadataProvider initValidationMetadataProvider() { return null; }

   /**
    * Merges the two {@link Map}s of {@link LifecycleStage} to {@link Collection} of
    * {@link Interceptor}. A simple {@link Map#putAll(Map)} does not work because it overwrites
    * the collections in the map instead of adding to them.
    */
   protected void mergeInterceptorMaps( Map<LifecycleStage, Collection<Interceptor>> dst, Map<LifecycleStage, Collection<Interceptor>> src ) {
      for ( Map.Entry<LifecycleStage, Collection<Interceptor>> entry : src.entrySet() ) {
         Collection<Interceptor> collection = dst.computeIfAbsent(entry.getKey(), k -> new LinkedList<>());
         collection.addAll(entry.getValue());
      }
   }
}
