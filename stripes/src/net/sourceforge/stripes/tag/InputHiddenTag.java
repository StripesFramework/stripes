package net.sourceforge.stripes.tag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTag;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA. User: tfenne Date: Jun 24, 2005 Time: 5:31:33 PM To change this
 * template use File | Settings | File Templates.
 */
public class InputHiddenTag extends InputTagSupport implements BodyTag {

    public int doStartTag() throws JspException {
        evaluateExpressions();
        getElAttributes().put("type", "hidden");
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
        Object override = getOverrideValueOrValues();
        Object originalValue = getElAttributes().get("value");
        Object valueWereGoingToUse = "";

        // Figure out where to pull the value from
        if (override != null) {
            valueWereGoingToUse = override;
        }
        else if (body != null) {
            valueWereGoingToUse = body;
        }
        else {
            valueWereGoingToUse = originalValue;
        }

        // Figure out how many times to write it out
        if (valueWereGoingToUse != null) {
            if (valueWereGoingToUse.getClass().isArray()) {
                for (Object value : (Object[]) valueWereGoingToUse) {
                    getElAttributes().put("value", value.toString());
                    writeSingletonTag(getPageContext().getOut(), "input");
                }
            }
            else if (valueWereGoingToUse instanceof Collection) {
                for (Object value : (Collection) valueWereGoingToUse) {
                    getElAttributes().put("value", value.toString());
                    writeSingletonTag(getPageContext().getOut(), "input");
                }
            }
            else {
                getElAttributes().put("value", valueWereGoingToUse.toString());
                writeSingletonTag(getPageContext().getOut(), "input");
            }
        }

        // Clear out the elAttributes just in case the tag gets reused
        getElAttributes().clear();

        return EVAL_PAGE;
    }
}
