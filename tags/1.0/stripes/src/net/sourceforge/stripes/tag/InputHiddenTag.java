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
        String body  = getBodyContentAsString();
        Object override = getOverrideValueOrValues();
        Object originalValue = this.value;
        Object valueWereGoingToUse = "";

        // Figure out where to pull the value from
        if (override != null) {
            valueWereGoingToUse = override;
        }
        else if (body != null) {
            valueWereGoingToUse = body;
        }
        else {
            valueWereGoingToUse = originalValue;
        }

        // Figure out how many times to write it out
        if (valueWereGoingToUse != null) {
            if (valueWereGoingToUse.getClass().isArray()) {
                for (Object value : (Object[]) valueWereGoingToUse) {
                    getAttributes().put("value", format(value));
                    writeSingletonTag(getPageContext().getOut(), "input");
                }
            }
            else if (valueWereGoingToUse instanceof Collection) {
                for (Object value : (Collection) valueWereGoingToUse) {
                    getAttributes().put("value", format(value));
                    writeSingletonTag(getPageContext().getOut(), "input");
                }
            }
            else {
                getAttributes().put("value", format(valueWereGoingToUse));
                writeSingletonTag(getPageContext().getOut(), "input");
            }
        }

        // Clear out the value from the attributes
        getAttributes().remove("value");

        return EVAL_PAGE;
    }
}
