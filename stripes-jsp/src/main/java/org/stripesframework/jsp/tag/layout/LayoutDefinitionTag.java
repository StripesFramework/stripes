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
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.stripesframework.web.exception.StripesRuntimeException;


/**
 * On the surface, allows a developer to define a layout using a custom tag - but is actually
 * the tag responsible for generating the output of the layout.  A layout can have zero or more
 * nested components, as well as regular text and other custom tags nested within it.
 *
 * @author Tim Fennell, Ben Gunter
 * @since Stripes 1.1
 */
public class LayoutDefinitionTag extends LayoutTag {

   private LayoutContext _context;
   private boolean       _renderPhase;
   private boolean       _silent;

   /**
    * Causes page evaluation to end once the end of the layout definition is reached.
    * @return SKIP_PAGE in all cases
    */
   @Override
   public int doEndTag() throws JspException {
      try {
         cleanUpComponentRenderers();
         _context.getOut().setSilent(_silent, _pageContext);
         return SKIP_PAGE;
      }
      catch ( IOException e ) {
         throw new JspException(e);
      }
      finally {
         _context = null;
         _renderPhase = false;
         _silent = false;
      }
   }

   /**
    * Looks up the layout context that has been setup by a {@link LayoutRenderTag}. Uses the
    * context to push any dynamic attributes supplied to the render tag in to the page context
    * available during the body of the {@link LayoutDefinitionTag}.
    *
    * @return {@code EVAL_BODY_INCLUDE} in all cases.
    */
   @Override
   public int doStartTag() throws JspException {
      try {
         // Flag this definition has rendered, even though it's not really done yet.
         _context.setRendered(true);

         // Put any additional parameters into page context for the definition to use
         if ( !_renderPhase ) {
            for ( Map.Entry<String, Object> entry : _context.getParameters().entrySet() ) {
               _pageContext.setAttribute(entry.getKey(), entry.getValue());
            }
         }

         // Put component renderers into the page context, even those from previous contexts
         exportComponentRenderers();

         // Enable output only if this is the definition execution, not a component render
         _context.getOut().setSilent(_renderPhase, _pageContext);

         return EVAL_BODY_INCLUDE;
      }
      catch ( IOException e ) {
         throw new JspException(e);
      }
   }

   @Override
   public void setPageContext( PageContext pageContext ) {
      // Call super method
      super.setPageContext(pageContext);

      // Initialize layout context and related fields
      _context = LayoutContext.lookup(pageContext);

      if ( _context == null || getLayoutParent() != null ) {
         throw new StripesRuntimeException("The JSP page " + getCurrentPagePath() + " contains a layout-definition tag and was invoked directly. "
               + "A layout-definition can only be invoked by a page that contains " + "a layout-render tag.");
      }

      _renderPhase = _context.isComponentRenderPhase();
      _silent = _context.getOut().isSilent();
   }
}
