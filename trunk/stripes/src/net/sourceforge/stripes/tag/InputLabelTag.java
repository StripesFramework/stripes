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
import net.sourceforge.stripes.localization.LocalizationUtility;

/**
 * <p>Tag handler for a tag that produces an HTML label tag which is capable of looking up
 * localized field names and formatting the label when validation errors exist.  The field being
 * labeled is identified using either the {@code name} attribute (preferred) or the
 * {@code for} attribute.  If the {@code name} attribute is supplied this will always be used as
 * the lookup key (optionally pre-pended with the form's action path).  If the {@code name} field
 * is not supplied, the tag will fall back to using the value supplied for the {@code for}
 * attribute.  This is done because the {@code for} attribute is used by HTML as a reference to the
 * {@code id} of the input being labeled.  In the case where the id is equal to the field name
 * it is unnecessary to specify a {@code name} attribute for the label tag.  In cases where the
 * field name (or other localized resource name) does not match an HTML ID, the {@code name}
 * attribute must be used.</p>
 *
 * <p>The value used for the label is the localized field name if one exists.  Localized field
 * names are looked up in the field name resource bundle first using {@code formActionPath.fieldName},
 * and then (if no value is found) using just {@code fieldName}. If no localized String can be found
 * then the body of the label tag is used. If no body is supplied then a warning String will be used
 * instead!</p>
 *
 * @author Tim Fennell
 * @since Stripes 1.1
 */
public class InputLabelTag extends InputTagSupport implements BodyTag {

    private boolean nameSet;

    /** Sets the HTML ID of the field for which this is a label. */
    public void setFor(String forId) {
        set("for", forId);

        // If the name field isn't set yet, set it with the forId.
        if (!nameSet) {
            super.setName(forId);
        }
    }

    /** Gets the HTML ID of the field for which this is a label. */
    public String getFor() { return get("for"); }

    /**
     * Sets the name of the form element/label to be rendered. Should only be invoked by
     * the JSP container as it also tracks whether or not the container has set the name, in
     * order to correctly handle pooling.
     */
    @Override
    public void setName(String name) {
        super.setName(name);
        this.nameSet = true;
    }

    /**
     * Does nothing.
     * @return EVAL_BODY_BUFFERED in all cases.
     */
    @Override
    public int doStartInputTag() throws JspException {
        return EVAL_BODY_BUFFERED;
    }

    /** Does nothing. */
    public void doInitBody() throws JspException { /** Do Nothing */ }

    /**
     * Does nothing.
     * @return SKIP_BODY in all cases.
     */
    public int doAfterBody() throws JspException {
        return SKIP_BODY;
    }

    /**
     * Performs the main work of the tag as described in the class level javadoc.
     * @return EVAL_PAGE in all cases.
     * @throws JspException if an IOException is encountered writing to the output stream.
     */
    @Override
    public int doEndInputTag() throws JspException {
        try {
            String label = getLocalizedFieldName();
            String fieldName = getAttributes().remove("name");

            if (label == null) {
                label = getBodyContentAsString();
            }

            if (label == null) {
                if (fieldName != null) {
                    label = LocalizationUtility.makePseudoFriendlyName(fieldName);
                }
                else {
                    label = "Label could not find localized field name and had no body nor name attribute.";
                }
            }

            // Write out the tag
            writeOpenTag(getPageContext().getOut(), "label");
            getPageContext().getOut().write(label);
            writeCloseTag(getPageContext().getOut(), "label");

            // Reset the field name so as to not screw up tag pooling
            if (this.nameSet) {
                super.setName(fieldName);
            }

            return EVAL_PAGE;
        }
        catch (IOException ioe) {
            throw new StripesJspException("Encountered an exception while trying to write to " +
                "the output from the stripes:label tag handler class, InputLabelTag.", ioe);
        }
    }

    /** Overridden to do nothing, since a label isn't really a form field. */
    @Override
    protected void registerWithParentForm() throws StripesJspException { }

    /**
     * Wraps the parent loadErrors() to suppress exceptions when the label is outside of a
     * stripes form tag.
     */
    @Override
    protected void loadErrors() {
        try {
            super.loadErrors();
        }
        catch (StripesJspException sje) {
            // Do nothing, we're suppressing this error
        }
    }
}
