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

import net.sourceforge.stripes.exception.StripesJspException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTag;
import java.io.IOException;

/**
 * <p>Generates an {@literal <option value="foo">Fooey</option>} HTML tag.  Coordinates with an
 * enclosing select tag to determine it's state (i.e. whether or not it is selected.)  As a result
 * some of the logic regarding state repopulation is a bit complex.</p>
 *
 * <p>Since options can have only a single value per option the value attribute of the tag
 * must be a scalar, which will be converted into a String using a Formatter if an
 * appropriate one can be found, otherwise the toString() method will be invoked.  The presence of
 * a "selected" attribute is used as an indication that this option believes it should be selected
 * by default - the value (as opposed to the presence) of the selected attribute is never used....</p>
 *
 * <p>The option tag delegates to its enclosing select tag to determine whether or not it should
 * be selected.  See the {@link InputSelectTag "select tag"} for documentation on how it
 * determines selection status.  If the select tag <em>has no opinion</em> on selection state
 * (note that this is not the same as select tag deeming the option should not be selected) then
 * the presence of the selected attribute (or lack thereof) is used to turn selection on or off.</p>
 *
 * <p>If the option has a body then the String value of that body will be used to generate the
 * body of the generated HTML option.  If the body is empty or not present then the label attribute
 * will be written into the body of the tag.</p>
 *
 * @author Tim Fennell
 */
public class InputOptionTag extends InputTagSupport implements BodyTag {
    private String selected;
    private String label;
    private Object value;

    /** Sets the value of this option. */
    public void setValue(Object value) { this.value = value; }

    /** Returns the value of the opion as set with setValue(). */
    public Object getValue() { return this.value; }

    /** Sets the label that will be used as the option body if no body is supplied. */
    public void setLabel(String label) { this.label = label; }

    /** Returns the value set with setLabel(). */
    public String getLabel() { return this.label; }

    /** Sets whether or not this option believes it should be selected by default. */
    public void setSelected(String selected) { this.selected = selected; }

    /** Returns the value set with setSelected(). */
    public String getSelected() { return this.selected; }

    /**
     * Does nothing.
     * @return EVAL_BODY_BUFFERED in all cases.
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
     * Locates the option's parent select tag, determines selection state and then writes out
     * an option tag with an appropriate body.
     *
     * @return EVAL_PAGE in all cases.
     * @throws JspException if the option is not contained inside an InputSelectTag or output
     *         cannot be written.
     */
    public int doEndInputTag() throws JspException {
        // Find our mandatory enclosing select tag
        InputSelectTag selectTag = getParentTag(InputSelectTag.class);
        if (selectTag == null) {
            throw new StripesJspException
                    ("Option tags must always be contained inside a select tag.");
        }

        if ( selectTag.isOptionSelected(getValue(), (this.selected != null)) ) {
            getAttributes().put("selected", "selected");
        }

        getAttributes().put("value", format(this.value));

        try {
            String actualLabel = getBodyContentAsString();

            if (actualLabel == null) {
                actualLabel = this.label;
            }

            writeOpenTag(getPageContext().getOut(), "option");
            if (actualLabel != null) {
                getPageContext().getOut().write(actualLabel);
            }
            writeCloseTag(getPageContext().getOut(), "option");

            // Clean out the attributes we modified
            getAttributes().remove("selected");
            getAttributes().remove("value");
        }
        catch (IOException ioe) {
            throw new JspException("IOException in InputOptionTag.doEndTag().", ioe);
        }

        return EVAL_PAGE;
    }

    /**
     * Overridden to make sure that options do not try and register themselves with
     * the form tag. This is done because options are not standalone input tags, but
     * always part of a select tag (which gets registered).
     */
    protected void registerWithParentForm() throws StripesJspException {
        // Do nothing, options are not standalone fields and should not register
    }
}
