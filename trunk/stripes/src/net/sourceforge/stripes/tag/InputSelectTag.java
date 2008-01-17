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
import net.sourceforge.stripes.validation.BooleanTypeConverter;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTag;

/**
 * <p>Coordinates with one or more other tags to produce a well formed HTML select tag with state
 * repopulation.  The select tag itself really only writes out the basic
 * {@literal <select name="foo"> ... </select>} piece of the structure, and provides mechanisms
 * for child options to determine whether or not they should render themselves as selected.</p>
 *
 * @author Tim Fennell
 *
 * @see InputOptionTag
 * @see InputOptionsCollectionTag
 * @see InputOptionsEnumerationTag
 */
public class InputSelectTag extends InputTagSupport implements BodyTag {
    private Object value;
    private Object selectedValueOrValues;

    /**
     * If the text value passed in matches the empty string or (ignoring case) "multiple",
     * or if the value can be converted to true by the {@link BooleanTypeConverter} then the
     * attribute will be set to "multiple", otherwise the attribute will not be output.
     */
    public void setMultiple(String multiple) {
        boolean isMultiple = "multiple".equalsIgnoreCase(multiple) || "".equals(multiple);
        if (!isMultiple) {
            BooleanTypeConverter converter = new BooleanTypeConverter();
            isMultiple = converter.convert(multiple, Boolean.class, null);
        }

        if (isMultiple) {
            set("multiple", "multiple");
        }
        else {
            getAttributes().remove("multiple");
        }
    }

    /** Gets the HTML attribute &quot;multiple&quot;. **/
    public String getMultiple() { return get("multiple"); }

    /**
     * Stores the value attribute in an instance variable since it is used to determine which
     * options are checked.
     */
    public void setValue(Object value) {
        this.value = value;
        this.selectedValueOrValues = value;
    }

    /**
     * Returns the String value set by the tag attribute on the JSP, or the override value set in
     * the HttpRequest, or the override value bound to the ActionBean object in scope.
     *
     * @return Object one of <em>String</em> if the value comes from the tag on the JSP,
     *         <em>String[]</em> if the value(s) comes from the HttpServletRequest, or the type
     *         of the field on the ActionBean which could be <em>Collection</em>, <em>Object[]</em>
     *         or <em>Object</em>.
     */
    public Object getValue() {
        return this.selectedValueOrValues;
    }

    /**
     * Returns the scalar value or Array or Collection of values that are to be selected in the
     * select tag.  This will either be the value returned by the PopulationStrategy, or the value
     * supplied by the container to setValue().
     *
     * @return an Object, Object[] or Collection<Object> of values that are selected
     */
    public Object getSelectedValueOrValues() {
        return this.selectedValueOrValues;
    }

    /**
     * Checks to see if the option value should be rendered as selected or not.  Consults with the
     * override values on the form submission or backing form, if there is one.  If there is no
     * ActionBean object present, then return the values of <em>selectedOnPage</em> supplied by the
     * option.
     *
     * @param optionValue the value of the option under consideration
     * @param selectedOnPage true if the page contains selected=... and false otherwise
     */
    public boolean isOptionSelected(Object optionValue, boolean selectedOnPage)
    throws StripesJspException {
        if (this.selectedValueOrValues != null) {
            return isItemSelected(optionValue, this.selectedValueOrValues);
        }
        else {
            return selectedOnPage;
        }
    }

    /**
     * Writes out the opening {@literal <select name="foo">} tag. Looks for values in the request
     * and in the ActionBean if one is present, and caches those values so it can efficiently
     * determine which child options should be selected or not.
     *
     * @return EVAL_BODY_INCLUDE in all cases
     * @throws JspException if the enclosing form tag cannot be found or output cannot be written
     */
    @Override
    public int doStartInputTag() throws JspException {
        writeOpenTag(getPageContext().getOut(), "select");
        Object override = getOverrideValueOrValues();
        if (override != null) {
            this.selectedValueOrValues = override;
        }
        return EVAL_BODY_INCLUDE;
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
     * Writes out the close select tag ({@literal </select>}).
     * @return EVAL_PAGE in all cases
     * @throws JspException if output cannot be written.
     */
    @Override
    public int doEndInputTag() throws JspException {
        writeCloseTag(getPageContext().getOut(), "select");
        this.selectedValueOrValues = this.value; // reset in case the tag is reused
        return EVAL_PAGE;
    }

    /** Releases the discovered selected values and then calls super-release(). */
    @Override
    public void release() {
        this.selectedValueOrValues = null;
        super.release();
    }
}
