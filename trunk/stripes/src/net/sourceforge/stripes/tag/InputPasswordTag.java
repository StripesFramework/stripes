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

/**
 * Tag class that implements an input tag of type password.  Defines one attribute in addition
 * to those provided by the HTML tag.  If {@code repopulate} is set then the password tag will
 * behave just like the text tag, and will repopulate values on error and during wizard flows. If
 * {@code repopulate} is not set, or is set to false then values will not be re-populated, but
 * initial/default values supplied to the tag on the JSP will be used.
 *
 * @author Tim Fennell
 * @since Stripes 1.1
 */
public class InputPasswordTag extends InputTextTag {
    private boolean repopulate = false;

    /** Sets whether or not the tag will repopulate the value if one is present. */
    public void setRepopulate(boolean repopulate) { this.repopulate = repopulate; }

    /** Returns true if the tag will repopulate values, false otherwise. */
    public boolean getRepopulate() { return repopulate; }

    /**
     * Constructs a new tag for generating password fields. Delegates to InputTextTag which it
     * extends, and then overrides the type of input field to "password".
     */
    public InputPasswordTag() {
        super();
        getAttributes().put("type", "password");
    }

    public int doEndInputTag() throws JspException {
        if (this.repopulate) {
            // If repopulate is set, delegate to the parent input text tag
            return super.doEndInputTag();
        }
        else {
            // Else just figure out if there is a default value and use it
            String body     = getBodyContentAsString();

            // Figure out where to pull the value from
            if (body != null) {
                getAttributes().put("value", body);
            }
            else if (getValue() != null) {
                getAttributes().put("value", getValue());
            }

            writeSingletonTag(getPageContext().getOut(), "input");

            // Restore the original state before we mucked with it
            getAttributes().remove("value");

            return EVAL_PAGE;
        }
    }
}
