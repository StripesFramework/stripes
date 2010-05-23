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

import java.util.Iterator;
import java.util.LinkedList;

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
    private String componentName;

    /**
     * Create a new instance to render the named component to a string.
     * 
     * @param componentName The name of the component to render.
     */
    public LayoutComponentRenderer(String componentName) {
        this.componentName = componentName;
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

    @Override
    public String toString() {
        final PageContext pageContext = getPageContext();
        if (pageContext == null) {
            log.error("Failed to render component \"", componentName, "\" without a page context!");
            return "[Failed to render component \"" + componentName + "\" without a page context!]";
        }

        // Get the current page so we can be sure not to invoke it again (see below)
        final String currentPage = (String) pageContext.getRequest().getAttribute(
                StripesConstants.REQ_ATTR_INCLUDE_PATH);

        // Grab some values from the current context so they can be restored when we're done
        final LayoutContext context = LayoutContext.lookup(pageContext);
        final boolean phaseFlag = context.isComponentRenderPhase();
        final String component = context.getComponent();
        final boolean silent = context.getOut().isSilent();

        // Descend the layout context stack, trying each context where the component is registered
        log.debug("Stringify component \"", componentName, "\" in ", currentPage);
        LinkedList<LayoutContext> stack = LayoutContext.getStack(pageContext, true);
        for (Iterator<LayoutContext> iter = stack.descendingIterator(); iter.hasNext();) {
            // Skip contexts where the desired component is not registered or which would invoke the
            // current page again.
            final LayoutContext source = iter.next();
            if (!source.getComponents().containsKey(componentName)
                    || source.getRenderPage().equals(currentPage)) {
                log.trace("Not stringifying \"", componentName, "\" in context ", source
                        .getRenderPage(), " -> ", source.getDefinitionPage());
                continue;
            }

            // Turn on the render phase flag and set the component to render
            context.setComponentRenderPhase(true);
            context.setComponent(componentName);

            try {
                log.debug("Start stringify \"", componentName, "\" in ", context.getRenderPage(),
                        " -> ", context.getDefinitionPage(), " from ", source.getRenderPage(),
                        " -> ", source.getDefinitionPage());
                context.getOut().openBuffer(pageContext);
                context.getOut().setSilent(true, pageContext);
                pageContext.include(source.getRenderPage(), false);
                log.debug("End stringify \"", componentName, "\" in ", context.getRenderPage(),
                        " -> ", context.getDefinitionPage(), " from ", source.getRenderPage(),
                        " -> ", source.getDefinitionPage());
            }
            catch (Exception e) {
                log.error(e, "Unhandled exception trying to render component \"", componentName,
                        "\" to a string in context ", source.getRenderPage(), " -> ", source
                                .getDefinitionPage());
                return "[Failed to render \"" + componentName + "\". See log for details.]";
            }
            finally {
                // Determine if the component rendered before resetting the context properties
                boolean rendered = context.getComponent() == null;

                // Reset the context properties
                context.setComponentRenderPhase(phaseFlag);
                context.setComponent(component);
                context.getOut().setSilent(silent, pageContext);

                // Pop the buffer contents and return them if the component did render
                String value = context.getOut().closeBuffer(pageContext);
                if (rendered)
                    return value;
            }
        }

        log.debug("Component \"", componentName, "\" evaluated to empty string in context ",
                context.getRenderPage(), " -> ", context.getDefinitionPage());
        return "";
    }
}
