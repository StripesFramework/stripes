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
import net.sourceforge.stripes.controller.StripesConstants;
import net.sourceforge.stripes.exception.StripesJspException;
import net.sourceforge.stripes.util.HtmlUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTag;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p>Form tag for use with the Stripes framework.  Supports all of the HTML attributes applicable
 * to the form tag, with one exception: due to Java method naming restrictions accept-charset is
 * specified as acceptcharset (but will be rendered correctly in the output HTML).</p>
 *
 * @author Tim Fennell
 */
public class FormTag extends HtmlTagSupport implements BodyTag {

    /** Stores the value of the action attribute before the context gets appended. */
    private String actionWithoutContext;

    /** A map of field name to field type for all fields registered with the form. */
    private Map<String,Class> fieldsPresent = new HashMap<String,Class>();

    /**
     * Sets the action for the form.  If the form action begins with a slash, the context path of
     * the web application will get prepended to the action before it is set. In general actions
     * should be specified as &quot;absolute&quot; paths within the web application, therefore
     * allowing them to function correctly regardless of the address currently shown in the
     * browser&apos; address bar.
     *
     * @param action the action path, relative to the root of the web application
     */
    public void setAction(String action) {
        this.actionWithoutContext = action;

        if (action.startsWith("/")) {
            HttpServletRequest request = (HttpServletRequest) getPageContext().getRequest();
            String contextPath = request.getContextPath();

            if (contextPath != null) {
                action = contextPath + action;
            }
        }

        HttpServletResponse response = (HttpServletResponse) getPageContext().getResponse();
        set("action", response.encodeURL(action));
    }

    public String getAction() { return this.actionWithoutContext; }

    ////////////////////////////////////////////////////////////
    // Additional attributes specific to the form tag
    ////////////////////////////////////////////////////////////
    public void   setAccept(String accept) { set("accept", accept); }
    public String getAccept() { return get("accept"); }

    public void   setAcceptCharset(String acceptCharset) { set("accept-charset", acceptCharset); }
    public String getAcceptCharset() { return get("accept-charset"); }

    public void   setEnctype(String enctype) { set("enctype", enctype); }
    public String getEnctype() { return get("enctype"); };

    public void   setMethod(String method) { set("method", method); }
    public String getMethod() { return get("method"); }

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
     * Does nothing except return EVAL_BODY_BUFFERED.  Everything of interest happens in doEndTag.
     */
    public int doStartTag() throws JspException {
        getTagStack().push(this);
        return EVAL_BODY_BUFFERED;
    }

    /** Np-op method. */
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
    public int doEndTag() throws JspException {
        try {
            JspWriter out = getPageContext().getOut();
            writeOpenTag(out, "form");

            // Write out a hiddien field with the name of the page in it....
            // The div is necessary in order to be XHTML compliant, where a form can contain
            // only block level elements (which seems stupid, but whatever).
            out.write("<div style=\"display: none;\">");
            out.write("<input type=\"hidden\" name=\"");
            out.write(StripesConstants.URL_KEY_SOURCE_PAGE);
            out.write("\" value=\"");
            HttpServletRequest request = (HttpServletRequest) getPageContext().getRequest();
            out.write( request.getServletPath());
            out.write("\" />");

            writeFieldsPresentHiddenField(out);
            out.write("</div>");

            getBodyContent().writeOut( getPageContext().getOut() );
            writeCloseTag(getPageContext().getOut(), "form");

            // Clean up any state the container won't reset during tag pooling
            this.fieldsPresent.clear();
        }
        catch (IOException ioe) {
            throw new StripesJspException("IOException in FormTag.doEndTag().", ioe);
        }
        finally {
            getTagStack().pop();
        }

        return EVAL_PAGE;
    }

    /**
     * Examines the form tag to determine what fields are present in the form and might
     * not get submitted to the server. Writes out a hidden field that contains the names
     * of all those fields so that we can detect non-submission when the request comes back.
     * Outputs only the names of checkboxes and select tags as these are the only HTML input
     * types that do not get submitted if no value is chosen.
     *
     * @param out the output  writer into which the hidden tag should be written
     * @throws IOException if the writer throws one
     */
    protected void writeFieldsPresentHiddenField(JspWriter out) throws IOException {
        // Write out an encoded list of the field names in the form
        Set<String> namesToInclude = new HashSet<String>();
        for (Map.Entry<String,Class> entry : this.fieldsPresent.entrySet()) {
            Class fieldClass = entry.getValue();
            if (InputSelectTag.class.isAssignableFrom(fieldClass)
                    || InputCheckBoxTag.class.isAssignableFrom(fieldClass)) {
                namesToInclude.add(entry.getKey());
            }
        }

        String hiddenFieldValue = HtmlUtil.combineValues(namesToInclude);
        out.write("<input type=\"hidden\" name=\"");
        out.write(StripesConstants.URL_KEY_FIELDS_PRESENT);
        out.write("\" value=\"");
        out.write(hiddenFieldValue);
        out.write("\" />");
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
        return (ActionBean) getPageContext().getRequest().getAttribute(this.actionWithoutContext);
    }

    /**
     * Used by nested tags to notify the form that a field with the specified name has been
     * written to the form.
     *
     * @param tag the input field tag being registered
     */
    public void registerField(InputTagSupport tag) {
        this.fieldsPresent.put(tag.getName(), tag.getClass());
    }

    /**
     * Gets the set of all field names for which fields have been refered withing the form up
     * until the point of calling this method. If this is called during doEndTag it will contain
     * all field names, if it is called during the body of the tag it will only contain the
     * input elements which have been processed up until that point.
     *
     * @return Set<String> - the set of field names seen so far
     */
    public Set<String> getRegisteredFields() {
        return this.fieldsPresent.keySet();
    }
}
