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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

/**
 * <p>Tag that generates HTML form fields of type {@literal <input type="file" ... />}.  The only
 * functionality provided above and beyond a straight HTML input tag is that the tag will find
 * its enclosing form tag and ensure that the for is set to POST instead of GET, and that the
 * encoding type of the form is properly set to multipart/form-data as both these settings are
 * necessary to correctly perform file uploads.</p>
 *
 * <p>Does not perform repopulation because default values for {@literal <input type="file/>} are
 * not allowed by the HTML specification.  One can only imagine this is because a malicous page
 * author could steal a user's files by defaulting the value and using JavaScript to auto-submit
 * forms!  As a result the tag does not accept a body because it would have no use for any
 * generated content.</p>
 *
 * @author Tim Fennell
 */
public class InputFileTag extends InputTagSupport implements Tag {

    /** Basic constructor that sets the input tag's type attribute to "file". */
    public InputFileTag() {
        super();
        getAttributes().put("type", "file");
    }

    /** Sets the content types accepted for files being uploaded. */
    public void setAccept(String accept) { set("accept", accept); }

    /** Returns the value, if any, set with setAccept(). */
    public String getAccept() { return get("accept"); }

    /**
     * Locates the parent tag and modifies it's method and enctype to be suitable for file upload.
     *
     * @return SKIP_BODY because the tag does not allow a body
     * @throws JspException if the enclosing form tag cannot be located
     */
    public int doStartInputTag() throws JspException {
        // Make sure the form is setup to do file uploads
        FormTag form = getParentFormTag();
        form.setMethod("post");
        form.setEnctype("multipart/form-data");

        return SKIP_BODY;
    }

    /**
     * Writes out a singleton tag representing the values stored on this tag instance.
     *
     * @return EVAL_PAGE is always returned
     * @throws JspException if a problem is encountered writing to the JSP page's output
     */
    public int doEndInputTag() throws JspException {
        writeSingletonTag(getPageContext().getOut(), "input");
        return EVAL_PAGE;
    }
}
