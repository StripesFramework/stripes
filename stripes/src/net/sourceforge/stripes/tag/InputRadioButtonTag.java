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
        getElAttributes().put("type", "radio");
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
        Object originalChecked = getElAttributes().remove("checked");
        Object checked = null;

        // Decide which source to pull from
        if (override != null) {
            checked = override;
        }
        else if (body != null) {
            checked = body;
        }
        else {
            checked = originalChecked;
        }

        // Now if the "checked" value matches this tags value, check it!
        Object value = getElAttributes().get("value");
        if (checked != null && value != null && checked.toString().equals(value.toString())) {
            getElAttributes().put("checked", "checked");
        }

        writeSingletonTag(getPageContext().getOut(), "input");

        // Restore the state of the tag to before we mucked with it
        getElAttributes().clear();

        return EVAL_PAGE;
    }

}
