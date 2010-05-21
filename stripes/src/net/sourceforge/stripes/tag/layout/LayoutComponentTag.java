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
import java.util.Iterator;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.jsp.JspException;

import net.sourceforge.stripes.exception.StripesJspException;
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
    private static final Pattern javaIdentifierPattern = Pattern
            .compile("\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*");

    private String name;
    private LayoutContext context;
    private Boolean silent;

    /** Gets the name of the component. */
    public String getName() { return name; }

    /** Sets the name of the component. */
    public void setName(String name) { this.name = name; }

    /**
     * Get the current layout context.
     * 
     * @throws StripesJspException If a {@link LayoutContext} is not found.
     */
    public LayoutContext getContext() throws StripesJspException {
        if (context == null) {
            context = LayoutContext.lookup(pageContext);

            if (context == null) {
                throw new StripesJspException("A component tag named \"" + getName() + "\" in "
                        + getCurrentPagePath() + " was unable to find a layout context.");
            }

            log.trace("Component ", getName() + " has context ", context.getRenderPage(), " -> ",
                    context.getDefinitionPage());
        }

        return context;
    }

    /**
     * True if this tag is the component to be rendered on this pass from
     * {@link LayoutDefinitionTag}.
     * 
     * @throws StripesJspException If a {@link LayoutContext} is not found.
     */
    public boolean isCurrentComponent() throws StripesJspException {
        String name = getContext().getComponent();
        return name != null && name.equals(getName());
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
     * as indicated by {@link LayoutContext#getCurrentComponentName()}, then evaluate this tag's
     * body by returning {@code EVAL_BODY_INCLUDE}.
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
            LayoutContext context = getContext();

            if (isChildOfRender()) {
                if (context.isComponentRenderPhase()) {
                    if (isCurrentComponent()) {
                        log.debug("Render ", getName(), " in ", context.getRenderPage());
                        silent = context.getOut().isSilent();
                        context.getOut().setSilent(false, pageContext);
                        return EVAL_BODY_INCLUDE;
                    }
                    else {
                        log.debug("No-op for ", getName(), " in ", context.getRenderPage());
                    }
                }
                else {
                    if (!javaIdentifierPattern.matcher(getName()).matches()) {
                        log.warn("The layout-component name '", getName(),
                                "' is not a valid Java identifier. While this may work, it can ",
                                "cause bugs that are difficult to track down. Please consider ",
                                "using valid Java identifiers for component names ",
                                "(no hyphens, no spaces, etc.)");
                    }

                    log.debug("Register component ", getName(), " with ", context.getRenderPage());
                    context.getComponents().put(getName(), new LayoutComponentRenderer(this));
                }
            }
            else if (isChildOfDefinition()) {
                if (!context.isComponentRenderPhase()) {
                    // Set render phase flag and the name of the component to render. Iterate down
                    // the stack of layout contexts, executing each render page that contains a
                    // component with the same name as this one until the component has rendered or
                    // we run out of contexts.
                    context.setComponentRenderPhase(true);
                    context.setComponent(getName());
                    Iterator<LayoutContext> iterator = LayoutContext.getStack(pageContext, true)
                            .descendingIterator();
                    while (iterator.hasNext() && context.getComponent() != null) {
                        LayoutContext renderer = iterator.next();
                        if (renderer.getComponents().containsKey(getName())) {
                            String renderPage = renderer.getRenderPage();
                            log.debug("Execute component ", getName(), " in ", context
                                    .getDefinitionPage(), " with include of ", renderPage);
                            pageContext.include(renderPage, false);
                        }
                    }
                    context.setComponentRenderPhase(false);

                    // The current component name should be cleared after the component has
                    // rendered. If it is not cleared then the component did not render so we need
                    // to output the default content from the layout definition.
                    if (context.getComponent() != null) {
                        log.debug("Component was not present in ", context.getRenderPage(),
                                " so using default content from ", context.getDefinitionPage());

                        silent = context.getOut().isSilent();
                        context.getOut().setSilent(false, pageContext);
                        context.setComponent(null);
                        return EVAL_BODY_INCLUDE;
                    }
                }
                else {
                    log.debug("No-op for ", getName(), " in ", context.getDefinitionPage());
                }
            }

            return SKIP_BODY;
        }
        catch (ServletException e) {
            throw new StripesJspException(e);
        }
        catch (IOException e) {
            throw new StripesJspException(e);
        }
    }

    /**
     * If this tag is the component that needs to be rendered, as indicated by
     * {@link LayoutContext#getCurrentComponentName()}, then set the current component name back to
     * null to indicate that the component has rendered.
     * 
     * @return SKIP_PAGE if this component is the current component, otherwise EVAL_PAGE.
     */
    @Override
    public int doEndTag() throws JspException {
        try {
            // Set current component name back to null as a signal to the component tag within the
            // definition tag that the component did, indeed, render and it should not output the
            // default contents.
            LayoutContext context = getContext();
            if (isCurrentComponent())
                context.setComponent(null);

            if (silent != null)
                context.getOut().setSilent(silent, pageContext);

            return EVAL_PAGE;
        }
        finally {
            this.context = null;
            this.silent = null;
        }
    }
}
