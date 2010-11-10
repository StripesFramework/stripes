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

import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.validation.ValidationError;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
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
public class IndividualErrorTag extends HtmlTagSupport {

    private static final Log log = Log.getInstance(IndividualErrorTag.class);

    /**
     * Does nothing
     * @return SKIP_BODY always
     * @throws JspException
     */
    @Override
    public int doStartTag() throws JspException {
        return SKIP_BODY;
    }

    /**
     * Outputs the error for the current iteration of the parent ErrorsTag.
     *
     * @return EVAL_PAGE in all circumstances
     */
    @Override
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
