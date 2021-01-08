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
package net.sourceforge.stripes.tag;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTag;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.controller.StripesConstants;
import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.exception.StripesJspException;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.validation.ValidationError;
import net.sourceforge.stripes.validation.ValidationErrors;


/**
 * <p>The errors tag has two modes, one where it displays all validation errors in a list
 * and a second mode when there is a single enclosed field-error tag that has no name attribute
 * in which case this tag iterates over the body, displaying each error in turn in place
 * of the field-error tag.</p>
 *
 * <p>In the first mode, where the default output is used, it is possible to change the output
 * for the entire application using a set of resources in the error messages bundle
 * (StripesResources.properties unless you have configured another).  If the properties are
 * undefined, the tag will output the text "Validation Errors" in a div with css class errorHeader,
 * then output an unordered list of error messages.  The following four resource strings
 * (shown with their default values) can be modified to create different default output:</p>
 *
 * <ul>
 *   <li>stripes.errors.header={@literal <div class="errorHeader">Validation Errors</div><ul>}</li>
 *   <li>stripes.errors.footer={@literal </ul>}</li>
 *   <li>stripes.errors.beforeError={@literal <li>}</li>
 *   <li>stripes.errors.afterError={@literal </li>}</li>
 * </ul>
 *
 * <p>The errors tag can also be used to display errors for a single field by supplying it
 * with a 'field' attribute which matches the name of a field on the page. In this case the tag
 * will display only if errors exist for the named field.  In this mode the tag will first look for
 * resources named:</p>
 *
 * <ul>
 *   <li>stripes.fieldErrors.header</li>
 *   <li>stripes.fieldErrors.footer</li>
 *   <li>stripes.fieldErrors.beforeError</li>
 *   <li>stripes.fieldErrors.afterError</li>
 * </ul>
 *
 * <p>If the {@code fieldErrors} resources cannot be found, the tag will default to using the
 * same resources and defaults as when displaying for all fields.</p>
 *
 * <p>Similar to the above, field specific, manner of display the errors tag can also be used
 * to output only errors not associated with a field, i.e. global errors.  This is done by setting
 * the {@code globalErrorsOnly} attribute to true.</p>
 *
 * <p>This tag has several ways of being attached to the errors of a specific action request.
 * If the tag is inside a form tag, it will display only errors that are associated
 * with that form. If supplied with an 'action' attribute, it will display errors only errors
 * associated with a request to that URL. Finally, if neither is the case, it
 * will always display as described in the paragraph above.</p>
 *
 * @author Greg Hinkle, Tim Fennell
 */
public class ErrorsTag extends HtmlTagSupport implements BodyTag {

   private static final Log log = Log.getInstance(ErrorsTag.class);

   /** The header that will be emitted if no header is defined in the resource bundle. */
   public static final String DEFAULT_HEADER = "<div class=\"errorHeader\">Validation Errors</div><ul>";

   /** The footer that will be emitted if no footer is defined in the resource bundle. */
   public static final String DEFAULT_FOOTER = "</ul>";

   /**
    * True if this tag will display errors, otherwise false. This is determined by the logic
    * laid out in the class level Javadoc around whether this errors tag is for the action
    * that was submitted in the request.
    */
   private boolean _display = false;

   /**
    * True if this tag contains a field-error child tag, which controls
    * the place of output of each error
    */
   private boolean _nestedErrorTagPresent = false;

   /** Sets the form action for which errors should be displayed. */
   private String _action;

   /** An optional attribute that declares a particular field to output errors for. */
   private String _field;

   /** An optional attribute that specified to display only the global errors. */
   private boolean _globalErrorsOnly;

   /** The collection of errors that match the filtering conditions */
   private SortedSet<ValidationError> _allErrors;

   /** An iterator of the list of matched errors */
   private Iterator<ValidationError> _errorIterator;

   /** The error displayed in the current iteration */
   private ValidationError _currentError;

   /** An index of the error being displayed - zero based */
   private int _index = 0;

   /**
    * Manages iteration, running again if there are more errors to display.  If there is no
    * nested FieldError tag, will ensure that the body is evaluated only once.
    *
    * @return EVAL_BODY_TAG if there are more errors to display, SKIP_BODY otherwise
    */
   @Override
   public int doAfterBody() throws JspException {
      if ( _display && _nestedErrorTagPresent && _errorIterator.hasNext() ) {
         _currentError = _errorIterator.next();
         _index++;

         // Reapply TEI attributes
         getPageContext().setAttribute("index", _index);
         getPageContext().setAttribute("error", _currentError);
         return EVAL_BODY_BUFFERED;
      } else {
         return SKIP_BODY;
      }
   }

   /**
    * Output the error list if this was an empty body tag and we're fully controlling output*
    *
    * @return EVAL_PAGE always
    * @throws JspException
    */
   @Override
   public int doEndTag() throws JspException {
      try {
         JspWriter writer = getPageContext().getOut();

         if ( _display && !_nestedErrorTagPresent ) {
            // Output all errors in a standard format
            Locale locale = getPageContext().getRequest().getLocale();
            ResourceBundle bundle = null;

            try {
               bundle = StripesFilter.getConfiguration().getLocalizationBundleFactory().getErrorMessageBundle(locale);
            }
            catch ( MissingResourceException mre ) {
               log.warn("The errors tag could not find the error messages resource bundle. ",
                     "As a result default headers/footers etc. will be used. Check that ", "you have a StripesResources.properties in your classpath (unless ",
                     "of course you have configured a different bundle).");
            }

            // Fetch the header and footer
            String header = getResource(bundle, "header", DEFAULT_HEADER);
            String footer = getResource(bundle, "footer", DEFAULT_FOOTER);
            String openElement = getResource(bundle, "beforeError", "<li>");
            String closeElement = getResource(bundle, "afterError", "</li>");

            // Write out the error messages
            writer.write(header);

            for ( ValidationError fieldError : _allErrors ) {
               String message = fieldError.getMessage(locale);
               if ( message != null && message.length() > 0 ) {
                  writer.write(openElement);
                  writer.write(message);
                  writer.write(closeElement);
               }
            }

            writer.write(footer);
         } else if ( _display && _nestedErrorTagPresent ) {
            // Output the collective body content
            getBodyContent().writeOut(writer);
         }

         // Reset the instance state in case the container decides to pool the tag
         _display = false;
         _nestedErrorTagPresent = false;
         _allErrors = null;
         _errorIterator = null;
         _currentError = null;
         _index = 0;

         return EVAL_PAGE;
      }
      catch ( IOException e ) {
         JspException jspe = new JspException("IOException encountered while writing errors " + "tag to the JspWriter.", e);
         log.warn(jspe);
         throw jspe;
      }
   }

   /** Sets the context variables for the current error and index */
   @Override
   public void doInitBody() throws JspException {
      // Apply TEI attributes
      getPageContext().setAttribute("index", _index);
      getPageContext().setAttribute("error", _currentError);
   }

   /**
    * Determines if the tag should display errors based on the action that it is displaying for,
    * and then fetches the appropriate list of errors and makes sure it is non-empty.
    *
    * @return SKIP_BODY if the errors are not to be output, or there aren't any<br/>
    *         EVAL_BODY_TAG if there are errors to display
    */
   @Override
   public int doStartTag() throws JspException {
      HttpServletRequest request = (HttpServletRequest)getPageContext().getRequest();
      ActionBean mainBean = (ActionBean)request.getAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN);
      FormTag formTag = getParentTag(FormTag.class);
      ValidationErrors errors = null;

      // If we are supplied with an 'action' attribute then display the errors
      // only if that action matches the 'action' of the current action bean
      if ( getAction() != null ) {
         if ( mainBean != null ) {
            String mainAction = StripesFilter.getConfiguration().getActionResolver().getUrlBinding(mainBean.getClass());

            if ( getAction().equals(mainAction) ) {
               errors = mainBean.getContext().getValidationErrors();
            }
         }
      }
      // Else we don't have an 'action' attribute, so see if we are nested in
      // a form tag
      else if ( formTag != null ) {
         ActionBean formBean = formTag.getActionBean();
         if ( formBean != null ) {
            errors = formBean.getContext().getValidationErrors();
         }

      }
      // Else if no name was set, and we're not in a action tag, we're global and ok to display
      else if ( mainBean != null ) {
         errors = mainBean.getContext().getValidationErrors();
      }

      // If we found some errors that are applicable for display, figure out what to do
      if ( errors != null ) {
         // Using a set ensures that duplicate messages get filtered out, which can
         // happen during multi-row validation
         _allErrors = new TreeSet<>(new ErrorComparator());

         if ( _field != null ) {
            // we're filtering for a specific field
            List<ValidationError> fieldErrors = errors.get(_field);
            if ( fieldErrors != null ) {
               _allErrors.addAll(fieldErrors);
            }
         } else if ( _globalErrorsOnly ) {
            List<ValidationError> globalErrors = errors.get(ValidationErrors.GLOBAL_ERROR);
            if ( globalErrors != null ) {
               _allErrors.addAll(globalErrors);
            }
         } else {
            for ( List<ValidationError> fieldErrors : errors.values() ) {
               if ( fieldErrors != null ) {
                  _allErrors.addAll(fieldErrors);
               }
            }
         }
      }

      // Make sure that after all this we really do have some errors
      if ( _allErrors != null && _allErrors.size() > 0 ) {
         _display = true;
         _errorIterator = _allErrors.iterator();
         _currentError = _errorIterator.next(); // load up the first error
         return EVAL_BODY_BUFFERED;
      } else {
         _display = false;
         return SKIP_BODY;

      }
   }

   /** Returns the value set with setAction(). */
   public String getAction() {
      return _action;
   }

   /**
    * Called by the IndividualErrorTag to fetch the current error from the set being iterated.
    *
    * @return The error displayed for this iteration of the errors tag
    */
   public ValidationError getCurrentError() {
      _nestedErrorTagPresent = true;
      return _currentError;
   }

   /** Gets the value set with setField(). */
   public String getField() {
      return _field;
   }

   /** Returns true if the error displayed is the first matching error. */
   public boolean isFirst() {
      return (_allErrors.first() == _currentError);
   }

   /** Indicated whether the tag is displaying only global errors. */
   public boolean isGlobalErrorsOnly() { return _globalErrorsOnly; }

   /** Returns true if the error displayed is the last matching error. */
   public boolean isLast() {
      return (_allErrors.last() == _currentError);
   }

   /** Sets the (optional) action of the form to display errors for, if they exist. */
   public void setAction( String action ) {
       _action = action;
   }

   /**
    * Sets the action attribute by figuring out what ActionBean class is identified
    * and then in turn finding out the appropriate URL for the ActionBean.
    *
    * @param beanclass the FQN of an ActionBean class, or a Class object for one.
    */
   public void setBeanclass( Object beanclass ) throws StripesJspException {
      String url = getActionBeanUrl(beanclass);
      if ( url == null ) {
         throw new StripesJspException(
               "The 'beanclass' attribute provided could not be " + "used to identify a valid and configured ActionBean. The value supplied was: " + beanclass);
      } else {
         _action = url;
      }
   }

   /** Sets the (optional) name of a field to display errors for, if errors exist. */
   public void setField( String field ) {
       _field = field;
   }

   /** Tells the tag to display (or not) only global errors and no field level errors. */
   public void setGlobalErrorsOnly( boolean globalErrorsOnly ) {
       _globalErrorsOnly = globalErrorsOnly;
   }

   /**
    * Utility method that is used to lookup the resources used for the errors header,
    * footer, and the strings that go before and after each error.
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

      String resource = null;
      if ( _field != null ) {
         try {
            resource = bundle.getString("stripes.fieldErrors." + name);
         }
         catch ( MissingResourceException mre ) { /* Do nothing */ }
      }

      if ( resource == null ) {
         try {
            resource = bundle.getString("stripes.errors." + name);
         }
         catch ( MissingResourceException mre ) {
            resource = fallback;
         }
      }

      return resource;
   }

   /**
    * Inner class Comparator used to provide a consistent ordering of validation errors.
    * Sorting is done by field name (the programmatic one, not the user visible one). Errors
    * without field names sort to the top since it is assumed that these are global errors
    * as oppose to field specific ones.
    */
   private static class ErrorComparator implements Comparator<ValidationError> {

      @Override
      public int compare( ValidationError e1, ValidationError e2 ) {
         // Identical errors should be suppressed
         if ( e1.equals(e2) ) {
            return 0;
         }

         String fn1 = e1.getFieldName();
         String fn2 = e2.getFieldName();
         boolean e1Global = fn1 == null || fn1.equals(ValidationErrors.GLOBAL_ERROR);
         boolean e2Global = fn2 == null || fn2.equals(ValidationErrors.GLOBAL_ERROR);

         // Sort globals above non-global errors
         if ( e1Global && !e2Global ) {
            return -1;
         }
         if ( e2Global && !e1Global ) {
            return 1;
         }
         if ( fn1 == null && fn2 == null ) {
            return 0;
         }

         // Then sort by field name, and if field names match make the first one come first
         int result = e1.getFieldName().compareTo(e2.getFieldName());
         if ( result == 0 ) {
            result = 1;
         }
         return result;
      }
   }
}
