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

import net.sourceforge.stripes.action.Message;
import net.sourceforge.stripes.controller.StripesConstants;
import net.sourceforge.stripes.controller.StripesFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * <p>Displays a list of non-error messages to the user. The list of messages can come from
 * either the request (preferred) or the session (checked 2nd).  Lists of messages can be stored
 * under any arbitrary key in request or session and the key can be specified to the messages
 * tag.  If no key is specified then the default key (and therefore default set of messages) is
 * used. Note that by default the ActionBeanContext stores messages in a
 * {@link net.sourceforge.stripes.controller.FlashScope} which causes them to be exposed as
 * request attributes in both the current and subsequent request (assuming a redirect is used).</p>
 *
 * <p>While similar in concept to the ErrorsTag, the MessagesTag is significantly simpler. It deals
 * with a List of Message objects, and does not understand any association between messages and
 * form fields, or even between messages and forms.  It is designed to be used to show arbitrary
 * messages to the user, the prime example being a confirmation message displayed on the subsequent
 * page following an action.</p>
 *
 * <p>The messages tag outputs a header before the messages, the messages themselves, and a footer
 * after the messages.  Default values are set for each of these four items.  Different values
 * can be specified in the error messages resource bundle (StripesResources.properties unless you
 * have configured another). The default configuration would look like this:
 *
 * <ul>
 *   <li>stripes.messages.header={@literal <ul class="messages">}</li>
 *   <li>stripes.messages.footer={@literal </ul>}</li>
 *   <li>stripes.messages.beforeMessage={@literal <li>}</li>
 *   <li>stripes.messages.afterMessage={@literal </li>}</li>
 * </ul>
 *
 * <p>It should also be noted that while the errors tag supports custom headers and footers
 * through the use of nested tags, the messages tag does not support this. In fact the
 * messages tag does not support body content at all - it will simply be ignored.</p>
 *
 * @author Tim Fennell
 * @since Stripes 1.1.2
 */
public class MessagesTag extends HtmlTagSupport {

    /** The header that will be emitted if no header is defined in the resource bundle. */
    public static final String DEFAULT_HEADER = "<ul class=\"messages\">";

    /** The footer that will be emitted if no footer is defined in the resource bundle. */
    public static final String DEFAULT_FOOTER = "</ul>";

    /** The key that will be used to perform a scope search for messages. */
    private String key = StripesConstants.REQ_ATTR_MESSAGES;

    /**
     * Does nothing, all processing is performed in doEndTag().
     * @return SKIP_BODY in all cases.
     */
    @Override
    public int doStartTag() throws JspException {
        return SKIP_BODY;
    }

    /**
     * Outputs the set of messages approprate for this tag.
     * @return EVAL_PAGE always
     */
    @Override
    public int doEndTag() throws JspException {
        try {
            List<Message> messages = getMessages();

            if (messages != null && messages.size() > 0) {
                JspWriter writer = getPageContext().getOut();

                // Output all errors in a standard format
                Locale locale = getPageContext().getRequest().getLocale();
                ResourceBundle bundle = StripesFilter.getConfiguration()
                        .getLocalizationBundleFactory().getErrorMessageBundle(locale);

                // Fetch the header and footer
                String header, footer, beforeMessage, afterMessage;
                try { header = bundle.getString("stripes.messages.header"); }
                catch (MissingResourceException mre) { header = DEFAULT_HEADER; }

                try { footer = bundle.getString("stripes.messages.footer"); }
                catch (MissingResourceException mre) { footer = DEFAULT_FOOTER; }

                try { beforeMessage = bundle.getString("stripes.messages.beforeMessage"); }
                catch (MissingResourceException mre) { beforeMessage = "<li>"; }

                try { afterMessage = bundle.getString("stripes.messages.afterMessage"); }
                catch (MissingResourceException mre) { afterMessage = "</li>"; }

                // Write out the error messages
                writer.write(header);

                for (Message message : messages) {
                    writer.write(beforeMessage);
                    writer.write(message.getMessage(locale));
                    writer.write(afterMessage);
                }

                writer.write(footer);
            }
            return EVAL_PAGE;
        }
        catch (IOException e) {
            throw new JspException("IOException encountered while writing messages " +
                    "tag to the JspWriter.", e);
        }
    }

    /** Gets the key that will be used to scope search for messages to display. */
    public String getKey() { return key; }

    /** Sets the key that will be used to scope search for messages to display. */
    public void setKey(String key) { this.key = key; }

    /**
     * Gets the list of messages that will be displayed by the tag.  Looks first in the request
     * under the specified key, and if none are found, then looks in session under the same key.
     *
     * @return List<Message> a possibly null list of messages to display
     */
    @SuppressWarnings("unchecked")
	protected List<Message> getMessages() {
        HttpServletRequest request = (HttpServletRequest) getPageContext().getRequest();
        List<Message> messages = (List<Message>) request.getAttribute( getKey() );

        if (messages == null) {
            messages = (List<Message>) request.getSession().getAttribute( getKey() );
            request.getSession().removeAttribute( getKey() );
        }

        return messages;
    }
}
