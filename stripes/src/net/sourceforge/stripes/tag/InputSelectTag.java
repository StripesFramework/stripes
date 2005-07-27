package net.sourceforge.stripes.tag;

import net.sourceforge.stripes.exception.StripesJspException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTag;

/**
 *
 *
 */
public class InputSelectTag extends InputTagSupport implements BodyTag {
    private Object selectedValueOrValues;

    /** Sets the HTML attribute &quot;multiple&quot;. **/
    public void setMultiple(String multiple) { set("multiple", multiple); }

    /** Gets the HTML attribute &quot;multiple&quot;. **/
    public String getMultiple() { return get("multiple"); }

    /**
     * Overrides setValue() in InputTagSupport to store the value attribute in an instance variable
     * since it is used to determine which options are checked.
     * @param value
     */
    public void setValue(String value) {
        this.selectedValueOrValues = value;
    }

    /**
     * Returns the String value set by the tag attribute on the JSP, or the override value set in
     * the HttpRequest, or the override value bound to the ActionBean object in scope.
     *
     * @return Object one of <em>String</em> if the value comes from the tag on the JSP,
     *         <em>String[]</em> if the value(s) comes from the HttpServletRequest, or the type
     *         of the field on the ActionBean which could be <em>Collection</em>, <em>Object[]</em>
     *         or <em>Object</em>.
     */
    public Object getSelectedValueOrValues() {
        return this.selectedValueOrValues;
    }

    /**
     * Checks to see if the option value should be rendered as selected or not.  Consults with the
     * override values on the form submission or backing form, if there is one.  If there is no
     * form object present, then return the values of <em>selectedOnPage</em> supplied by the
     * option.
     *
     * @param optionValue the value of the option under consideration
     * @param selectedOnPage true if the page contains selected=... and false otherwise
     */
    public boolean isOptionSelected(String optionValue, boolean selectedOnPage)
    throws StripesJspException {
        if (getParentFormTag().getActionBean() != null) {
            return isItemSelected(optionValue, this.selectedValueOrValues);
        }
        else {
            return selectedOnPage;
        }
    }

    /** Writes out the opening &lt;select&gt; tag and includes the body. */
    public int doStartTag() throws JspException {
        evaluateExpressions();
        writeOpenTag(getPageContext().getOut(), "select");
        Object override = getOverrideValueOrValues();
        if (override != null) {
            this.selectedValueOrValues = override;
        }
        return EVAL_BODY_INCLUDE;
    }

    /** Does nothing because the body is always included. */
    public void doInitBody() throws JspException { }

    /** Just returns SKIP_BODY to move on to processing the rest of the page. */
    public int doAfterBody() throws JspException {
        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        writeCloseTag(getPageContext().getOut(), "select");
        return SKIP_BODY;
    }

    public void release() {
        this.selectedValueOrValues = null;
        super.release();
    }
}
