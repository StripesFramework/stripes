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

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.BodyTag;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.exception.StripesJspException;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidationMetadata;

/**
 * Tag that generates HTML form fields of type {@literal <input type="text" name="foo"
 * value="bar"/>}, which can dynamically re-populate their value. Text tags may have only a single
 * value, whose default may be set using either the body of the tag, or using the value="" attribute
 * of the tag. At runtime the contents of the text field are determined by looking for the first
 * non-null value in the following list:
 *
 * <ul>
 *   <li>A value with the same name in the HttpServletRequest
 *   <li>A value on the ActionBean if an ActionBean instance is present
 *   <li>The contents of the body of the tag
 *   <li>The value attribute of the tag
 * </ul>
 *
 * @author Tim Fennell
 */
public class InputTextTag extends InputTagSupport implements BodyTag {
  private Object value;
  private String maxlength;

  /** Basic constructor that sets the input tag's type attribute to "text". */
  public InputTextTag() {
    getAttributes().put("type", "text");
  }

  /** Sets the default value of the text field (if no body is present). */
  public void setValue(Object value) {
    this.value = value;
  }

  /** Returns the value set using setValue(). */
  public Object getValue() {
    return this.value;
  }

  /** Sets the HTML attribute of the same name. */
  public void setMaxlength(String maxlength) {
    this.maxlength = maxlength;
  }

  /** Gets the HTML attribute of the same name. */
  public String getMaxlength() {
    return maxlength;
  }

  /**
   * Gets the maxlength value that is in effect for this tag, as determined by checking {@link
   * #getMaxlength()} and then the {@code maxlength} element of the {@link Validate} annotation on
   * the associated {@link ActionBean} property.
   *
   * @throws StripesJspException if thrown by {@link #getValidationMetadata()}
   */
  protected String getEffectiveMaxlength() throws StripesJspException {
    if (getMaxlength() == null) {
      ValidationMetadata validation = getValidationMetadata();
      if (validation != null && validation.maxlength() != null)
        return validation.maxlength().toString();
      else return null;
    } else {
      return getMaxlength();
    }
  }

  /**
   * Sets type input tags type to "text".
   *
   * @return EVAL_BODY_BUFFERED in all cases.
   */
  @Override
  public int doStartInputTag() throws JspException {
    return EVAL_BODY_BUFFERED;
  }

  /** Does nothing. */
  public void doInitBody() throws JspException {}

  /**
   * Does nothing.
   *
   * @return SKIP_BODY in all cases.
   */
  public int doAfterBody() throws JspException {
    return SKIP_BODY;
  }

  /**
   * Determines which source is applicable for the value of the text field and then writes out the
   * tag.
   *
   * @return EVAL_PAGE in all cases.
   * @throws JspException if the enclosing form tag cannot be found, or output cannot be written.
   */
  @Override
  public int doEndInputTag() throws JspException {
    // Find out if we have a value from the PopulationStrategy
    Object value = getSingleOverrideValue();

    // Figure out where to pull the value from
    if (value != null) {
      getAttributes().put("value", format(value));
    }

    set("maxlength", getEffectiveMaxlength());
    writeSingletonTag(getPageContext().getOut(), "input");

    // Restore the original state before we mucked with it
    getAttributes().remove("value");

    return EVAL_PAGE;
  }
}
