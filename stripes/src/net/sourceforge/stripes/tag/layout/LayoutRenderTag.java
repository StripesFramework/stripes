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
import java.util.LinkedList;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.DynamicAttributes;

import net.sourceforge.stripes.exception.StripesJspException;
import net.sourceforge.stripes.util.Log;

/**
 * Renders a named layout, optionally overriding one or more components in the layout. Any
 * attributes provided to the class other than 'name' will be placed into page context during
 * the evaluation of the layout, making them available to other tags, and in EL.
 *
 * @author Tim Fennell, Ben Gunter
 * @since Stripes 1.1
 */
public class LayoutRenderTag extends LayoutTag implements DynamicAttributes {
    private static final Log log = Log.getInstance(LayoutRenderTag.class);

    private String name;
    private LayoutContext context;
    private Boolean newContext;
    private boolean silent;

    /**
     * True if this is the "outer" tag. That is, the render tag that kicks off the whole layout
     * rendering process.
     */
    public boolean isOuterTag() {
        LinkedList<LayoutContext> stack = LayoutContext.getStack(pageContext, false);
        return stack != null && stack.size() < 2;
    }

    /** Gets the name of the layout to be used. */
    public String getName() { return name; }

    /** Sets the name of the layout to be used. */
    public void setName(String name) { this.name = name; }

    /** Look up an existing layout context or create a new one if none is found. */
    public LayoutContext getContext() {
        if (context == null) {
            LayoutContext context = LayoutContext.lookup(pageContext);
            boolean contextNew = false;

            if (context == null || !context.isComponentRenderPhase()) {
                context = LayoutContext.push(this);
                contextNew = true;
            }

            this.context = context;
            this.newContext = contextNew;
        }

        return context;
    }

    /** True if the context returned by {@link #getContext()} was newly created by this tag. */
    public boolean isNewContext() {
        // Force initialization of the context if necessary
        if (newContext == null)
            getContext();

        return newContext;
    }

    /** Used by the JSP container to provide the tag with dynamic attributes. */
    public void setDynamicAttribute(String uri, String localName, Object value) throws JspException {
        getContext().getParameters().put(localName, value);
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
        LayoutContext context = getContext();
        silent = context.getOut().isSilent();

        if (isNewContext()) {
            log.debug("Start layout init in ", context.getRenderPage());

            // Ensure absolute path for layout name
            if (!getName().startsWith("/")) {
                throw new StripesJspException("The name= attribute of the layout-render tag must be " +
                    "an absolute path, starting with a forward slash (/). Please modify the " +
                    "layout-render tag with the name '" + getName() + "' accordingly.");
            }

            pushPageContextAttributes(context.getParameters());
        }

        // Render tags never output their contents directly
        context.getOut().setSilent(true, pageContext);

        log.debug("Start component render phase for ", context.getComponent(), " in ", context
                .getRenderPage());

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
            LayoutContext context = getContext();
            if (isNewContext()) {
                // Substitution of the layout writer for the regular JSP writer does not work for
                // the initial render tag. Its body evaluation still uses the original JSP writer
                // for output. Clear the output buffer before executing the definition page.
                if (isOuterTag()) {
                    try {
                        context.getOut().clear();
                    }
                    catch (IOException e) {
                        log.debug("Could not clear output buffer: ", e.getMessage());
                    }
                }

                log.debug("End layout init in ", context.getRenderPage());

                try {
                    log.debug("Start layout exec in ", context.getDefinitionPage());
                    boolean silent = context.getOut().isSilent();
                    context.getOut().setSilent(true, pageContext);
                    pageContext.include(this.name, false);
                    context.getOut().setSilent(silent, pageContext);
                    log.debug("End layout exec in ", context.getDefinitionPage());
                }
                catch (Exception e) {
                    throw new StripesJspException(
                        "An exception was raised while invoking a layout. The layout used was " +
                        "'" + this.name + "'. The following information was supplied to the render " +
                        "tag: " + this.context.toString(), e);
                }

                // Check that the layout actually got rendered as some containers will
                // just quietly ignore includes of non-existent pages!
                if (!context.isRendered()) {
                    throw new StripesJspException(
                            "Attempt made to render a layout that does not exist. The layout name " +
                            "provided was '" + this.name + "'. Please check that a JSP/view exists at " +
                            "that location within your web application."
                        );
                }

                LayoutContext.pop(pageContext);
                popPageContextAttributes(); // remove any dynattrs from page scope
            }

            // Restore output's silent flag
            context.getOut().setSilent(silent, pageContext);

            log.debug("End component render phase for ", context.getComponent(), " in ", context
                    .getRenderPage());
        }
        finally {
            this.context = null;
            this.newContext = null;
            this.silent = false;
        }

        return EVAL_PAGE;
    }
}
