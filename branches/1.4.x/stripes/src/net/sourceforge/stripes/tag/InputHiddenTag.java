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
import java.util.Collection;

/**
 * <p>Generates one or more {@literal <input type="hidden" ... />} HTML tags based on the value
 * supplied.  The hidden tag assigns the value attribute by scanning in the following order:
 * <ul>
 *   <li>for one or more values with the same name in the HttpServletRequest</li>
 *   <li>for a field on the ActionBean with the same name (if a bean instance is present)</li>
 *   <li>by collapsing the body content to a String, if a body is present</li>
 *   <li>referring to the result of the EL expression contained in the value attribute of the tag.</li>
 * </ul>
 * </p>
 *
 * <p>The result of this scan can produce either a Collection, an Array or any other Object. In the
 * first two cases the tag will output an HTML hidden form field tag for each value in the
 * Collection or Array.  In all other cases the Object is toString()'d (unless it is null) and a
 * single hidden field will be written.</p>
 *
 * @author Tim Fennell
 */
public class InputHiddenTag extends InputTagSupport implements BodyTag {
    private Object value;

    /** Basic constructor that sets the input tag's type attribute to "hidden". */
    public InputHiddenTag() {
        super();
        getAttributes().put("type", "hidden");
    }

    /**
     * Sets the value that will be used for the hidden field(s) if no body or repopulation
     * value is found.
     *
     * @param value the result of an EL evaluation, can be a Collection, Array or other.
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /** Returns the value set with setValue(). */
    public Object getValue() {
        return this.value;
    }

    /**
     * Sets the tag up as a hidden tag.
     *
     * @return EVAL_BODY_BUFFERED to always buffer the body (so it can be used as the value)
     */
    public int doStartInputTag() throws JspException {
        return EVAL_BODY_BUFFERED;
    }

    /** Does nothing. */
    public void doInitBody() throws JspException {

    }

    /**
     * Does nothing.
     * @return SKIP_BODY in all cases.
     */
    public int doAfterBody() throws JspException {
        return SKIP_BODY;
    }

    /**
     * Determines the value(s) that will be used for the tag and then proceeds to generate
     * one or more hidden fields to contain those values.
     *
     * @return EVAL_PAGE in all cases.
     * @throws JspException if the enclosing form tag cannot be found, or output cannot be written.
     */
    public int doEndInputTag() throws JspException {
        // Find out if we have a value from the PopulationStrategy
        Object valueOrValues = getOverrideValueOrValues();

        // Figure out how many times to write it out
        if (valueOrValues == null) {
            getAttributes().put("value", "");
            writeSingletonTag(getPageContext().getOut(), "input");
        }
        else if (valueOrValues.getClass().isArray()) {
            for (Object value : (Object[]) valueOrValues) {
                getAttributes().put("value", format(value));
                writeSingletonTag(getPageContext().getOut(), "input");
            }
        }
        else if (valueOrValues instanceof Collection) {
            for (Object value : (Collection) valueOrValues) {
                getAttributes().put("value", format(value));
                writeSingletonTag(getPageContext().getOut(), "input");
            }
        }
        else {
            getAttributes().put("value", format(valueOrValues));
            writeSingletonTag(getPageContext().getOut(), "input");
        }

        // Clear out the value from the attributes
        getAttributes().remove("value");

        return EVAL_PAGE;
    }
}
