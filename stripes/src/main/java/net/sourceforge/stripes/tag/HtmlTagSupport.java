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

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.tagext.BodyContent;
import jakarta.servlet.jsp.tagext.DynamicAttributes;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.sourceforge.stripes.exception.StripesJspException;
import net.sourceforge.stripes.util.HtmlUtil;
import net.sourceforge.stripes.util.Log;

/**
 * Provides basic facilities for any tag that wishes to mimic a standard HTML/XHTML tag. Includes
 * getters and setters for all basic HTML attributes and JavaScript event attributes. Also includes
 * several of the support methods from the Tag interface, but does not directly or indirectly
 * implement either Tag or BodyTag.
 *
 * @author Tim Fennell
 */
public abstract class HtmlTagSupport extends StripesTagSupport implements DynamicAttributes {
  /** Log implementation used to log errors during tag writing. */
  private static final Log log = Log.getInstance(HtmlTagSupport.class);

  /** Map containing all attributes of the tag. */
  private final Map<String, String> attributes = new HashMap<>();

  /** Storage for a BodyContent instance, should the eventual child class implement BodyTag. */
  private BodyContent bodyContent;

  /**
   * Sets the named attribute to the supplied value.
   *
   * @param name the name of the attribute
   * @param value the value of the attribute
   */
  protected final void set(String name, String value) {
    if (value == null) {
      this.attributes.remove(name);
    } else {
      this.attributes.put(name, value);
    }
  }

  /**
   * Gets the value of the named attribute, or null if it is not set.
   *
   * @param name the name of the attribute
   */
  protected final String get(String name) {
    return this.attributes.get(name);
  }

  /**
   * Gets the map containing the attributes of the tag.
   *
   * @return the map of attributes
   */
  protected final Map<String, String> getAttributes() {
    return this.attributes;
  }

  /**
   * Accepts any dynamic attributes that are supplied to the tag and stored them in the map of
   * attributes that get written back to the page.
   *
   * @param uri the URI of the namespace of the attribute if it has one. Totally ignored!
   * @param name the name of the attribute
   * @param value the value of the attribute
   */
  @Override
  public void setDynamicAttribute(String uri, String name, Object value) {
    set(name, value == null ? "" : value.toString());
  }

  /**
   * Returns the BodyContent of the tag if one has been provided by the JSP container.
   *
   * @return the body content
   */
  public BodyContent getBodyContent() {
    return bodyContent;
  }

  /**
   * Called by the JSP container to set the BodyContent on the tag.
   *
   * @param bodyContent the body content
   */
  public void setBodyContent(BodyContent bodyContent) {
    this.bodyContent = bodyContent;
  }

  /** Release method to clean up the state of the tag ready for re-use. */
  @Override
  public void release() {
    this.pageContext = null;
    this.parentTag = null;
    this.bodyContent = null;
    this.attributes.clear();
  }

  /**
   * Checks to see if there is a body content for this tag, and if its value is non-null and
   * non-zero-length. If so, returns it as a String, otherwise returns null.
   *
   * @return String the value of the body if one was set
   */
  protected String getBodyContentAsString() {
    String returnValue = null;

    if (this.bodyContent != null) {
      String body = getBodyContent().getString();

      if (body != null && !body.isEmpty()) {
        returnValue = body;
      }
    }

    return returnValue;
  }

  /**
   * Returns true if HTML tags that have no body tags and should be closed like XML tags, with
   * "/&gt;". False if such HTML tags should be closed in the style of HTML4, with just a "&gt;".
   *
   * @return true if XML tags should be used, false if HTML tags should be used
   * @see PageOptionsTag#setHtmlMode(String)
   */
  protected boolean isXmlTags() {
    return !"html".equalsIgnoreCase(PageOptionsTag.getHtmlMode(pageContext));
  }

  /**
   * Writes out an opening tag. Uses the parameter "tag" to determine the name of the open tag and
   * then uses the map of attributes assembled through various setter calls to fill in the tag
   * attributes.
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
    } catch (IOException ioe) {
      JspException jspException =
          new JspException(
              "IOException encountered while writing open tag <" + tag + "> to the JspWriter.",
              ioe);
      log.warn(jspException);
      throw jspException;
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
    } catch (IOException ioe) {
      JspException jspException =
          new JspException(
              "IOException encountered while writing close tag </" + tag + "> to the JspWriter.",
              ioe);
      log.warn(jspException);
      throw jspException;
    }
  }

  /**
   * Writes out a singleton tag (aka a bodiless tag or self-closing tag). Similar to writeOpenTag
   * except that instead of leaving the tag open, it closes the tag.
   *
   * @param writer the JspWriter to write the open tag to
   * @throws JspException if the JspWriter causes an exception
   */
  protected void writeSingletonTag(JspWriter writer) throws JspException {
    try {
      writer.print("<");
      writer.print("input");
      writeAttributes(writer);
      writer.print(isXmlTags() ? " />" : ">");
    } catch (IOException ioe) {
      JspException jspException =
          new JspException(
              "IOException encountered while writing singleton tag <"
                  + "input"
                  + "/> to the JspWriter.",
              ioe);
      log.warn(jspException);
      throw jspException;
    }
  }

  /**
   * For every attribute stored in the attributes map for this tag, writes out the tag attributes in
   * the form x="y". All attributes are HTML encoded before being written to the page to ensure that
   * HTML special characters are rendered properly.
   *
   * @param writer the JspWriter to write the open tag to
   * @throws IOException if the JspWriter causes an exception
   */
  protected void writeAttributes(JspWriter writer) throws IOException {
    for (Map.Entry<String, String> attr : getAttributes().entrySet()) {
      // Skip the output of blank attributes!
      String value = attr.getValue();
      if (value == null) continue;

      writer.print(" ");
      writer.print(attr.getKey());
      writer.print("=\"");
      writer.print(HtmlUtil.encode(value));
      writer.print("\"");
    }
  }

  /**
   * Evaluates a single expression and returns the result. If the expression cannot be evaluated
   * then an ELException is caught, wrapped in a JspException and re-thrown.
   *
   * @param <R> the type of the result
   * @param expression the expression to be evaluated
   * @param resultType the Class representing the desired return type from the expression
   * @throws StripesJspException when an ELException occurs trying to evaluate the expression
   */
  @SuppressWarnings({"unchecked", "deprecation"})
  protected <R> R evaluateExpression(String expression, Class<R> resultType)
      throws StripesJspException {
    try {
      return (R)
          this.pageContext
              .getExpressionEvaluator()
              .evaluate(expression, resultType, this.pageContext.getVariableResolver(), null);
    } catch (jakarta.servlet.jsp.el.ELException ele) {
      throw new StripesJspException(
          "Could not evaluate EL expression  ["
              + expression
              + "] with result type ["
              + resultType.getName()
              + "] in tag class of type: "
              + getClass().getName(),
          ele);
    }
  }

  /**
   * Returns a String representation of the class, including the map of attributes that are set on
   * the tag, the toString of its parent tag, and the pageContext.
   *
   * @return a String representation of the tag
   */
  @Override
  public String toString() {
    return getClass().getSimpleName()
        + "{"
        + "attributes="
        + attributes
        + ", parentTag="
        + parentTag
        + ", pageContext="
        + pageContext
        + "}";
  }

  /**
   * Sets the value of the id attribute.
   *
   * @param id the value of the id attribute
   */
  public void setId(String id) {
    set("id", id);
  }

  /**
   * Gets the value of the id attribute.
   *
   * @return the value of the id attribute
   */
  public String getId() {
    return get("id");
  }

  /**
   * Sets the value of the class attribute.
   *
   * @param cssClass the value of the class attribute
   */
  public void setClass(String cssClass) {
    set("class", cssClass);
  }

  /**
   * Sets the value of the class attribute.
   *
   * @param cssClass the value of the class attribute
   */
  public void setCssClass(String cssClass) {
    set("class", cssClass);
  }

  /**
   * Gets the value of the class attribute.
   *
   * @return the value of the class attribute
   */
  public String getCssClass() {
    return get("class");
  }

  /**
   * Sets the value of the title attribute.
   *
   * @param title the value of the title attribute
   */
  public void setTitle(String title) {
    set("title", title);
  }

  /**
   * Gets the value of the title attribute.
   *
   * @return the value of the title attribute
   */
  public String getTitle() {
    return get("title");
  }

  /**
   * Sets the value of the style attribute.
   *
   * @param style the value of the style attribute
   */
  public void setStyle(String style) {
    set("style", style);
  }

  /**
   * Gets the value of the style attribute.
   *
   * @return the value of the style attribute
   */
  public String getStyle() {
    return get("style");
  }

  /**
   * Sets the value of the dir attribute.
   *
   * @param dir the value of the dir attribute
   */
  public void setDir(String dir) {
    set("dir", dir);
  }

  /**
   * Gets the value of the dir attribute.
   *
   * @return the value of the dir attribute
   */
  public String getDir() {
    return get("dir");
  }

  /**
   * Sets the value of the lang attribute.
   *
   * @param lang the value of the lang attribute
   */
  public void setLang(String lang) {
    set("lang", lang);
  }

  /**
   * Gets the value of the lang attribute.
   *
   * @return the value of the lang attribute
   */
  public String getLang() {
    return get("lang");
  }

  /**
   * Sets the value of the tabindex attribute.
   *
   * @param tabindex the value of the tabindex attribute
   */
  public void setTabindex(String tabindex) {
    set("tabindex", tabindex);
  }

  /**
   * Gets the value of the tabindex attribute.
   *
   * @return the value of the tabindex attribute
   */
  public String getTabindex() {
    return get("tabindex");
  }

  /**
   * Sets the value of the accesskey attribute.
   *
   * @param accesskey the value of the accesskey attribute
   */
  public void setAccesskey(String accesskey) {
    set("accesskey", accesskey);
  }

  /**
   * Gets the value of the accesskey attribute.
   *
   * @return the value of the accesskey attribute
   */
  public String getAccesskey() {
    return get("accesskey");
  }

  /**
   * Sets the value of the onfocus attribute.
   *
   * @param onfocus the value of the onfocus attribute
   */
  public void setOnfocus(String onfocus) {
    set("onfocus", onfocus);
  }

  /**
   * Gets the value of the onfocus attribute.
   *
   * @return the value of the onfocus attribute
   */
  public String getOnfocus() {
    return get("onfocus");
  }

  /**
   * Sets the value of the onblur attribute.
   *
   * @param onblur the value of the onblur attribute
   */
  public void setOnblur(String onblur) {
    set("onblur", onblur);
  }

  /**
   * Gets the value of the onblur attribute.
   *
   * @return the value of the onblur attribute
   */
  public String getOnblur() {
    return get("onblur");
  }

  /**
   * Sets the value of the onselect attribute.
   *
   * @param onselect the value of the onselect attribute
   */
  public void setOnselect(String onselect) {
    set("onselect", onselect);
  }

  /**
   * Gets the value of the onselect attribute.
   *
   * @return the value of the onselect attribute
   */
  public String getOnselect() {
    return get("onselect");
  }

  /**
   * Sets the value of the onchange attribute.
   *
   * @param onchange the value of the onchange attribute
   */
  public void setOnchange(String onchange) {
    set("onchange", onchange);
  }

  /**
   * Gets the value of the onchange attribute.
   *
   * @return the value of the onchange attribute
   */
  public String getOnchange() {
    return get("onchange");
  }

  /**
   * Sets the value of the onclick attribute.
   *
   * @param onclick the value of the onclick attribute
   */
  public void setOnclick(String onclick) {
    set("onclick", onclick);
  }

  /**
   * Gets the value of the onclick attribute.
   *
   * @return the value of the onclick attribute
   */
  public String getOnclick() {
    return get("onclick");
  }

  /**
   * Sets the value of the ondblclick attribute.
   *
   * @param ondblclick the value of the ondblclick attribute
   */
  public void setOndblclick(String ondblclick) {
    set("ondblclick", ondblclick);
  }

  /**
   * Gets the value of the ondblclick attribute.
   *
   * @return the value of the ondblclick attribute
   */
  public String getOndblclick() {
    return get("ondblclick");
  }

  /**
   * Sets the value of the onmousedown attribute.
   *
   * @param onmousedown the value of the onmousedown attribute
   */
  public void setOnmousedown(String onmousedown) {
    set("onmousedown", onmousedown);
  }

  /**
   * Gets the value of the onmousedown attribute.
   *
   * @return the value of the onmousedown attribute
   */
  public String getOnmousedown() {
    return get("onmousedown");
  }

  /**
   * Sets the value of the onmouseup attribute.
   *
   * @param onmouseup the value of the onmouseup attribute
   */
  public void setOnmouseup(String onmouseup) {
    set("onmouseup", onmouseup);
  }

  /**
   * Gets the value of the onmouseup attribute.
   *
   * @return the value of the onmouseup attribute
   */
  public String getOnmouseup() {
    return get("onmouseup");
  }

  /**
   * Sets the value of the onmouseover attribute.
   *
   * @param onmouseover the value of the onmouseover attribute
   */
  public void setOnmouseover(String onmouseover) {
    set("onmouseover", onmouseover);
  }

  /**
   * Gets the value of the onmouseover attribute.
   *
   * @return the value of the onmouseover attribute
   */
  public String getOnmouseover() {
    return get("onmouseover");
  }

  /**
   * Sets the value of the onmousemove attribute.
   *
   * @param onmousemove the value of the onmousemove attribute
   */
  public void setOnmousemove(String onmousemove) {
    set("onmousemove", onmousemove);
  }

  /**
   * Gets the value of the onmousemove attribute.
   *
   * @return the value of the onmousemove attribute
   */
  public String getOnmousemove() {
    return get("onmousemove");
  }

  /**
   * Sets the value of the onmouseout attribute.
   *
   * @param onmouseout the value of the onmouseout attribute
   */
  public void setOnmouseout(String onmouseout) {
    set("onmouseout", onmouseout);
  }

  /**
   * Gets the value of the onmouseout attribute.
   *
   * @return the value of the onmouseout attribute
   */
  public String getOnmouseout() {
    return get("onmouseout");
  }

  /**
   * Sets the value of the onkeypress attribute.
   *
   * @param onkeypress the value of the onkeypress attribute
   */
  public void setOnkeypress(String onkeypress) {
    set("onkeypress", onkeypress);
  }

  /**
   * Gets the value of the onkeypress attribute.
   *
   * @return the value of the onkeypress attribute
   */
  public String getOnkeypress() {
    return get("onkeypress");
  }

  /**
   * Sets the value of the onkeydown attribute.
   *
   * @param onkeydown the value of the onkeydown attribute
   */
  public void setOnkeydown(String onkeydown) {
    set("onkeydown", onkeydown);
  }

  /**
   * Gets the value of the onkeydown attribute.
   *
   * @return the value of the onkeydown attribute
   */
  public String getOnkeydown() {
    return get("onkeydown");
  }

  /**
   * Sets the value of the onkeyup attribute.
   *
   * @param onkeyup the value of the onkeyup attribute
   */
  public void setOnkeyup(String onkeyup) {
    set("onkeyup", onkeyup);
  }

  /**
   * Gets the value of the onkeyup attribute.
   *
   * @return the value of the onkeyup attribute
   */
  public String getOnkeyup() {
    return get("onkeyup");
  }
}
