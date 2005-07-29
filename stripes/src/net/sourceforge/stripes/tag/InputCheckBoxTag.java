package net.sourceforge.stripes.tag;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTag;

/**
 * Created by IntelliJ IDEA. User: tfenne Date: Jun 26, 2005 Time: 3:14:35 PM To change this
 * template use File | Settings | File Templates.
 */
public class InputCheckBoxTag extends InputTagSupport implements BodyTag {
    private Log log = LogFactory.getLog(InputCheckBoxTag.class);

    public void setChecked(String checked) { set("checked", checked); }
    public String getChecked() { return get("checked"); }

    public int doStartTag() throws JspException {
        evaluateExpressions();
        getElAttributes().put("type", "checkbox");
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
        Object override = getOverrideValueOrValues();
        String body     = getBodyContentAsString();
        String originalChecked = getAttributes().remove("checked");  // need to restore this later
        Object checked = null;

        // Figure out where to pull the default value from
        if (override != null) {
            checked = override;
        }
        else if (body != null) {
            checked = body;
        }
        else {
            checked = originalChecked;
        }

        // If the value of this checkbox is contained in the value or override value, check it
        if (isItemSelected(getValue(), checked)) {
            getElAttributes().put("checked", "checked");
        }

        writeSingletonTag(getPageContext().getOut(), "input");

        // Restore the tags state to before we mucked with it
        getElAttributes().clear();
        
        return EVAL_PAGE;
    }
}
