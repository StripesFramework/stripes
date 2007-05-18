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

import net.sourceforge.stripes.tag.StripesTagSupport;
import net.sourceforge.stripes.util.Log;

import javax.servlet.jsp.JspException;
import java.io.IOException;
import java.util.Stack;
import java.util.Map;

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

    /**
     * Prefix used to construct the request attribute name used to pass context from the
     * LayoutRenderTag to the LayoutDefinitionTag.
     */
    public static final String PREFIX = "stripes.layout.";

    private LayoutContext context;

    /**
     * Looks up the layout context that has been setup by a LayoutRenderTag. Uses the context
     * to push any dynamic attributes supplied to the render tag in to the page context
     * available during the body of the LayouDefinitionTag.
     *
     * @return EVAL_BODY_INCLUDE in all cases.
     */
    @Override
    @SuppressWarnings("unchecked")
	public int doStartTag() throws JspException {
        // Since the layout-render tag pushes a new writer onto the stack, we can clear the
        // buffer here to make sure we don't output anything outside the layout-def tag.
        try {
            getPageContext().getOut().clearBuffer();
        }
        catch (IOException ioe) {
            // Not a whole lot we can do if we cannot clear the buffer :/
            log.warn("Could not clear buffer before rendering a layout.", ioe);
        }

        // Assuming that the layout definition page is always included, the following line gets
        // the name of the page the tag is sitting on, as per Servlet 2.4 spec, page 65.
        String name = (String) getPageContext().getRequest()
                .getAttribute("javax.servlet.include.servlet_path");

        // Fetch the layout context containing parameters and component overrides
        Stack<LayoutContext> stack = (Stack<LayoutContext>)
                getPageContext().getRequest().getAttribute(PREFIX + name);
        this.context = stack.peek();

        // Put any additional parameters into page context for the definition to use
        for (Map.Entry<String,Object> entry : this.context.getParameters().entrySet()) {
            getPageContext().setAttribute(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String,String> entry : this.context.getComponents().entrySet()) {
            getPageContext().setAttribute(entry.getKey(), entry.getValue());
        }

        return EVAL_BODY_INCLUDE;
    }

    /**
     * Causes page evaluation to end once the end of the layout definition is reached.
     * @return SKIP_PAGE in all cases
     */
    @Override
    public int doEndTag() throws JspException {
        return SKIP_PAGE;
    }

    /**
     * Called by nested tags to find out if they have permission to render their content, or
     * if they have been overriden in the layout rendering tag.  Returns true if a component
     * has not been overriden and should render as normal.  Returns false, and writes out the
     * overriden component when the component has been overridden.
     *
     * @param componentName the name of the component about to render
     * @return true if the component should render itself, false otherwise
     * @throws JspException if the JspWriter could not be written to
     */
    public boolean permissionToRender(String componentName) throws JspException {
        if (this.context.getComponents().containsKey(componentName)) {
            try {
                getPageContext().getOut().write(this.context.getComponents().get(componentName));
            }
            catch (IOException ioe) {
                throw new JspException("Could not output overrident layout component.", ioe);
            }
            return false;
        }
        else {
            return true;
        }
    }
}
