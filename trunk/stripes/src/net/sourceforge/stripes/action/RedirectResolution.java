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

import net.sourceforge.stripes.util.UrlBuilder;
import net.sourceforge.stripes.controller.FlashScope;
import net.sourceforge.stripes.controller.StripesConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

/**
 * <p>Resolution that uses the Servlet API to <em>redirect</em> the user to another path by issuing
 * a client side redirect. Unlike the ForwardResolution the RedirectResolution can send the user to
 * any URL anywhere on the web - though it is more commonly used to send the user to a location
 * within the same application.<p>
 *
 * <p>By default the RedirectResolution will prepend the context path of the web application to
 * any URL before redirecting the request. To prevent the context path from being prepended
 * use the constructor: {@code RedirectResolution(String,boolean)}.</p>
 *
 * <p>It is also possible to append paramters to the URL to which the user will be redirected.
 * This can be done by manually adding parameters with the addParameter() and addParameters()
 * methods, and by invoking includeRequestParameters() which will cause all of the current
 * request parameters to be included into the URL.</p>
 *
 * @see ForwardResolution
 * @author Tim Fennell
 */
public class RedirectResolution extends OnwardResolution implements Resolution {
    private boolean prependContext;
    private boolean includeRequestParameters;
    Map<String,Object> parameters = new HashMap<String,Object>();

    /**
     * Simple constructor that takes the URL to which to forward the user. Defaults to
     * prepending the context path to the url supplied before redirecting.
     *
     * @param url the URL to which the user's browser should be re-directed.
     */
    public RedirectResolution(String url) {
        this(url, true);
    }

    /**
     * Constructor that allows explicit control over whether or not the context path is
     * prepended to the URL before redirecting.
     *
     * @param url the URL to which the user's browser should be re-directed.
     * @param prependContext true if the context should be prepended, false otherwise
     */
    public RedirectResolution(String url, boolean prependContext) {
        setPath(url);
        this.prependContext = prependContext;
    }

    /**
     * If set to true, will cause absoultly all request parameters present in the current request
     * to be appended to the redirect URL that will be sent to the browser. Since some browsers
     * and servers cannot handle extremely long URLs, care should be taken when using this
     * method with large form posts.
     *
     * @param inc whether or not current request parameters should be included in the redirect
     * @return RedirectResolution, this resolution so that methods can be chained
     */
    public RedirectResolution includeRequestParameters(boolean inc) {
        this.includeRequestParameters = inc;
        return this;
    }

    /**
     * Adds a request parameter with zero or more values to the redirect URL.  Values may
     * be supplied using varargs, or alternatively by suppling a single value parameter which is
     * an instance of Collection.
     *
     * @param name the name of the URL parameter
     * @param values zero or more scalar values, or a single Collection
     * @return this RedirectResolution so that methods can be chained
     */
    public RedirectResolution addParameter(String name, Object... values) {
        this.parameters.put(name, values);
        return this;
    }

    /**
     * Bulk adds one or more request parameters to the redirect URL. Each entry in the Map
     * represents a single named parameter, with the values being either a scalar value,
     * an array or a Collection.
     *
     * @param parameters a Map of parameters as described above
     * @return this RedirectResolution so that methods can be chained
     */
    public RedirectResolution addParameters(Map<String,Object> parameters) {
        this.parameters.putAll(parameters);
        return this;
    }

    /**
     * Attempts to redirect the user to the specified URL.
     *
     * @throws ServletException thrown when the Servlet container encounters an error
     * @throws IOException thrown when the Servlet container encounters an error
     */
    public void execute(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        String path = getPath();
        if (this.prependContext) {
            path = request.getContextPath() + path;
        }

        // Use a UrlBuilder to munge in any parameters
        UrlBuilder builder = new UrlBuilder(path);
        if (this.includeRequestParameters) {
            builder.addParameters(request.getParameterMap());
        }
        builder.addParameters(this.parameters);

        // Add the flash scope id if there's a flash scope present
        FlashScope flash = FlashScope.getCurrent(request, false);
        if (flash != null) {
            builder.addParameter(StripesConstants.URL_KEY_FLASH_SCOPE_ID, flash.key());
        }

        response.sendRedirect( response.encodeRedirectURL(builder.toString()) );
    }
}
