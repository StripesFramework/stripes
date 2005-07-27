package net.sourceforge.stripes.tag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTag;

/**
 * Created by IntelliJ IDEA. User: tfenne Date: Jun 26, 2005 Time: 3:14:35 PM To change this
 * template use File | Settings | File Templates.
 */
public class InputRadioButtonTag extends InputTagSupport implements BodyTag {
    public void setChecked(String checked) { set("checked", checked); }
    public String getChecked() { return get("checked"); }

    public int doStartTag() throws JspException {
        evaluateExpressions();
        set("type", "radio");
        return EVAL_BODY_BUFFERED;
    }

    public void doInitBody() throws JspException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public int doAfterBody() throws JspException {
        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        // Find out if we have a value from the PopulationStrategy
        Object override = getSingleOverrideValue();
        String body     = getBodyContentAsString();
        String originalChecked = getAttributes().remove("checked"); // need to restore this later
        String checked = null;

        // Decide which source to pull from
        if (override != null) {
            checked = override.toString();
        }
        else if (body != null) {
            checked = body;
        }
        else {
            checked = originalChecked;
        }

        // Now if the "checked" value matches this tags value, check it!
        if (checked != null && checked.equals(getValue())) {
            setChecked("checked");
        }

        writeSingletonTag(getPageContext().getOut(), "input");

        // Restore the state of the tag to before we mucked with it
        if (originalChecked != null) {
            setChecked(originalChecked);
        }
        else {
            getAttributes().remove("checked");
        }
        return EVAL_PAGE;
    }

}
