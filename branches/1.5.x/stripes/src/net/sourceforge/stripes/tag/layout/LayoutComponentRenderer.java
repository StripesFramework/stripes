/* Copyright 2010 Ben Gunter
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

import javax.servlet.ServletException;
import javax.servlet.jsp.PageContext;

import net.sourceforge.stripes.controller.StripesConstants;
import net.sourceforge.stripes.util.Log;

/**
 * <p>
 * An object that can be stuffed into a scope (page, request, application, etc.) and render a layout
 * component to a string. This allows for use of EL expressions to output a component (as described
 * in the book <em>Stripes ... and web development is fun again</em>) without requiring that all
 * components be evaluated and buffered just in case a string representation is needed. The
 * evaluation happens only when necessary, saving cycles and memory.
 * </p>
 * <p>
 * When {@link #toString()} is called, the component renderer will evaluate the body of any
 * {@link LayoutComponentTag} found in the stack of {@link LayoutContext}s maintained in the JSP
 * {@link PageContext} having the same name as that passed to the constructor. The page context must
 * be provided with a call to {@link #pushPageContext(PageContext)} for the renderer to work
 * correctly.
 * </p>
 * 
 * @author Ben Gunter
 * @since Stripes 1.5.4
 */
public class LayoutComponentRenderer {
    private static final Log log = Log.getInstance(LayoutComponentRenderer.class);

    private LinkedList<PageContext> pageContext;
    private String component;
    private LayoutContext context;

    /**
     * Create a new instance to render the named component to a string.
     * 
     * @param component The name of the component to render.
     */
    public LayoutComponentRenderer(String component) {
        this.component = component;
    }

    /**
     * Push a new page context onto the page context stack. The last page context pushed onto the
     * stack is the one that will be used to evaluate the component tag's body.
     */
    public void pushPageContext(PageContext pageContext) {
        if (this.pageContext == null) {
            this.pageContext = new LinkedList<PageContext>();
        }

        this.pageContext.add(pageContext);
    }

    /** Pop the last page context off the stack and return it. */
    public PageContext popPageContext() {
        return pageContext == null || pageContext.isEmpty() ? null : pageContext.removeLast();
    }

    /** Get the last page context that was pushed onto the stack. */
    public PageContext getPageContext() {
        return pageContext == null || pageContext.isEmpty() ? null : pageContext.getLast();
    }

    /** Get the path to the currently executing JSP. */
    public String getCurrentPage() {
        return (String) getPageContext().getRequest().getAttribute(
                StripesConstants.REQ_ATTR_INCLUDE_PATH);
    }

    /**
     * Write the component to the page context's writer, optionally buffering the output.
     * 
     * @return True if the named component was found and it indicated that it successfully rendered;
     *         otherwise, false.
     * @throws IOException If thrown by {@link LayoutContext#doInclude(PageContext, String)}
     * @throws ServletException If thrown by {@link LayoutContext#doInclude(PageContext, String)}
     */
    public boolean write() throws ServletException, IOException {
        final PageContext pageContext = getPageContext();
        if (pageContext == null) {
            log.error("Failed to render component \"", this.component, "\" without a page context!");
            return false;
        }

        // Grab some values from the current context so they can be restored when we're done
        final LayoutContext savedContext = this.context;
        final LayoutContext currentContext = LayoutContext.lookup(pageContext);
        final boolean savedPhase = currentContext.isComponentRenderPhase();
        final boolean savedSilent = currentContext.getOut().isSilent();
        final String savedComponent = currentContext.getComponent();
        log.debug("Render component \"", this.component, "\" in ", getCurrentPage());

        // Turn on the render phase flag and set the component to render
        currentContext.setComponentRenderPhase(true);
        currentContext.setComponent(this.component);
        currentContext.getOut().setSilent(true, pageContext);

        // Descend the stack from here, trying each context where the component is registered
        try {
            for (LayoutContext context = savedContext == null ? currentContext : savedContext
                    .getPrevious(); context != null; context = context.getPrevious()) {

                // Skip contexts where the desired component is not registered.
                if (!context.getComponents().containsKey(this.component)) {
                    log.trace("Not rendering \"", this.component, "\" in context ",
                            context.getRenderPage(), " -> ", context.getDefinitionPage());
                    continue;
                }
                this.context = context;

                log.debug("Start execute \"", this.component, "\" in ",
                        currentContext.getRenderPage(), " -> ", currentContext.getDefinitionPage(),
                        " from ", context.getRenderPage(), " -> ", context.getDefinitionPage());
                currentContext.doInclude(pageContext, context.getRenderPage());
                log.debug("End execute \"", this.component, "\" in ",
                        currentContext.getRenderPage(), " -> ", currentContext.getDefinitionPage(),
                        " from ", context.getRenderPage(), " -> ", context.getDefinitionPage());

                // If the component name has been cleared then the component rendered
                if (currentContext.getComponent() == null)
                    return true;
            }
        }
        finally {
            // Reset the context properties
            currentContext.setComponentRenderPhase(savedPhase);
            currentContext.setComponent(savedComponent);
            currentContext.getOut().setSilent(savedSilent, pageContext);
            this.context = savedContext;
        }

        log.debug("Component \"", this.component, "\" evaluated to empty string in context ",
                currentContext.getRenderPage(), " -> ", currentContext.getDefinitionPage());
        return false;
    }

    /**
     * Open a buffer in {@link LayoutWriter}, call {@link #write()} to render the component and then
     * return the buffer contents.
     */
    @Override
    public String toString() {
        final PageContext pageContext = getPageContext();
        if (pageContext == null) {
            log.error("Failed to render component \"", this.component, "\" without a page context!");
            return "[Failed to render component \"" + this.component
                    + "\" without a page context!]";
        }

        final LayoutContext context = LayoutContext.lookup(pageContext);
        String contents;
        context.getOut().openBuffer(pageContext);
        try {
            log.debug("Start stringify \"", this.component, "\" in ", context.getRenderPage(),
                    " -> ", context.getDefinitionPage());
            write();
        }
        catch (Exception e) {
            log.error(e, "Unhandled exception trying to render component \"", this.component,
                    "\" to a string in context ", context.getRenderPage(), //
                    " -> ", context.getDefinitionPage());
            return "[Failed to render \"" + this.component + "\". See log for details.]";
        }
        finally {
            log.debug("End stringify \"", this.component, "\" in ", context.getRenderPage(),
                    " -> ", context.getDefinitionPage());
            contents = context.getOut().closeBuffer(pageContext);
        }

        return contents;
    }
}
