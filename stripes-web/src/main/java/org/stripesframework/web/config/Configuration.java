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

import java.util.Collection;

import javax.servlet.ServletContext;

import org.stripesframework.web.controller.ActionBeanContextFactory;
import org.stripesframework.web.controller.ActionBeanPropertyBinder;
import org.stripesframework.web.controller.ActionResolver;
import org.stripesframework.web.controller.Interceptor;
import org.stripesframework.web.controller.LifecycleStage;
import org.stripesframework.web.controller.ObjectFactory;
import org.stripesframework.web.controller.multipart.MultipartWrapperFactory;
import org.stripesframework.web.exception.ExceptionHandler;
import org.stripesframework.web.exception.StripesRuntimeException;
import org.stripesframework.web.format.FormatterFactory;
import org.stripesframework.web.localization.LocalePicker;
import org.stripesframework.web.localization.LocalizationBundleFactory;
import org.stripesframework.web.validation.TypeConverterFactory;
import org.stripesframework.web.validation.ValidationMetadataProvider;


/**
 * <p>Type safe interface for accessing configuration information used to configure Stripes. All
 * Configuration implementations are handed a reference to the BootstrapPropertyResolver to
 * enable them to find initial values and fully initialize themselves.  Through the
 * BootstrapPropertyResolver implementations also get access to the ServletConfig of the
 * DispatcherServlet which can be used for locating configuration values if desired.</p>
 *
 * <p>Implementations of Configuration should fail fast.  At initialization time they should
 * detect as many failures as possible and raise an exception.  Since exceptions in Configuration
 * are considered fatal there are no exception specifications and implementations are expected to
 * throw runtime exceptions with plenty of details about the failure and its suspected cause(s).</p>
 *
 * @author Tim Fennell
 */
public interface Configuration {

   /**
    * Returns an instance of an action bean context factory which will used throughout Stripes
    * to manufacture ActionBeanContext objects. This allows projects to extend ActionBeanContext
    * and provide additional type safe methods for accessing contextual information cleanly.
    *
    * @return ActionBeanContextFactory an instance of ActionBeanContextFactory
    */
   ActionBeanContextFactory getActionBeanContextFactory();

   /**
    * Returns an instance of ActionBeanPropertyBinder that is responsible for binding all
    * properties to all ActionBeans at runtime.  The instance should be cached by the Configuration
    * since multiple entities in the system may access the ActionBeanPropertyBinder throughout the
    * lifetime of the application.
    *
    * @return ActionBeanPropertyBinder the property binder to be used by Stripes
    */
   ActionBeanPropertyBinder getActionBeanPropertyBinder();

   /**
    * Returns an instance of ActionResolver that will be used by Stripes to lookup and resolve
    * ActionBeans.  The instance should be cached by the Configuration since multiple entities
    * in the system may access the ActionResolver throughout the lifetime of the application.
    *
    * @return the Class representing the configured ActionResolver
    */
   ActionResolver getActionResolver();

   /**
    * Implementations should implement this method to simply return a reference to the
    * BootstrapPropertyResolver passed to the Configuration at initialization time.
    *
    * @return BootstrapPropertyResolver the instance passed to the init() method
    */
   BootstrapPropertyResolver getBootstrapPropertyResolver();

   /**
    * Returns an instance of ExceptionHandler that can be used by Stripes to handle any
    * exceptions that arise as the result of processing a request.
    *
    * @return ExceptionHandler an instance of ExceptionHandler
    */
   ExceptionHandler getExceptionHandler();

   /**
    * Returns an instance of FormatterFactory that is responsible for creating Formatter objects
    * for converting rich types into Strings for display on pages.
    *
    * @return LocalePicker an instance of a LocalePicker implementation
    */
   FormatterFactory getFormatterFactory();

   /**
    * Fetches the interceptors that should be executed around the lifecycle stage applied.
    * Must return a non-null collection, but the collection may be empty. The Interceptors
    * are invoked around the code which executes the given lifecycle function (e.g.
    * ActionBeanResolution), and as a result can execute code both before and after it.
    *
    * @return Collection<Interceptor> an ordered collection of interceptors to be executed
    *         around the given lifecycle stage.
    */
   Collection<Interceptor> getInterceptors( LifecycleStage stage );

   /**
    * Returns an instance of LocalePicker that is responsible for choosing the Locale for
    * each request that enters the system.
    *
    * @return LocalePicker an instance of a LocalePicker implementation
    */
   LocalePicker getLocalePicker();

   /**
    * Returns an instance of LocalizationBundleFactory that is responsible for looking up
    * resource bundles for the varying localization needs of a web application. The instance should
    * be cached by the Configuration since multiple entities in the system may access the
    * LocalizationBundleFactory throughout the lifetime of the application.
    *
    * @return LocalizationBundleFactory an instance of a LocalizationBundleFactory implementation
    */
   LocalizationBundleFactory getLocalizationBundleFactory();

   /**
    * Returns an instance of MultipartWrapperFactory that can be used by Stripes to construct
    * MultipartWrapper instances for dealing with multipart requests (those containing file
    * uploads).
    *
    * @return MultipartWrapperFactory an instance of the wrapper factory
    */
   MultipartWrapperFactory getMultipartWrapperFactory();

   /**
    * Returns an instance of {@link ObjectFactory} that is used throughout Stripes to instantiate
    * classes.
    *
    * @return an instance of {@link ObjectFactory}.
    */
   ObjectFactory getObjectFactory();

   /**
    * Retrieves the ServletContext for the context within which the Stripes application
    * is executing.
    *
    * @return the ServletContext in which the application is running
    */
   ServletContext getServletContext();

   /**
    * Returns an instance of TypeConverterFactory that is responsible for providing lookups and
    * instances of TypeConverters for the validation system.  The instance should be cached by the
    * Configuration since multiple entities in the system may access the TypeConverterFactory
    * throughout the lifetime of the application.
    *
    * @return TypeConverterFactory an instance of a TypeConverterFactory implementation
    */
   TypeConverterFactory getTypeConverterFactory();

   /**
    * Returns an instance of {@link ValidationMetadataProvider} that can be used by Stripes to
    * determine what validations need to be applied during
    * {@link LifecycleStage#BindingAndValidation}.
    *
    * @return an instance of {@link ValidationMetadataProvider}
    */
   ValidationMetadataProvider getValidationMetadataProvider();

   /**
    * Called by the DispatcherServlet to initialize the Configuration. Any operations which may
    * fail and cause the Configuration to be inaccessible should be performed here (e.g.
    * opening a configuration file and reading the contents).
    */
   void init();

   /**
    * Utility method that is used to implement the main pattern of this class: lookup the
    * name of a class based on a property name, instantiate the named class and initialize it.
    *
    * @param componentType a Class object representing a subclass of ConfigurableComponent
    * @param propertyName the name of the property to look up for the class name
    * @return an instance of the component, or null if one was not configured.
    */
   @SuppressWarnings({ "unchecked", "rawtypes" })
   default <T extends ConfigurableComponent> T initializeComponent( Class<T> componentType, String propertyName ) {
      Class clazz = getBootstrapPropertyResolver().getClassProperty(propertyName, componentType);
      if ( clazz != null ) {
         try {
            T component;

            ObjectFactory objectFactory = getObjectFactory();
            if ( objectFactory != null ) {
               component = objectFactory.newInstance((Class<T>)clazz);
            } else {
               component = (T)clazz.getConstructor().newInstance();
            }

            component.init(this);
            return component;
         }
         catch ( Exception e ) {
            throw new StripesRuntimeException(
                  "Could not instantiate configured " + componentType.getSimpleName() + " of type [" + clazz.getSimpleName() + "]. Please check "
                        + "the configuration parameters specified in your web.xml.", e);

         }
      } else {
         return null;
      }
   }

   /** Returns true if the Stripes application is running in debug mode. */
   boolean isDebugMode();

   /**
    * Supplies the Configuration with a BootstrapPropertyResolver. This method is guaranteed to
    * be invoked prior to the init method.
    *
    * @param resolver a BootStrapPropertyResolver which can be used to find any values required
    *        by the Configuration in order to initialize
    */
   void setBootstrapPropertyResolver( BootstrapPropertyResolver resolver );

   /** Enable or disable debug mode. */
   void setDebugMode( boolean debugMode );
}
