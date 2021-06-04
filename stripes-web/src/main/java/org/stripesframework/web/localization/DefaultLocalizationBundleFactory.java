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
package org.stripesframework.web.localization;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.stripesframework.web.config.BootstrapPropertyResolver;
import org.stripesframework.web.config.Configuration;


/**
 * Very simple default implementation of a bundle factory.  Looks for configuration parameters in
 * the bootstrap properties called "LocalizationBundleFactory.ErrorMessageBundle" and
 * "LocalizationBundleFactory.FieldNameBundle".  If one or both of these values is not specified
 * the default bundle name of "StripesResources" will be used in its place.
 *
 * @see BootstrapPropertyResolver
 * @author Tim Fennell
 */
public class DefaultLocalizationBundleFactory implements LocalizationBundleFactory {

   /** The name of the default resource bundle for Stripes. */
   public static final String BUNDLE_NAME = "StripesResources";

   /** The configuration parameter for changing the default error message resource bundle. */
   public static final String ERROR_MESSAGE_BUNDLE = "LocalizationBundleFactory.ErrorMessageBundle";

   /** The configuration parameter for changing the default field name resource bundle. */
   public static final String FIELD_NAME_BUNDLE = "LocalizationBundleFactory.FieldNameBundle";

   /** Holds the configuration passed in at initialization time. */
   private Configuration _configuration;
   private String        _errorBundleName;
   private String        _fieldBundleName;

   /**
    * Looks for a bundle called StripesResources with the supplied locale if one is provided,
    * or with the default locale if the locale provided is null.
    *
    * @param locale an optional locale, may be null.
    * @return ResourceBundle a bundle in which to look for localized error messages
    * @throws MissingResourceException if a suitable bundle cannot be found
    */
   @Override
   public ResourceBundle getErrorMessageBundle( Locale locale ) throws MissingResourceException {
      try {
         if ( locale == null ) {
            return ResourceBundle.getBundle(_errorBundleName);
         } else {
            return ResourceBundle.getBundle(_errorBundleName, locale);
         }
      }
      catch ( MissingResourceException mre ) {
         MissingResourceException mre2 = new MissingResourceException(
               "Could not find the error message resource bundle needed by Stripes. This " + "almost certainly means that a properties file called '"
                     + _errorBundleName + ".properties' could not be found in the classpath. "
                     + "This properties file is needed to lookup validation error messages. Please "
                     + "ensure the file exists in WEB-INF/classes or elsewhere in your classpath.", _errorBundleName, "");
         mre2.setStackTrace(mre.getStackTrace());
         throw mre2;
      }
   }

   /**
    * Looks for a bundle called StripesResources with the supplied locale if one is provided,
    * or with the default locale if the locale provided is null.
    *
    * @param locale an optional locale, may be null.
    * @return ResourceBundle a bundle in which to look for localized field names
    * @throws MissingResourceException if a suitable bundle cannot be found
    */
   @Override
   public ResourceBundle getFormFieldBundle( Locale locale ) throws MissingResourceException {
      try {
         if ( locale == null ) {
            return ResourceBundle.getBundle(_fieldBundleName);
         } else {
            return ResourceBundle.getBundle(_fieldBundleName, locale);
         }
      }
      catch ( MissingResourceException mre ) {
         MissingResourceException mre2 = new MissingResourceException(
               "Could not find the form field resource bundle needed by Stripes. This " + "almost certainly means that a properties file called '"
                     + _fieldBundleName + ".properties' could not be found in the classpath. "
                     + "This properties file is needed to lookup form field names. Please "
                     + "ensure the file exists in WEB-INF/classes or elsewhere in your classpath.", _fieldBundleName, "");
         mre2.setStackTrace(mre.getStackTrace());
         throw mre2;
      }
   }

   /**
    * Uses the BootstrapPropertyResolver attached to the Configuration in order to look for
    * configured bundle names in the servlet init parameters etc.  If those can't be found then
    * the default bundle names are put in place.
    */
   @Override
   public void init( Configuration configuration ) throws Exception {
      setConfiguration(configuration);

      _errorBundleName = configuration.getBootstrapPropertyResolver().
            getProperty(ERROR_MESSAGE_BUNDLE);
      if ( _errorBundleName == null ) {
         _errorBundleName = BUNDLE_NAME;
      }

      _fieldBundleName = configuration.getBootstrapPropertyResolver().getProperty(FIELD_NAME_BUNDLE);
      if ( _fieldBundleName == null ) {
         _fieldBundleName = BUNDLE_NAME;
      }
   }

   protected Configuration getConfiguration() {
      return _configuration;
   }

   protected void setConfiguration( Configuration configuration ) {
      _configuration = configuration;
   }
}
