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

import jakarta.servlet.jsp.tagext.BodyTag;
import jakarta.servlet.jsp.tagext.BodyContent;
import jakarta.servlet.jsp.tagext.Tag;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.JspException;

/**
 * <p>
 * Used to supply parameters when nested inside tags that implement
 * {@link ParameterizableTag}. The value is either obtained from the value
 * attribute, or if that is not present, then the body of the tag.</p>
 *
 * <p>
 * Once the value has been established the parent tag is looked for, and the
 * parameter is handed over to it.</p>
 *
 * <p>
 * Primarily used by the LinkTag and UrlTag.</p>
 *
 * @author Tim Fennell
 * @since Stripes 1.4
 * @see ParamTag
 */
public class ParamTag implements BodyTag {

    private String name;
    private Object value;
    private BodyContent bodyContent;
    private Tag parentTag;
    private PageContext pageContext;

    /**
     * Sets the value of the parameter(s) to be added to the URL.
     * @param value
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Gets the value attribute, as set with setValue().
     * @return 
     */
    public Object getValue() {
        return value;
    }

    /**
     * Sets the name of the parameter(s) that will be added to the URL.
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the name of the parameter(s) that will be added to the URL.
     * @return 
     */
    public String getName() {
        return name;
    }

    /**
     * Used by the container to set the contents of the body of the tag.
     * @param bodyContent
     */
    public void setBodyContent(BodyContent bodyContent) {
        this.bodyContent = bodyContent;
    }

    /**
     * Used by the container to set the page context for the tag.
     * @param pageContext
     */
    public void setPageContext(PageContext pageContext) {
        this.pageContext = pageContext;
    }

    /**
     * Used by the container to provide the tag with access to its parent tag on
     * the page.
     * @param tag
     */
    public void setParent(Tag tag) {
        this.parentTag = tag;
    }

    /**
     * Required spec method to allow others to access the parent of the tag.
     * @return 
     */
    public Tag getParent() {
        return this.parentTag;
    }

    /**
     * Does nothing.
     * @throws jakarta.servlet.jsp.JspException
     */
    public void doInitBody() throws JspException {
        /* Do Nothing */ }

    /**
     * Does nothing.
     *
     * @return SKIP_BODY in all cases.
     * @throws jakarta.servlet.jsp.JspException
     */
    public int doAfterBody() throws JspException {
        return SKIP_BODY;
    }

    /**
     * Does nothing.
     *
     * @return EVAL_BODY_BUFFERED in all cases.
     * @throws jakarta.servlet.jsp.JspException
     */
    public int doStartTag() throws JspException {
        return EVAL_BODY_BUFFERED;
    }

    /**
     * Figures out what to use as the value, and then finds the parent link and
     * adds the parameter.
     *
     * @return EVAL_PAGE in all cases.
     * @throws jakarta.servlet.jsp.JspException
     */
    public int doEndTag() throws JspException {
        Object valueToSet = value;

        // First figure out what value to send to the parent link tag
        if (value == null) {
            if (this.bodyContent == null) {
                valueToSet = "";
            } else {
                valueToSet = this.bodyContent.getString();
            }
        }

        // Find the parent link tag
        Tag parameterizable = this.parentTag;
        while (parameterizable != null && !(parameterizable instanceof ParameterizableTag)) {
            parameterizable = parameterizable.getParent();
        }

        ((ParameterizableTag) parameterizable).addParameter(name, valueToSet);
        return EVAL_PAGE;
    }

    /**
     * Does nothing.
     */
    public void release() {
        /* Do nothing. */ }

    /**
     *
     * @return
     */
    public PageContext getPageContext() {
        return pageContext;
    }
}
