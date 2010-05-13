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

import javax.servlet.ServletException;
import javax.servlet.jsp.JspException;

import net.sourceforge.stripes.exception.StripesJspException;
import net.sourceforge.stripes.tag.StripesTagSupport;
import net.sourceforge.stripes.util.Log;

/**
 * Defines a component in a layout. Used both to define the components in a layout definition
 * and to provide overridden component definitions during a layout rendering request.
 *
 * @author Tim Fennell
 * @since Stripes 1.1
 */
public class LayoutComponentTag extends StripesTagSupport {
    private static final Log log = Log.getInstance(LayoutComponentTag.class);

    /** Regular expression that matches valid Java identifiers. */
    private static final Pattern javaIdentifierPattern = Pattern
            .compile("\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*");

    private String name;
    private LayoutContext context;

    /** Gets the name of the component. */
    public String getName() { return name; }

    /** Sets the name of the component. */
    public void setName(String name) { this.name = name; }

    /** Get the current layout context. */
    public LayoutContext getContext() {
        if (context == null) {
            context = LayoutContext.find(getPageContext(), getLayoutName());
        }

        return context;
    }

    /**
     * True if this tag is the component to be rendered on this pass from
     * {@link LayoutDefinitionTag}.
     */
    public boolean isCurrentComponent() {
        LayoutContext context = getContext();
        if (context == null) {
            return false;
        }
        else {
            String name = context.getCurrentComponentName();
            return name != null && name.equals(getName());
        }
    }

    /**
     * Get the name of the layout that is being rendered; that is, the path to the JSP containing
     * the {@link LayoutDefinitionTag}. This value is also used to find the current layout context
     * in the page context.
     */
    public String getLayoutName() {
        LayoutRenderTag render;
        LayoutDefinitionTag definition;

        if ((render = getParentTag(LayoutRenderTag.class)) != null)
            return render.getName();
        else if ((definition = getParentTag(LayoutDefinitionTag.class)) != null)
            return definition.getLayoutName();
        else
            return null;
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
        LayoutRenderTag renderTag;

        if ((renderTag = getParentTag(LayoutRenderTag.class)) != null) {
            if (!renderTag.isRecursing()) {
                if (!javaIdentifierPattern.matcher(getName()).matches()) {
                    log.warn("The layout-component name '", getName(), "' is not a valid Java identifier. ",
                            "While this may work, it can cause bugs that are difficult to track down. Please ",
                            "consider using valid Java identifiers for component names (no hyphens, no spaces, etc.)");
                }

                renderTag.addComponent(getName(), new LayoutComponentRenderer(this));
            }

            return isCurrentComponent() ? EVAL_BODY_INCLUDE : SKIP_BODY;
        }
        else if (getParentTag(LayoutDefinitionTag.class) != null) {
            // Include the page that had the render tag on it to execute the component tags again.
            // Only the component(s) with a name matching the current component name in the context
            // will actually produce output.
            try {
                getContext().setCurrentComponentName(getName());
                getPageContext().include(getContext().getRenderPage());
            }
            catch (ServletException e) {
                throw new StripesJspException(e);
            }
            catch (IOException e) {
                throw new StripesJspException(e);
            }

            // The current component name should be cleared after the component tag in the render
            // tag has rendered. If it is not cleared then the component did not render so we need
            // to output the default contents from the layout definition.
            if (getContext().getCurrentComponentName() != null) {
                getContext().setCurrentComponentName(null);
                return EVAL_BODY_INCLUDE;
            }
            else {
                return SKIP_BODY;
            }
        }
        else {
            return SKIP_BODY;
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
            if (isCurrentComponent()) {
                getContext().setCurrentComponentName(null);
                return SKIP_PAGE;
            }
            else {
                return EVAL_PAGE;
            }
        }
        finally {
            this.context = null;
        }
    }
}
