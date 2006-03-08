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
package net.sourceforge.stripes.tag.layout;

import net.sourceforge.stripes.tag.StripesTagSupport;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.BodyContent;

/**
 * Defines a component in a layout. Used both to define the components in a layout definition
 * and to provide overriden component definitions during a layout rendering request.
 *
 * @author Tim Fennell
 * @since Stripes 1.1
 */
public class LayoutComponentTag extends StripesTagSupport implements BodyTag {
    private String name;
    private BodyContent bodyContent;
    private LayoutDefinitionTag definitionTag;
    private LayoutRenderTag renderTag;

    /** Gets the name of the component. */
    public String getName() { return name; }

    /** Sets the name of the component. */
    public void setName(String name) { this.name = name; }

    /** Save the body content output by the tag. */
    public void setBodyContent(BodyContent bodyContent) {
        this.bodyContent = bodyContent;
    }

    /**
     * Behaviour varies depending on whether the tag is nested inside a LayoutRenderTag or a
     * LayoutDefinitionTag.  In the first case it will always render it's output to a buffer so that
     * it can be provided to the render tag.  In the second case, checks to see if the component
     * has been overridden.  If so, does nothing, else writes its content to the output stream.
     *
     * @return EVAL_BODY_BUFFERED, EVAL_BODY_INCLUDE or SKIP_BODY as described above.
     */
    public int doStartTag() throws JspException {
        this.definitionTag = getParentTag(LayoutDefinitionTag.class);
        this.renderTag = getParentTag(LayoutRenderTag.class);

        if (this.renderTag != null) {
            return EVAL_BODY_BUFFERED;
        }
        else if (this.definitionTag.permissionToRender(this.name)) {
            return EVAL_BODY_INCLUDE;
        }
        else {
            return SKIP_BODY;
        }
    }

    /** Does nothing. */
    public void doInitBody() throws JspException { /* Do Nothing */ }

    /**
     * Does nothing.
     * @return SKIP_BODY in all cases.
     */
    public int doAfterBody() throws JspException { return SKIP_BODY; }

    /**
     * If the tag is nested in a LayoutRenderTag, provides the tag with the generated contents.
     * Otherwise, does nothing.
     *
     * @return EVAL_PAGE in all cases.
     */
    public int doEndTag() throws JspException {
        if (this.renderTag != null && this.bodyContent != null) {
            this.renderTag.addComponent(this.name, this.bodyContent.getString());
        }

        // Clean up in case the tag gets pooled
        this.bodyContent = null;
        this.definitionTag = null;
        this.renderTag = null;


        return EVAL_PAGE;
    }
}
