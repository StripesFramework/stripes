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

import javax.servlet.jsp.JspException;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>Tag class that generates an image button for use in HTML forms, e.g:</p>
 *
 *<pre>{@literal <input name="foo" type="image" src="/app/foo.gif" alt="foo"/>}</pre>
 *
 * <p>Provides a couple of facilities above and beyond using plain HTML tags. The main
 * advantage is a localization capability. The tag looks in the Stripes Field Name
 * message bundle for resources to be used as the src URL for the image and the alt
 * text of the image.  In order it will look for and use:</p>
 *
 * <ul>
 *   <li>resouce: actionPath.inputName.[src|alt]</li>
 *   <li>resouce: inputName.[src|alt]</li>
 *   <li>tag attributes: src and alt
 * </ul>
 *
 * <p>If localized values exist these are preferred over the values specified directly
 * on the tag.</p>
 *
 * <p>Additionally if the 'src' URL (whether acquired from the tag attribute or the
 * resource bundle) starts with a slash, the tag will prepend the context path of the
 * web application.</p>
 *
 * @author Tim Fennell
 * @since Stripes 1.3
 */
public class InputImageTag extends InputTagSupport {

    /** Sets the tag's type to be an image input. */
    public InputImageTag() {
        super();
        set("type", "image");
    }

    /**
     * Does nothing.
     * @return SKIP_BODY in all cases
     */
    @Override
    public int doStartInputTag() throws JspException { return SKIP_BODY; }

    /**
     * Does the major work of the tag as described in the class level javadoc. Checks for
     * localized src and alt attributes and preprends the context path to any src URL that
     * starts with a slash.
     *
     * @return EVAL_PAGE always
     */
    @Override
    public int doEndInputTag() throws JspException {
        // See if we should use a URL to a localized image
        String name = getAttributes().get("name");
        String src = getLocalizedFieldName(name + ".src");
        if (src != null) { setSrc(src); }

        // And see if we have localized alt text too
        String alt = getLocalizedFieldName(name + ".alt");
        if (alt != null) { setAlt(alt); }

        // Prepend the context path to the src URL
        src = getSrc();
        if (src != null && src.startsWith("/")) {
            String ctx = ((HttpServletRequest) getPageContext().getRequest()).getContextPath();
            setSrc(ctx + src);
        }

        writeSingletonTag(getPageContext().getOut(), "input");
        return EVAL_PAGE;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Getter/Setter methods for additional attributes
    ///////////////////////////////////////////////////////////////////////////
    public void setAlign(String align) { set("align", align); }
    public String getAlign() { return get("align"); }

    public void setAlt(String alt) { set("alt", alt); }
    public String getAlt() { return get("alt"); }

    public void setSrc(String src) { set("src", src); }
    public String getSrc() { return get("src"); }

    public void setValue(String value) { set("value", value); }
    public String getValue() { return get("value"); }
}
