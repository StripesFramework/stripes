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

import net.sourceforge.stripes.controller.StripesConstants;
import net.sourceforge.stripes.tag.StripesTagSupport;
import net.sourceforge.stripes.util.Log;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

/**
 * On the surface, allows a developer to define a layout using a custom tag - but is actually
 * the tag responsible for generating the output of the layout.  A layout can have zero or more
 * nested components, as well as regular text and other custom tags nested within it.
 *
 * @author Tim Fennell
 * @since Stripes 1.1
 */
public class LayoutDefinitionTag extends StripesTagSupport {
    private static final Log log = Log.getInstance(LayoutDefinitionTag.class);

    private String layoutName;
    private LayoutContext context;

    /**
     * Assuming that the layout definition page is always included, the following line gets the name
     * of the page the tag is sitting on, as per Servlet 2.4 spec, page 65.
     */
    public String getLayoutName() {
        if (layoutName == null) {
            layoutName = (String) getPageContext().getRequest().getAttribute(
                    StripesConstants.REQ_ATTR_INCLUDE_PATH);
        }

        return layoutName;
    }

    /** Get the current layout context. */
    public LayoutContext getLayoutContext() {
        if (context == null) {
            context = LayoutContext.find(getPageContext(), getLayoutName());
        }

        return context;
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
        // Try to clear the buffer to make sure we don't output anything outside the layout-def tag.
        PageContext pageContext = getPageContext();
        try {
            pageContext.getOut().clearBuffer();
        }
        catch (IOException ioe) {
            // Not a whole lot we can do if we cannot clear the buffer :/
            log.warn("Could not clear buffer before rendering a layout.", ioe);
        }

        // Put any additional parameters into page context for the definition to use
        LayoutContext context = getLayoutContext();
        for (Map.Entry<String, Object> entry : context.getParameters().entrySet()) {
            pageContext.setAttribute(entry.getKey(), entry.getValue());
        }
        for (Entry<String, LayoutComponentRenderer> entry : context.getComponents().entrySet()) {
            entry.getValue().pushPageContext(pageContext);
            pageContext.setAttribute(entry.getKey(), entry.getValue());
        }

        return EVAL_BODY_INCLUDE;
    }

    /**
     * Causes page evaluation to end once the end of the layout definition is reached.
     * @return SKIP_PAGE in all cases
     */
    @Override
    public int doEndTag() throws JspException {
        try {
            getLayoutContext().setRendered(true);
            return SKIP_PAGE;
        }
        finally {
            // Pop our page context off the renderer's page context stack
            for (LayoutComponentRenderer renderer : context.getComponents().values()) {
                renderer.popPageContext();
            }

            // Set fields back to null
            this.layoutName = null;
            this.context = null;
        }
    }
}
