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

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.Wizard;
import net.sourceforge.stripes.controller.StripesConstants;
import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.controller.ActionResolver;
import net.sourceforge.stripes.exception.StripesJspException;
import net.sourceforge.stripes.util.CryptoUtil;
import net.sourceforge.stripes.util.HtmlUtil;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.util.UrlBuilder;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationError;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.TryCatchFinally;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;

/**
 * <p>Form tag for use with the Stripes framework.  Supports all of the HTML attributes applicable
 * to the form tag, with one exception: due to JSP attribute naming restrictions accept-charset is
 * specified as acceptcharset (but will be rendered correctly in the output HTML).</p>
 *
 * @author Tim Fennell
 */
public class FormTag extends HtmlTagSupport implements BodyTag, TryCatchFinally, ParameterizableTag {
    /** Log used to log error and debugging information for this class. */
    private static final Log log = Log.getInstance(FormTag.class);

    /** Stores the field name (or magic values ''/'first') to set focus on. */
    private String focus;
    private boolean focusSet = false;
    private boolean partial = false;
    private String enctype = null;
    private String method = null;

    /** Stores the value of the action attribute before the context gets appended. */
    private String actionWithoutContext;

    /** Stores the value of the beanclass attribute. */
    private Object beanclass;

    /**
     * The {@link ActionBean} class to which the form will submit, as determined by the
     * {@link ActionResolver}. This may be null if the action attribute is set but its value does
     * not resolve to an {@link ActionBean}.
     */
    private Class<? extends ActionBean> actionBeanClass;

    /** Builds the action attribute with parameters */
    private UrlBuilder urlBuilder;

    /** A map of field name to field type for all fields registered with the form. */
    private Map<String,Class<?>> fieldsPresent = new HashMap<String,Class<?>>();

    /**
     * Sets the action for the form.  If the form action begins with a slash, and does not
     * already contain the context path, then the context path of the web application will get
     * prepended to the action before it is set. In general actions should be specified as
     * &quot;absolute&quot; paths within the web application, therefore allowing them to function
     * correctly regardless of the address currently shown in the browser&apos;s address bar.
     *
     * @param action the action path, relative to the root of the web application
     */
    public void setAction(String action) {
        this.actionWithoutContext = action;
    }

    public String getAction() { return this.actionWithoutContext; }

    /** Get the URL binding for the form's {@link ActionBean} from the {@link ActionResolver}. */
    protected String getActionBeanUrlBinding() {
        ActionResolver resolver = StripesFilter.getConfiguration().getActionResolver();
        if (actionBeanClass == null) {
            String binding = resolver.getUrlBindingFromPath(actionWithoutContext);
            if (binding == null)
                binding = actionWithoutContext;
            return binding;
        }
        else {
            return resolver.getUrlBinding(actionBeanClass);
        }
    }

    /** Lazily looks up and returns the type of action bean the form will submit to. */
    protected Class<? extends ActionBean> getActionBeanClass() {
        if (this.actionBeanClass == null) {
            ActionResolver resolver = StripesFilter.getConfiguration().getActionResolver();
            this.actionBeanClass = resolver.getActionBeanType(getActionBeanUrlBinding());
        }

        return this.actionBeanClass;
    }

    /**
     * Sets the 'action' attribute by inspecting the bean class provided and asking the current
     * ActionResolver what the appropriate URL is.
     *
     * @param beanclass the String FQN of the class, or a Class representing the class
     * @throws StripesJspException if the URL cannot be determined for any reason, most likely
     *         because of a mis-spelled class name, or a class that's not an ActionBean
     */
    public void setBeanclass(Object beanclass) throws StripesJspException {
        this.beanclass = beanclass;

        String url = getActionBeanUrl(beanclass);
        if (url == null) {
            throw new StripesJspException("Could not determine action from 'beanclass' supplied. " +
                "The value supplied was '" + beanclass + "'. Please ensure that this bean type " +
                "exists and is in the classpath. If you are developing a page and the ActionBean " +
                "does not yet exist, consider using the 'action' attribute instead for now.");
        }
        else {
            setAction(url);
        }
    }

    /** Corresponding getter for 'beanclass', will always return null. */
    public Object getBeanclass() { return null; }

    /** Sets the name of the field that should receive focus when the form is rendered. */
    public void setFocus(String focus) { this.focus = focus; }
    /** Gets the name of the field that should receive focus when the form is rendered. */
    public String getFocus() { return focus; }

    /** Gets the flag that indicates if this is a partial form. */
    public boolean isPartial() { return partial; }
    /** Sets the flag that indicates if this is a partial form. */
    public void setPartial(boolean partial) { this.partial = partial; }

    /** Sets the form encoding. */
    public void setEnctype(String enctype) { this.enctype = enctype; }
    /** Gets the form encoding. */
    public String getEnctype() { return enctype; }

    /** Sets the HTTP method to use when the form is submitted. */
    public void setMethod(String method) { this.method = method; }
    /** Gets the HTTP method to use when the form is submitted. */
    public String getMethod() { return method; }

    ////////////////////////////////////////////////////////////
    // Additional attributes specific to the form tag
    ////////////////////////////////////////////////////////////
    public void   setAccept(String accept) { set("accept", accept); }
    public String getAccept() { return get("accept"); }

    public void   setAcceptcharset(String acceptCharset) { set("accept-charset", acceptCharset); }
    public String getAcceptcharset() { return get("accept-charset"); }

    public void   setName(String name) { set("name", name); }
    public String getName() { return get("name"); }

    public void   setTarget(String target) { set("target", target); }
    public String getTarget() { return get("target"); }

    public void   setOnreset(String onreset) { set("onreset", onreset); }
    public String getOnreset() { return get("onreset"); }

    public void   setOnsubmit(String onsubmit) { set("onsubmit", onsubmit); }
    public String getOnsubmit() { return get("onsubmit"); }

    ////////////////////////////////////////////////////////////
    // TAG methods
    ////////////////////////////////////////////////////////////

    /**
     * Does sanity checks and returns EVAL_BODY_BUFFERED. Everything else of interest happens in
     * doEndTag.
     */
    @Override
    public int doStartTag() throws JspException {
        if (this.actionWithoutContext == null) {
            throw new StripesJspException("The form tag attributes 'beanClass' and 'action' "
                    + "are both null. One of the two must be supplied to determine which "
                    + "action bean should handle the form submission.");
        }
        getTagStack().push(this);
        urlBuilder = new UrlBuilder(pageContext.getRequest().getLocale(), getAction(), false)
                .setEvent(null);
        return EVAL_BODY_BUFFERED;
    }

    /** No-op method. */
    public void doInitBody() throws JspException { }

    /** Just returns SKIP_BODY so that the body is included only once. */
    public int doAfterBody() throws JspException {
        return SKIP_BODY;
    }

    /**
     * Writes things out in the following order:
     * <ul>
     *   <li>The form open tag</li>
     *   <li>Hidden fields for the form name and source page</li>
     *   <li>The buffered body content</li>
     *   <li>The form close tag</li>
     * </ul>
     *
     * <p>All of this is done in doEndTag to allow form elements to modify the form tag itself if
     * necessary.  A prime example of this is the InputFileTag, which needs to ensure that the form
     * method is POST and the enctype is correct.</p>
     */
    @Override
    public int doEndTag() throws JspException {
        try {
            // Default the method to post
            if (getMethod() == null) {
                setMethod("post");
            }

            set("method", getMethod());
            set("enctype", getEnctype());
            set("action", buildAction());

            JspWriter out = getPageContext().getOut();
            if (!isPartial()) {
                writeOpenTag(out, "form");
            }
            if (getBodyContent() != null) {
                getBodyContent().writeOut( getPageContext().getOut() );
            }

            if (!isPartial()) {
                writeHiddenTags(out);
                writeCloseTag(getPageContext().getOut(), "form");
            }

            // Write out a warning if focus didn't find a field
            if (this.focus != null && !this.focusSet) {
                log.error("Form with action [", getAction(), "] has 'focus' set to '", this.focus,
                          "', but did not find a field with matching name to set focus on.");
            }

            // Clean up any state that we've modified during tag processing, so that the container
            // can use tag pooling
            this.actionBeanClass = null;
            this.fieldsPresent.clear();
            this.focusSet = false;
            this.urlBuilder = null;
        }
        catch (IOException ioe) {
            throw new StripesJspException("IOException in FormTag.doEndTag().", ioe);
        }

        return EVAL_PAGE;
    }

    /** Rethrows the passed in throwable in all cases. */
    public void doCatch(Throwable throwable) throws Throwable { throw throwable; }

    /**
     * Used to ensure that the form is always removed from the tag stack so that there is
     * never any confusion about tag-parent hierarchies.
     */
    public void doFinally() {
        try { getTagStack().pop(); }
        catch (Throwable t) {
            /* Suppress anything, because otherwise this might mask any causal exception. */
        }
    }

    /** Write out hidden tags that are internally required by Stripes to process request. */
    protected void writeHiddenTags(JspWriter out) throws IOException, JspException {
        /*
         * The div is necessary in order to be XHTML compliant, where a form can contain only block
         * level elements (which seems stupid, but whatever).
         */
        out.write("<div style=\"display: none;\">");
        writeSourcePageHiddenField(out);
        if (isWizard()) {
            writeWizardFields();
        }
        writeFieldsPresentHiddenField(out);
        out.write("</div>");
    }

    /** Write out a hidden field with the name of the page in it. */
    protected void writeSourcePageHiddenField(JspWriter out) throws IOException {
        out.write("<input type=\"hidden\" name=\"");
        out.write(StripesConstants.URL_KEY_SOURCE_PAGE);
        out.write("\" value=\"");
        out.write(getSourcePageValue());
        out.write("\" />");
    }

    /** Get the encrypted value for the hidden _sourcePage field. */
    protected String getSourcePageValue() {
        HttpServletRequest request = (HttpServletRequest) getPageContext().getRequest();
        return CryptoUtil.encrypt(request.getServletPath());
    }

    /**
     * <p>In general writes out a hidden field notifying the server exactly what fields were
     * present on the page.  Exact behaviour depends upon whether or not the current form
     * is a wizard or not. When the current form is <b>not</b> a wizard this method examines
     * the form tag to determine what fields present in the form might not get submitted to
     * the server (e.g. checkboxes, selects), writes out a hidden field that contains the names
     * of all those fields so that we can detect non-submission when the request comes back.</p>
     *
     * <p>In the case of a wizard form the value output is the full list of all fields that were
     * present on the page. This is done because the list is used to drive required field
     * validation knowing that in a wizard required fields may be spread across several pages.</p>
     *
     * <p>In both cases the value is encrypted to stop the user maliciously spoofing the value.</p>
     *
     * @param out the output  writer into which the hidden tag should be written
     * @throws IOException if the writer throws one
     */
    protected void writeFieldsPresentHiddenField(JspWriter out) throws IOException {
        out.write("<input type=\"hidden\" name=\"");
        out.write(StripesConstants.URL_KEY_FIELDS_PRESENT);
        out.write("\" value=\"");
        out.write(getFieldsPresentValue());
        out.write("\" />");
    }

    /** Get the encrypted value of the __fp hidden field. */
    protected String getFieldsPresentValue() {
        // Figure out what set of names to include
        Set<String> namesToInclude = new HashSet<String>();

        if (isWizard()) {
            namesToInclude.addAll(this.fieldsPresent.keySet());
        }
        else {
            for (Map.Entry<String,Class<?>> entry : this.fieldsPresent.entrySet()) {
                Class<?> fieldClass = entry.getValue();
                if (InputSelectTag.class.isAssignableFrom(fieldClass)
                        || InputCheckBoxTag.class.isAssignableFrom(fieldClass)) {
                    namesToInclude.add(entry.getKey());
                }
            }
        }

        // Combine the names into a delimited String and encrypt it
        String hiddenFieldValue = HtmlUtil.combineValues(namesToInclude);
        return CryptoUtil.encrypt(hiddenFieldValue);
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
		String binding = getActionBeanUrlBinding();
		HttpServletRequest request = (HttpServletRequest) getPageContext().getRequest();
		ActionBean bean = (ActionBean) request.getAttribute(binding);
		if (bean == null) {
			HttpSession session = request.getSession(false);
			if (session != null)
				bean = (ActionBean) session.getAttribute(binding);
		}
		return bean;
	}

    /**
     * Returns true if the ActionBean this form posts to represents a Wizard action bean and
     * false in all other situations.  If the form cannot determine the ActionBean being posted
     * to for any reason it will return false.
     */
    protected boolean isWizard() {
        ActionBean bean = getActionBean();
        Class<? extends ActionBean> clazz = null;
        if (bean == null) {
            clazz = getActionBeanClass();

            if (clazz == null) {
                log.error("Could not locate an ActionBean that was bound to the URL [",
                          this.actionWithoutContext, "]. Without an ActionBean class Stripes ",
                          "cannot determine whether the ActionBean is a wizard or not. ",
                          "As a result wizard behaviour will be disabled.");
                return false;
            }
        }
        else {
            clazz = bean.getClass();
        }

        return clazz.getAnnotation(Wizard.class) != null;
    }

    /**
     * Writes out hidden fields for all fields that are present in the request but are not
     * explicitly present in this form.  Excludes any fields that have special meaning to
     * Stripes and are not really application data.  Uses the stripes:wizard-fields tag to
     * do the grunt work.
     */
    protected void writeWizardFields() throws JspException {
        WizardFieldsTag tag = new WizardFieldsTag();
        tag.setPageContext(getPageContext());
        tag.setParent(this);
        try {
            tag.doStartTag();
            tag.doEndTag();
        }
        finally {
            tag.doFinally();
            tag.release();
        }
    }

    /**
     * Used by nested tags to notify the form that a field with the specified name has been
     * written to the form.
     *
     * @param tag the input field tag being registered
     */
    public void registerField(InputTagSupport tag) {
        this.fieldsPresent.put(tag.getName(), tag.getClass());
        setFocusOnFieldIfRequired(tag);
    }

    /**
     * Checks to see if the field should receive focus either because it is the named
     * field for receiving focus, because it is the first field in the form (and first
     * field focus was specified), or because it is the first field in error.
     *
     * @param tag the input tag being registered with the form
     */
    protected void setFocusOnFieldIfRequired(InputTagSupport tag) {
        // Decide whether or not this field should be focused
        if (this.focus != null && !this.focusSet) {
            ActionBean bean = getActionBean();
            ValidationErrors errors = bean == null ? null : bean.getContext().getValidationErrors();

            // If there are validation errors, select the first field in error
            if (errors != null && errors.hasFieldErrors()) {
                List<ValidationError> fieldErrors = errors.get(tag.getName());
                if (fieldErrors != null && fieldErrors.size() > 0) {
                    tag.setFocus(true);
                    this.focusSet = true;
                }
            }
            // Else set the named field, or the first field if that's desired
            else if (this.focus.equals(tag.getName())) {
                    tag.setFocus(true);
                    this.focusSet = true;
            }
            else if ("".equals(this.focus) || "first".equalsIgnoreCase(this.focus)) {
                if ( !(tag instanceof InputHiddenTag) ) {
                    tag.setFocus(true);
                    this.focusSet = true;
                }
            }
        }
    }

    /**
     * Gets the set of all field names for which fields have been referred within the form up
     * until the point of calling this method. If this is called during doEndTag it will contain
     * all field names, if it is called during the body of the tag it will only contain the
     * input elements which have been processed up until that point.
     *
     * @return Set<String> - the set of field names seen so far
     */
    public Set<String> getRegisteredFields() {
        return this.fieldsPresent.keySet();
    }

    /**
     * Appends a parameter to the "action" attribute of the form tag. For clean URLs the value will
     * be embedded in the URL if possible. Otherwise, it will be added to the query string.
     * 
     * @param name the parameter name
     * @param valueOrValues the parameter value(s)
     * @see ParameterizableTag#addParameter(String, Object)
     */
    public void addParameter(String name, Object valueOrValues) {
        urlBuilder.addParameter(name, valueOrValues);
    }

    /**
     * Builds the action attribute, including the context path and any parameters.
     * 
     * @return the action attribute
     */
    protected String buildAction() {
        String action = urlBuilder.toString();
        if (action.startsWith("/")) {
            HttpServletRequest request = (HttpServletRequest) getPageContext().getRequest();
            String contextPath = request.getContextPath();

            // *Always* prepend the context path if "beanclass" was used
            // Otherwise, *only* prepend it if it is not already present
            if (contextPath.length() > 1
                    && (beanclass != null || !action.startsWith(contextPath + '/'))) {
                action = contextPath + action;
            }
        }
        HttpServletResponse response = (HttpServletResponse) getPageContext().getResponse();
        return response.encodeURL(action);
    }
}
