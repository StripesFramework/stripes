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
package net.sourceforge.stripes.action;

import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.controller.StripesConstants;
import net.sourceforge.stripes.controller.FlashScope;
import net.sourceforge.stripes.util.Log;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;

/**
 * Encapsulates information about the current request.  Also provides access to the underlying
 * Servlet API should you need to use it for any reason.
 *
 * @author Tim Fennell
 */
public class ActionBeanContext {
    private static Log log = Log.getInstance(ActionBeanContext.class);

    private HttpServletRequest request;
    private HttpServletResponse response;
    private String eventName;
    private ValidationErrors validationErrors;

    /**
     * Retreives the HttpServletRequest object that is associated with the current request.
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
     * Returns the set of validation errors associated with the current form.
     * @return a Collection of validation errors
     */
    public ValidationErrors getValidationErrors() {
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
     * remove() or clear().</p>
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
     * @throws IllegalStateException if the information required to construct a source page
     *         resolution cannot be found in the request.
     *
     */
    public Resolution getSourcePageResolution() {
        String sourcePage = request.getParameter(StripesConstants.URL_KEY_SOURCE_PAGE);

        if (sourcePage == null) {
            throw new IllegalStateException(
                    "Here's how it is. Someone (quite possible the Stripes Dispatcher) needed " +
                    "to get the source page resolution. But no source page was supplied in the " +
                    "request, and unless you override ActionBeanContext.getSourcePageResolution() " +
                    "you're going to need that value. When you use a <stripes:form> tag a hidden " +
                    "field called '" + StripesConstants.URL_KEY_SOURCE_PAGE + "' is included. " +
                    "If you write your own forms or links that could generate validation errors, " +
                    "you must include a value  for this parameter. This can be done by calling " +
                    "request.getServletPath().");
        }
        else {
            return new ForwardResolution(sourcePage);
        }
    }


    /**
     * Returns a String with the name of the event for which the instance holds context, and
     * the set of validation errors, if any.
     */
    public String toString() {
        return "ActionBeanContext{" +
            "eventName='" + eventName + "'" +
            ", validationErrors=" + validationErrors +
            "}";
    }
}
