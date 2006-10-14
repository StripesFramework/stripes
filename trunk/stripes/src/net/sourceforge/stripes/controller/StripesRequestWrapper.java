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
package net.sourceforge.stripes.controller;

import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.controller.multipart.MultipartWrapper;
import net.sourceforge.stripes.exception.StripesServletException;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * HttpServletRequestWrapper that is used to make the file upload functionality transparent.
 * Every request handled by Stripes is wrapped.  Those containing multipart form file uploads
 * are parsed and treated differently, while normal requests are silently wrapped and all calls
 * are delgated to the real request.
 *
 * @author Tim Fennell
 */
@SuppressWarnings("deprecation") // otherwise deprecated methods in super are warned!
public class StripesRequestWrapper extends HttpServletRequestWrapper {
    /** The Multipart Request that parses out all the pieces. */
    private MultipartWrapper multipart;

    /** The Locale that is going to be used to process the request. */
    private Locale locale;

    /**
     * Looks for the StripesRequesetWrapper for the specific request and returns it. This is done
     * by checking to see if the request is a StripesRequestWrapper, and if not, successively
     * unwrapping the request until the StripesRequestWrapper is found.
     *
     * @param request the ServletRequest that is wrapped by a StripesRequestWrapper
     * @return the StripesRequestWrapper that is wrapping the supplied request
     * @throws IllegalStateException if the request is not wrapped by Stripes
     */
    public static StripesRequestWrapper findStripesWrapper(ServletRequest request) {
        // Loop through any request wrappers looking for the stripes one
        while ( !(request instanceof StripesRequestWrapper)
                && request != null
                && request instanceof HttpServletRequestWrapper) {
            request = ((HttpServletRequestWrapper) request).getRequest();
        }

        // If we have our wrapper after the loop exits, we're good; otherwise...
        if (request instanceof StripesRequestWrapper) {
            return (StripesRequestWrapper) request;
        }

        else {
            throw new IllegalStateException("A request made it through to some part of Stripes " +
                "without being wrapped in a StripesRequestWrapper. The StripesFilter is " +
                "responsible for wrapping the request, so it is likely that either the " +
                "StripesFilter is not deployed, or that it's mappings do not include the " +
                "DispatcherServlet _and_ *.jsp. Stripes does not requiire that the Stripes " +
                "wrapper is the only request wrapper, or the outermost; only that is is present.");
        }
    }

    /**
     * Constructor that will, if the POST is multi-part, parse the POST data and make it
     * available through the normal channels.  If the request is not a multi-part post then it is
     * just wrapped and the behaviour is unchanged.
     *
     * @param request the HttpServletRequest to wrap
     *        this is not a file size limit, but a post size limit.
     * @throws FileUploadLimitExceededException if the total post size is larger than the limit
     * @throws StripesServletException if any other error occurs constructing the wrapper
     */
    public StripesRequestWrapper(HttpServletRequest request) throws StripesServletException {
        super(request);

        String contentType = request.getContentType();
        if (contentType != null && contentType.startsWith("multipart/form-data")) {
            constructMultipartWrapper(request);
        }
    }

    /**
     * Responsible for constructing the MultipartWrapper object and setting it on to
     * the instnace variable 'multipart'.
     *
     * @param request the HttpServletRequest to wrap
     *        this is not a file size limit, but a post size limit.
     * @throws StripesServletException if any other error occurs constructing the wrapper
     */
    protected void constructMultipartWrapper(HttpServletRequest request) throws StripesServletException {
        try {
            this.multipart =
                    StripesFilter.getConfiguration().getMultipartWrapperFactory().wrap(request);
        }
        catch (IOException e) {
            throw new StripesServletException("Could not construct request wrapper.", e);
        }
    }

    /** Returns true if this request is wrapping a multipart request, false otherwise. */
    public boolean isMultipart() {
        return this.multipart != null;
    }

    /**
     * Fetches just the names of regular parameters and does not include file upload parameters. If
     * the request is multipart then the information is sourced from the parsed multipart object
     * otherwise it is just pulled out of the request in the usual manner.
     */
    public Enumeration<String> getParameterNames() {
        if ( isMultipart() ) {
            return this.multipart.getParameterNames();
        }
        else {
            return super.getParameterNames();
        }
    }

    /**
     * Returns all values sent in the request for a given parameter name. If the request is
     * multipart then the information is sourced from the parsed multipart object otherwise it is
     * just pulled out of the request in the usual manner.  Values are consistent with
     * HttpServletRequest.getParameterValues(String).  Values for file uploads cannot be retrieved
     * in this way (though parameters sent along with file uploads can).
     */
    public String[] getParameterValues(String name) {
        if ( isMultipart() ) {
            return this.multipart.getParameterValues(name);
        }
        else {
            return super.getParameterValues(name);
        }
    }

    /**
     * Retrieves the first value of the specified parameter from the request. If the parameter was
     * not sent, null will be returned.
     */
    public String getParameter(String name) {
        String[] values = getParameterValues(name);
        if (values != null && values.length > 0) {
            return values[0];
        }
        else {
            return null;
        }
    }


    /**
     * Returns a map of parameter name and values.
     */
    public Map<String,String[]> getParameterMap() {
        if (isMultipart()) {
            Map parameterMap = new HashMap();
            Enumeration names = this.multipart.getParameterNames();

            while (names.hasMoreElements()) {
                String name = (String) names.nextElement();
                parameterMap.put(name, getParameterValues(name));
            }

            return parameterMap;
        }
        else {
            return super.getParameterMap();
        }
    }

    /**
     * Provides access to the Locale being used to process the request.
     * @return a Locale object representing the chosen locale for the request.
     * @see net.sourceforge.stripes.localization.LocalePicker
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     *  Returns a single element enumeration containing the selected Locale for this request.
     *  @see net.sourceforge.stripes.localization.LocalePicker
     */
    public Enumeration getLocales() {
        List list = new ArrayList();
        list.add(this.locale);
        return Collections.enumeration(list);
    }

    ///////////////////////////////////////////////////////////////////////////
    // The following methods are specific to the StripesRequestWrapper and are
    // not present in the HttpServletRequest interface.
    ///////////////////////////////////////////////////////////////////////////

    /** Used by the dispatcher to set the Locale chosen by the configured LocalePicker. */
    protected void setLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * Returns the names of request parameters that represent files being uploaded by the user. If
     * no file upload parameters are submitted returns an empty enumeration.
     */
    public Enumeration<String> getFileParameterNames() {
        return this.multipart.getFileParameterNames();
    }

    /**
     * Returns a FileBean representing an uploaded file with the form field name = &quot;name&quot;.
     * If the form field was present in the request, but no file was uploaded, this method will
     * return null.
     *
     * @param name the form field name of type file
     * @return a FileBean if a file was actually submitted by the user, otherwise null
     */
    public FileBean getFileParameterValue(String name) {
        if (this.multipart != null) {
            return this.multipart.getFileParameterValue(name);
        }
        else {
            return null;
        }
    }
}
