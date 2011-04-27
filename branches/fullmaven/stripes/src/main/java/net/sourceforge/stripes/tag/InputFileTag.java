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
package net.sourceforge.stripes.tag;

import javax.servlet.jsp.JspException;

/**
 * <p>Tag that generates HTML form fields of type {@literal <input type="file" ... />}.  The only
 * functionality provided above and beyond a straight HTML input tag is that the tag will find
 * its enclosing form tag and ensure that the for is set to POST instead of GET, and that the
 * encoding type of the form is properly set to multipart/form-data as both these settings are
 * necessary to correctly perform file uploads.</p>
 *
 * <p>Does not perform repopulation because default values for {@literal <input type="file/>} are
 * not allowed by the HTML specification.  One can only imagine this is because a malicious page
 * author could steal a user's files by defaulting the value and using JavaScript to auto-submit
 * forms!  As a result the tag does not accept a body because it would have no use for any
 * generated content.</p>
 *
 * @author Tim Fennell
 */
public class InputFileTag extends InputTagSupport {

    /** Basic constructor that sets the input tag's type attribute to "file". */
    public InputFileTag() {
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
    @Override
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
    @Override
    public int doEndInputTag() throws JspException {
        writeSingletonTag(getPageContext().getOut(), "input");
        return EVAL_PAGE;
    }
}
