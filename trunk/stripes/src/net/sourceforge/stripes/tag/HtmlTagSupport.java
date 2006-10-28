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
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.util.HtmlUtil;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.el.ELException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.DynamicAttributes;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides basic facilities for any tag that wishes to mimic a standard HTML/XHTML tag. Includes
 * getters and setters for all basic HTML attributes and JavaScript event attributes.  Also includes
 * several of the support methods from the Tag interface, but does not directly or indirectly
 * implement either Tag or BodyTag.
 *
 * @author Tim Fennell
 */
public abstract class HtmlTagSupport extends StripesTagSupport implements DynamicAttributes {
    /** Log implementation used to log errors during tag writing. */
    private static final Log log = Log.getInstance(HtmlTagSupport.class);

    /** Map containing all attributes of the tag. */
    private final Map<String,String> attributes = new HashMap<String,String>();

    /** Storage for a BodyContent instance, should the eventual child class implement BodyTag. */
    private BodyContent bodyContent;

    /** Sets the named attribute to the supplied value. */
    protected final void set(String name, String value) {
        this.attributes.put(name, value);
    }

    /** Gets the value of the named attribute, or null if it is not set. */
    protected final String get(String name) {
        return this.attributes.get(name);
    }

    /** Gets the map containing the attributes of the tag. */
    protected final Map<String,String> getAttributes() {
        return this.attributes;
    }

    /**
     * Accepts any dynamic attributes that are supplied to the tag and stored them
     * in the map of attributes that get written back to the page.
     *
     * @param uri the URI of the namespace of the attribute if it has one. Totally ignored!
     * @param name the name of the attribute
     * @param value the value of the attribute
     * @throws JspException not thrown from this class; included so that subclasses can
     *         override the method and throw the interface exception
     */
    public void setDynamicAttribute(String uri, String name, Object value) throws JspException {
        set(name, value == null ? "" : value.toString());
    }

    /** Returns the BodyContent of the tag if one has been provided by the JSP container. */
    public BodyContent getBodyContent() {
        return bodyContent;
    }

    /** Called by the JSP container to set the BodyContent on the tag. */
    public void setBodyContent(BodyContent bodyContent) {
        this.bodyContent = bodyContent;
    }

    /** Release method to clean up the state of the tag ready for re-use. */
    public void release() {
        this.pageContext = null;
        this.parentTag = null;
        this.bodyContent = null;
        this.attributes.clear();
    }

    /**
     * Checks to see if there is a body content for this tag, and if its value is non-null
     * and non-zero-length.  If so, returns it as a String, otherwise returns null.

     * @return String the value of the body if one was set
     */
    protected String getBodyContentAsString() {
        String returnValue = null;

        if (this.bodyContent != null) {
            String body = getBodyContent().getString();

            if (body != null && body.length() > 0) {
                returnValue = body;
            }
        }

        return returnValue;
    }

    /**
     * Writes out an opening tag.  Uses the parameter "tag" to determine the name of the open tag
     * and then uses the map of attributes assembled through various setter calls to fill in the
     * tag attributes.
     *
     * @param writer the JspWriter to write the open tag to
     * @param tag the name of the tag to use
     * @throws JspException if the JspWriter causes an exception
     */
    protected void writeOpenTag(JspWriter writer, String tag) throws JspException {
        try {
            writer.print("<");
            writer.print(tag);
            writeAttributes(writer);
            writer.print(">");
        }
        catch (IOException ioe) {
            JspException jspe = new JspException("IOException encountered while writing open tag <" +
                tag + "> to the JspWriter.", ioe);
            log.warn(jspe);
            throw jspe;
        }
    }

    /**
     * Writes out a close tag using the tag name supplied.
     *
     * @param writer the JspWriter to write the open tag to
     * @param tag the name of the tag to use
     * @throws JspException if the JspWriter causes an exception
     */
    protected void writeCloseTag(JspWriter writer, String tag) throws JspException {
        try {
            writer.print("</");
            writer.print(tag);
            writer.print(">");
        }
        catch (IOException ioe) {
            JspException jspe = new JspException("IOException encountered while writing close tag </" +
                tag + "> to the JspWriter.", ioe);
            log.warn(jspe);
            throw jspe;
        }
    }

    /**
     * Writes out a singleton tag (aka a bodiless tag or self-closing tag).  Similar to
     * writeOpenTag except that instead of leaving the tag open, it closes the tag.
     *
     * @param writer the JspWriter to write the open tag to
     * @param tag the name of the tag to use
     * @throws JspException if the JspWriter causes an exception
     */
    protected void writeSingletonTag(JspWriter writer, String tag) throws JspException{
        try {
            writer.print("<");
            writer.print(tag);
            writeAttributes(writer);
            writer.print(" />");
        }
        catch (IOException ioe) {
            JspException jspe = new JspException("IOException encountered while writing singleton tag <" +
                tag + "/> to the JspWriter.", ioe);
            log.warn(jspe);
            throw jspe;
        }
    }

    /**
     * For every attribute stored in the attributes map for this tag, writes out the tag
     * attributes in the form x="y".  All attributes are HTML encoded before being written
     * to the page to ensure that HTML special characters are rendered properly.
     *
     * @param writer the JspWriter to write the open tag to
     * @throws IOException if the JspWriter causes an exception
     */
    protected void writeAttributes(JspWriter writer) throws IOException {
        for (Map.Entry<String,String> attr: getAttributes().entrySet() ) {
            // Skip the output of blank attributes!
            String value = attr.getValue();
            if (value == null || value.length() == 0) continue;

            writer.print(" ");
            writer.print(attr.getKey());
            writer.print("=\"");
            writer.print( HtmlUtil.encode(value) );
            writer.print("\"");
        }
    }


    /**
     * Evaluates a single expression and returns the result.  If the expression cannot be evaluated
     * then an ELException is caught, wrapped in a JspException and re-thrown.
     *
     * @param expression the expression to be evaluated
     * @param resultType the Class representing the desired return type from the expression
     * @throws StripesJspException when an ELException occurs trying to evaluate the expression
     */
    protected <R> R evaluateExpression(String expression, Class<R> resultType) throws StripesJspException {
        try {
            return (R) this.pageContext.getExpressionEvaluator().
                evaluate(expression, resultType, this.pageContext.getVariableResolver(), null);
        }
        catch (ELException ele) {
            throw new StripesJspException
                ("Could not evaluate EL expression  [" + expression + "] with result type [" +
                    resultType.getName() + "] in tag class of type: " + getClass().getName(), ele);
        }
    }


    /**
     * Returns a String representation of the class, including the map of attributes that
     * are set on the tag, the toString of its parent tag, and the pageContext.
     */
    public String toString() {
        return getClass().getSimpleName()+ "{" +
            "attributes=" + attributes +
            ", parentTag=" + parentTag +
            ", pageContext=" + pageContext +
            "}";
    }


    public void setId(String id) { set("id", id); }
    public String getId() { return get("id"); }

    public void setClass(String cssClass) { set("class", cssClass); }
    public void setCssClass(String cssClass) { set("class", cssClass); }
    public String getCssClass() { return get("class"); }

    public void setTitle(String  title) { set("title",  title); }
    public String getTitle() { return get("title"); }

    public void setStyle(String  style) { set("style",  style); }
    public String getStyle() { return get("style"); }

    public void setDir(String  dir) { set("dir",  dir); }
    public String getDir() { return get("dir"); }

    public void setLang(String  lang) { set("lang",  lang); }
    public String getLang() { return get("lang"); }

    public void setTabindex(String  tabindex) { set("tabindex",  tabindex); }
    public String getTabindex() { return get("tabindex"); }

    public void setAccesskey(String  accesskey) { set("accesskey",  accesskey); }
    public String getAccesskey() { return get("accesskey"); }

    public void setOnfocus(String  onfocus) { set("onfocus",  onfocus); }
    public String getOnfocus() { return get("onfocus"); }

    public void setOnblur(String  onblur) { set("onblur",  onblur); }
    public String getOnblur() { return get("onblur"); }

    public void setOnselect(String  onselect) { set("onselect",  onselect); }
    public String getOnselect() { return get("onselect"); }

    public void setOnchange(String  onchange) { set("onchange",  onchange); }
    public String getOnchange() { return get("onchange"); }

    public void setOnclick(String  onclick) { set("onclick",  onclick); }
    public String getOnclick() { return get("onclick"); }

    public void setOndblclick(String  ondblclick) { set("ondblclick",  ondblclick); }
    public String getOndblclick() { return get("ondblclick"); }

    public void setOnmousedown(String  onmousedown) { set("onmousedown",  onmousedown); }
    public String getOnmousedown() { return get("onmousedown"); }

    public void setOnmouseup(String  onmouseup) { set("onmouseup",  onmouseup); }
    public String getOnmouseup() { return get("onmouseup"); }

    public void setOnmouseover(String  onmouseover) { set("onmouseover",  onmouseover); }
    public String getOnmouseover() { return get("onmouseover"); }

    public void setOnmousemove(String  onmousemove) { set("onmousemove",  onmousemove); }
    public String getOnmousemove() { return get("onmousemove"); }

    public void setOnmouseout(String  onmouseout) { set("onmouseout",  onmouseout); }
    public String getOnmouseout() { return get("onmouseout"); }

    public void setOnkeypress(String  onkeypress) { set("onkeypress",  onkeypress); }
    public String getOnkeypress() { return get("onkeypress"); }

    public void setOnkeydown(String  onkeydown) { set("onkeydown",  onkeydown); }
    public String getOnkeydown() { return get("onkeydown"); }

    public void setOnkeyup(String  onkeyup) { set("onkeyup",  onkeyup); }
    public String getOnkeyup() { return get("onkeyup"); }
}
