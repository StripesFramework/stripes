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

import net.sourceforge.stripes.util.OgnlUtil;
import net.sourceforge.stripes.exception.StripesJspException;
import ognl.OgnlException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import java.util.Collection;

/**
 * <p>Writes a set of {@literal <option value="foo">bar</option>} tags to the page based on the
 * contents of a Collection.  Each element in the collection is represented by a single option
 * tag on the page.  Uses the label and value attributes on the tag to name the properties of the
 * objects in the Collection that should be used to generate the body of the HTML option tag and
 * the value attribute of the HTML option tag respectively.</p>
 *
 * <p>E.g. a tag declaration that looks like:</p>
 *   <pre>{@literal <stripes:options-collection collection="${cats} value="catId" label="name"/>}</pre>
 *
 * <p>would cause the container to look for a Collection called "cats" across the various JSP
 * scopes and set it on the tag.  The tag would then proceed to iterate through that collection
 * calling getCatId() and getName() on each cat to produce HTML option tags.</p>
 *
 * <p>All other attributes on the tag (other than collection, value and label) are passed directly
 * through to the InputOptionTag which is used to generate the individual HTML options tags. As a
 * result the InputOptionsCollectionTag will exhibit the same re-population/selection behaviour
 * as the regular options tag.</p>
 *
 * <p>Since the tag has no use for one it does not allow a body.</p>
 *
 * @author Tim Fennell
 */
public class InputOptionsCollectionTag extends HtmlTagSupport implements Tag {
    private Collection collection;
    private String value;
    private String label;

    /** Sets the collection that will be used to generate options. */
    public void setCollection(Collection collection) {
        this.collection = collection;
    }

    /** Returns the value set with setCollection(). */
    public Collection getCollection() {
        return this.collection;
    }

    /**
     * Sets the name of the property that will be fetched on each bean in the collection in
     * order to generate the value attribute of each option.
     *
     * @param value the name of the attribute
     */
    public void setValue(String value) {
        this.value = value;
    }

    /** Returns the property name set with setValue(). */
    public String getValue() {
        return value;
    }

    /**
     * Sets the name of the property that will be fetched on each bean in the collection in
     * order to generate the body of each option (i.e. what is seen by the user).
     *
     * @param label the name of the attribute
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /** Gets the property name set with setLabel(). */
    public String getLabel() {
        return label;
    }


    /**
     * Iterates through the collection and uses an instance of InputOptionTag to generate each
     * individual option with the correct state.  It is assumed that each element in the collection
     * has non-null values for the properties specified for generating the label and value.
     *
     * @return SKIP_BODY in all cases
     * @throws JspException if either the label or value attributes specify properties that are
     *         not present on the beans in the collection, or output cannot be written.
     */
    public int doStartTag() throws JspException {
        String labelProperty = getLabel();
        String valueProperty = getValue();

        InputOptionTag tag = new InputOptionTag();
        tag.setParent(this);
        tag.setPageContext(getPageContext());
        tag.getAttributes().putAll(getAttributes());

        try {
            for (Object item : this.collection) {
                Object label = null;
                if (labelProperty != null) {
                    label = OgnlUtil.getValue(labelProperty, item);
                }
                else {
                    label = item;
                }

                Object value = null;
                if (valueProperty != null) {
                    value = OgnlUtil.getValue(valueProperty, item);
                }
                else {
                    value = item;
                }

                tag.setLabel(label.toString());
                tag.setValue(value);
                tag.doStartTag();
                tag.doInitBody();
                tag.doAfterBody();
                tag.doEndTag();
            }
        }
        catch (OgnlException oe) {
            throw new StripesJspException("A problem occurred generating an options-collection. " +
                "Most likely either [" + labelProperty + "] or ["+ valueProperty + "] is not a " +
                "valid property of the beans in the collection: " + this.collection);
        }

        return SKIP_BODY;
    }

    /**
     * Does nothing.
     *
     * @return EVAL_PAGE in all cases.
     */
    public int doEndTag() throws JspException {
        return EVAL_PAGE;
    }
}
