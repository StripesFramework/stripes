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

import net.sourceforge.stripes.exception.StripesJspException;
import net.sourceforge.stripes.util.HtmlUtil;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTag;
import java.io.IOException;

/**
 * <p>Tag that generates HTML form fields of type
 * {@literal <textarea name="foo"> ... </textarea>}, which can dynamically re-populate their
 * value. Textareas may have only a single value, whose default may be set using either the body
 * of the textarea, or using the value="" attribute of the tag. At runtime the contents of the
 * textarea are determined by looking for the first non-null value in the following list:</p>
 *
 * <ul>
 *   <il>A value with the same name in the HttpServletRequest</li>
 *   <il>A value on the ActionBean if an ActionBean instance is present</li>
 *   <il>The contents of the body of the textarea</li>
 *   <il>The value attribute of the tag</li>
 * </ul>
 *
 * @author Tim Fennell
 */
public class InputTextAreaTag extends InputTagSupport implements BodyTag {
    private Object value;

    /** Sets the default value of the textarea (if no body is present). */
    public void setValue(Object value) { this.value = value; }

    /** Returns the value set using setValue(). */
    public Object getValue() { return this.value; }


    /** Sets the HTML attribute of the same name. */
    public void setCols(String cols) { set("cols", cols); }
    /** Gets the HTML attribute of the same name. */
    public String getCols() { return get("cols"); }

    /** Sets the HTML attribute of the same name. */
    public void setRows(String rows) { set("rows", rows); }
    /** Gets the HTML attribute of the same name. */
    public String getRows() { return get("rows"); }

    /**
     * Does nothing.
     * @return EVAL_BODY_BUFFERED in all cases.
     */
    public int doStartInputTag() throws JspException {
        return EVAL_BODY_BUFFERED;
    }

    /** Does nothing. */
    public void doInitBody() throws JspException { }

    /**
     * Does nothing.
     * @return SKIP_BODY in all cases.
     */
    public int doAfterBody() throws JspException {
        return SKIP_BODY;
    }


    /**
     * Determines which source is applicable for the contents of the textarea and then writes
     * out the textarea tag including the body.
     *
     * @return EVAL_PAGE in all cases.
     * @throws JspException if the enclosing form tag cannot be found, or output cannot be written.
     */
    public int doEndInputTag() throws JspException {
        try {
            // Find out if we have a value from the PopulationStrategy
            Object value   = getSingleOverrideValue();

            writeOpenTag(getPageContext().getOut(), "textarea");

            // Write out the contents of the text area
            if (value != null) {
                getPageContext().getOut().write( HtmlUtil.encode(format(value)) );
            }

            writeCloseTag(getPageContext().getOut(), "textarea");

            return EVAL_PAGE;
        }
        catch (IOException ioe) {
            throw new StripesJspException("Could not write out textarea tag.", ioe);
        }
    }
}
