package net.sourceforge.stripes.tag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTag;

/**
 * <p>Tag that generates HTML form fields of type
 * {@literal <input type="text" name="foo" value="bar"/>}, which can dynamically re-populate their
 * value. Text tags may have only a single value, whose default may be set using either the body
 * of the tag, or using the value="" attribute of the tag. At runtime the contents of the
 * text field are determined by looking for the first non-null value in the following list:</p>
 *
 * <ul>
 *   <il>A value with the same name in the HttpServletRequest</li>
 *   <il>A value on the ActionBean if an ActionBean instance is present</li>
 *   <il>The contents of the body of the tag</li>
 *   <il>The value attribute of the tag</li>
 * </ul>
 *
 * @author Tim Fennell
 */
 public class InputTextTag extends InputTagSupport implements BodyTag {
    private String value;

    /** Basic constructor that sets the input tag's type attribute to "text". */
    public InputTextTag() {
        super();
        getAttributes().put("type", "text");
    }

    /** Sets the default value of the textarea (if no body is present). */
    public void setValue(String value) { this.value = value; }

    /** Returns the value set using setValue(). */
    public String getValue() { return this.value; }


    /** Sets the HTML attribute of the same name. */
    public void setMaxlength(String maxlength) { set("maxlength", maxlength); }
    /** Gets the HTML attribute of the same name. */
    public String getMaxlength() { return get("maxlength"); }

    /** Sets the HTML attribute of the same name. */
    public void setReadonly(String readonly) { set("readonly", readonly); }
    /** Gets the HTML attribute of the same name. */
    public String getReadonly() { return get("readonly"); }

    /**
     * Sets type input tags type to "text".
     * @return EVAL_BODY_BUFFERED in all cases.
     */
    public int doStartInputTag() throws JspException {
        getAttributes().put("type", "text");
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
     * Determines which source is applicable for the value of the text field and then writes
     * out the tag.
     *
     * @return EVAL_PAGE in all cases.
     * @throws JspException if the enclosing form tag cannot be found, or output cannot be written.
     */
    public int doEndInputTag() throws JspException {
        // Find out if we have a value from the PopulationStrategy
        String body     = getBodyContentAsString();
        Object override = getSingleOverrideValue();

        // Figure out where to pull the value from
        if (override != null) {
            getAttributes().put("value", format(override));
        }
        else if (body != null) {
            getAttributes().put("value", body);
        }
        else if (this.value != null) {
            getAttributes().put("value", this.value);
        }

        writeSingletonTag(getPageContext().getOut(), "input");

        // Restore the original state before we mucked with it
        getAttributes().remove("value");

        return EVAL_PAGE;
    }
}
