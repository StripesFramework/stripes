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

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.stripesframework.web.controller.ActionBeanContextFactory;
import org.stripesframework.web.controller.ActionBeanPropertyBinder;
import org.stripesframework.web.controller.ActionResolver;
import org.stripesframework.web.controller.Interceptor;
import org.stripesframework.web.controller.LifecycleStage;
import org.stripesframework.web.controller.ObjectFactory;
import org.stripesframework.web.controller.multipart.MultipartWrapperFactory;
import org.stripesframework.web.exception.ExceptionHandler;
import org.stripesframework.web.exception.StripesRuntimeException;
import org.stripesframework.web.format.Formatter;
import org.stripesframework.web.format.FormatterFactory;
import org.stripesframework.web.localization.LocalePicker;
import org.stripesframework.web.localization.LocalizationBundleFactory;
import org.stripesframework.web.util.Log;
import org.stripesframework.web.util.ReflectUtil;
import org.stripesframework.web.validation.TypeConverter;
import org.stripesframework.web.validation.TypeConverterFactory;
import org.stripesframework.web.validation.ValidationMetadataProvider;


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

   /** The Configuration Key for looking up the name of the ObjectFactory class */
   public static final String OBJECT_FACTORY = "ObjectFactory.Class";

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

   /**
    * Calls super.init() then adds Formatters and TypeConverters found in
    * packages listed in {@link BootstrapPropertyResolver#PACKAGES} to their respective factories.
    */
   @SuppressWarnings("unchecked")
   @Override
   public void init() {
      super.init();

      List<Class<? extends Formatter>> formatters = getBootstrapPropertyResolver().getClassPropertyList(Formatter.class);
      for ( Class<? extends Formatter> formatter : formatters ) {
         Type[] typeArguments = ReflectUtil.getActualTypeArguments(formatter, Formatter.class);
         log.trace("Found Formatter [", formatter, "] - type parameters: ", typeArguments);
         if ( (typeArguments != null) && (typeArguments.length == 1) && !typeArguments[0].equals(Object.class) ) {
            if ( typeArguments[0] instanceof Class ) {
               log.debug("Adding auto-discovered Formatter [", formatter, "] for [", typeArguments[0], "] (from type parameter)");
               getFormatterFactory().add((Class<?>)typeArguments[0], (Class<? extends Formatter<?>>)formatter);
            } else {
               log.warn("Type parameter for non-abstract Formatter [", formatter, "] is not a class.");
            }
         }

         TargetTypes targetTypes = formatter.getAnnotation(TargetTypes.class);
         if ( targetTypes != null ) {
            for ( Class<?> targetType : targetTypes.value() ) {
               log.debug("Adding auto-discovered Formatter [", formatter, "] for [", targetType, "] (from TargetTypes annotation)");
               getFormatterFactory().add(targetType, (Class<? extends Formatter<?>>)formatter);
            }
         }
      }

      List<Class<? extends TypeConverter>> typeConverters = getBootstrapPropertyResolver().getClassPropertyList(TypeConverter.class);
      for ( Class<? extends TypeConverter> typeConverter : typeConverters ) {
         Type[] typeArguments = ReflectUtil.getActualTypeArguments(typeConverter, TypeConverter.class);
         log.trace("Found TypeConverter [", typeConverter, "] - type parameters: ", typeArguments);
         if ( (typeArguments != null) && (typeArguments.length == 1) && !typeArguments[0].equals(Object.class) ) {
            if ( typeArguments[0] instanceof Class ) {
               log.debug("Adding auto-discovered TypeConverter [", typeConverter, "] for [", typeArguments[0], "] (from type parameter)");
               getTypeConverterFactory().add((Class<?>)typeArguments[0], (Class<? extends TypeConverter<?>>)typeConverter);
            } else {
               log.warn("Type parameter for non-abstract TypeConverter [", typeConverter, "] is not a class.");
            }
         }

         TargetTypes targetTypes = typeConverter.getAnnotation(TargetTypes.class);
         if ( targetTypes != null ) {
            for ( Class<?> targetType : targetTypes.value() ) {
               log.debug("Adding auto-discovered TypeConverter [", typeConverter, "] for [", targetType, "] (from TargetTypes annotation)");
               getTypeConverterFactory().add(targetType, (Class<? extends TypeConverter<?>>)typeConverter);
            }
         }
      }
   }

   /** Looks for a class name in config and uses that to create the component. */
   @Override
   protected ActionBeanContextFactory initActionBeanContextFactory() {
      return initializeComponent(ActionBeanContextFactory.class, ACTION_BEAN_CONTEXT_FACTORY);
   }

   /** Looks for a class name in config and uses that to create the component. */
   @Override
   protected ActionBeanPropertyBinder initActionBeanPropertyBinder() {
      return initializeComponent(ActionBeanPropertyBinder.class, ACTION_BEAN_PROPERTY_BINDER);
   }

   /** Looks for a class name in config and uses that to create the component. */
   @Override
   protected ActionResolver initActionResolver() {
      return initializeComponent(ActionResolver.class, ACTION_RESOLVER);
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
      List<Class<?>> coreInterceptorClasses = getBootstrapPropertyResolver().getClassPropertyList(CORE_INTERCEPTOR_LIST);
      if ( coreInterceptorClasses.size() == 0 ) {
         return super.initCoreInterceptors();
      } else {
         return initInterceptors(coreInterceptorClasses);
      }
   }

   /** Looks for a true/false value in config. */
   @Override
   protected Boolean initDebugMode() {
      try {
         return Boolean.valueOf(getBootstrapPropertyResolver().getProperty(DEBUG_MODE).toLowerCase());
      }
      catch ( Exception e ) {
         return null;
      }
   }

   /** Looks for a class name in config and uses that to create the component. */
   @Override
   protected ExceptionHandler initExceptionHandler() {
      return initializeComponent(ExceptionHandler.class, EXCEPTION_HANDLER);
   }

   /** Looks for a class name in config and uses that to create the component. */
   @Override
   protected FormatterFactory initFormatterFactory() {
      return initializeComponent(FormatterFactory.class, FORMATTER_FACTORY);
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
   protected Map<LifecycleStage, Collection<Interceptor>> initInterceptors( List classes ) {

      Map<LifecycleStage, Collection<Interceptor>> map = new HashMap<>();

      for ( Object type : classes ) {
         try {
            Interceptor interceptor = getObjectFactory().newInstance((Class<? extends Interceptor>)type);
            addInterceptor(map, interceptor);
         }
         catch ( Exception e ) {
            throw new StripesRuntimeException("Could not instantiate configured Interceptor [" + type.getClass().getName() + "].", e);
         }
      }

      return map;
   }

   /** Looks for a class name in config and uses that to create the component. */
   @Override
   protected LocalePicker initLocalePicker() {
      return initializeComponent(LocalePicker.class, LOCALE_PICKER);
   }

   /** Looks for a class name in config and uses that to create the component. */
   @Override
   protected LocalizationBundleFactory initLocalizationBundleFactory() {
      return initializeComponent(LocalizationBundleFactory.class, LOCALIZATION_BUNDLE_FACTORY);
   }

   /** Looks for a class name in config and uses that to create the component. */
   @Override
   protected MultipartWrapperFactory initMultipartWrapperFactory() {
      return initializeComponent(MultipartWrapperFactory.class, MULTIPART_WRAPPER_FACTORY);
   }

   /** Looks for a class name in config and uses that to create the component. */
   @Override
   protected ObjectFactory initObjectFactory() {
      return initializeComponent(ObjectFactory.class, OBJECT_FACTORY);
   }

   /** Looks for a class name in config and uses that to create the component. */
   @Override
   protected TypeConverterFactory initTypeConverterFactory() {
      return initializeComponent(TypeConverterFactory.class, TYPE_CONVERTER_FACTORY);
   }

   /** Looks for a class name in config and uses that to create the component. */
   @Override
   protected ValidationMetadataProvider initValidationMetadataProvider() {
      return initializeComponent(ValidationMetadataProvider.class, VALIDATION_METADATA_PROVIDER);
   }
}
