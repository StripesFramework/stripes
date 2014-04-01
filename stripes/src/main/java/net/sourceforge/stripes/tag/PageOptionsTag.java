/*
 *  Copyright 2010 Timothy Stone.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package net.sourceforge.stripes.tag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import net.sourceforge.stripes.controller.StripesFilter;

/**
 * <p>
 * Provides a tag to override the {@link StripesFilter} configuration property
 * <code>Stripes.HtmlMode</code>.
 * </p>
 * <p>
 * <code>htmlMode</code> accepts any string value, however any value not equal to <code>html</code>,
 * case-insensitive, puts Stripes into its default mode of XHTML-compatible output.
 * </p>
 * <p>
 * Examples of the tag's use then might be:
 * </p>
 * <ul>
 * <li>&lt;s:options htmlMode="html" /&gt; produces HTML4 and HTML5 form elements, e.g., &lt;img src
 * &#8230; &gt;</li>
 * <li>&lt;s:options htmlMode="xhtml" /&gt; produces XHTML-compatible form elements, e.g., &lt;img
 * src &#8230; /&gt;</li>
 * <li>&lt;s:options htmlMode="default" /&gt; produces XHTML form elements</li>
 * </ul>
 * <p>
 * Typical use of the tag in context of a Stripes application follows:
 * </p>
 * <p>
 * Deployer will set the application RuntimeConfiguration of <code>Stripes.HtmlMode</code>. A
 * deployer choosing not to set this option, defaults the Stripes application to its
 * XHTML-compatible format.
 * </p>
 * <code>Stripes.HtmlMode</code> will set the default X/HTML output for the <strong>entire</strong>
 * application. Individual views of the application wishing to alter the application default will
 * provide this tag, at or near the beginning of the view, or JSP.</p>
 * 
 * @author Timothy Stone
 * @since 1.5.5
 */
public class PageOptionsTag extends StripesTagSupport {
    /** Configuration key that sets the default HTML mode for the application. */
    public static String CFG_KEY_HTML_MODE = "Stripes.HtmlMode";

    /** Request attribute that affects how HTML is rendered by other tags. */
    public static String REQ_ATTR_HTML_MODE = "__stripes_html_mode";

    /**
     * Get the HTML mode for the given page context. If the request attribute
     * {@link #REQ_ATTR_HTML_MODE} is present then use that value. Otherwise, use the global
     * configuration property {@link #CFG_KEY_HTML_MODE}.
     */
    public static String getHtmlMode(PageContext pageContext) {
        String htmlMode = (String) pageContext.getAttribute(REQ_ATTR_HTML_MODE,
                PageContext.REQUEST_SCOPE);

        if (htmlMode == null) {
            htmlMode = StripesFilter.getConfiguration().getBootstrapPropertyResolver()
                    .getProperty(CFG_KEY_HTML_MODE);
        }

        return htmlMode;
    }

    /**
     * This field is not initialized to null because null is a valid value that may be passed to
     * {@link #setHtmlMode(String)}. Initializing to a constant differentiates between a field that
     * was never changed after initialization and a field that was set to null.
     */
    private String htmlMode = REQ_ATTR_HTML_MODE;

    @Override
    public int doStartTag() throws JspException {
        return SKIP_BODY;
    }

    @Override
    public int doEndTag() throws JspException {
        // This is an intentional use of identity instead of equality
        if (this.htmlMode != REQ_ATTR_HTML_MODE) {
            pageContext.getRequest().setAttribute(REQ_ATTR_HTML_MODE, this.htmlMode);
        }

        return EVAL_PAGE;
    }

    /** Set the HTML mode string. */
    public void setHtmlMode(String htmlMode) {
        this.htmlMode = htmlMode;
    }
}
