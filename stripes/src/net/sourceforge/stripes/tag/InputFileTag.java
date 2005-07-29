package net.sourceforge.stripes.tag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

/**
 * Created by IntelliJ IDEA. User: tfenne Date: Jun 24, 2005 Time: 5:31:33 PM To change this
 * template use File | Settings | File Templates.
 */
public class InputFileTag extends InputTagSupport implements Tag {

    public void setAccept(String accept) { set("accept", accept); }
    public String getAccept() { return get("accept"); }

    public int doStartTag() throws JspException {
        evaluateExpressions();
        getElAttributes().put("type", "file");

        // Make sure the form is setup to do file uploads
        FormTag form = getParentFormTag();
        form.setMethod("post");
        form.setEnctype("multipart/form-data");

        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        writeSingletonTag(getPageContext().getOut(), "input");
        getElAttributes().clear();
        return EVAL_PAGE;
    }
}
