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

import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.PageContext;

/**
 * Used to move contextual information about a layout rendering between a LayoutRenderTag and
 * a LayoutDefinitionTag. Holds the set of overridden components and any parameters provided
 * to the render tag.
 *
 * @author Tim Fennell
 * @since Stripes 1.1
 */
public class LayoutContext {
    /**
     * Prefix used to construct the request attribute name used to pass context from the
     * LayoutRenderTag to the LayoutDefinitionTag.
     */
    public static final String PREFIX = "stripes.layout.";

    /**
     * Look up the stack of layout contexts associated with the named layout in a JSP page context.
     * If {@code create} is true and no stack is found then one will be created and placed in the
     * page context.
     * 
     * @param pageContext The JSP page context to search for the layout context stack.
     * @param layoutName The name of the layout with which the contexts are associated.
     * @param create If true and no stack is found, then create and save a new stack.
     */
    @SuppressWarnings("unchecked")
    public static LinkedList<LayoutContext> findStack(PageContext pageContext, String layoutName,
            boolean create) {
        String key = PREFIX + layoutName;
        ServletRequest request = pageContext.getRequest();
        LinkedList<LayoutContext> stack = (LinkedList<LayoutContext>) request.getAttribute(key);
        if (create && stack == null) {
            stack = new LinkedList<LayoutContext>();
            request.setAttribute(key, stack);
        }
        return stack;
    }

    /**
     * Look up the current layout context associated with the named layout in a JSP page context.
     * 
     * @param pageContext The JSP page context to search for the layout context stack.
     * @param layoutName The name of the layout with which the contexts are associated.
     */
    public static LayoutContext find(PageContext pageContext, String layoutName) {
        LinkedList<LayoutContext> stack = findStack(pageContext, layoutName, true);
        return !stack.isEmpty() ? stack.getLast() : null;
    }

    /**
     * Remove the current layout context from the stack of layout contexts associated with the named
     * layout.
     * 
     * @param pageContext The JSP page context to search for the layout context stack.
     * @param layoutName The name of the layout with which the contexts are associated.
     * @return The layout context that was popped off the stack, or null if the stack was not found
     *         or was empty.
     */
    public static LayoutContext pop(PageContext pageContext, String layoutName) {
        LinkedList<LayoutContext> stack = findStack(pageContext, layoutName, false);
        return stack != null && !stack.isEmpty() ? stack.removeLast() : null;
    }

    private Map<String,LayoutComponentRenderer> components = new HashMap<String,LayoutComponentRenderer>();
    private Map<String,Object> parameters = new HashMap<String,Object>();
    private String renderPage, currentComponentName;
    private boolean rendered = false;

    /**
     * Gets the Map of overridden components.  Will return an empty Map if no components were
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

    /** Set the path to the page that contains the {@link LayoutRenderTag} that created this context. */
    public void setRenderPage(String layoutRenderer) { this.renderPage = layoutRenderer; }

    /** Get the name of the component to be rendered on this pass from {@link LayoutDefinitionTag}. */
    public String getCurrentComponentName() { return currentComponentName; }

    /** Set the name of the component to be rendered on this pass from {@link LayoutDefinitionTag}. */
    public void setCurrentComponentName(String currentComponentName) { this.currentComponentName = currentComponentName; }

    /** To String implementation the parameters, and the component names. */
    @Override
    public String toString() {
        return "LayoutContext{" +
                "component names=" + components.keySet() +
                ", parameters=" + parameters +
                '}';
    }
}
