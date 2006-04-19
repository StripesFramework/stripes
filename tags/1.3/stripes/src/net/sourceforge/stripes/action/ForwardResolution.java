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

import net.sourceforge.stripes.util.Log;

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
    private static final Log log = Log.getInstance(ForwardResolution.class);

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

        String path = getPath();
        log.trace("Forwarding to path: ", path);

        request.getRequestDispatcher(path).forward(request, response);
    }
}
