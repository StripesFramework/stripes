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
