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

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.BodyTag;
import java.io.IOException;

/**
 * Tag for generating links to pages or ActionBeans within a Stripes
 * application. Provides basic services such as including the context path at
 * the start of the href URL (only when the URL starts with a '/' and does not
 * contain the context path already), and including a parameter to name the
 * source page from which the link came. Also provides the ability to add
 * complex parameters to the URL through the use of nested Param tags.
 *
 * @see ParamTag
 * @author Tim Fennell
 */
public class LinkTag extends LinkTagSupport implements BodyTag {

    /**
     * Does nothing.
     *
     * @return EVAL_BODY_BUFFERED in all cases
     * @throws jakarta.servlet.jsp.JspException
     */
    @Override
    public int doStartTag() throws JspException {
        return EVAL_BODY_BUFFERED;
    }

    /**
     * Does nothing.
     * @throws jakarta.servlet.jsp.JspException
     */
    public void doInitBody() throws JspException {
        /* Do Nothing. */ }

    /**
     * Does nothing.
     *
     * @return SKIP_BODY in all cases
     * @throws jakarta.servlet.jsp.JspException
     */
    public int doAfterBody() throws JspException {
        return SKIP_BODY;
    }

    /**
     * Prepends the context to the href attribute if necessary, and then folds
     * all the registered parameters into the URL.
     *
     * @return EVAL_PAGE in all cases
     * @throws JspException
     */
    @Override
    public int doEndTag() throws JspException {
        try {
            set("href", buildUrl());
            writeOpenTag(getPageContext().getOut(), "a");
            String body = getBodyContentAsString();
            if (body == null || body.trim().length() == 0) {
                body = get("href");
            }
            if (body != null) {
                getPageContext().getOut().write(body.trim());
            }
            writeCloseTag(getPageContext().getOut(), "a");
        } catch (IOException ioe) {
            throw new StripesJspException("IOException while writing output in LinkTag.", ioe);
        }

        // Restore state and go on with the page
        getAttributes().remove("href");
        clearParameters();
        return EVAL_PAGE;
    }

    /**
     * Pass through to {@link LinkTagSupport#setUrl(String)}.
     * @param href
     */
    public void setHref(String href) {
        setUrl(href);
    }

    /**
     * Pass through to {@link LinkTagSupport#getUrl()}.
     * @return 
     */
    public String getHref() {
        return getUrl();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Additional HTML Attributes supported by the tag
    ///////////////////////////////////////////////////////////////////////////

    /**
     *
     * @param charset
     */
    public void setCharset(String charset) {
        set("charset", charset);
    }

    /**
     *
     * @return
     */
    public String getCharset() {
        return get("charset");
    }

    /**
     *
     * @param coords
     */
    public void setCoords(String coords) {
        set("coords", coords);
    }

    /**
     *
     * @return
     */
    public String getCoords() {
        return get("coords");
    }

    /**
     *
     * @param hreflang
     */
    public void setHreflang(String hreflang) {
        set("hreflang", hreflang);
    }

    /**
     *
     * @return
     */
    public String getHreflang() {
        return get("hreflang");
    }

    /**
     *
     * @param name
     */
    public void setName(String name) {
        set("name", name);
    }

    /**
     *
     * @return
     */
    public String getName() {
        return get("name");
    }

    /**
     *
     * @param rel
     */
    public void setRel(String rel) {
        set("rel", rel);
    }

    /**
     *
     * @return
     */
    public String getRel() {
        return get("rel");
    }

    /**
     *
     * @param rev
     */
    public void setRev(String rev) {
        set("rev", rev);
    }

    /**
     *
     * @return
     */
    public String getRev() {
        return get("rev");
    }

    /**
     *
     * @param shape
     */
    public void setShape(String shape) {
        set("shape", shape);
    }

    /**
     *
     * @return
     */
    public String getShape() {
        return get("shape");
    }

    /**
     *
     * @param target
     */
    public void setTarget(String target) {
        set("target", target);
    }

    /**
     *
     * @return
     */
    public String getTarget() {
        return get("target");
    }

    /**
     *
     * @param type
     */
    public void setType(String type) {
        set("type", type);
    }

    /**
     *
     * @return
     */
    public String getType() {
        return get("type");
    }
}
