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
 * Resolution that uses the Servlet API to <em>forward</em> the user to another path within the
 * same web application using a server side forward.
 *
 * @see RedirectResolution
 * @author Tim Fennell
 */
public class ForwardResolution extends OnwardResolution implements Resolution {
    /**
     * Simple constructor that takes in the path to forward the user to.
     * @param path the path within the web application that the user should be forwarded to
     */
    public ForwardResolution(String path) {
        super(path);
    }

    /**
     * Constructs a ForwardResolution that will forward to the URL appropriate for
     * the ActionBean supplied.  This constructor should be preferred when forwarding
     * to an ActionBean as it will ensure the correct URL is always used.
     *
     * @param beanType the Class object representing the ActionBean to redirect to
     */
    public ForwardResolution(Class<? extends ActionBean> beanType) {
        super(beanType);
    }

    /**
     * Constructs a ForwardResolution that will forward to the URL appropriate for
     * the ActionBean supplied.  This constructor should be preferred when forwarding
     * to an ActionBean as it will ensure the correct URL is always used.
     *
     * @param beanType the Class object representing the ActionBean to redirect to
     * @param event the event that should be triggered on the redirect
     */
    public ForwardResolution(Class<? extends ActionBean> beanType, String event) {
        super(beanType, event);
    }

    /**
     * Attempts to forward the user to the specified path.
     * @throws ServletException thrown when the Servlet container encounters an error
     * @throws IOException thrown when the Servlet container encounters an error
     */
    public void execute(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        request.getRequestDispatcher(getPath()).forward(request, response);
    }
}
