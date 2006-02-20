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
package net.sourceforge.stripes.mock;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;

/**
 * Mock implementation of a RequesetDispatcher used for testing purposes. Note that the mock
 * implementation does not support actually forwarding the request, or including other resources.
 * The methods are implemented to record that a forward/include took place and then simply
 * return.
 *
 * @author Tim Fennell
 * @since Stripes 1.1.1
 */
public class MockRequestDispatcher implements RequestDispatcher {
    private String url;

    /** Constructs a request dispatcher, giving it a handle to the creating request. */
    public MockRequestDispatcher(String url) {
        this.url = url;
    }

    /** Simply stores the URL that was requested for forward, and returns. */
    public void forward(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        getMockRequest(req).setForwardUrl(this.url);
    }

    /** Simply stores that the URL was included an then returns. */
    public void include(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        getMockRequest(req).addIncludedUrl(this.url);
    }

    /** Locates the MockHttpServletRequest in case it is wrapped. */
    public MockHttpServletRequest getMockRequest(ServletRequest request) {
        while (request != null & !(request instanceof MockHttpServletRequest)) {
            request = ((HttpServletRequestWrapper) request).getRequest();
        }

        return (MockHttpServletRequest) request;
    }
}
