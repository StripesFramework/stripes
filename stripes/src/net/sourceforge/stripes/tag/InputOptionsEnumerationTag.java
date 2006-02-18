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

import net.sourceforge.stripes.exception.StripesJspException;
import net.sourceforge.stripes.util.OgnlUtil;
import net.sourceforge.stripes.util.ReflectUtil;
import ognl.OgnlException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

/**
 * <p>Writes a set of {@literal <option value="foo">bar</option>} tags to the page based on the
 * values of a enum.  Each value in the enum is represented by a single option tag on the page. The
 * options will be generated in ordinal value order (i.e. the order they are declared in the
 * enum). Uses the label attribute on the tag to name the property of the enum that should be used
 * to generate the body of the HTML option tag.  If the label attribute is not set then the
 * tag will call toString() on the enum value and use that as the body of the tag.</p>
 *
 * <p>E.g. a tag declaration that looks like:</p>
 *   <pre>{@literal <stripes:options-enumeration collection="net.kitty.EyeColor"/>}</pre>
 *
 * <p>would result in the tag attempting to invoke the equivelant of
 * {@code Class.forName("net.kitty.EyeColor")} and cast the result to type Class<Enum>. If that
 * fails, a JspException will be raised.  The tag will then proceed to call name() in order
 * to fetch the value of the enum and use that for the value of the option, and call toString() to
 * provide the body of the option because a label attribute was not specified.</p>
 *
 * <p>All other attributes on the tag (other than enum and label) are passed directly
 * through to the InputOptionTag which is used to generate the individual HTML options tags. As a
 * result the InputOptionsCollectionTag will exhibit the same re-population/selection behaviour
 * as the regular options tag.</p>
 *
 * <p>Since the tag has no use for one it does not allow a body.</p>
 *
 * @author Tim Fennell
 */
public class InputOptionsEnumerationTag extends HtmlTagSupport implements Tag {
    private String className;
    private String label;

    /** Sets the fully qualified name of an enumeration class. */
    public void setEnum(String name) {
        this.className = name;
    }

    /** Gets the enum class name set with setEnum(). */
    public String getEnum() {
        return this.className;
    }

    /** Sets the name of the property that will be used to generate the option's label. */
    public void setLabel(String label) {
        this.label = label;
    }

    /** Gets the name of the property that will be used to generate the option's label. */
    public String getLabel() {
        return this.label;
    }

    /**
     * Attempts to instantiate the Class object representing the enum and fetch the values of the
     * enum.  Then generates an option per value using an instance of an InputOptionTag.
     *
     * @return SKIP_BODY in all cases.
     * @throws JspException if the class name supplied is not a valid class, or cannot be cast
     *         to Class<Enum>.
     */
    public int doStartTag() throws JspException {
        Class<Enum> clazz = null;
        try {
            clazz = (Class<Enum>) ReflectUtil.findClass(this.className);
        }
        catch (Exception e) {
            throw new StripesJspException
                    ("Could not process class [" + this.className + "]. Attribute 'enum' on " +
                            "tag options-enumeration must be the fully qualified name of a " +
                            "class which is a java 1.5 enum.", e);
        }

        if (!clazz.isEnum()) {
            throw new StripesJspException
                    ("The class name supplied, [" + this.className + "], does not appear to be " +
                     "a JDK1.5 enum class.");
        }
        
        Enum[] enums = clazz.getEnumConstants();

        InputOptionTag tag = new InputOptionTag();
        tag.setParent(this);
        tag.setPageContext(getPageContext());
        tag.getAttributes().putAll(getAttributes());
        tag.getAttributes().remove("enum");

        try {
            for (Enum item : enums) {
                Object value = item.name();
                Object label = null;
                if (this.label != null) {
                    label = OgnlUtil.getValue(this.label, item);
                }
                else {
                    label = item.toString();
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
            throw new StripesJspException("A problem occurred generating an options-enumeration. " +
                "Most likely either the class [" + getEnum() + "] is not an enum or, [" +
                    this.label + "] is not a valid property of the enum.");
        }

        return SKIP_BODY;
    }

    /**
     * Does nothing.
     * @return EVAL_PAGE in all cases.
     */
    public int doEndTag() throws JspException {
        return EVAL_PAGE;
    }

}
