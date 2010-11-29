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
package net.sourceforge.stripes.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sourceforge.stripes.controller.FlashScope;
import net.sourceforge.stripes.controller.StripesConstants;
import net.sourceforge.stripes.exception.SourcePageNotFoundException;
import net.sourceforge.stripes.util.CryptoUtil;
import net.sourceforge.stripes.validation.ValidationErrors;

/**
 * <p>Encapsulates information about the current request.  Also provides access to the underlying
 * Servlet API should you need to use it for any reason.</p>
 *
 * <p>Developers should generally consider subclassing ActionBeanContext to provide a facade
 * to contextual state for their application.  Type safe getters and setter can be added to
 * the subclass and used by the application, thus hiding where the information is actually
 * stored.  This approach is documented in more detail in the Stripes documentation on
 * <a href="http://stripesframework.org/display/stripes/State+Management">State Management</a>.</p>
 *
 * @author Tim Fennell
 */
public class ActionBeanContext {
    private HttpServletRequest request;
    private HttpServletResponse response;
    private ServletContext servletContext;
    private String eventName;
    private ValidationErrors validationErrors;

    /**
     * Retrieves the HttpServletRequest object that is associated with the current request.
     * @return HttpServletRequest the current request
     */
    public HttpServletRequest getRequest() {
        return request;
    }

    /**
     * Used by the DispatcherServlet to set the HttpServletRequest for the current request
     * @param request the current request
     */
    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    /**
     * Retrieves the HttpServletResponse that is associated with the current request.
     * @return HttpServletResponse the current response
     */
    public HttpServletResponse getResponse() {
        return response;
    }

    /**
     * Used by the DispatcherServlet to set the HttpServletResponse that is associated with
     * the current request.
     * @param response the current response
     */
    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    /**
     * Retrieves the ServletContext object that is associated with the context in which the
     * current request is being processed.
     * @return ServletContext the current ServletContext
     */
    public ServletContext getServletContext() {
        return servletContext;
    }

    /**
     * Sets the ServletContext object that is associated with the context in which the
     * current request is being processed.
     * @param servletContext the current ServletContext
     */
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /**
     * Supplies the name of the event being handled.  While a specific method is usually invoked on
     * an ActionBean, through the use of default handlers ambiguity can arise.  This allows
     * ActionBeans to definitively know the name of the event that was fired.
     *
     * @return String the name of the event being handled
     */
    public String getEventName() {
        return eventName;
    }

    /**
     * Used by the DispatcherServlet to set the name of the even being handled.
     * @param eventName the name of the event being handled
     */
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    /**
     * Returns the set of validation errors associated with the current form. Lazily
     * initialized the set of errors, and will never return null.
     *
     * @return a Collection of validation errors
     */
    public ValidationErrors getValidationErrors() {
        if (this.validationErrors == null) {
            this.validationErrors = new ValidationErrors();
        }

        return validationErrors;
    }

    /**
     * Replaces the current set of validation errors.
     * @param validationErrors a collect of validation errors
     */
    public void setValidationErrors(ValidationErrors validationErrors) {
        this.validationErrors = validationErrors;
    }

    /**
     * <p>Returns the default set of non-error messages associated with the current request.
     * Guaranteed to always return a List, though the list may be empty. It is envisaged that
     * messages will normally be added to the request as follows:</p>
     *
     *<pre>
     *getContext().getMessages().add( ... );
     *</pre>
     *
     * <p>To remove messages from the current request fetch the list of messages and invoke
     * remove() or clear().  Messages will be made available to JSPs during the current
     * request and in the subsequent request if a redirect is issued.</p>
     *
     * @return a List of Message objects associated with the current request, never null.
     * @see ActionBeanContext#getMessages(String)
     */
    public List<Message> getMessages() {
        return getMessages(StripesConstants.REQ_ATTR_MESSAGES);
    }

    /**
     * <p>Returns the set of non-error messages associated with the current request under the
     * specified key. Can be used to manage multiple lists of messages, for different purposes.
     * Guaranteed to always return a List, though the list may be empty. It is envisaged that
     * messages will normally be added to the request as follows:</p>
     *
     *<pre>
     *getContext().getMessages(key).add( ... );
     *</pre>
     *
     * <p>To remove messages from the current request fetch the list of messages and invoke
     * remove() or clear().</p>
     *
     * <p>Messages are stored in a {@link net.sourceforge.stripes.controller.FlashScope} for
     * the current request. This means that they are available in request scope using the
     * supplied key during both this request, and the subsequent request if it is the result
     * of a redirect.</p>
     *
     * @return a List of Message objects associated with the current request, never null.
     */
    @SuppressWarnings("unchecked")
	public List<Message> getMessages(String key) {
        FlashScope scope = FlashScope.getCurrent(getRequest(), true);
        List<Message> messages = (List<Message>) scope.get(key);

        if (messages == null) {
            messages = new ArrayList<Message>();
            scope.put(key, messages);
        }

        return messages;
    }

    /**
     * Gets the Locale that is being used to service the current request. This is *not* the value
     * that was submitted in the request, but the value picked by the configured LocalePicker
     * which takes into consideration the locales preferred in the request.
     *
     * @return Locale the locale being used for the current request
     * @see net.sourceforge.stripes.localization.LocalePicker
     */
    public Locale getLocale() {
        return this.request.getLocale();
    }

    /**
     * <p>Returns a resolution that can be used to return the user to the page from which they
     * submitted they current request.  Most useful in situations where a user-correctable error
     * has occurred that was too difficult or expensive to check at validation time. In that case
     * an ActionBean can call setValidationErrors() and then return the resolution provided by
     * this method.</p>
     *
     * @return Resolution a resolution that will forward the user to the page they came from
     * @throws SourcePageNotFoundException if the information required to construct a source page
     *             resolution cannot be found in the request.
     * @see #getSourcePage()
     */
    public Resolution getSourcePageResolution() throws SourcePageNotFoundException {
        String sourcePage = getSourcePage();
        if (sourcePage == null) {
            throw new SourcePageNotFoundException(this);
        }
        else {
            return new ForwardResolution(sourcePage);
        }
    }

    /**
     * <p>
     * Returns the context-relative path to the page from which the user submitted they current
     * request.
     * </p>
     * 
     * @return Resolution a resolution that will forward the user to the page they came from
     * @throws IllegalStateException if the information required to construct a source page
     *             resolution cannot be found in the request.
     * @see #getSourcePageResolution()
     */
    public String getSourcePage() {
        String sourcePage = request.getParameter(StripesConstants.URL_KEY_SOURCE_PAGE);
        if (sourcePage != null) {
            sourcePage = CryptoUtil.decrypt(sourcePage);
        }
        return sourcePage;
    }

    /**
     * Returns a String with the name of the event for which the instance holds context, and
     * the set of validation errors, if any.
     */
    @Override
    public String toString() {
        return getClass().getName() + "{" +
            "eventName='" + eventName + "'" +
            ", validationErrors=" + validationErrors +
            "}";
    }
}
