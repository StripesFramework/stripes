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
import net.sourceforge.stripes.util.UrlBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTag;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
        HttpServletResponse response = (HttpServletResponse) getPageContext().getResponse();
        String originalHref = getHref(); // Save for later, so we can restore the value

        if (originalHref != null) {
            String href = originalHref;

            // Append the context path, but only if the user didn't already
            if (!originalHref.startsWith(request.getContextPath())) {
                href = request.getContextPath() + href;
            }

            UrlBuilder builder = new UrlBuilder(href);
            builder.addParameter(StripesConstants.URL_KEY_SOURCE_PAGE, request.getServletPath());
            builder.addParameters(this.parameters);
            setHref(response.encodeURL(builder.toString()));
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
