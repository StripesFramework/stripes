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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.DynamicAttributes;

import net.sourceforge.stripes.exception.StripesJspException;
import net.sourceforge.stripes.tag.StripesTagSupport;
import net.sourceforge.stripes.util.HttpUtil;

/**
 * Renders a named layout, optionally overriding one or more components in the layout. Any
 * attributes provided to the class other than 'name' will be placed into page context during
 * the evaluation of the layout, making them available to other tags, and in EL.
 *
 * @author Tim Fennell
 * @since Stripes 1.1
 */
public class LayoutRenderTag extends StripesTagSupport implements DynamicAttributes {
    private String name;
    private Boolean recursing;
    private LayoutContext context;

    /** Gets the name of the layout to be used. */
    public String getName() { return name; }

    /** Sets the name of the layout to be used. */
    public void setName(String name) { this.name = name; }

    /**
     * The layout tags work in a quirky way: layout-render includes the page referenced in its
     * {@code name} attribute, which again includes the page containing the layout-render tag once
     * for each layout-component tag it encounters. This flag is false if this is the initial
     * invocation of the render tag and true if this invocation is coming from the layout-definition
     * tag as a request to render a component.
     */
    public boolean isRecursing() {
        if (recursing == null) {
            recursing = getContext().getCurrentComponentName() != null;
        }

        return recursing;
    }

    /** Look up an existing layout context or create a new one if none is found. */
    public LayoutContext getContext() {
        if (context == null) {
            if (getName() != null)
                context = LayoutContext.find(getPageContext(), getName());
            if (context == null)
                context = new LayoutContext();
        }

        return context;
    }

    /** Used by the JSP container to provide the tag with dynamic attributes. */
    public void setDynamicAttribute(String uri, String localName, Object value) throws JspException {
        getContext().getParameters().put(localName, value);
    }

    /**
     * Allows nested tags to register themselves for rendering in the layout.
     * 
     * @param name the name of the component to be overridden in the layout
     * @param renderer the object that will render the component to a string
     */
    public void addComponent(String name, LayoutComponentRenderer renderer) {
        getContext().getComponents().put(name, renderer);
    }

    /**
     * On the first pass (see {@link #isRecursing()}):
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
        if (!isRecursing()) {
            // Ensure absolute path for layout name
            if (!getName().startsWith("/")) {
                throw new StripesJspException("The name= attribute of the layout-render tag must be " +
                    "an absolute path, starting with a forward slash (/). Please modify the " +
                    "layout-render tag with the name '" + getName() + "' accordingly.");
            }

            pushPageContextAttributes(getContext().getParameters());
        }

        return EVAL_BODY_INCLUDE;
    }

    /**
     * After the first pass (see {@link #isRecursing()}):
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
            if (!isRecursing()) {
                // Put the components into the request, for the definition tag to use.. using a stack
                // to allow for the same layout to be nested inside itself :o
                PageContext pageContext = getPageContext();
                LayoutContext.findStack(pageContext, getName(), true).add(getContext());

                // Now include the target JSP
                HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
                getContext().setRenderPage(HttpUtil.getRequestedServletPath(request));
                try {
                    pageContext.include(this.name, false);
                }
                catch (Exception e) {
                    throw new StripesJspException(
                        "An exception was raised while invoking a layout. The layout used was " +
                        "'" + this.name + "'. The following information was supplied to the render " +
                        "tag: " + this.context.toString(), e);
                }

                // Check that the layout actually got rendered as some containers will
                // just quietly ignore includes of non-existent pages!
                if (!getContext().isRendered()) {
                    throw new StripesJspException(
                            "Attempt made to render a layout that does not exist. The layout name " +
                            "provided was '" + this.name + "'. Please check that a JSP/view exists at " +
                            "that location within your web application."
                        );
                }

                LayoutContext.pop(pageContext, getName());
                popPageContextAttributes(); // remove any dynattrs from page scope
            }
        }
        finally {
            this.recursing = null;
            this.context = null;
        }

        return EVAL_PAGE;
    }
}
