package net.sourceforge.stripes.tag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTag;

/**
 * Created by IntelliJ IDEA. User: tfenne Date: Jun 24, 2005 Time: 5:31:33 PM To change this
 * template use File | Settings | File Templates.
 */
public class InputTextTag extends InputTagSupport implements BodyTag {
    public void setMaxlength(String maxlength) { set("maxlength", maxlength); }
    public String getMaxlength() { return get("maxlength"); }

    public void setReadonly(String readonly) { set("readonly", readonly); }
    public String getReadonly() { return get("readonly"); }

    public int doStartTag() throws JspException {
        evaluateExpressions();
        set("type", "text");
        return EVAL_BODY_BUFFERED;
    }

    public void doInitBody() throws JspException {

    }

    public int doAfterBody() throws JspException {
        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        // Find out if we have a value from the PopulationStrategy
        String body  = getBodyContentAsString();
        Object override = getSingleOverrideValue();
        String originalValue = getValue();

        // Figure out where to pull the value from
        if (override != null) {
            setValue(override.toString());
        }
        else if (body != null) {
            setValue(body);
        }

        writeSingletonTag(getPageContext().getOut(), "input");

        // Restore the original state before we mucked with it
        if (originalValue != null) {
            setValue(originalValue);
        }
        else {
            getAttributes().remove("value");
        }

        return EVAL_PAGE;
    }
}
