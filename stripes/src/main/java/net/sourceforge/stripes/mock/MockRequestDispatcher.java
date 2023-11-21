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
package net.sourceforge.stripes.mock;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequestWrapper;
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
