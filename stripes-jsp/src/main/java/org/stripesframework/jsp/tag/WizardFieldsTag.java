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
package org.stripesframework.jsp.tag;

import static org.stripesframework.web.controller.StripesConstants.URL_KEY_FIELDS_PRESENT;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TryCatchFinally;

import org.stripesframework.web.action.ActionBean;
import org.stripesframework.web.controller.ActionResolver;
import org.stripesframework.web.controller.StripesConstants;
import org.stripesframework.web.controller.StripesFilter;
import org.stripesframework.jsp.exception.StripesJspException;
import org.stripesframework.web.exception.StripesServletException;
import org.stripesframework.web.util.CryptoUtil;
import org.stripesframework.web.util.HtmlUtil;


/**
 * <p>Examines the request and include hidden fields for all parameters that have do
 * not have form fields in the current form. Will include multiple values for
 * parameters that have them.  Excludes 'special' parameters like the source
 * page parameter, and the parameter that conveyed the event name.</p>
 *
 * <p>Very useful for implementing basic wizard flow without relying on session
 * scoping of ActionBeans, and without having to name all the parameters that
 * should be carried forward in the form.</p>
 *
 * @author Tim Fennell
 */
public class WizardFieldsTag extends StripesTagSupport implements TryCatchFinally {

   private boolean _currentFormOnly = false;

   /** Rethrows the passed in throwable in all cases. */
   @Override
   public void doCatch( Throwable throwable ) throws Throwable { throw throwable; }

   /**
    * Performs the main work of the tag, as described in the class level javadoc.
    * @return EVAL_PAGE in all cases.
    */
   @Override
   public int doEndTag() throws JspException {
      // Get the current form.
      FormTag form = getParentTag(FormTag.class);

      // Get the action bean on this form
      ActionBean actionBean = form.getActionBean();

      // If current form only is not specified, go ahead, otherwise check that
      // the current form had an ActionBean attached - which indicates that the
      // last submit was to the same form/action as this form
      if ( !isCurrentFormOnly() || actionBean != null ) {
         writeWizardFields(form);
      }

      return EVAL_PAGE;
   }

   /**
    * Used to ensure that the input tag is always removed from the tag stack so that there is
    * never any confusion about tag-parent hierarchies.
    */
   @Override
   public void doFinally() {
      try {
         getTagStack().pop();
      }
      catch ( Throwable t ) {
         /* Suppress anything, because otherwise this might mask any causal exception. */
      }
   }

   /** Skips over the body because there shouldn't be one. */
   @Override
   public int doStartTag() throws JspException {
      getTagStack().push(this);
      return SKIP_BODY;
   }

   /** Gets whether the tag will output fields for the current form only, or in all cases. */
   public boolean isCurrentFormOnly() { return _currentFormOnly; }

   /**
    * Sets whether or not the parameters should be output only if the form matches the current
    * request.  Defaults to false.
    */
   public void setCurrentFormOnly( boolean currentFormOnly ) { _currentFormOnly = currentFormOnly; }

   /** Returns the list of parameters that should be excluded from the hidden tag. */
   protected Set<String> getExcludes( FormTag form ) {
      Set<String> excludes = new HashSet<>();
      excludes.addAll(form.getRegisteredFields());
      excludes.add(StripesConstants.URL_KEY_SOURCE_PAGE);
      excludes.add(StripesConstants.URL_KEY_FIELDS_PRESENT);
      excludes.add(StripesConstants.URL_KEY_EVENT_NAME);
      excludes.add(StripesConstants.URL_KEY_FLASH_SCOPE_ID);

      // Use the submitted action bean to eliminate any event related parameters
      ServletRequest request = getPageContext().getRequest();
      ActionBean submittedActionBean = (ActionBean)request.getAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN);

      if ( submittedActionBean != null ) {
         String eventName = submittedActionBean.getContext().getEventName();
         if ( eventName != null ) {
            excludes.add(eventName);
            excludes.add(eventName + ".x");
            excludes.add(eventName + ".y");
         }
      }
      return excludes;
   }

   /** Returns all the submitted parameters in the current or the former requests. */
   @SuppressWarnings("unchecked")
   protected Set<String> getParamNames() {
      // Combine actual parameter names with input names from the form, which might not be
      // represented by a real request parameter
      Set<String> paramNames = new HashSet<>();
      ServletRequest request = getPageContext().getRequest();
      paramNames.addAll(request.getParameterMap().keySet());
      String fieldsPresent = request.getParameter(URL_KEY_FIELDS_PRESENT);
      if ( fieldsPresent != null ) {
         paramNames.addAll(HtmlUtil.splitValues(CryptoUtil.decrypt(fieldsPresent)));
      }
      return paramNames;
   }

   /**
    * Returns true if {@code name} is the name of an event handled by {@link ActionBean}s of type
    * {@code beanType}.
    *
    * @param beanType An {@link ActionBean} class
    * @param name The name to look up
    */
   protected boolean isEventName( Class<? extends ActionBean> beanType, String name ) {
       if ( beanType == null || name == null ) {
           return false;
       }

      try {
         ActionResolver actionResolver = StripesFilter.getConfiguration().getActionResolver();
         return actionResolver.getHandler(beanType, name) != null;
      }
      catch ( StripesServletException e ) {
         // Ignore the exception and assume the name is not an event
         return false;
      }
   }

   /**
    * Write out a hidden field which contains parameters that should be sent along with the actual
    * form fields.
    */
   protected void writeWizardFields( FormTag form ) throws JspException, StripesJspException {
      // Set up a hidden tag to do the writing for us
      InputHiddenTag hidden = new InputHiddenTag();
      hidden.setPageContext(getPageContext());
      hidden.setParent(getParent());

      // Get the list of all parameters.
      Set<String> paramNames = getParamNames();
      // Figure out the list of parameters we should not include
      Set<String> excludes = getExcludes(form);

      // Loop through the request parameters and output the values
      Class<? extends ActionBean> actionBeanType = form.getActionBeanClass();
      for ( String name : paramNames ) {
         if ( !excludes.contains(name) && !isEventName(actionBeanType, name) ) {
            hidden.setName(name);
            try {
               hidden.doStartTag();
               hidden.doAfterBody();
               hidden.doEndTag();
            }
            catch ( Throwable t ) {
               /** Catch whatever comes back out of the doCatch() method and deal with it */
               try {
                  hidden.doCatch(t);
               }
               catch ( Throwable t2 ) {
                   if ( t2 instanceof JspException ) {
                       throw (JspException)t2;
                   }
                   if ( t2 instanceof RuntimeException ) {
                       throw (RuntimeException)t2;
                   } else {
                       throw new StripesJspException(t2);
                   }
               }
            }
            finally {
               hidden.doFinally();
            }
         }
      }
   }
}
