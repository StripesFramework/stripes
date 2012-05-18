/* Copyright 2007 Ben Gunter
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

import java.io.IOException;

import javax.servlet.jsp.JspException;

import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.format.Formatter;
import net.sourceforge.stripes.format.FormatterFactory;
import net.sourceforge.stripes.util.Log;

/**
 * This tag accepts an object and formats it using an appropriate
 * {@link Formatter}. The resulting {@link String} can be assigned in the page,
 * request, session or application scopes by using "var" and "scope" or it can
 * be written directly to the JSP output.
 * 
 * @author Ben Gunter
 * @since Stripes 1.5
 */
public class FormatTag extends VarTagSupport {
    private static final Log log = Log.getInstance(FormatTag.class);
    private Object value;
    private String formatType;
    private String formatPattern;

    /** Get the format pattern */
    public String getFormatPattern() {
        return formatPattern;
    }

    /** Set the format pattern */
    public void setFormatPattern(String formatPattern) {
        this.formatPattern = formatPattern;
    }

    /** Get the format type */
    public String getFormatType() {
        return formatType;
    }

    /** Set the format type */
    public void setFormatType(String formatType) {
        this.formatType = formatType;
    }

    /** Get the object to be formatted */
    public Object getValue() {
        return value;
    }

    /** Set the object to be formatted */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Attempts to format an object using an appropriate {@link Formatter}. If
     * no formatter is available for the object, then this method will call
     * <code>toString()</code> on the object. A null <code>value</code> will
     * be formatted as an empty string.
     * 
     * @param value
     *            the object to be formatted
     * @return the formatted value
     */
    @SuppressWarnings("unchecked")
    protected String format(Object value) {
        if (value == null)
            return "";

        FormatterFactory factory = StripesFilter.getConfiguration().getFormatterFactory();
        Formatter formatter = factory.getFormatter(value.getClass(),
                                                   getPageContext().getRequest().getLocale(),
                                                   this.formatType,
                                                   this.formatPattern);
        if (formatter == null)
            return String.valueOf(value);
        else
            return formatter.format(value);
    }

    /**
     * Calls {@link #format(Object)} and writes the resulting {@link String} to
     * the JSP output.
     * 
     * @param value
     *            the object to be formatted and written
     * @throws JspException
     */
    protected void writeOut(Object value) throws JspException {
        String formatted = format(value);
        try {
            pageContext.getOut().print(formatted);
        }
        catch (IOException e) {
            JspException jspe = new JspException(
                    "IOException encountered while writing formatted value '"
                            + formatted + " to the JspWriter.", e);
            log.warn(jspe);
            throw jspe;
        }
    }

    @Override
    public int doStartTag() throws JspException {
        return SKIP_BODY;
    }

    @Override
    public int doEndTag() throws JspException {
        if (var == null) {
            writeOut(value);
        }
        else {
            export(format(value));
        }
        return EVAL_PAGE;
    }
}
