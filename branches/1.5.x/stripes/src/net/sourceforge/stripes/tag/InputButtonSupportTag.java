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

import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.JspException;

/**
 * <p>Support tag class that can generate HTML form fields with localized value attributes.
 * Primarily used to contain identical functionality between submit, reset and button input types.
 * The only capability offered above and beyond a pure html tag is the ability to lookup the value
 * of the button (i.e. the text on the button that the user sees) from a localized resource bundle.
 * The tag will set it's value using the first non-null result from the following list:</p>
 *
 * <ul>
 *   <li>formName.buttonName from the localized resource bundle</li>
 *   <li>buttonName from the localized resource bundle</li>
 *   <li>the trimmed body of the tag</li>
 *   <li>the value attribute of the tag</li>
 * </ul>
 *
 * @author Tim Fennell
 */
public class InputButtonSupportTag extends InputTagSupport implements BodyTag {
    private String value;

    /** Sets the value to use for the submit button if all other strategies fail. */
    public void setValue(String value) { this.value = value; }

    /** Returns the value set with setValue(). */
    public String getValue() { return this.value; }

    /**
     * Does nothing.
     * @return EVAL_BODY_BUFFERED in all cases.
     */
    @Override
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
     * Looks up the appropriate value to use for the submit button and then writes the tag
     * out to the page.
     * @return EVAL_PAGE in all cases.
     * @throws javax.servlet.jsp.JspException if output cannot be written.
     */
    @Override
    public int doEndInputTag() throws JspException {
        // Find out if we have a value from the PopulationStrategy
        String body = getBodyContentAsString();
        String localizedValue = getLocalizedFieldName();

        // Figure out where to pull the value from
        if (localizedValue != null) {
            getAttributes().put("value", localizedValue);
        }
        else if (body != null) {
            getAttributes().put("value", body.trim());
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
