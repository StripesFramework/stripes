package net.sourceforge.stripes.tag;

import net.sourceforge.stripes.controller.StripesConstants;
import net.sourceforge.stripes.exception.StripesJspException;
import net.sourceforge.stripes.action.ActionBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTag;
import java.io.IOException;

/**
 * <p>Form tag for use with the Stripes framework.  Supports all of the HTML attributes applicable
 * to the form tag, with one exception: due to Java method naming restrictions accept-charset is
 * specified as acceptcharset (but will be rendered correctly in the output HTML).</p>
 *
 * @author Tim Fennell
 */
public class FormTag extends HtmlTagSupport implements BodyTag {

    ////////////////////////////////////////////////////////////
    // Additional attributes specific to the form tag
    ////////////////////////////////////////////////////////////
    public void   setAccept(String accept) { set("accept", accept); }
    public String getAccept() { return get("accept"); }

    public void   setAcceptCharset(String acceptCharset) { set("accept-charset", acceptCharset); }
    public String getAcceptCharset() { return get("accept-charset"); }

    /**
     * Sets the action for the form.  If the form action begins with a slash, the context path of
     * the web application will get prepended to the action before it is set. In general actions
     * should be specified as &quot;absolute&quot; paths within the web application, therefore
     * allowing them to function correctly regardless of the address currently shown in the
     * browser&apos; address bar.
     *
     * @param action the action path, relative to the root of the web application
     */
    public void setAction(String action) {
        if (action.startsWith("/")) {
            HttpServletRequest request = (HttpServletRequest) getPageContext().getRequest();
            String contextPath = request.getContextPath();

            if (contextPath != null) {
                action = contextPath + action;
            }
        }

        set("action", action);
    }

    public String getAction() { return get("action"); }

    public void   setEnctype(String enctype) { set("enctype", enctype); }
    public String getEnctype() { return get("enctype"); };

    public void   setMethod(String method) { set("method", method); }
    public String getMethod() { return get("method"); }

    public void   setName(String name) { set("name", name); }
    public String getName() { return get("name"); }

    public void   setTarget(String target) { set("target", target); }
    public String getTarget() { return get("target"); }

    ////////////////////////////////////////////////////////////
    // TAG methods
    ////////////////////////////////////////////////////////////

    /**
     * Does nothing except return EVAL_BODY_BUFFERED.  Everything of interest happens in doEndTag.
     */
    public int doStartTag() throws JspException {
        evaluateExpressions();
        return EVAL_BODY_BUFFERED;
    }

    /** Don't need to do anything here. */
    public void doInitBody() throws JspException { }

    /** Just returns SKIP_BODY so that the body is included only once. */
    public int doAfterBody() throws JspException {
        return SKIP_BODY;
    }

    /**
     * Writes things out in the following order:
     * <ul>
     *   <li>The form open tag</li>
     *   <li>Hidden fields for the form name and source page</li>
     *   <li>The buffered body content</li>
     *   <li>The form close tag</li>
     * </ul>
     *
     * <p>All of this is done in doEndTag to allow form elements to modify the form tag itself if
     * necessary.  A prime example of this is the InputFileTag, which needs to ensure that the form
     * method is POST and the enctype is correct.</p>
     */
    public int doEndTag() throws JspException {
        try {
            JspWriter out = getPageContext().getOut();
            writeOpenTag(out, "form");

            // Write out a hidden field with the form name
            out.write("<input type=\"hidden\" name=\"");
            out.write(StripesConstants.URL_KEY_FORM_NAME);
            out.write("\" value=\"");
            out.write(getName());
            out.write("\"/>");

            // Write out a hiddien field with the name of the page in it
            out.write("<input type=\"hidden\" name=\"");
            out.write(StripesConstants.URL_KEY_SOURCE_PAGE);
            out.write("\" value=\"");
            HttpServletRequest request = (HttpServletRequest) getPageContext().getRequest();
            out.write( request.getServletPath());
            out.write("\"/>");

            getBodyContent().writeOut( getPageContext().getOut() );
            writeCloseTag(getPageContext().getOut(), "form");
        }
        catch (IOException ioe) {
            throw new StripesJspException("IOException in FormTag.doEndTag().", ioe);
        }

        return EVAL_PAGE;
    }

    /**
     * Fetches the ActionBean associated with the form if one is present.  An ActionBean will not
     * be created (and hence not present) by default.  An ActionBean will only be present if the
     * current request got bound to the same ActionBean as the current form uses.  E.g. if we are
     * re-showing the page as the result of an error, or the same ActionBean is used for a
     * &quot;pre-Action&quot; and the &quot;post-action&quot;.
     *
     * @return ActionBean the ActionBean bound to the form if there is one
     */
    protected ActionBean getActionBean() {
        return (ActionBean) getPageContext().getRequest().getAttribute(getName());
    }
}
