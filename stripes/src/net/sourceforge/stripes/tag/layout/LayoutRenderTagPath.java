/* Copyright 2012 Ben Gunter
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
import java.util.List;

import javax.servlet.jsp.PageContext;

import net.sourceforge.stripes.exception.StripesJspException;

/**
 * Uniquely identifies a {@link LayoutRenderTag} within a page. Within a single page, any number of
 * render tags can be accessible via the same "path," where a path consists of zero or more
 * component tags that are parents of the render tag. This class helps to distinguish between
 * multiple render tags with the same component path by assigning sequential indexes to them.
 * 
 * @author Ben Gunter
 * @since Stripes 1.5.7
 */
public class LayoutRenderTagPath {
    private List<String> componentPath;
    private int index;

    /** Construct a new instance to identify the specified tag. */
    public LayoutRenderTagPath(LayoutRenderTag tag) {
        this.componentPath = calculateComponentPath(tag);
        this.index = incrementIndex(tag.getPageContext());
    }

    /**
     * Calculate the path to a render tag. The path is a list of names of components that must
     * execute, in order, so that the specified render tag can execute.
     * 
     * @param tag The render tag.
     * @return A list of component names or null if the render tag is not a child of a component.
     */
    protected List<String> calculateComponentPath(LayoutRenderTag tag) {
        LinkedList<String> path = null;

        for (LayoutTag parent = tag.getLayoutParent(); parent instanceof LayoutComponentTag;) {
            if (path == null)
                path = new LinkedList<String>();

            path.addFirst(((LayoutComponentTag) parent).getName());

            parent = parent.getLayoutParent();
            parent = parent instanceof LayoutRenderTag ? parent.getLayoutParent() : null;
        }

        return path;
    }

    /** Get the next index for this path from the specified page context. */
    protected int incrementIndex(PageContext pageContext) {
        String key = getClass().getName() + "#" + toStringWithoutIndex();
        Integer index = (Integer) pageContext.getAttribute(key);
        if (index == null)
            index = 0;
        else
            ++index;
        pageContext.setAttribute(key, index);
        return index;
    }

    /** Get the names of the {@link LayoutComponentTag}s that are parent tags of the render tag. */
    public List<String> getComponentPath() {
        return componentPath;
    }

    /** Get the index (zero-based) of the combined render page and component path within the page. */
    public int getIndex() {
        return index;
    }

    /**
     * True if the specified tag is a component that must execute so that the current component tag
     * can execute. That is, this tag is a parent of the current component.
     * 
     * @param tag The tag to check to see if it is part of this path.
     * @throws StripesJspException if thrown by {@link #getContext()}.
     */
    public boolean isPathComponent(LayoutComponentTag tag) throws StripesJspException {
        List<String> path = getComponentPath();
        return path == null ? false : isPathComponent(tag, path.iterator());
    }

    /**
     * Recursive method called from {@link #isPathComponent()} that returns true if the specified
     * tag's name is present in the component path iterator at the same position where this tag
     * occurs in the render/component tag tree. For example, if the path iterator contains the
     * component names {@code ["foo", "bar"]} then this method will return true if the tag's name is
     * {@code "bar"} and it is a child of a render tag that is a child of a component tag whose name
     * is {@code "foo"}.
     * 
     * @param tag The tag to check
     * @param path The path to the check the tag against
     * @return
     */
    protected boolean isPathComponent(LayoutComponentTag tag, Iterator<String> path) {
        LayoutTag parent = tag.getLayoutParent();
        if (parent instanceof LayoutRenderTag) {
            parent = parent.getLayoutParent();
            if (!(parent instanceof LayoutComponentTag) || parent instanceof LayoutComponentTag
                    && isPathComponent((LayoutComponentTag) parent, path) && path.hasNext()) {
                return tag.getName().equals(path.next());
            }
        }

        return false;
    }

    @Override
    public boolean equals(Object obj) {
        LayoutRenderTagPath that = (LayoutRenderTagPath) obj;
        if (index != that.index)
            return false;
        if (componentPath == that.componentPath)
            return true;
        if (componentPath == null || that.componentPath == null)
            return false;
        return componentPath.equals(that.componentPath);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return toStringWithoutIndex() + '[' + index + ']';
    }

    /** Get a string representation of this instance without including the index. */
    public String toStringWithoutIndex() {
        if (componentPath == null)
            return "";

        StringBuilder s = new StringBuilder();
        for (Iterator<String> it = componentPath.iterator(); it.hasNext();) {
            s.append(it.next());
            if (it.hasNext())
                s.append('>');
        }
        return s.toString();
    }
}
