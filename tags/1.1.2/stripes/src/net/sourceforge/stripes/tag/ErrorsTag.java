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
import net.sourceforge.stripes.controller.ActionResolver;
import net.sourceforge.stripes.controller.StripesConstants;
import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.validation.ValidationError;
import net.sourceforge.stripes.validation.ValidationErrors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTag;
import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.ResourceBundle;
import java.util.MissingResourceException;

/**
 * <p>The errors tag has two modes, one where it displays all validation errors in a list
 * and a second mode when there is a single enclosed field-error tag that has no name attribute
 * in which case this tag iterates over the body, displaying each error in turn in place
 * of the field-error tag.</p>
 *
 * <p>In the first mode, where the default output is used, it is possible to change the output
 * for the entire application using a set of resources in the error messages bundle
 * (StripesResources.properties unless you have configured another).  If the properties are
 * undefined, the tag will output the text "Validation Errors" in a div with css class errorHeader,
 * then output an unordered list of error messages.  The following four resource strings
 * (shown with their default values) can be modified to create different default ouput:</p>
 *
 * <ul>
 *   <li>stripes.errors.header={@literal <div class="errorHeader">Validation Errors</div><ul>}</li>
 *   <li>stripes.errors.footer={@literal </ul>}</li>
 *   <li>stripes.errors.beforeError={@literal <li>}</li>
 *   <li>stripes.errors.afterError={@literal </li>}</li>
 * </ul>
 *
 * <p>This tag has several ways of being attached to the errors of a specific action request.
 * If the tag is inside a action tag, it will display if the validation errors are associated
 * with that action. If supplied a name attribute, it will display errors only if the name
 * attribute matches the name of the submitted action. Finally, if neither is the case, it
 * will always display as described in the paragraph above.</p>
 *
 * @author Greg Hinkle
 */
public class ErrorsTag extends HtmlTagSupport implements BodyTag {

    private final Log log = Log.getInstance(ErrorsTag.class);

    /** The header that will be emitted if no header is defined in the resource bundle. */
    public static final String DEFAULT_HEADER =
            "<div class=\"errorHeader\">Validation Errors</div><ul>";

    /** The footer that will be emitted if no footer is defined in the resource bundle. */
    public static final String DEFAULT_FOOTER = "</ul>";


    /**
     * True if this tag will display errors, otherwise false. This is determined by the logic
     * laid out in the class level Javadoc around whether this errors tag is for the action
     * that was submitted in the request.
     */
    private boolean display = false;

    /**
     * True if this tag contains a field-error child tag, which controls
     * the place of output of each error
     */
    private boolean nestedErrorTagPresent = false;

    /** Sets the form action for which errors should be displayed. */
    private String action;

    /** An optional attribute that declares a particular field to output errors for. */
    private String field;

    /** The collection of errors that match the filtering conditions */
    private SortedSet<ValidationError> allErrors;

    /** An iterator of the list of matched errors */
    private Iterator<ValidationError> errorIterator;

    /** The error displayed in the current iteration */
    private ValidationError currentError;

    /** An index of the error being displayed - zero based */
    private int index = 0;


    /**
     * Called by the IndividualErrorTag to fetch the current error from the set being iterated.
     *
     * @return The error displayed for this iteration of the errors tag
     */
    public ValidationError getCurrentError() {
        this.nestedErrorTagPresent = true;
        return currentError;
    }

    /**
     * Returns true if the error displayed is the first matching error.
     */
    public boolean isFirst() {
        return (this.allErrors.first() == this.currentError);
    }

    /**
     * Returns true if the error displayed is the last matching error.
     */
    public boolean isLast() {
        return (this.allErrors.last() == currentError);
    }

    /** Sets the (optional) action of the form to display errors for, if they exist. */
    public void setAction(String action) {
        this.action = action;
    }

    /** Returns the value set with setAction(). */
    public String getAction() {
        return this.action;
    }

    /** Sets the (optional) name of a field to display errors for, if errors exist. */
    public void setField(String field) {
        this.field = field;
    }

    /** Gets the value set with setField(). */
    public String getField() {
        return field;
    }


    /**
     * Determines if the tag should display errors based on the action that it is displaying for,
     * and then fetches the appropriate list of errors and makes sure it is non-empty.
     *
     * @return SKIP_BODY if the errors are not to be output, or there aren't any<br/>
     *         EVAL_BODY_TAG if there are errors to display
     */
    public int doStartTag() throws JspException {

        // TODO: Find a way to access the action resolver to ensure we're getting the right action name
        HttpServletRequest request = (HttpServletRequest) getPageContext().getRequest();
        String action = (String) request.getAttribute(ActionResolver.RESOLVED_ACTION);

        if (getAction() != null) {
            // The errors tag was supplied a action name, see if it is the one submitted in the req
            if (getAction().equals(action)) {
                this.display = true;
            }
        } else {
            // See if the enclosing action tag (if any) is the one submitted
            FormTag formTag = getParentTag(FormTag.class);
            if (formTag != null) {
                if (formTag.getAction().equals(action)) {
                    this.display = true;
                }
            }
            // Else if no name was set, and we're not in a action tag, we're global, so display
            else {
                this.display = true;
            }
        }

        // If we think we're going to display, go off and find the set of errors to
        // display and see if there are any
        if (this.display) {
            ValidationErrors validationErrors = null;
            ActionBean bean = getActionBean();
            if (bean != null) {
                validationErrors = bean.getContext().getValidationErrors();
            }

            if (validationErrors == null || validationErrors.size() == 0) {
                this.display = false;
            }
            else {
                // Using a set ensures that duplicate messages get filtered out, which can
                // happen during multi-row validation
                this.allErrors = new TreeSet<ValidationError>(new ErrorComparator());

                if (this.field != null) {
                    // we're filtering for a specific field
                    List<ValidationError> fieldErrors = validationErrors.get(this.field);
                    if (fieldErrors != null) {
                        this.allErrors.addAll(fieldErrors);
                    }

                    if (this.allErrors.size() == 0) {
                        this.display = false;
                        return SKIP_BODY;
                    }
                }
                else {
                    for (List<ValidationError> fieldErrors : validationErrors.values()) {
                        if (fieldErrors != null) {
                            this.allErrors.addAll(fieldErrors);
                        }
                    }
                }

                // Setup the objects needed for iteration
                this.errorIterator = this.allErrors.iterator();
                this.currentError = this.errorIterator.next(); // load up the first error
                return EVAL_BODY_BUFFERED;
            }
        }

        return SKIP_BODY;
    }


    /**
     * Sets the context variables for the current error and index
     */
    public void doInitBody() throws JspException {
        // Apply TEI attributes
        getPageContext().setAttribute("index", this.index);
        getPageContext().setAttribute("error", this.currentError);
    }


    /**
     * Manages iteration, running again if there are more errors to display.  If there is no
     * nested FieldError tag, will ensure that the body is evaluated only once.
     *
     * @return EVAL_BODY_TAG if there are more errors to display, SKIP_BODY otherwise
     */
    public int doAfterBody() throws JspException {
        if (this.display && this.nestedErrorTagPresent && this.errorIterator.hasNext()) {
            this.currentError = this.errorIterator.next();
            this.index++;

            // Reapply TEI attributes
            getPageContext().setAttribute("index", this.index);
            getPageContext().setAttribute("error", this.currentError);
            return EVAL_BODY_BUFFERED;
        }
        else {
            return SKIP_BODY;
        }
    }


    /**
     * Output the error list if this was an empty body tag and we're fully controlling output*
     *
     * @return EVAL_PAGE always
     * @throws JspException
     */
    public int doEndTag() throws JspException {
        try {
            JspWriter writer = getPageContext().getOut();

            if (this.display && !this.nestedErrorTagPresent) {
                // Output all errors in a standard format
                Locale locale = getPageContext().getRequest().getLocale();
                ResourceBundle bundle = StripesFilter.getConfiguration()
                        .getLocalizationBundleFactory().getErrorMessageBundle(locale);

                // Fetch the header and footer
                String header, footer, openElement, closeElement;
                try { header = bundle.getString("stripes.errors.header"); }
                catch (MissingResourceException mre) { header = DEFAULT_HEADER; }

                try { footer = bundle.getString("stripes.errors.footer"); }
                catch (MissingResourceException mre) { footer = DEFAULT_FOOTER; }

                try { openElement = bundle.getString("stripes.errors.beforeError"); }
                catch (MissingResourceException mre) { openElement = "<li>"; }

                try { closeElement = bundle.getString("stripes.errors.afterError"); }
                catch (MissingResourceException mre) { closeElement = "</li>"; }

                // Write out the error messages
                writer.write(header);

                for (ValidationError fieldError : this.allErrors) {
                    writer.write(openElement);
                    writer.write(fieldError.getMessage(locale));
                    writer.write(closeElement);
                }

                writer.write(footer);
            }
            else if (this.display && this.nestedErrorTagPresent) {
                // Output the collective body content
                getBodyContent().writeOut(writer);
            }

            // Reset the instance state in case the container decides to pool the tag
            this.display = false;
            this.nestedErrorTagPresent = false;
            this.field = null;
            this.allErrors = null;
            this.errorIterator = null;
            this.currentError = null;
            this.index = 0;

            return EVAL_PAGE;
        }
        catch (IOException e) {
            JspException jspe = new JspException("IOException encountered while writing errors " +
                    "tag to the JspWriter.", e);
            log.warn(jspe);
            throw jspe;
        }
    }

    /**
     * Fetches the ActionBean associated with the action if one is present.  An ActionBean will not
     * be created (and hence not present) by default.  An ActionBean will only be present if the
     * current request got bound to the same ActionBean as the current action uses.  E.g. if we are
     * re-showing the page as the result of an error, or the same ActionBean is used for a
     * &quot;pre-Action&quot; and the &quot;post-action&quot;.
     *
     * @return ActionBean the ActionBean bound to the action if there is one
     */
    protected ActionBean getActionBean() {
        return (ActionBean) getPageContext().getRequest().
                getAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN);
    }

    /**
     * Inner class Comparator used to provide a consistent ordering of validation errors.
     * Sorting is done by field name (the programmatic one, not the user visible one). Errors
     * without field names sort to the top since it is assumed that these are global errors
     * as oppose to field specific ones.
     */
    private static class ErrorComparator implements Comparator<ValidationError> {
        public int compare(ValidationError e1, ValidationError e2) {
            if (e1.getFieldName() == null && e2.getFieldName() != null) {
                return -1;
            }
            if (e2.getFieldName() == null && e1.getFieldName() != null) {
                return 1;
            }
            if (e1.getFieldName() == null && e2.getFieldName() == null) {
                return 0;
            }

            return e1.getFieldName().compareTo(e2.getFieldName());
        }
    }
}
