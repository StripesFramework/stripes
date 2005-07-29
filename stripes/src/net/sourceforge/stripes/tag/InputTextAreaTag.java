package net.sourceforge.stripes.tag;

import net.sourceforge.stripes.exception.StripesJspException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTag;
import java.io.IOException;

/**
 *
 */
public class InputTextAreaTag extends InputTagSupport implements BodyTag {
    public int doStartTag() throws JspException {
        evaluateExpressions();
        return EVAL_BODY_BUFFERED;
    }

    public void doInitBody() throws JspException {
        // Do Nothing
    }

    /**
     * Examines the contents of the body, and if a non-null, non-empty body was provided it will
     * be used as the value of this textarea.  Otherwise the value attribute will be used.
     */
    public int doAfterBody() throws JspException {
        return SKIP_BODY;
    }


    /**
     * Writes out the textarea tag including a body based off either the value attribute, or the
     * body of the jsp tag.
     */
    public int doEndTag() throws JspException {
        try {
            // Find out if we have a value from the PopulationStrategy
            Object override = getSingleOverrideValue();
            String body     = getBodyContentAsString();
            Object originalValue = getElAttributes().remove("value");
            Object value = null;

            // Figure out which source to pull from
            if (override != null) {
                value = override;
            }
            else if (body != null) {
                value = body;
            }
            else {
                value = originalValue;
            }

            writeOpenTag(getPageContext().getOut(), "textarea");

            // Write out the contents of the text area
            if (value != null) {
                getPageContext().getOut().write(value.toString());
            }

            writeCloseTag(getPageContext().getOut(), "textarea");

            // Restore the original state of the tag before we mucked with it
            getElAttributes().clear();

            return EVAL_PAGE;
        }
        catch (IOException ioe) {
            throw new StripesJspException("Could not write out textarea tag.", ioe);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Additional attribute getters and setters.
    ///////////////////////////////////////////////////////////////////////////
    public void setCols(String cols) { set("cols", cols); }
    public String getCols() { return get("cols"); }

    public void setRows(String rows) { set("rows", rows); }
    public String getRows() { return get("rows"); }

    public void setReadonly(String readonly) { set("readonly", readonly); }
    public String getReadonly() { return get("readonly"); }

}
