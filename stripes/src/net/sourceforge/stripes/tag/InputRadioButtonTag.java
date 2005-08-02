package net.sourceforge.stripes.tag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTag;

/**
 * <p>Generates {@literal <input type="radio" value="foo"/>} HTML tags based on the attribute set
 * on the tag and the state of the form. Since a single radio button widget on a HTML page can
 * have only a single value, the value tag attribute must always resolve to a String (though this
 * is somewhat lax since the EL will coerce almost anything to a String). Similarly since radio
 * button sets can have only a single selected value at a time the checked attribute of the tag
 * must also be a String.</p>
 *
 * <p>Radio buttons perform automatic (re-)population of state.  They prefer, in order, the value
 * in the HttpServletRequest, the valuee in the ActionBean and lastly the valuee set using
 * checked="" on the page.  If the value of the current radio button matches the checked value
 * from the preferred source then the attribute checked="checked" will be written in the HTML
 * tag.</p>
 *
 * <p>The tag may include a body and if present the body is converted to a String and overrides the
 * <b>checked</b> tag attribute.</p>
 *
 * @author Tim Fennell
 */
public class InputRadioButtonTag extends InputTagSupport implements BodyTag {
    private String checked;

    /** Basic constructor that sets the input tag's type attribute to "radio". */
    public InputRadioButtonTag() {
        super();
        getAttributes().put("type", "radio");
    }

    /**
     * Sets the value amongst a set of radio buttons, that should be "checked" by default.
     * @param checked the default value for a set of radio buttons
     */
    public void setChecked(String checked) { this.checked = checked; }

    /** Returns the value set with setChecked(). */
    public String getChecked() { return this.checked; }

    /** Sets the String value of this individual checkbox. */
    public void setValue(String value) { set("value", value); }

    /** Returns the value set with setValue() */
    public String getValue() { return get("value"); }

    /**
     * Sets the input tag type to "radio".
     * @return EVAL_BODY_BUFFERED in all cases.
     */
    public int doStartInputTag() throws JspException {
        return EVAL_BODY_BUFFERED;
    }

    /** Does nothing. */
    public void doInitBody() throws JspException { }

    /**
     * Does nothing.
     * @return SKIP_BODY in all cases.
     */
    public int doAfterBody() throws JspException {
        return SKIP_BODY;
    }

    /**
     * Determines the state of the set of radio buttons and then writes the radio button to the
     * output stream with checked="checked" or not as appropriate.
     *
     * @return EVAL_PAGE in all cases.
     * @throws JspException if the parent form tag cannot be found, or output cannot be written.
     */
    public int doEndInputTag() throws JspException {
        // Find out if we have a value from the PopulationStrategy
        Object override     = getSingleOverrideValue();
        String body         = getBodyContentAsString();
        Object checkedOnTag = this.checked;
        Object actualChecked = null;

        // Decide which source to pull from
        if (override != null) {
            actualChecked = override;
        }
        else if (body != null) {
            actualChecked = body;
        }
        else {
            actualChecked = checkedOnTag;
        }

        // Now if the "checked" value matches this tags value, check it!
        String value = getValue();
        if (actualChecked != null && value != null && value.equals(actualChecked.toString())) {
            getAttributes().put("checked", "checked");
        }

        writeSingletonTag(getPageContext().getOut(), "input");

        // Restore the state of the tag to before we mucked with it
        getAttributes().remove("checked");

        return EVAL_PAGE;
    }
}
