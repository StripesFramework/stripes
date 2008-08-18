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

import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.JspException;

/**
 * Can be used within a stripes:errors tag to show a footer on an error list.
 * The contents of this tag will only be displayed on the last iteration of an
 * errors list.
 *
 * @author Greg Hinkle
 */
public class ErrorsFooterTag extends HtmlTagSupport implements Tag {

    @Override
    public int doStartTag() throws JspException {
        ErrorsTag errorsTag = getParentTag(ErrorsTag.class);

        if (errorsTag.isLast())
            return EVAL_BODY_INCLUDE;
        else
            return SKIP_BODY;
    }

    @Override
    public int doEndTag() throws JspException {
        return EVAL_PAGE;
    }

}
