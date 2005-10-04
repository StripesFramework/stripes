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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;

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
 * @see ForwardResolution
 * @author Tim Fennell
 */
public class RedirectResolution extends OnwardResolution implements Resolution {
    private boolean prependContext;

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

        response.sendRedirect(path);
    }
}
