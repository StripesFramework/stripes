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

import net.sourceforge.stripes.controller.StripesConstants;
import net.sourceforge.stripes.exception.StripesJspException;

import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.JspException;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Tag for generating links to pages or ActionBeans within a Stripes application. Provides
 * basic services such as including the context path at the start of the href URL, and
 * including a parameter to name the source page from which the link came. Also provides the
 * ability to add complex parameters to the URL through the use of nested LinkParam tags.
 *
 * @see LinkParamTag
 * @author Tim Fennell
 */
public class LinkTag extends HtmlTagSupport implements BodyTag {
    private Map<String,Object> parameters = new HashMap<String,Object>();

    /**
     * Used by stripes:link-param tags (and possibly other tags at some distant point in
     * the future) to add a parameter to the parent link tag.
     *
     * @param name the name of the parameter(s) to add
     * @param valueOrValues
     */
    public void addParameter(String name, Object valueOrValues) {
        this.parameters.put(name, valueOrValues);
    }

    /**
     * Does nothing.
     * @return EVAL_BODY_BUFFERED in all cases
     */
    public int doStartTag() throws JspException {
        return EVAL_BODY_BUFFERED;
    }

    /** Does nothing. */
    public void doInitBody() throws JspException { /* Do Nothing. */ }

    /**
     * Does nothing.
     * @return SKIP_BODY in all cases
     */
    public int doAfterBody() throws JspException {
        return SKIP_BODY;
    }

    /**
     * Prepends the context to the href attibute if necessary, and then folds all the
     * registered parameters into the URL.
     *
     * @return EVAL_PAGE in all cases
     * @throws JspException
     */
    public int doEndTag() throws JspException {
        HttpServletRequest request = (HttpServletRequest) getPageContext().getRequest();
        String originalHref = getHref(); // Save for later, so we can restore the value

        if (originalHref != null) {
            StringBuilder href = new StringBuilder(256);

            // Append the context path, but only if the user didn't already
            if (!originalHref.startsWith(request.getContextPath())) {
                href.append(request.getContextPath());
            }
            href.append(originalHref);

            // Add in the source page
            if (href.indexOf("?") == -1) {
                href.append('?');
            }
            else {
                href.append('&');
            }
            href.append(StripesConstants.URL_KEY_SOURCE_PAGE);
            href.append('=');
            href.append(request.getServletPath());

            // For all the parameters registered by nested LinkParam tags, re-write the URL
            for (Map.Entry<String,Object> parameter : this.parameters.entrySet()) {
                String name = parameter.getKey();
                Object valueOrValues = parameter.getValue();

                if (valueOrValues == null) {
                    appendParameter(href, name, valueOrValues);
                }
                else if (valueOrValues.getClass().isArray()) {
                    Object[] values = (Object[]) valueOrValues;
                    for (Object value : values) {
                        appendParameter(href, name, value);
                    }
                }
                else if (valueOrValues instanceof Collection) {
                    Collection values = (Collection) valueOrValues;
                    for (Object value : values) {
                        appendParameter(href, name, value);
                    }
                }
                else {
                    appendParameter(href, name, valueOrValues);
                }
            }

            setHref(href.toString());
        }

        try {
            writeOpenTag(getPageContext().getOut(), "a");
            String body = getBodyContentAsString();
            if (body != null) {
                getPageContext().getOut().write(body.trim());
            }
            writeCloseTag(getPageContext().getOut(), "a");
        }
        catch (IOException ioe) {
            throw new StripesJspException("IOException while writing output in LinkTag.", ioe);
        }

        // Restore state and go on with the page
        setHref(originalHref);
        this.parameters.clear();
        return EVAL_PAGE;
    }

    /**
     * Appends the value of a scalar parameter to the URL. Checks to see if the value is
     * null, and if so generates a parameter with no value.  URL Encodes the parameter
     * to make sure it is safe to shove into the URL.
     */
    protected void appendParameter(StringBuilder href, String name, Object value)
        throws StripesJspException {
        try {
            href.append('&');
            href.append(name);
            href.append('=');
            if (value != null) {
                href.append( URLEncoder.encode(value.toString(), "UTF-8") );
            }
        }
        catch (UnsupportedEncodingException uee) {
            throw new StripesJspException("Unsupported encoding?  UTF-8?  That's unpossible.");
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Additional HTML Attributes supported by the tag
    ///////////////////////////////////////////////////////////////////////////
    public void   setCharset(String charset) { set("charset", charset); }
    public String getCharset() { return get("charset"); }

    public void   setCoords(String coords) { set("coords", coords); }
    public String getCoords() { return get("coords"); }

    public void   setHref(String href) { set("href", href); }
    public String getHref() { return get("href"); }

    public void   setHreflang(String hreflang) { set("hreflang", hreflang); }
    public String getHreflang() { return get("hreflang"); }

    public void   setName(String name) { set("name", name); }
    public String getName() { return get("name"); }

    public void   setRel(String rel) { set("rel", rel); }
    public String getRel() { return get("rel"); }

    public void   setRev(String rev) { set("rev", rev); }
    public String getRev() { return get("rev"); }

    public void   setShape(String shape) { set("shape", shape); }
    public String getShape() { return get("shape"); }

    public void   setTarget(String target) { set("target", target); }
    public String getTarget() { return get("target"); }

    public void   setType(String type) { set("type", type); }
    public String getType() { return get("type"); }
}
