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
        Object value = getElAttributes().get("value");
        Object originalSelected = getElAttributes().remove("selected");

        InputSelectTag selectTag = getParentTag(InputSelectTag.class);
        if (selectTag.isOptionSelected(value, originalSelected != null)) {
            getElAttributes().put("selected", "selected");
        }

        try {
            Object originalLabel = getElAttributes().remove("label");
            Object label         = originalLabel;

            if (getBodyContent() != null) {
                label = getBodyContent().getString();
            }

            writeOpenTag(getPageContext().getOut(), "option");
            if (label != null) {
                getPageContext().getOut().write(label.toString());
            }
            writeCloseTag(getPageContext().getOut(), "option");

            getElAttributes().clear();
        }
        catch (IOException ioe) {
            throw new JspException("IOException in InputOptionTag.doEndTag().", ioe);
        }

        return EVAL_PAGE;
    }
}
