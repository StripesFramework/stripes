/* Copyright (C) 2005 Tim Fennell
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the license with this software. If not,
 * it can be found online at http://www.fsf.org/licensing/licenses/lgpl.html
 */
package net.sourceforge.stripes.tag;

import net.sourceforge.stripes.controller.StripesConstants;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import java.util.Stack;
import java.util.ListIterator;
import java.util.Map;
import java.util.HashMap;

/**
 * A very basic implementation of the Tag interface that is similar in manner to the standard
 * TagSupport class, but with less clutter.
 *
 * @author Tim Fennell
 */
public abstract class StripesTagSupport implements Tag {
    /** Storage for a PageContext during evaluation. */
    protected PageContext pageContext;
    /** Storage for the parent tag of this tag. */
    protected Tag parentTag;

    /**
     * A map that is used to store values of page context attributes before they were
     * replaced with other values for the body of the tag.
     */
    private Map<String,Object> previousAttributeValues;

    /** Called by the Servlet container to set the page context on the tag. */
    public void setPageContext(PageContext pageContext) {
        this.pageContext = pageContext;
    }

    /** Retrieves the pageContext handed to the tag by the container. */
    public PageContext getPageContext() {
        return this.pageContext;
    }

    /** From the Tag interface - allows the container to set the parent tag on the JSP. */
    public void setParent(Tag tag) {
        this.parentTag = tag;
    }

    /** From the Tag interface - allows fetching the parent tag on the JSP. */
    public Tag getParent() {
        return this.parentTag;
    }

    /**
     * Abstract method from the Tag interface. Abstract because it seems to make the
     * child tags more readable if they implement their own do() methods, even when
     * they just return one of the constants and do nothing else.
     */
    public abstract int doStartTag() throws JspException;

    /**
     * Abstract method from the Tag interface. Abstract because it seems to make the
     * child tags more readable if they implement their own do() methods, even when
     * they just return one of the constants and do nothing else.
     */
    public abstract int doEndTag() throws JspException;

    /**
     * No-op implementation of release().
     */
    public void release() { }

    /**
     * Pushes new values for the attributes supplied into the page context, preserving
     * the old values so that they can be put back into page context end of the tag's
     * execution (usually in doEndTag).  If this method is called, the tag <b>must</b>
     * also call{@link #popPageContextAttributes()}.
     */
    public void pushPageContextAttributes(Map<String,Object> attributes) {
        this.previousAttributeValues = new HashMap<String,Object>();

        for (Map.Entry<String,Object> entry : attributes.entrySet()) {
            String name = entry.getKey();
            this.previousAttributeValues.put(name, pageContext.getAttribute(name));
            this.pageContext.setAttribute(name, entry.getValue());
        }
    }

    /**
     * Attempts to restore page context attributes to their state prior to a call to
     * pushPageContextAttributes(). Attributes that had values prior to the execution of
     * this tag have their values restored.  Attributes that did not have values
     * are removed from the page context.
     */
    public void popPageContextAttributes() {
        for (Map.Entry<String,Object> entry : this.previousAttributeValues.entrySet()) {
            if (entry.getValue() == null) {
                this.pageContext.removeAttribute(entry.getKey());
            }
            else {
                this.pageContext.setAttribute(entry.getKey(), entry.getValue());
            }
        }

        // Null out the map so erroneous values don't get picked up on tag pooling!
        this.previousAttributeValues = null;
    }


    /**
     * <p>Locates the enclosing tag of the type supplied.  If no enclosing tag of the type supplied
     * can be found anywhere in the ancestry of this tag, null is returned.</p>
     *
     * @return T Tag of the type supplied, or null if none can be found
     */
    protected <T extends Tag> T getParentTag(Class<T> tagType) {
        Tag parent = getParent();
        while (parent != null) {
            if (tagType.isAssignableFrom(parent.getClass())) {
                return (T) parent;
            }
            parent = parent.getParent();
        }

        // If we can't find it by the normal way, try our own tag stack!
        Stack<StripesTagSupport> stack = getTagStack();
        ListIterator<StripesTagSupport> iterator = stack.listIterator(stack.size());
        while (iterator.hasPrevious()) {
            StripesTagSupport tag = iterator.previous();
            if (tagType.isAssignableFrom(tag.getClass())) {
                return (T) tag;
            }
        }

        return null;
    }

    /**
     * Fetches a tag stack that is stored in the request. This tag stack is used to help
     * Stripes tags find one another when they are spread across multiple included JSPs
     * and/or tag files - situations in which the usual parent tag relationship fails.
     */
    protected Stack<StripesTagSupport> getTagStack() {
        Stack<StripesTagSupport> stack = (Stack<StripesTagSupport>)
                getPageContext().getRequest().getAttribute(StripesConstants.REQ_ATTR_TAG_STACK);

        if (stack == null) {
            stack = new Stack<StripesTagSupport>();
            getPageContext().getRequest().setAttribute(StripesConstants.REQ_ATTR_TAG_STACK, stack);
        }

        return stack;
    }
}
