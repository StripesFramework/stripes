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
package net.sourceforge.stripes.tag.layout;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import net.sourceforge.stripes.exception.StripesJspException;
import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.util.Log;


/**
 * Defines a component in a layout. Used both to define the components in a layout definition
 * and to provide overridden component definitions during a layout rendering request.
 *
 * @author Tim Fennell, Ben Gunter
 * @since Stripes 1.1
 */
public class LayoutComponentTag extends LayoutTag {

   private static final Log log = Log.getInstance(LayoutComponentTag.class);

   /** Regular expression that matches valid Java identifiers. */
   private static final Pattern javaIdentifierPattern = Pattern.compile("\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*");

   private String        _name;
   private LayoutContext _context;
   private boolean       _silent;
   private Boolean       _componentRenderPhase;

   /**
    * If this tag is the component that needs to be rendered, as indicated by
    * {@link LayoutContext#getComponent()}, then set the current component name back to null to
    * indicate that the component has rendered.
    *
    * @return SKIP_PAGE if this component is the current component, otherwise EVAL_PAGE.
    */
   @Override
   public int doEndTag() throws JspException {
      try {
         // Set current component name back to null as a signal to the component tag within the
         // definition tag that the component did, indeed, render and it should not output the
         // default contents.
         if ( isCurrentComponent() ) {
            _context.setComponent(null);
         }

         // If the component render phase flag was changed, then restore it now
         if ( _componentRenderPhase != null ) {
            _context.setComponentRenderPhase(_componentRenderPhase);
         }

         // Restore output's silent flag
         _context.getOut().setSilent(_silent, _pageContext);

         return EVAL_PAGE;
      }
      catch ( IOException e ) {
         throw new JspException(e);
      }
      finally {
         _context = null;
         _silent = false;
         _componentRenderPhase = null;
      }
   }

   /**
    * <p>
    * If this tag is nested within a {@link LayoutDefinitionTag}, then evaluate the corresponding
    * {@link LayoutComponentTag} nested within the {@link LayoutRenderTag} that invoked the parent
    * {@link LayoutDefinitionTag}. If, after evaluating the corresponding tag, the component has
    * not been rendered then evaluate this tag's body by returning {@code EVAL_BODY_INCLUDE}.
    * </p>
    * <p>
    * If this tag is nested within a {@link LayoutRenderTag} and this tag is the current component,
    * as indicated by {@link LayoutContext#getComponent()}, then evaluate this tag's body by
    * returning {@code EVAL_BODY_INCLUDE}.
    * </p>
    * <p>
    * In all other cases, skip this tag's body by returning SKIP_BODY.
    * </p>
    *
    * @return {@code EVAL_BODY_INCLUDE} or {@code SKIP_BODY}, as described above.
    */
   @Override
   public int doStartTag() throws JspException {
      try {
         if ( _context.isComponentRenderPhase() ) {
            if ( isChildOfRender() ) {
               if ( isCurrentComponent() ) {
                  log.debug("Render ", getName(), " in ", _context.getRenderPage());
                  _context.getOut().setSilent(false, _pageContext);
                  return EVAL_BODY_INCLUDE;
               } else if ( _context.getComponentPath().isPathComponent(this) ) {
                  log.debug("Silently execute '", getName(), "' in ", _context.getRenderPage());
                  _context.getOut().setSilent(true, _pageContext);
                  return EVAL_BODY_INCLUDE;
               } else {
                  log.debug("No-op for ", getName(), " in ", _context.getRenderPage());
               }
            } else if ( isChildOfDefinition() ) {
               log.debug("No-op for ", getName(), " in ", _context.getDefinitionPage());
            } else if ( isChildOfComponent() ) {
               // Use a layout component renderer to do the heavy lifting
               log.debug("Invoke component renderer for nested render of \"", getName(), "\"");
               LayoutComponentRenderer renderer = (LayoutComponentRenderer)_pageContext.getAttribute(getName());
               if ( renderer == null ) {
                  log.debug("No component renderer in page context for '" + getName() + "'");
               }
               boolean rendered = renderer != null && renderer.write();

               // If the component did not render then we need to output the default contents
               // from the layout definition.
               if ( !rendered ) {
                  log.debug("Component was not present in ", _context.getRenderPage(), " so using default content from ", _context.getDefinitionPage());

                  _context.getOut().setSilent(false, _pageContext);
                  return EVAL_BODY_INCLUDE;
               }
            }
         } else {
            if ( isChildOfRender() ) {
               if ( !javaIdentifierPattern.matcher(getName()).matches() ) {
                  log.warn("The layout-component name '", getName(), "' is not a valid Java identifier. While this may work, it can ",
                        "cause bugs that are difficult to track down. Please consider ", "using valid Java identifiers for component names ",
                        "(no hyphens, no spaces, etc.)");
               }

               log.debug("Register component ", getName(), " with ", _context.getRenderPage());

               // Look for an existing renderer for a component with the same name
               LayoutComponentRenderer renderer = null;
               for ( LayoutContext c = _context; c != null && renderer == null; c = c.getPrevious() ) {
                  renderer = c.getComponents().get(getName());
               }

               // If not found then create a new one
               if ( renderer == null ) {
                  renderer = new LayoutComponentRenderer(getName());
               }

               _context.getComponents().put(getName(), renderer);
            } else if ( isChildOfDefinition() ) {
               // Use a layout component renderer to do the heavy lifting
               log.debug("Invoke component renderer for direct render of \"", getName(), "\"");
               LayoutComponentRenderer renderer = (LayoutComponentRenderer)_pageContext.getAttribute(getName());
               if ( renderer == null ) {
                  log.debug("No component renderer in page context for '" + getName() + "'");
               }
               boolean rendered = renderer != null && renderer.write();

               // If the component did not render then we need to output the default contents
               // from the layout definition.
               if ( !rendered ) {
                  log.debug("Component was not present in ", _context.getRenderPage(), " so using default content from ", _context.getDefinitionPage());

                  _componentRenderPhase = _context.isComponentRenderPhase();
                  _context.setComponentRenderPhase(true);
                  _context.setComponent(getName());
                  _context.getOut().setSilent(false, _pageContext);
                  return EVAL_BODY_INCLUDE;
               }
            } else if ( isChildOfComponent() ) {
               /*
                * This condition cannot be true since component tags do not execute except in
                * component render phase, thus any component tags embedded with them will not
                * execute either. I've left this block here just as a placeholder for this
                * explanation.
                */
            }
         }

         _context.getOut().setSilent(true, _pageContext);
         return SKIP_BODY;
      }
      catch ( Exception e ) {
         log.error(e, "Unhandled exception trying to render component \"", getName(), "\" to a string in context ", _context.getRenderPage(), " -> ",
               _context.getDefinitionPage());

         if ( e instanceof RuntimeException ) {
            throw (RuntimeException)e;
         } else {
            throw new StripesJspException(e);
         }
      }
   }

   /** Gets the name of the component. */
   public String getName() { return _name; }

   /**
    * True if this tag is the component to be rendered on this pass from
    * {@link LayoutDefinitionTag}.
    *
    * @throws StripesJspException If a {@link LayoutContext} is not found.
    */
   public boolean isCurrentComponent() throws StripesJspException {
      String name = _context.getComponent();
      if ( name == null || !name.equals(getName()) ) {
         return false;
      }

      final LayoutTag parent = getLayoutParent();
      if ( !(parent instanceof LayoutRenderTag) ) {
         return _context.getComponentPath().getComponentPath() == null;
      }

      final LayoutRenderTagPath got = ((LayoutRenderTag)parent).getPath();
      return got != null && got.equals(_context.getComponentPath());
   }

   /** Sets the name of the component. */
   public void setName( String name ) { _name = name; }

   @Override
   public void setPageContext( PageContext pageContext ) {
      // Call super method
      super.setPageContext(pageContext);

      // Initialize the layout context and related fields
      _context = LayoutContext.lookup(pageContext);

      if ( _context == null ) {
         throw new StripesRuntimeException("A component tag named \"" + getName() + "\" in " + getCurrentPagePath() + " was unable to find a layout context.");
      }

      log.trace("Component ", getName() + " has context ", _context.getRenderPage(), " -> ", _context.getDefinitionPage());

      _silent = _context.getOut().isSilent();
   }
}
