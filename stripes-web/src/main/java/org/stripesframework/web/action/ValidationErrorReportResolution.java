/* Copyright 2010 Ben Gunter
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
package org.stripesframework.web.action;

import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.stripesframework.web.controller.StripesFilter;
import org.stripesframework.web.exception.SourcePageNotFoundException;
import org.stripesframework.web.util.HtmlUtil;
import org.stripesframework.web.util.Log;
import org.stripesframework.web.validation.ValidationError;


/**
 * A resolution that streams a simple HTML response to the client detailing the validation errors
 * that apply for an {@link ActionBeanContext}.
 *
 * @author Ben Gunter
 * @since Stripes 1.5.5
 */
public class ValidationErrorReportResolution implements Resolution {

   /** The header that will be emitted if no header is defined in the resource bundle. */
   public static final String DEFAULT_HEADER = "<div class=\"errorHeader\">Validation Errors</div><ul>";

   /** The footer that will be emitted if no footer is defined in the resource bundle. */
   public static final String DEFAULT_FOOTER = "</ul>";

   private static final Log log = Log.getInstance(ValidationErrorReportResolution.class);

   private final ActionBeanContext _context;

   /** Construct a new instance to report validation errors in the specified context. */
   public ValidationErrorReportResolution( ActionBeanContext context ) {
      _context = context;
   }

   @Override
   public void execute( HttpServletRequest request, HttpServletResponse response ) throws Exception {
      // log an exception for the stack trace
      SourcePageNotFoundException exception = new SourcePageNotFoundException(getContext());
      log.error(exception);

      // start the HTML error report
      response.setContentType("text/html");
      PrintWriter writer = response.getWriter();
      writer.println("<div style=\"font-family: Arial, sans-serif; font-size: 10pt;\">");
      writer.println("<h1>Stripes validation error report</h1><p>");
      writer.println(HtmlUtil.encode(exception.getMessage()));
      writer.println("</p><h2>Validation errors</h2><p>");
      sendErrors(request, response);
      writer.println("</p></div>");
   }

   /** Get the action bean context on which the validation errors occurred. */
   public ActionBeanContext getContext() {
      return _context;
   }

   /**
    * Utility method that is used to lookup the resources used for the error header, footer, and
    * the strings that go before and after each error.
    *
    * @param bundle the bundle to look up the resource from
    * @param name the name of the resource to lookup (prefixes will be added)
    * @param fallback a value to return if no resource can be found
    * @return the value to use for the named resource
    */
   protected String getResource( ResourceBundle bundle, String name, String fallback ) {
      if ( bundle == null ) {
         return fallback;
      }

      String resource;
      try {
         resource = bundle.getString("stripes.errors." + name);
      }
      catch ( MissingResourceException mre ) {
         resource = fallback;
      }

      return resource;
   }

   /**
    * Called by {@link #execute(HttpServletRequest, HttpServletResponse)} to write the actual
    * validation errors to the client. The {@code header}, {@code footer}, {@code beforeError} and
    * {@code afterError} resources are used by this method.
    *
    * @param request The servlet request.
    * @param response The servlet response.
    */
   protected void sendErrors( HttpServletRequest request, HttpServletResponse response ) throws Exception {
      // Output all errors in a standard format
      Locale locale = request.getLocale();
      ResourceBundle bundle = null;

      try {
         bundle = StripesFilter.getConfiguration().getLocalizationBundleFactory().getErrorMessageBundle(locale);
      }
      catch ( MissingResourceException mre ) {
         log.warn(getClass().getName(), " could not find the error messages resource bundle. ",
               "As a result default headers/footers etc. will be used. Check that ", "you have a StripesResources.properties in your classpath (unless ",
               "of course you have configured a different bundle).");
      }

      // Fetch the header and footer
      String header = getResource(bundle, "header", DEFAULT_HEADER);
      String footer = getResource(bundle, "footer", DEFAULT_FOOTER);
      String openElement = getResource(bundle, "beforeError", "<li>");
      String closeElement = getResource(bundle, "afterError", "</li>");

      // Write out the error messages
      PrintWriter writer = response.getWriter();
      writer.write(header);

      for ( List<ValidationError> list : getContext().getValidationErrors().values() ) {
         for ( ValidationError fieldError : list ) {
            writer.write(openElement);
            writer.write(HtmlUtil.encode(fieldError.getMessage(locale)));
            writer.write(closeElement);
         }
      }

      writer.write(footer);
   }
}
