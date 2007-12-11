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

import net.sourceforge.stripes.controller.ActionBeanPropertyBinder;
import net.sourceforge.stripes.controller.ActionResolver;
import net.sourceforge.stripes.controller.ActionBeanContextFactory;
import net.sourceforge.stripes.localization.LocalizationBundleFactory;
import net.sourceforge.stripes.localization.LocalePicker;
import net.sourceforge.stripes.validation.TypeConverterFactory;
import net.sourceforge.stripes.validation.ValidationMetadataProvider;
import net.sourceforge.stripes.tag.TagErrorRendererFactory;
import net.sourceforge.stripes.tag.PopulationStrategy;
import net.sourceforge.stripes.format.FormatterFactory;
import net.sourceforge.stripes.controller.Interceptor;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.controller.multipart.MultipartWrapperFactory;
import net.sourceforge.stripes.exception.ExceptionHandler;

import javax.servlet.ServletContext;
import java.util.Collection;

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
 * throw runtime exceptions with plenty of details about the failiure and its suspected cause(s).</p>
 *
 * @author Tim Fennell
 */
public interface Configuration {
    /**
     * Supplies the Configuration with a BootstrapPropertyResolver. This method is guaranteed to
     * be invoked prior to the init method.
     * 
     * @param resolver a BootStrapPropertyResolver which can be used to find any values required
     *        by the Configuration in order to initialize
     */
    void setBootstrapPropertyResolver(BootstrapPropertyResolver resolver);

    /**
     * Called by the DispatcherServlet to initialize the Configuration. Any operations which may
     * fail and cause the Configuration to be inaccessible should be performed here (e.g.
     * opening a configuration file and reading the contents).
     */
    void init();

    /**
     * Implementations should implement this method to simply return a reference to the
     * BootstrapPropertyResolver passed to the Configuration at initialization time.
     *
     * @return BootstrapPropertyResolver the instance passed to the init() method
     */
    BootstrapPropertyResolver getBootstrapPropertyResolver();

    /**
     * Retrieves the ServletContext for the context within which the Stripes application
     * is executing.
     *
     * @return the ServletContext in which the application is running
     */
    ServletContext getServletContext();

    /** Enable or disable debug mode. */
    void setDebugMode(boolean debugMode);

    /** Returns true if the Stripes application is running in debug mode. */
    boolean isDebugMode();

    /**
     * Returns an instance of ActionResolver that will be used by Stripes to lookup and resolve
     * ActionBeans.  The instance should be cached by the Configuration since multiple entities
     * in the system may access the ActionResolver throughout the lifetime of the application.
     *
     * @return the Class representing the configured ActionResolver
     */
    ActionResolver getActionResolver();

    /**
     * Returns an instance of ACtionBeanPropertyBinder that is responsible for binding all
     * properties to all ActionBeans at runtime.  The instance should be cached by the Configuration
     * since multiple entities in the system may access the ActionBeanPropertyBinder throughout the
     * lifetime of the application.
     *
     * @return ActionBeanPropertyBinder the property binder to be used by Stripes
     */
    ActionBeanPropertyBinder getActionBeanPropertyBinder();

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
     * Returns an instance of LocalizationBundleFactory that is responsible for looking up
     * resource bundles for the varying localization needs ot a web application. The instance should
     * be cached by the Configuration since multiple entities in the system may access the
     * LocalizationBundleFactory throughout the lifetime of the application.
     *
     * @return LocalizationBundleFactory an instance of a LocalizationBundleFactory implementation
     */
    LocalizationBundleFactory getLocalizationBundleFactory();

    /**
     * Returns an instance of LocalePicker that is responsible for choosing the Locale for
     * each request that enters the system.
     *
     * @return LocalePicker an instance of a LocalePicker implementation
     */
    LocalePicker getLocalePicker();

    /**
     * Returns an instance of FormatterFactory that is responsible for creating Formatter objects
     * for converting rich types into Strings for display on pages.
     *
     * @return LocalePicker an instance of a LocalePicker implementation
     */
    FormatterFactory getFormatterFactory();

    /**
     * Returns an instance of a tag error renderer factory for building custom error renderers
     * for form input tags that have field errors.
     *
     * @return TagErrorRendererFactory an instance of TagErrorRendererFactory
     */
    TagErrorRendererFactory getTagErrorRendererFactory();

    /**
     * Returns an instance of a PopulationStrategy that determines from where a tag's value
     * should be repopulated.
     *
     * @return PopulationStrategy an instance of PopulationStrategy
     */
    PopulationStrategy getPopulationStrategy();

    /**
     * Returns an instance of an action bean context factory which will used throughout Stripes
     * to manufacture ActionBeanContext objects. This allows projects to extend ActionBeanContext
     * and provide additional type safe methods for accessing contextual information cleanly.
     *
     * @return ActionBeanContextFactory an instance of ActionBeanContextFactory
     */
    ActionBeanContextFactory getActionBeanContextFactory();

    /**
     * Fetches the interceptors that should be executed around the lifecycle stage applied.
     * Must return a non-null collection, but the collection may be empty. The Interceptors
     * are invoked around the code which executes the given lifecycle function (e.g.
     * ActionBeanResolution), and as a result can execute code both before and after it.
     *
     * @return Collection<Interceptor> an ordered collection of interceptors to be executed
     *         around the given lifecycle stage.
     */
    Collection<Interceptor> getInterceptors(LifecycleStage stage);

    /**
     * Returns an instance of ExceptionHandler that can be used by Stripes to handle any
     * exceptions that arise as the result of processing a request.
     *
     * @return ExceptionHandler an instance of ExceptionHandler
     */
    ExceptionHandler getExceptionHandler();

    /**
     * Returns an instance of MultipartWrapperFactory that can be used by Stripes to construct
     * MultipartWrapper instances for dealing with multipart requests (those containing file
     * uploads).
     *
     * @return MultipartWrapperFactory an instance of the wrapper factory
     */
    MultipartWrapperFactory getMultipartWrapperFactory();

    /**
     * Returns an instance of {@link ValidationMetadataProvider} that can be used by Stripes to
     * determine what validations need to be applied during
     * {@link LifecycleStage#BindingAndValidation}.
     * 
     * @return an instance of {@link ValidationMetadataProvider}
     */
    ValidationMetadataProvider getValidationMetadataProvider();
}
