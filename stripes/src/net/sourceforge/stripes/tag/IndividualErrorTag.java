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

import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.validation.ValidationError;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.Tag;
import java.io.IOException;
import java.util.Locale;

/**
 * The individual-error tag works in concert with a parent errors tag to control the
 * output of each iteration of an error. Placed within a parent errors tag,
 * the body output of the parent will be displayed for each matching error
 * to output iterate the body of that parent displaying each error in turn.
 *
 * @author Greg Hinkle
 */
public class IndividualErrorTag extends HtmlTagSupport implements Tag {

    private final Log log = Log.getInstance(ErrorsTag.class);

    /**
     * Does nothing
     * @return SKIP_BODY always
     * @throws JspException
     */
    public int doStartTag() throws JspException {
        return SKIP_BODY;
    }

    /**
     * Outputs the error for the current iteration of the parent ErrorsTag.
     *
     * @return EVAL_PAGE in all circumstances
     */
    public int doEndTag() throws JspException {

        Locale locale = getPageContext().getRequest().getLocale();
        JspWriter writer = getPageContext().getOut();

        ErrorsTag parentErrorsTag = getParentTag(ErrorsTag.class);
        if (parentErrorsTag != null) {
            // Mode: sub-tag inside an errors tag
            try {
                ValidationError error = parentErrorsTag.getCurrentError();
                writer.write( error.getMessage(locale) );
            }
            catch (IOException ioe) {
                JspException jspe = new JspException("IOException encountered while writing " +
                    "error tag to the JspWriter.", ioe);
                log.warn(jspe);
                throw jspe;
            }
        }

        return EVAL_PAGE;
    }
}
