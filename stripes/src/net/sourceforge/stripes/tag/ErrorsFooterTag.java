package net.sourceforge.stripes.tag;

import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.JspException;

/**
 * Can be used within a stripes:errors tag to show a footer on an error list.
 * The contents of this tag will only be displayed on the last iteration of an
 * errors list.
 *
 * @author Greg Hinkle
 */
public class ErrorsFooterTag extends HtmlTagSupport implements Tag {

    public int doStartTag() throws JspException {
        ErrorsTag errorsTag = getParentTag(ErrorsTag.class);

        if (errorsTag.isLast())
            return EVAL_BODY_INCLUDE;
        else
            return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        return EVAL_PAGE;
    }

}
