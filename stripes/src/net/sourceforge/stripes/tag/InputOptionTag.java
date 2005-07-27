package net.sourceforge.stripes.tag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTag;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA. User: tfenne Date: Jun 29, 2005 Time: 7:52:38 AM To change this
 * template use File | Settings | File Templates.
 */
public class InputOptionTag extends InputTagSupport implements BodyTag {
    public void setLabel(String label) { set("label", label); }
    public String getLabel() { return get("label"); }

    public void setSelected(String selected) { set("selected", selected); }
    public String getSelected() { return get("selected"); }


    public int doStartTag() throws JspException {
        evaluateExpressions();
        return EVAL_BODY_BUFFERED;
    }

    public void doInitBody() throws JspException {
    }

    public int doAfterBody() throws JspException {
        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        String value = getValue();
        String originalSelected = getAttributes().remove("selected");  // need to reset this later

        InputSelectTag selectTag = getParentTag(InputSelectTag.class);
        if (selectTag.isOptionSelected(value, originalSelected != null)) {
            setSelected("selected");
        }

        try {
            String originalLabel = getAttributes().remove("label");
            String label = originalLabel;

            if (getBodyContent() != null) {
                label = getBodyContent().getString();
            }

            writeOpenTag(getPageContext().getOut(), "option");
            getPageContext().getOut().write(label);
            writeCloseTag(getPageContext().getOut(), "option");

            // Done in case "optimizing" tag poolers don't call release() and setXXX() again
            // for other tags on the page
            if (originalSelected != null) {
                setSelected(originalSelected);
            }
            else {
                getAttributes().remove("selected");
            }

            if (originalLabel != null) {
                setLabel(label);
            }
        }
        catch (IOException ioe) {
            throw new JspException("IOException in InputOptionTag.doEndTag().", ioe);
        }

        return EVAL_PAGE;
    }
}
