package net.sourceforge.stripes.tag;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.controller.StripesConstants;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.validation.ValidationError;
import net.sourceforge.stripes.validation.ValidationErrors;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTag;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * <p>The errors tag has two modes, one where it displays all validation errors in a list
 * and a second mode when there is a single enclosed field-error tag that has no name attribute
 * in which case this tag iterates over the body, displaying each error in turn in place
 * of the field-error tag.</p>
 *
 * <p>This tag has several ways of being attached to the errors of a specific form request.
 * If the tag is inside a form tag, it will display if the validation errors are associated
 * with that form. If supplied a name attribute, it will display errors only if the name
 * attribute matches the name of the submitted form. Finally, if neither is the case, it
 * will always display as described in the paragraph above.</p>
 *
 * @author Greg Hinkle
 */
public class ErrorsTag extends HtmlTagSupport implements BodyTag {

    private final Log log = Log.getInstance(ErrorsTag.class);

    /**
     * True if this tag will display errors, otherwise false. This is determined by the logic
     * laid out in the class level Javadoc around whether this errors tag is for the form
     * that was submitted in the request.
     */
    private boolean display = false;

    /**
     * True if this tag contains a field-error child tag, which controls
     * the place of output of each error
     */
    private boolean nestedErrorTagPresent = false;

    /** Sets the name of the form for which errors should be displayed. */
    private String form;

    /** An optional attribute that declares a particular field to output errors for. */
    private String field;

    /** The list of errors that match the filtering conditions */
    private LinkedList<ValidationError> allErrors;

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
        return (this.allErrors.getFirst() == this.currentError);
    }

    /**
     * Returns true if the error displayed is the last matching error.
     */
    public boolean isLast() {
        return (this.allErrors.getLast() == currentError);
    }

    /** Sets the (optional) name of the form to display errors for, if they exist. */
    public void setForm(String form) {
        this.form = form;
    }

    /** Returns the value set with setForm(). */
    public String getForm() {
        return this.form;
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
     * Determines if the tag should display errors based on the form that it is displaying for,
     * and then fetches the appropriate list of errors and makes sure it is non-empty.
     *
     * @return SKIP_BODY if the errors are not to be output, or there aren't any<br/>
     *         EVAL_BODY_TAG if there are errors to display
     */
    public int doStartTag() throws JspException {

        // TODO: Find a way to access the action resolver to ensure we're getting the right action name
        String actionName =
                getPageContext().getRequest().getParameter(StripesConstants.URL_KEY_FORM_NAME);

        if (getForm() != null) {
            // The errors tag was supplied a form name, see if it is the one submitted in the req
            if (getForm().equals(actionName)) {
                this.display = true;
            }
        } else {
            // See if the enclosing form tag (if any) is the one submitted
            FormTag formTag = getParentTag(FormTag.class);
            if (formTag != null) {
                if (formTag.getName().equals(actionName)) {
                    this.display = true;
                }
            }
            // Else if no name was set, and we're not in a form tag, we're global, so display
            else {
                this.display = true;
            }
        }

        // If we think we're going to display, go off and find the set of errors to
        // display and see if there are any
        if (this.display) {
            ValidationErrors validationErrors = getActionBean().getContext().getValidationErrors();
            if (validationErrors == null) {
                this.display = false;
            }
            else {
                this.allErrors = new LinkedList<ValidationError>();

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
                errorIterator = allErrors.iterator();
                currentError = errorIterator.next(); // load up the first error
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

                writer.write("<div class=\"errorHeader\">");
                writer.write("Validation Errors");
                writer.write("</div>");

                writer.write("<ul>");

                for (ValidationError fieldError : this.allErrors) {
                    writer.write("<li>");
                    writer.write(fieldError.getMessage(locale));
                    writer.write("</li>");
                }

                writer.write("</ul>");
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
     * Fetches the ActionBean associated with the form if one is present.  An ActionBean will not
     * be created (and hence not present) by default.  An ActionBean will only be present if the
     * current request got bound to the same ActionBean as the current form uses.  E.g. if we are
     * re-showing the page as the result of an error, or the same ActionBean is used for a
     * &quot;pre-Action&quot; and the &quot;post-action&quot;.
     *
     * @return ActionBean the ActionBean bound to the form if there is one
     */
    protected ActionBean getActionBean() {
        return (ActionBean) getPageContext().getRequest().
                getAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN);
    }


}
