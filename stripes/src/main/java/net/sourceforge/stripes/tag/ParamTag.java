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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.Tag;


/**
 * <p>Used to supply parameters when nested inside tags that implement {@link ParameterizableTag}.
 * The value is either obtained from the value attribute, or if that is not present, then the
 * body of the tag.</p>
 *
 * <p>Once the value has been established the parent tag is looked for, and the parameter is handed
 * over to it.</p>
 *
 * <p>Primarily used by the LinkTag and UrlTag.</p>
 * @author Tim Fennell
 * @since Stripes 1.4
 * @see ParamTag
 */
public class ParamTag implements BodyTag {

   private String      _name;
   private Object      _value;
   private BodyContent _bodyContent;
   private Tag         _parentTag;
   private PageContext _pageContext;

   /**
    * Does nothing.
    * @return SKIP_BODY in all cases.
    */
   @Override
   public int doAfterBody() throws JspException { return SKIP_BODY; }

   /**
    * Figures out what to use as the value, and then finds the parent link and adds
    * the parameter.
    * @return EVAL_PAGE in all cases.
    */
   @Override
   public int doEndTag() throws JspException {
      Object valueToSet = _value;

      // First figure out what value to send to the parent link tag
      if ( _value == null ) {
         if ( _bodyContent == null ) {
            valueToSet = "";
         } else {
            valueToSet = _bodyContent.getString();
         }
      }

      // Find the parent link tag
      Tag parameterizable = _parentTag;
      while ( parameterizable != null && !(parameterizable instanceof ParameterizableTag) ) {
         parameterizable = parameterizable.getParent();
      }

      ((ParameterizableTag)parameterizable).addParameter(_name, valueToSet);
      return EVAL_PAGE;
   }

   /** Does nothing. */
   @Override
   public void doInitBody() throws JspException { /* Do Nothing */ }

   /**
    * Does nothing.
    * @return EVAL_BODY_BUFFERED in all cases.
    */
   @Override
   public int doStartTag() throws JspException { return EVAL_BODY_BUFFERED; }

   /** Gets the name of the parameter(s) that will be added to the URL. */
   public String getName() {
      return _name;
   }

   public PageContext getPageContext() {
      return _pageContext;
   }

   /** Required spec method to allow others to access the parent of the tag. */
   @Override
   public Tag getParent() {
      return _parentTag;
   }

   /** Gets the value attribute, as set with setValue(). */
   public Object getValue() {
      return _value;
   }

   /** Does nothing. */
   @Override
   public void release() { /* Do nothing. */ }

   /** Used by the container to set the contents of the body of the tag. */
   @Override
   public void setBodyContent( BodyContent bodyContent ) {
       _bodyContent = bodyContent;
   }

   /** Sets the name of the parameter(s) that will be added to the URL. */
   public void setName( String name ) {
       _name = name;
   }

   /** Used by the container to set the page context for the tag. */
   @Override
   public void setPageContext( PageContext pageContext ) {
       _pageContext = pageContext;
   }

   /** Used by the container to provide the tag with access to it's parent tag on the page. */
   @Override
   public void setParent( Tag tag ) {
      _parentTag = tag;
   }

   /** Sets the value of the parameter(s) to be added to the URL. */
   public void setValue( Object value ) {
       _value = value;
   }
}
