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
import java.util.Iterator;
import java.util.LinkedList;

import javax.servlet.ServletException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyContent;

import net.sourceforge.stripes.exception.StripesJspException;
import net.sourceforge.stripes.exception.StripesRuntimeException;

/**
 * An object that can be stuffed into a scope (page, request, application, etc.) and render a layout
 * component to a string. This allows for use of EL expressions to output a component (as described
 * in the book <em>Stripes ... and web development is fun again</em>) without requiring that all
 * components be evaluated and buffered just in case a string representation is needed. The
 * evaluation happens only when necessary, saving cycles and memory.
 * 
 * @author Ben Gunter
 * @since Stripes 1.5.4
 */
public class LayoutComponentRenderer {
    private LinkedList<PageContext> pageContext;
    private String componentName;

    /**
     * Create a new instance to render the specified component tag to a string. The tag itself is
     * only used to get other information that is necessary to turn a component into a string, such
     * as layout name and component name.
     * 
     * @param tag The layout component to render.
     * @throws StripesJspException If the tag cannot find a layout context.
     */
    public LayoutComponentRenderer(LayoutComponentTag tag) throws StripesJspException {
        this.componentName = tag.getName();
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
        if (pageContext == null || pageContext.isEmpty())
            return null;
        else
            return pageContext.removeLast();
    }

    /** Get the last page context that was pushed onto the stack. */
    public PageContext getPageContext() {
        if (pageContext == null || pageContext.isEmpty())
            return null;
        else
            return pageContext.getLast();
    }

    @Override
    public String toString() {
        // Save the current component name so it can be restored when we're done
        PageContext pageContext = getPageContext();
        if (pageContext == null)
            return componentName + " (page context is missing)";

        // FIXME decorator pattern is broken
        if (1 == Integer.valueOf("1"))
            return componentName + " (fix me!)";

        Iterator<LayoutContext> iterator = LayoutContext.getStack(pageContext, true)
                .descendingIterator();
        while (iterator.hasNext()) {
            LayoutContext context = iterator.next();
            boolean flag = context.isComponentRenderPhase();
            String name = context.getComponent();
            context.setComponentRenderPhase(true);
            context.setComponent(componentName);

            try {
                if (context.getComponents().containsKey(componentName)) {
                    BodyContent body = pageContext.pushBody();
                    pageContext.include(context.getRenderPage(), false);
                    pageContext.popBody();
                    if (context.getComponent() == null)
                        return body.getString();
                }
            }
            catch (ServletException e) {
                throw new StripesRuntimeException(e);
            }
            catch (IOException e) {
                throw new StripesRuntimeException(e);
            }
            finally {
                context.setComponentRenderPhase(flag);
                context.setComponent(name);
            }
        }

        return componentName + " (render failed)";
    }
}
