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

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.exception.StripesJspException;
import net.sourceforge.stripes.format.Formatter;
import net.sourceforge.stripes.format.FormatterFactory;
import net.sourceforge.stripes.localization.LocalizationUtility;
import net.sourceforge.stripes.validation.ValidationError;
import net.sourceforge.stripes.validation.ValidationErrors;

import javax.servlet.jsp.JspException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Parent class for all input tags in stripes.  Provides support methods for retrieving all the
 * attributes that are shared across form input tags.  Also provides accessors for finding the
 * specified &quot;override&quot; value and for finding the enclosing support tag.
 *
 * @author Tim Fennell
 */
public abstract class InputTagSupport extends HtmlTagSupport {
    /** PopulationStrategy used to find non-default values for input tags. */
    private static PopulationStrategy populationStrategy = new DefaultPopulationStrategy();

    private String formatType;
    private String formatPattern;

    /** A list of the errors related to this input tag instance */
    protected List<ValidationError> fieldErrors;
    /** The error renderer to be utilized for error output of this input tag */
    protected TagErrorRenderer errorRenderer;

    public void setDisabled(String disabled) { set("disabled", disabled); }
    public String getDisabled() { return get("disabled"); }

    public void setName(String name) { set("name", name); }
    public String getName() { return get("name"); }

    public void setSize(String size) { set("size", size); }
    public String getSize() { return get("size"); }

    /** Sets the type of ouput to format, e.g. date or time. */
    public void setFormatType(String formatType) { this.formatType = formatType; }

    /** Returns the value set with setFormatAs() */
    public String getFormatType() { return this.formatType; }

    /** Sets the named format pattern, or a custom format pattern. */
    public void setFormatPattern(String formatPattern) { this.formatPattern = formatPattern; }

    /** Returns the value set with setFormatPattern() */
    public String getFormatPattern() { return this.formatPattern; }


    /**
     * Gets the value for this tag based on the current population strategy.  The value returned
     * could be a scalar value, or it could be an array or collection depending on what the
     * population strategy finds.  For example, if the user submitted multiple values for a
     * checkbox, the default population strategy would return a String[] containg all submitted
     * values.
     *
     * @return Object either a value/values for this tag or null
     * @throws StripesJspException if the enclosing form tag (which is required at all times, and
     *         necessary to perform repopulation) cannot be located
     */
    protected Object getOverrideValueOrValues() throws StripesJspException {
        return populationStrategy.getValue(this);
    }

    /**
     * Returns a single value for the the value of this field.  This can be used to ensure that
     * only a single value is returned by the population strategy, which is useful in the case
     * of text inputs etc. which can have only a single value.
     *
     * @return Object either a single value or null
     * @throws StripesJspException if the enclosing form tag (which is required at all times, and
     *         necessary to perform repopulation) cannot be located
     */
    protected Object getSingleOverrideValue() throws StripesJspException {
        Object unknown = getOverrideValueOrValues();
        Object returnValue = null;

        if (unknown != null && unknown instanceof Object[]) {
            Object[] array = (Object[]) unknown;
            if (array.length > 0) {
                returnValue = array[0];
            }
        }
        else if (unknown != null && unknown instanceof Collection) {
            Collection collection = (Collection) unknown;
            if (collection.size() > 0) {
                returnValue = collection.iterator().next();
            }
        }
        else {
            returnValue = unknown;
        }

        return returnValue;
    }

    /**
     * <p>Locates the enclosing stripes form tag. If no form tag can be found, because the tag
     * was not enclosed in one on the JSP, an exception is thrown.</p>
     *
     * @return FormTag the enclosing form tag on the JSP
     * @throws StripesJspException if an enclosing form tag cannot be found
     */
    protected FormTag getParentFormTag() throws StripesJspException {
        FormTag parent = getParentTag(FormTag.class);

        if (parent == null) {
            throw new StripesJspException
                ("InputTag of type [" + getClass().getName() + "] must be enclosed inside a " +
                    "stripes form tag.");
        }

        return parent;
    }

    /**
     * Utility method for determining if a String value is contained within an Object, where the
     * object may be either a String, String[], Object, Object[] or Collection.  Used primarily
     * by the InputCheckBoxTag and InputSelectTag to determine if specific check boxes or
     * options should be selected based on the values contained in the JSP, HttpServletRequest and
     * the ActionBean.
     *
     * @param value the value that we are searching for
     * @param selected a String, String[], Object, Object[] or Collection (of scalars) denoting the
     *        selected items
     * @return boolean true if the String can be found, false otherwise
     */
    protected boolean isItemSelected(Object value, Object selected) {
        // Since this is a checkbox, there could be more than one checked value, which means
        // this could be a single value type, array or collection
        if (selected != null) {
            String stringValue = (value == null) ? "" : format(value);

            if (selected instanceof Object[]) {
                Object[] selectedIf = (Object[]) selected;
                for (Object item : selectedIf) {
                    if ( (format(item).equals(stringValue)) ) {
                        return true;
                    }
                }
            }
            else if (selected instanceof Collection) {
                Collection selectedIf = (Collection) selected;
                for (Object item : selectedIf) {
                    if ( (format(item).equals(stringValue)) ) {
                        return true;
                    }
                }
            }
            else {
                if( format(selected).equals(stringValue) ) {
                    return true;
                }
            }
        }

        // If we got this far without returning, then this is not a selected item
        return false;
    }

    /**
     * Fetches the localized name for this field if one exists in the resource bundle. Relies on
     * there being a "name" attribute on the tag, and the pageContext being set on the tag. First
     * checks for a value of {actionPath}.{fieldName} in the specified bundle, and if that exists,
     * returns it.  If that does not exist, will look for just "fieldName".
     *
     * @return a localized field name if one can be found, or null if one cannot be found.
     */
    protected String getLocalizedFieldName() throws StripesJspException {
        String name = getAttributes().get("name");
        Locale locale = getPageContext().getRequest().getLocale();
        String actionPath = null;

        try {
            actionPath = getParentFormTag().getAction();
        }
        catch (StripesJspException sje) { /* Do nothing. */}

        return LocalizationUtility.getLocalizedFieldName(name, actionPath, locale);
    }

    /**
     * Attempts to format an object using the Stripes formatting system.  If no formatter can
     * be found, then a simple String.valueOf(input) will be returned.  If the value passed in
     * is null, then the empty string will be returned.
     */
    protected String format(Object input) {
        if (input == null) return "";

        FormatterFactory factory = StripesFilter.getConfiguration().getFormatterFactory();
        Formatter formatter = factory.getFormatter(input.getClass(),
                                                   getPageContext().getRequest().getLocale(),
                                                   this.formatType,
                                                   this.formatPattern);
        if (formatter != null) {
            return formatter.format(input);
        }
        else {
            return String.valueOf(input);
        }
    }

    /**
     * Find errors that are related to the form field this input tag represents and place
     * them in an instance variable to use during error rendering.
     */
    protected void loadErrors() throws StripesJspException {
        ActionBean actionBean = getActionBean();
        if (actionBean != null) {
            ValidationErrors validationErrors = actionBean.getContext().getValidationErrors();

            if (validationErrors != null) {
                this.fieldErrors = validationErrors.get(getName());
            }
        }
    }

    /**
     * Access for the field errors that occured on the form input this tag represents
     * @return List<ValidationError> the list of validation errors for this field
     */
    public List<ValidationError> getFieldErrors() {
        return fieldErrors;
    }

    /**
     * Fetches the ActionBean associated with the form if one is present.  An ActionBean will not
     * be created (and hence not present) by default.  An ActionBean will only be present if the
     * current request got bound to the same ActionBean as the current form uses.  E.g. if we are
     * re-showing the page as the result of an error, or the same ActionBean is used for a
     * &quot;pre-Action&quot; and the &quot;post-action&quot;.
     *
     * @return ActionBean the ActionBean bound to the form if there is one
     */
    protected ActionBean getActionBean() throws StripesJspException {
        return getParentFormTag().getActionBean();
    }



    /**
     * Final implementation of the doStartTag() method that allows the base InputTagSupport class
     * to insert functionality before and after the tag performs it's doStartTag equivelant
     * method. Finds errors related to this field and intercepts with a {@link TagErrorRenderer}
     * if appropriate.
     *
     * @return int the value returned by the child class from doStartInputTag()
     */
    public final int doStartTag() throws JspException {
        registerWithParentForm();

        // Deal with any error rendering
        loadErrors();
        if (this.fieldErrors != null) {
            this.errorRenderer = StripesFilter.getConfiguration()
                    .getTagErrorRendererFactory().getTagErrorRenderer(this);
            this.errorRenderer.doBeforeStartTag();
        }

        return doStartInputTag();
    }

    /**
     * Registers the field with the parent form within which it must be enclosed.
     * @throws StripesJspException if the parent form tag is not found
     */
    protected void registerWithParentForm() throws StripesJspException {
        getParentFormTag().registerField(getName());
    }

    /** Abstract method implemented in child classes instead of doStartTag(). */
    public abstract int doStartInputTag() throws JspException;

    /**
     * Final implementation of the doEndTag() method that allows the base InputTagSupport class
     * to insert functionality before and after the tag performs it's doEndTag equivelant
     * method.
     *
     * @return int the value returned by the child class from doStartInputTag()
     */
    public final int doEndTag() throws JspException {
        int result = doEndInputTag();

        if (this.fieldErrors != null) {
            this.errorRenderer.doAfterEndTag();
        }

        this.errorRenderer = null;
        this.fieldErrors = null;

        return result;
    }

    /** Abstract method implemented in child classes instead of doEndTag(). */
    public abstract int doEndInputTag() throws JspException;
}
