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

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTag;

import org.stripesframework.jsp.exception.StripesJspException;


/**
 * <p>A Stripes version of the {@literal <c:url/>} tag that adds some Stripes specific
 * parameters to the URL.  Designed to generate URLs and either write them into the page
 * or set them into one of the JSP scopes.</p>
 *
 * <p>Cooperates with the Stripes ParamTag to accept any number of parameters that will be
 * merged into the URL before rendering.</p>
 *
 * @author Tim Fennell
 * @since Stripes 1.4
 * @see ParamTag
 */
public class UrlTag extends LinkTagSupport implements BodyTag {

   String _var;
   String _scope;

   /**
    * Does nothing.
    * @return {@link #SKIP_BODY} in all cases.
    */
   @Override
   public int doAfterBody() throws JspException { return SKIP_BODY; }

   /**
    * Generates the URL and either writes it into the page or sets it in the appropraite
    * JSP scope.
    *
    * @return {@link #EVAL_PAGE} in all cases.
    * @throws JspException if the output stream cannot be written to.
    */
   @Override
   public int doEndTag() throws JspException {
      String url = buildUrl();

      // If the user specified a 'var', then set the url as a scoped variable
      if ( _var != null ) {
         String s = (_scope) == null ? "page" : _scope;

         if ( s.equalsIgnoreCase("request") ) {
            getPageContext().getRequest().setAttribute(_var, url);
         } else if ( s.equalsIgnoreCase("session") ) {
            getPageContext().getSession().setAttribute(_var, url);
         } else if ( s.equalsIgnoreCase("application") ) {
            getPageContext().getServletContext().setAttribute(_var, url);
         } else {
            getPageContext().setAttribute(_var, url);
         }

      }
      // Else just write it out to the page
      else {
         try {
            getPageContext().getOut().write(url);
         }
         catch ( IOException ioe ) {
            throw new StripesJspException("IOException while trying to write url to page.", ioe);
         }
      }

      clearParameters();

      return EVAL_PAGE;
   }

   /** Does nothing. */
   @Override
   public void doInitBody() throws JspException { /* Do Nothing. */ }

   /**
    * Does nothing.
    * @return {@link #EVAL_BODY_BUFFERED} in all cases.
    */
   @Override
   public int doStartTag() throws JspException { return EVAL_BODY_BUFFERED; }

   /** Gets the name of scope to store the scoped variable specified by 'var' in. */
   public String getScope() { return _scope; }

   /** Gets the URL as supplied on the page. */
   public String getValue() { return getUrl(); }

   /** Gets the name of the scoped variable to store the URL in. */
   public String getVar() { return _var; }

   /** Sets the name of scope to store the scoped variable specified by 'var' in. */
   public void setScope( String scope ) { _scope = scope; }

   /** Sets the URL as supplied on the page. */
   public void setValue( String value ) { setUrl(value); }

   /** Sets the name of the scoped variable to store the URL in. */
   public void setVar( String var ) { _var = var; }
}
