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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.jsp.PageContext;

import net.sourceforge.stripes.util.Log;

/**
 * Used to move contextual information about a layout rendering between a LayoutRenderTag and
 * a LayoutDefinitionTag. Holds the set of overridden components and any parameters provided
 * to the render tag.
 *
 * @author Tim Fennell, Ben Gunter
 * @since Stripes 1.1
 */
public class LayoutContext {
    private static final Log log = Log.getInstance(LayoutContext.class);

    /** The attribute name by which the stack of layout contexts can be found in the request. */
    public static final String LAYOUT_CONTEXT_KEY = LayoutContext.class.getName() + "#Context";

    /**
     * Create a new layout context for the given render tag and push it onto the stack of layout
     * contexts in a JSP page context.
     */
    public static LayoutContext push(LayoutRenderTag renderTag) {
        LayoutContext context = new LayoutContext(renderTag);
        log.debug("Push context ", context.getRenderPage(), " -> ", context.getDefinitionPage());

        PageContext pageContext = renderTag.getPageContext();
        LayoutContext previous = lookup(pageContext);
        if (previous == null) {
            // Create a new layout writer and push a new body
            context.out = new LayoutWriter(pageContext.getOut());
            pageContext.pushBody(context.out);
        }
        else {
            // Link the two nodes
            context.out = previous.out;
            previous.next = context;
            context.previous = previous;
        }

        pageContext.setAttribute(LAYOUT_CONTEXT_KEY, context);
        return context;
    }

    /**
     * Look up the current layout context in a JSP page context.
     * 
     * @param pageContext The JSP page context to search for the layout context stack.
     */
    public static LayoutContext lookup(PageContext pageContext) {
        LayoutContext context = (LayoutContext) pageContext.getAttribute(LAYOUT_CONTEXT_KEY);
        if (context == null) {
            context = (LayoutContext) pageContext.getRequest().getAttribute(LAYOUT_CONTEXT_KEY);
            if (context != null) {
                for (LayoutContext c = context.getFirst(); c != context; c = c.getNext()) {
                    for (Entry<String, Object> entry : c.getParameters().entrySet()) {
                        pageContext.setAttribute(entry.getKey(), entry.getValue(),
                                PageContext.PAGE_SCOPE);
                    }
                }

                pageContext.setAttribute(LAYOUT_CONTEXT_KEY, context);
                pageContext.getRequest().removeAttribute(LAYOUT_CONTEXT_KEY);
            }
        }
        return context;
    }

    /**
     * Remove the current layout context from the stack of layout contexts.
     * 
     * @param pageContext The JSP page context to search for the layout context stack.
     * @return The layout context that was popped off the stack, or null if the stack was not found
     *         or was empty.
     */
    public static LayoutContext pop(PageContext pageContext) {
        LayoutContext context = lookup(pageContext);
        log.debug("Pop context ", context.getRenderPage(), " -> ", context.getDefinitionPage());

        pageContext.setAttribute(LAYOUT_CONTEXT_KEY, context.previous);

        if (context.previous == null) {
            pageContext.popBody();
        }
        else {
            context.previous.next = null;
            context.previous = null;
        }

        return context;
    }

    private LayoutContext previous, next;
    private LayoutRenderTag renderTag;
    private LayoutWriter out;
    private Map<String,LayoutComponentRenderer> components = new HashMap<String,LayoutComponentRenderer>();
    private Map<String,Object> parameters = new HashMap<String,Object>();
    private String renderPage, component;
    private List<String> componentPath;
    private boolean componentRenderPhase, rendered;

    /**
     * A new context may be created only by a {@link LayoutRenderTag}. The tag provides all the
     * information necessary to initialize the context.
     * 
     * @param renderTag The tag that is beginning a new layout render process.
     */
    public LayoutContext(LayoutRenderTag renderTag) {
        this.renderTag = renderTag;
        this.renderPage = renderTag.getCurrentPagePath();

        LinkedList<String> path = null;
        for (LayoutTag parent = renderTag.getLayoutParent(); parent instanceof LayoutComponentTag;) {
            if (path == null)
                path = new LinkedList<String>();

            path.addFirst(((LayoutComponentTag) parent).getName());

            parent = parent.getLayoutParent();
            parent = parent instanceof LayoutRenderTag ? parent.getLayoutParent() : null;
        }

        if (path != null) {
            this.componentPath = Collections.unmodifiableList(path);
            log.debug("Path is ", this.componentPath);
        }
    }

    /** Get the previous layout context from the stack. */
    public LayoutContext getPrevious() { return previous; }

    /** Get the next layout context from the stack. */
    public LayoutContext getNext() { return next; }

    /** Get the first context in the list. */
    public LayoutContext getFirst() {
        for (LayoutContext c = this;; c = c.getPrevious()) {
            if (c.getPrevious() == null)
                return c;
        }
    }

    /** Get the last context in the list. */
    public LayoutContext getLast() {
        for (LayoutContext c = this;; c = c.getNext()) {
            if (c.getNext() == null)
                return c;
        }
    }

    /**
     * <p>
     * Called when a layout tag needs to execute a page in order to execute another layout tag.
     * Special handling is implemented to ensure the included page is aware of the current layout
     * context while also ensuring that pages included by other means (e.g., {@code jsp:include} or
     * {@code c:import}) are <em>not</em> aware of the current layout context.
     * </p>
     * <p>
     * This method calls {@link PageContext#include(String, boolean)} with {@code false} as the
     * second parameter so that the response is not flushed before the include request executes.
     * </p>
     */
    public void doInclude(PageContext pageContext, String relativeUrlPath) throws ServletException,
            IOException {
        try {
            pageContext.getRequest().setAttribute(LAYOUT_CONTEXT_KEY, this);
            pageContext.include(relativeUrlPath, false);
        }
        finally {
            pageContext.getRequest().removeAttribute(LAYOUT_CONTEXT_KEY);
        }
    }

    /** Get the render tag that created this context. */
    public LayoutRenderTag getRenderTag() { return renderTag; }

    /**
     * Gets the Map of overridden components. Will return an empty Map if no components were
     * overridden.
     */
    public Map<String,LayoutComponentRenderer> getComponents() { return components; }

    /** Gets the Map of parameters.  Will return an empty Map if none were provided. */
    public Map<String,Object> getParameters() { return parameters; }

    /** Returns true if the layout has been rendered, false otherwise. */
    public boolean isRendered() { return rendered; }

    /** False initially, should be set to true when the layout is actually rendered. */
    public void setRendered(final boolean rendered) { this.rendered = rendered; }

    /** Get the path to the page that contains the {@link LayoutRenderTag} that created this context. */
    public String getRenderPage() { return renderPage; }

    /** Get the path to the page that contains the {@link LayoutDefinitionTag} referenced by the render tag. */
    public String getDefinitionPage() { return getRenderTag().getName(); }

    /** True if the intention of the current page execution is solely to render a component. */
    public boolean isComponentRenderPhase() { return componentRenderPhase; }

    /** Set the flag that indicates that the coming execution phase is solely to render a component. */ 
    public void setComponentRenderPhase(boolean b) { this.componentRenderPhase = b; } 

    /** Get the name of the component to be rendered during the current phase of execution. */
    public String getComponent() { return component; }

    /** Set the name of the component to be rendered during the current phase of execution. */
    public void setComponent(String component) { this.component = component; }

    /**
     * Get the list of components in the render page that must execute so that the render tag that
     * created this context can execute.
     */
    public List<String> getComponentPath() { return componentPath; }

    /** Get the layout writer to which the layout is rendered. */
    public LayoutWriter getOut() { return out; }

    /** To String implementation the parameters, and the component names. */
    @Override
    public String toString() {
        return "LayoutContext{" +
                "component names=" + components.keySet() +
                ", parameters=" + parameters +
                '}';
    }
}
