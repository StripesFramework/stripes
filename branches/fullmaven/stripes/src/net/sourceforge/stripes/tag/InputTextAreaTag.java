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
import net.sourceforge.stripes.util.HtmlUtil;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTag;
import java.io.IOException;

/**
 * <p>Tag that generates HTML form fields of type
 * {@literal <textarea name="foo"> ... </textarea>}, which can dynamically re-populate their
 * value. Textareas may have only a single value, whose default may be set using either the body
 * of the textarea, or using the value="" attribute of the tag. At runtime the contents of the
 * textarea are determined by looking for the first non-null value in the following list:</p>
 *
 * <ul>
 *   <il>A value with the same name in the HttpServletRequest</li>
 *   <il>A value on the ActionBean if an ActionBean instance is present</li>
 *   <il>The contents of the body of the textarea</li>
 *   <il>The value attribute of the tag</li>
 * </ul>
 *
 * @author Tim Fennell
 */
public class InputTextAreaTag extends InputTagSupport implements BodyTag {
    private Object value;

    /** Sets the default value of the textarea (if no body is present). */
    public void setValue(Object value) { this.value = value; }

    /** Returns the value set using setValue(). */
    public Object getValue() { return this.value; }


    /** Sets the HTML attribute of the same name. */
    public void setCols(String cols) { set("cols", cols); }
    /** Gets the HTML attribute of the same name. */
    public String getCols() { return get("cols"); }

    /** Sets the HTML attribute of the same name. */
    public void setRows(String rows) { set("rows", rows); }
    /** Gets the HTML attribute of the same name. */
    public String getRows() { return get("rows"); }

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
     * Determines which source is applicable for the contents of the textarea and then writes
     * out the textarea tag including the body.
     *
     * @return EVAL_PAGE in all cases.
     * @throws JspException if the enclosing form tag cannot be found, or output cannot be written.
     */
    @Override
    public int doEndInputTag() throws JspException {
        try {
            // Find out if we have a value from the PopulationStrategy
            Object value = getSingleOverrideValue();

            writeOpenTag(getPageContext().getOut(), "textarea");

            // Write out the contents of the text area
            if (value != null) {
                // Most browsers have this annoying habit of eating the first newline
                // in a textarea tag. Since this is probably not desired, sometimes
                // we need to add an extra newline into the output before the value
                String body = getBodyContentAsString();
                if (body == null || !body.equals(value)) {
                    getPageContext().getOut().write('\n');
                }

                getPageContext().getOut().write( HtmlUtil.encode(format(value)) );
            }

            writeCloseTag(getPageContext().getOut(), "textarea");

            return EVAL_PAGE;
        }
        catch (IOException ioe) {
            throw new StripesJspException("Could not write out textarea tag.", ioe);
        }
    }
}
