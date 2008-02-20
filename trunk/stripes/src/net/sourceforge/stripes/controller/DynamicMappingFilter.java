/* Copyright 2008 Ben Gunter
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

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletResponseWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.exception.StripesServletException;

/**
 * <p>
 * A servlet filter that dynamically maps URLs to {@link ActionBean}s. This filter can be used to
 * allow Stripes to dispatch requests to {@link ActionBean}s based on their URL binding, even if
 * the URL to which they are bound is not explicitly mapped in {@code web.xml}.
 * </p>
 * <p>
 * There are a few caveats that must be observed when using this filter:
 * <ul>
 * <li>{@link StripesFilter} <em>MUST</em> be defined in {@code web.xml} so that it will be
 * loaded and initialized.</li>
 * <li>This filter <em>MUST</em> be mapped to {@code /*} to work correctly.</li>
 * <li>This filter <em>MUST</em> be the last filter in the filter chain. When it dynamically maps
 * an {@link ActionBean} to a URL, the filter chain is interrupted.</li>
 * </ul>
 * </p>
 * <p>
 * {@link StripesFilter} and {@link DispatcherServlet} need not be mapped to any URL patterns in
 * {@code web.xml} since this filter will determine at runtime whether or not they need to be
 * invoked. In fact, you don't even need to define {@link DispatcherServlet} in {@code web.xml} at
 * all because this filter uses an instance it creates and manages internally. However, some
 * resources, such as JSPs, may require access to the Stripes {@link Configuration}. Thus,
 * {@link StripesFilter} should be mapped to {@code *.jsp} if you intend to access JSPs directly.
 * </p>
 * <p>
 * This filter takes the following approach to determining when to dispatch an {@link ActionBean}:
 * <ol>
 * <li>Allow the request to process normally, trapping any HTTP errors that are returned.</li>
 * <li>If no error was returned then do nothing, allowing the request to complete successfully. If
 * any error other than {@code 404} was returned then send the error through. Otherwise ...</li>
 * <li>Check the {@link ActionResolver} to see if an {@link ActionBean} is mapped to the URL. If
 * not, then send the {@code 404} error through. Otherwise...</li>
 * <li>Invoke {@link StripesFilter} and {@link DispatcherServlet}</li>
 * </ol>
 * </p>
 * <p>
 * One benefit of this approach is that static resources can be delivered from the same namespace to
 * which an {@link ActionBean} is mapped. For example, if your {@code UserActionBean} is mapped to
 * {@code @UrlBinding("/user/{id}/{$event}")} and you have a static file at {@code /user/icon.gif},
 * then your icon will be delivered correctly because the initial request will not have returned a
 * {@code 404} error.
 * </p>
 * <p>
 * This is the suggested mapping for this filter in {@code web.xml}.
 * </p>
 * 
 * <pre>
 *  &lt;filter&gt;
 *      &lt;description&gt;Dynamically maps URLs to ActionBeans.&lt;/description&gt;
 *      &lt;display-name&gt;Stripes Dynamic Mapping Filter&lt;/display-name&gt;
 *      &lt;filter-name&gt;DynamicMappingFilter&lt;/filter-name&gt;
 *      &lt;filter-class&gt;
 *          net.sourceforge.stripes.controller.DynamicMappingFilter
 *      &lt;/filter-class&gt;
 *  &lt;/filter&gt;
 *  
 *  &lt;filter-mapping&gt;
 *      &lt;filter-name&gt;DynamicMappingFilter&lt;/filter-name&gt;
 *      &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 *      &lt;dispatcher&gt;REQUEST&lt;/dispatcher&gt;
 *      &lt;dispatcher&gt;FORWARD&lt;/dispatcher&gt;
 *      &lt;dispatcher&gt;INCLUDE&lt;/dispatcher&gt;
 *  &lt;/filter-mapping&gt;
 * </pre>
 * 
 * @author Ben Gunter
 * @since Stripes 1.5
 */
public class DynamicMappingFilter implements Filter {
    /**
     * An {@link HttpServletResponseWrapper} that traps HTTP errors by overriding
     * {@code sendError(int, ..)}. The error code can be retrieved by calling
     * {@link #getErrorCode()}. A call to {@link #proceed()} sends the error to the client.
     * 
     * @author Ben Gunter
     * @since Stripes 1.5
     */
    public static class ErrorTrappingResponseWrapper extends HttpServletResponseWrapper {
        private Integer errorCode;
        private String errorMessage;

        /** Wrap the given {@code response}. */
        public ErrorTrappingResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void sendError(int errorCode, String errorMessage) throws IOException {
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
        }

        @Override
        public void sendError(int errorCode) throws IOException {
            this.errorCode = errorCode;
            this.errorMessage = null;
        }

        /** Get the error code that was passed into {@code sendError(int, ..)} */
        public Integer getErrorCode() {
            return errorCode;
        }

        /**
         * Send the error, if any, to the client. If {@code sendError(int, ..)} has not previously
         * been called, then do nothing.
         */
        public void proceed() throws IOException {
            if (errorCode != null) {
                if (errorMessage == null)
                    super.sendError(errorCode);
                else
                    super.sendError(errorCode, errorMessage);
            }
        }
    }

    private boolean initialized = false;
    private ServletContext servletContext;
    private StripesFilter stripesFilter;
    private DispatcherServlet stripesDispatcher;

    public void init(final FilterConfig config) throws ServletException {
        this.servletContext = config.getServletContext();
        this.stripesDispatcher = new DispatcherServlet();
        this.stripesDispatcher.init(new ServletConfig() {
            public String getInitParameter(String name) {
                return config.getInitParameter(name);
            }

            public Enumeration<?> getInitParameterNames() {
                return config.getInitParameterNames();
            }

            public ServletContext getServletContext() {
                return config.getServletContext();
            }

            public String getServletName() {
                return config.getFilterName();
            }
        });
    }

    public void destroy() {
        stripesDispatcher.destroy();
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // Initialize (only once)
        if (!initialized)
            doOneTimeConfiguration();

        // Wrap the response in a wrapper that catches errors (but not exceptions)
        ErrorTrappingResponseWrapper wrapper = findWrapper(response);
        if (wrapper == null) {
            wrapper = new ErrorTrappingResponseWrapper((HttpServletResponse) response);
            chain.doFilter(request, wrapper);
        }
        else {
            chain.doFilter(request, response);
        }

        // If a SC_NOT_FOUND error occurred, then try to match an ActionBean to the URL
        if (wrapper.getErrorCode() != null) {
            if (wrapper.getErrorCode() == HttpServletResponse.SC_NOT_FOUND) {
                final ErrorTrappingResponseWrapper finalWrapper = wrapper;
                stripesFilter.doFilter(request, response, new FilterChain() {
                    public void doFilter(ServletRequest request, ServletResponse response)
                            throws IOException, ServletException {
                        String uri = ((HttpServletRequest) request).getRequestURI();
                        String contextPath = ((HttpServletRequest) request).getContextPath();
                        if (contextPath.length() > 1)
                            uri = uri.substring(contextPath.length());

                        // Look for an ActionBean that is mapped to the URI
                        Class<? extends ActionBean> beanType = StripesFilter.getConfiguration()
                                .getActionResolver().getActionBeanType(uri);

                        // If found then call the dispatcher directly. Otherwise, send the error.
                        if (beanType == null)
                            finalWrapper.proceed();
                        else
                            stripesDispatcher.service(request, response);
                    }
                });
            }
            else {
                wrapper.proceed();
            }
        }
    }

    /**
     * Perform initialization that can't be done in {@code init(..)}. This is normally called only
     * once, on the first invocation of {@code doFilter(..)}.
     */
    protected void doOneTimeConfiguration() throws ServletException {
        stripesFilter = (StripesFilter) servletContext.getAttribute(StripesFilter.class.getName());
        if (stripesFilter == null) {
            throw new StripesServletException("Could not get a reference to StripesFilter from "
                    + "the servlet context. The dynamic mapping filter works in conjunction with "
                    + "StripesFilter and requires that it be defined in web.xml");
        }
        initialized = true;
    }

    /**
     * Ascends the stack of response wrappers searching for an instance of
     * {@link ErrorTrappingResponseWrapper}.
     */
    protected ErrorTrappingResponseWrapper findWrapper(ServletResponse response) {
        while (response instanceof ServletResponseWrapper) {
            if (response instanceof ErrorTrappingResponseWrapper)
                return (ErrorTrappingResponseWrapper) response;
            response = ((ServletResponseWrapper) response).getResponse();
        }
        return null;
    }
}