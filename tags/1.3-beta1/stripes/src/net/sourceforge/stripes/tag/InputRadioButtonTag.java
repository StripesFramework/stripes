/* Copyright (C) 2005 Tim Fennell
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the license with this software. If not,
 * it can be found online at http://www.fsf.org/licensing/licenses/lgpl.html
 */
package net.sourceforge.stripes.tag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTag;

/**
 * <p>Generates {@literal <input type="radio" value="foo"/>} HTML tags based on the attribute set
 * on the tag and the state of the form. Since a single radio button widget on a HTML page can
 * have only a single value, the value tag attribute must be a Scalar object.  The value will be
 * converted to a String using the Stripes formatting system (with appropriate defaults), or by
 * calling toString if an appropriate Formatter does not exist. Similarly since radio button sets
 * can have only a single selected value at a time the checked attribute of the tag must also be
 * a scalar value.</p>
 *
 * <p>Radio buttons perform automatic (re-)population of state.  They prefer, in order, the value
 * in the HttpServletRequest, the value in the ActionBean and lastly the valuee set using
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
    private Object value;

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

    /** Sets the Object value of this individual checkbox. */
    public void setValue(Object value) { this.value = value; }

    /** Returns the value set with setValue() */
    public Object getValue() { return this.value; }

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
     * Returns the body of the tag if it is present and not empty, otherwise returns
     * the value of the 'checked' attribute.
     */
    public Object getValueOnPage() {
        String body         = getBodyContentAsString();
        if (body != null) {
            return body;
        }
        else {
            return this.checked;
        }
    }

    /**
     * Determines the state of the set of radio buttons and then writes the radio button to the
     * output stream with checked="checked" or not as appropriate.
     *
     * @return EVAL_PAGE in all cases.
     * @throws JspException if the parent form tag cannot be found, or output cannot be written.
     */
    public int doEndInputTag() throws JspException {
        Object actualChecked = getSingleOverrideValue();
        String formattedValue = format(this.value);

        // Now if the "checked" value matches this tags value, check it!
        if (actualChecked != null && this.value != null && formattedValue.equals(format(actualChecked))) {
            getAttributes().put("checked", "checked");
        }

        getAttributes().put("value", formattedValue);

        writeSingletonTag(getPageContext().getOut(), "input");

        // Restore the state of the tag to before we mucked with it
        getAttributes().remove("checked");
        getAttributes().remove("value");

        return EVAL_PAGE;
    }
}
