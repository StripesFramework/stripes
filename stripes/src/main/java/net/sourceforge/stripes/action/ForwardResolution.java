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

import net.sourceforge.stripes.controller.AsyncResponse;
import net.sourceforge.stripes.controller.StripesConstants;
import net.sourceforge.stripes.util.Log;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <p>
 * Resolution that uses the Servlet API to <em>forward</em> the user to another
 * path within the same web application using a server side forward.</p>
 *
 * <p>
 * There is one case when this resolution will issue an include instead of a
 * forward. The Servlet specification is ambiguous about what should happen when
 * a forward is issued inside of an include. The behaviour varies widely by
 * container, from outputting only the content of the forward, to only the
 * content prior to the include! To make this behaviour more consistent the
 * ForwardResolution will automatically determine if it is executing inside of
 * an include, and if that is the case it will <i>include</i> the appropriate
 * URL instead of
 * <i>forwarding</i> to it. This behaviour can be turned off be calling
 * {@literal autoInclude(false)}.</p>
 *
 * <p>
 * You can optionally set an HTTP status code with {@link #setStatus(int)}, in
 * which case a call to {@code response.setStatus(status)} will be made when
 * executing the resolution.</p>
 *
 * @see RedirectResolution
 * @author Tim Fennell
 */
public class ForwardResolution extends OnwardResolution<ForwardResolution> {

    private boolean autoInclude = true;
    private static final Log log = Log.getInstance(ForwardResolution.class);
    private String event;
    private Integer status;

    /**
     * Simple constructor that takes in the path to forward the user to.
     *
     * @param path the path within the web application that the user should be
     * forwarded to
     */
    public ForwardResolution(String path) {
        super(path);
    }

    /**
     * Constructs a ForwardResolution that will forward to the URL appropriate
     * for the ActionBean supplied. This constructor should be preferred when
     * forwarding to an ActionBean as it will ensure the correct URL is always
     * used.
     *
     * @param beanType the Class object representing the ActionBean to redirect
     * to
     */
    public ForwardResolution(Class<? extends ActionBean> beanType) {
        super(beanType);
    }

    /**
     * Constructs a ForwardResolution that will forward to the URL appropriate
     * for the ActionBean supplied. This constructor should be preferred when
     * forwarding to an ActionBean as it will ensure the correct URL is always
     * used.
     *
     * @param beanType the Class object representing the ActionBean to redirect
     * to
     * @param event the event that should be triggered on the redirect
     */
    public ForwardResolution(Class<? extends ActionBean> beanType, String event) {
        super(beanType, event);
        this.event = event;
    }

    /**
     * If true then the ForwardResolution will automatically detect when it is
     * executing as part of a server-side Include and <i>include</i> the
     * supplied URL instead of forwarding to it. Defaults to true.
     *
     * @param auto whether or not to automatically detect and use includes
     */
    public void autoInclude(boolean auto) {
        this.autoInclude = auto;
    }

    /**
     * Get the HTTP status, or <code>null</code> if none was explicitly set.
     *
     * @return HTTP status code associated with this resolution or null if none
     * was set.
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * Explicitly sets an HTTP status code, in which case a call to
     * {@code response.setStatus(status)} will be made when executing the
     * resolution.
     *
     * @param status - HTTP status code to associated with this resolution.
     * @return Current resolution object
     */
    public ForwardResolution setStatus(int status) {
        this.status = status;
        return this;
    }

    /**
     * Attempts to forward the user to the specified path.
     *
     * @param request - HTTP servlet request
     * @param response - HTTP servlet response
     * @throws ServletException thrown when the Servlet container encounters an
     * error
     * @throws IOException thrown when the Servlet container encounters an error
     */
    public void execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (status != null) {
            response.setStatus(status);
        }
        String path = getUrl(request.getLocale());

        // Set event name as a request attribute
        String oldEvent = (String) request.getAttribute(StripesConstants.REQ_ATTR_EVENT_NAME);
        request.setAttribute(StripesConstants.REQ_ATTR_EVENT_NAME, event);

        // are we asynchronous ?
        AsyncResponse asyncResponse = AsyncResponse.get(request);
        if (asyncResponse != null) {
            // async started, dispatch...
            log.trace("Async mode, dispatching to URL: ", path);
            asyncResponse.dispatch(path);
        } else {
            // Figure out if we're inside an include, and use an include instead of a forward
            if (autoInclude && request.getAttribute(StripesConstants.REQ_ATTR_INCLUDE_PATH) != null) {
                log.trace("Including URL: ", path);
                request.getRequestDispatcher(path).include(request, response);
            } else {
                log.trace("Forwarding to URL: ", path);
                request.getRequestDispatcher(path).forward(request, response);
            }

            // Revert event name to its original value
            request.setAttribute(StripesConstants.REQ_ATTR_EVENT_NAME, oldEvent);
        }

    }
}
