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

import net.sourceforge.stripes.localization.LocalizationUtility;

import javax.servlet.jsp.JspException;
import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

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
    public int doStartInputTag() throws JspException { return SKIP_BODY; }

    /**
     * Does the major work of the tag as described in the class level javadoc. Checks for
     * localized src and alt attributes and preprends the context path to any src URL that
     * starts with a slash.
     *
     * @return EVAL_PAGE always
     */
    public int doEndInputTag() throws JspException {
        String name = getAttributes().get("name");
        Locale locale = getPageContext().getRequest().getLocale();
        String actionPath = getParentFormTag().getAction();

        // See if we should use a URL to a localized image
        String src = LocalizationUtility.getLocalizedFieldName(name + ".src", actionPath, locale);
        if (src != null) {
            setSrc(src);
        }

        // And see if we have localized alt text too
        String alt = LocalizationUtility.getLocalizedFieldName(name + ".alt", actionPath, locale);
        if (alt != null) {
            setAlt(alt);
        }

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
}
