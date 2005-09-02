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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

/**
 * Can be used within a stripes:errors tag to show a header on an error list.
 * The contents of this tag will only be displayed on the first iteration of an
 * errors list.
 *
 * @author Greg Hinkle
 */
public class ErrorsHeaderTag extends HtmlTagSupport implements Tag {

    public int doStartTag() throws JspException {
        ErrorsTag errorsTag = getParentTag(ErrorsTag.class);
        if (errorsTag.isFirst())
            return EVAL_BODY_INCLUDE;
        else
            return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        return EVAL_PAGE;
    }
}
