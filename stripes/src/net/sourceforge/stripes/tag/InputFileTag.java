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
    public int doStartTag() throws JspException {
        getAttributes().put("type", "file");

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
    public int doEndTag() throws JspException {
        writeSingletonTag(getPageContext().getOut(), "input");
        return EVAL_PAGE;
    }
}
