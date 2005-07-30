package net.sourceforge.stripes.tag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTag;

/**
 * Created by IntelliJ IDEA. User: tfenne Date: Jun 24, 2005 Time: 5:31:33 PM To change this
 * template use File | Settings | File Templates.
 */
public class InputSubmitTag extends InputTagSupport implements BodyTag {

    public int doStartTag() throws JspException {
        evaluateExpressions();
        getElAttributes().put("type", "submit");
        return EVAL_BODY_BUFFERED;
    }

    public void doInitBody() throws JspException {

    }

    public int doAfterBody() throws JspException {
        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        // Find out if we have a value from the PopulationStrategy
        String body = getBodyContentAsString();
        String localizedValue = getLocalizedFieldName();

        // Figure out where to pull the value from
        if (localizedValue != null) {
            getElAttributes().put("value", localizedValue);
        }
        else if (body != null) {
            getElAttributes().put("value", body);
        }

        writeSingletonTag(getPageContext().getOut(), "input");

        // Restore the original state before we mucked with it
        getElAttributes().clear();

        return EVAL_PAGE;
    }
}
