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
package org.stripesframework.jsp.tag.layout;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.DynamicAttributes;
import javax.servlet.jsp.tagext.Tag;

import org.stripesframework.jsp.exception.StripesJspException;
import org.stripesframework.web.exception.StripesRuntimeException;
import org.stripesframework.web.util.Log;


/**
 * Renders a named layout, optionally overriding one or more components in the layout. Any
 * attributes provided to the class other than 'name' will be placed into page context during
 * the evaluation of the layout, making them available to other tags, and in EL.
 *
 * @author Tim Fennell, Ben Gunter
 * @since Stripes 1.1
 */
public class LayoutRenderTag extends LayoutTag implements BodyTag, DynamicAttributes {

   private static final Log log = Log.getInstance(LayoutRenderTag.class);

   private String              _name;
   private LayoutContext       _context;
   private boolean             _contextIsNew;
   private boolean             _silent;
   private LayoutRenderTagPath _path;
   private BodyContent         _bodyContent;

   /** Returns {@link Tag#SKIP_BODY}. */
   @Override
   public int doAfterBody() throws JspException {
      return SKIP_BODY;
   }

   /**
    * After the first pass (see {@link LayoutContext#isComponentRenderPhase()}):
    * <ul>
    * <li>Ensure the layout rendered successfully by checking {@link LayoutContext#isRendered()}.</li>
    * <li>Remove the current layout context from request scope.</li>
    * <li>Restore previous page context attribute values.</li>
    * </ul>
    *
    * @return EVAL_PAGE in all cases.
    */
   @Override
   public int doEndTag() throws JspException {
      try {
         if ( _contextIsNew ) {
            log.debug("End layout init in ", _context.getRenderPage());

            try {
               log.debug("Start layout exec in ", _context.getDefinitionPage());
               _context.getOut().setSilent(true, _pageContext);
               _context.doInclude(_pageContext, getName());
               log.debug("End layout exec in ", _context.getDefinitionPage());
            }
            catch ( Exception e ) {
               throw new StripesJspException("An exception was raised while invoking a layout. The layout used was " + "'" + getName()
                     + "'. The following information was supplied to the render " + "tag: " + _context.toString(), e);
            }

            // Check that the layout actually got rendered as some containers will
            // just quietly ignore includes of non-existent pages!
            if ( !_context.isRendered() ) {
               throw new StripesJspException("Attempt made to render a layout that does not exist. The layout name " + "provided was '" + getName()
                     + "'. Please check that a JSP/view exists at " + "that location within your web application.");
            }

            _context.getOut().setSilent(_silent, _pageContext);
            LayoutContext.pop(_pageContext);
            popPageContextAttributes(); // remove any dynattrs from page scope
         } else {
            _context.getOut().setSilent(_silent, _pageContext);
         }

         if ( _context.isComponentRenderPhase() ) {
            log.debug("End component render phase for ", _context.getComponent(), " in ", _context.getRenderPage());
            cleanUpComponentRenderers();
         }

         return EVAL_PAGE;
      }
      catch ( IOException e ) {
         throw new JspException(e);
      }
      finally {
         _context = null;
         _contextIsNew = false;
         _path = null;
         _silent = false;

         if ( _bodyContent != null ) {
            _bodyContent.clearBody();
            _bodyContent = null;
         }
      }
   }

   /** Does nothing. */
   @Override
   public void doInitBody() throws JspException {
   }

   /**
    * On the first pass (see {@link LayoutContext#isComponentRenderPhase()}):
    * <ul>
    * <li>Push the values of any dynamic attributes into page context attributes for the duration
    * of the tag.</li>
    * <li>Create a new context and places it in request scope.</li>
    * <li>Include the layout definition page named by the {@code name} attribute.</li>
    * </ul>
    *
    * @return EVAL_BODY_INCLUDE in all cases
    */
   @Override
   public int doStartTag() throws JspException {
      try {
         if ( _contextIsNew ) {
            log.debug("Start layout init in ", _context.getRenderPage());
            pushPageContextAttributes(_context.getParameters());
         }

         if ( _context.isComponentRenderPhase() ) {
            log.debug("Start component render phase for ", _context.getComponent(), " in ", _context.getRenderPage());
            exportComponentRenderers();
         }

         // Render tags never output their contents directly
         _context.getOut().setSilent(true, _pageContext);

         return _contextIsNew ? EVAL_BODY_BUFFERED : EVAL_BODY_INCLUDE;
      }
      catch ( IOException e ) {
         throw new JspException(e);
      }
   }

   /** Gets the name of the layout to be used. */
   public String getName() { return _name; }

   /** Get the {@link LayoutRenderTagPath} that identifies this tag within the current page. */
   public LayoutRenderTagPath getPath() { return _path; }

   /** Returns true if this tag is a child of the current component tag. */
   public boolean isChildOfCurrentComponent() {
      try {
         LayoutTag parent = getLayoutParent();
         return parent instanceof LayoutComponentTag && ((LayoutComponentTag)parent).isCurrentComponent();
      }
      catch ( StripesJspException e ) {
         // This exception would have been thrown before this tag ever executed
         throw new StripesRuntimeException("Something has happened that should never happen", e);
      }
   }

   /**
    * Set the tag's body content. Called by the JSP engine during component registration phase,
    * when {@link #doStartTag()} returns {@link BodyTag#EVAL_BODY_BUFFERED}
    */
   @Override
   public void setBodyContent( BodyContent bodyContent ) {
      _bodyContent = bodyContent;
   }

   /** Used by the JSP container to provide the tag with dynamic attributes. */
   @Override
   public void setDynamicAttribute( String uri, String localName, Object value ) throws JspException {
      _context.getParameters().put(localName, value);
   }

   /** Sets the name of the layout to be used and then calls {@link #initialize()}. */
   public void setName( String name ) {
      _name = name;
      initialize();
   }

   /**
    * Initialize fields before execution begins. Typically, this would be done by overriding
    * {@link #setPageContext(javax.servlet.jsp.PageContext)}, but that isn't possible in this case
    * because some of the logic depends on {@link #setName(String)} having been called, which does
    * not happen until after {@link #setPageContext(javax.servlet.jsp.PageContext)} has been
    * called.
    */
   protected void initialize() {
      LayoutContext context = LayoutContext.lookup(_pageContext);

      boolean create = context == null || !context.isComponentRenderPhase() || isChildOfCurrentComponent();

      LayoutRenderTagPath path;
      if ( create ) {
         context = LayoutContext.push(this);
         path = context.getComponentPath();
      } else {
         path = new LayoutRenderTagPath(this);
      }

      _context = context;
      _contextIsNew = create;
      _path = path;
      _silent = context.getOut().isSilent();
   }
}
