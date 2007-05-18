/* Copyright 2005-2006 Tim Fennell
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sourceforge.stripes.tag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTag;

/**
 * <p>Implements an HTML tag that generates form fields of type {@literal <input type="checkbox"/>}.
 * Since a single checkbox widget on a HTML page can have only a single value, the value tag
 * attribute must always resolve to a scalar value which will be converted to a String using
 * the Stripes Formatting system, or by caling toString() if an appropriate Formatter is
 * not found.</p>
 *
 * <p>Checkboxes perform automatic (re-)population of state.  They prefer, in order, values in the
 * HttpServletRequest, values in the ActionBean and lastly values set using checked="" on the page.
 * The "checked" attribute is a complex attribute and may be a Collection, an Array or a scalar
 * Java Object.  In the first two cases a check is performed to see if the value in the value="foo"
 * attribute is one of the elements in the checked collection or array.  In the last case, the
 * value is matched directly against the String form of the checked attribute.  If in any case a
 * checkbox's value matches then a checked="checked" attribute will be added to the HTML written.</p>
 *
 * <p>The tag may include a body and if present the body is converted to a String and overrides the
 * <b>checked</b> tag attribute.</p>
 *
 * @author Tim Fennell
 */
public class InputCheckBoxTag extends InputTagSupport implements BodyTag {
    private Object checked;
    private Object value = Boolean.TRUE; // default value to supply true/false checkbox behaviour

    /** Basic constructor that sets the input tag's type attribute to "checkbox". */
    public InputCheckBoxTag() {
        super();
        getAttributes().put("type", "checkbox");
    }

    /**
     * Sets the default checked values for checkboxes with this name.
     *
     * @param checked may be either a Collection or Array of checked values, or a single Checked
     *        value.  Values do not have to be Strings, but will need to be convertible to String
     *        using the toString() method.
     */
    public void setChecked(Object checked) {
        this.checked = checked;
    }

    /** Returns the value originally set using setChecked(). */
    public Object getChecked() {
        return this.checked;
    }

    /** Sets the value that this checkbox will submit if it is checked. */
    public void setValue(Object value) { this.value = value; }

    /** Returns the value that this checkbox will submit if it is checked. */
    public Object getValue() { return this.value; }


    /** Does nothing. */
    @Override
    public int doStartInputTag() throws JspException {
        return EVAL_BODY_BUFFERED;
    }

    /** Does nothing. */
    public void doInitBody() throws JspException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /** Ensure that the body is evaluated only once. */
    public int doAfterBody() throws JspException {
        return SKIP_BODY;
    }

    /**
     * Returns the body of the tag if it is present and not empty, otherwise returns
     * the value of the 'checked' attribute.
     */
    @Override
    public Object getValueOnPage() {
        Object value = getBodyContentAsString();
        if (value != null) {
            return value;
        }
        else {
            return this.checked;
        }
    }

    /**
     * Does the main work of the tag, including determining the tags state (checked or not) and
     * writing out a singleton tag representing the checkbox.
     *
     * @return always returns EVAL_PAGE to continue page execution
     * @throws JspException if the checkbox is not contained inside a stripes InputFormTag, or has
     *         problems writing to the output.
     */
    @Override
    public int doEndInputTag() throws JspException {
        // Find out if we have a value from the PopulationStrategy
        Object checked = getOverrideValueOrValues();

        // If the value of this checkbox is contained in the value or override value, check it
        getAttributes().put("value", format(this.value));
        if (isItemSelected(this.value, checked)) {
            getAttributes().put("checked", "checked");
        }

        writeSingletonTag(getPageContext().getOut(), "input");

        // Restore the tags state to before we mucked with it
        getAttributes().remove("checked");
        getAttributes().remove("value");

        return EVAL_PAGE;
    }
}
