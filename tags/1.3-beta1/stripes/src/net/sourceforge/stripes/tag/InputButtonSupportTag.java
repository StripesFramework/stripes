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
 *   <li>the body of the tag</li>
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
    public int doEndInputTag() throws JspException {
        // Find out if we have a value from the PopulationStrategy
        String body = getBodyContentAsString();
        String localizedValue = getLocalizedFieldName();

        // Figure out where to pull the value from
        if (localizedValue != null) {
            getAttributes().put("value", localizedValue);
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
