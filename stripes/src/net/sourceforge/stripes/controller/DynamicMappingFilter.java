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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.exception.StripesServletException;
import net.sourceforge.stripes.util.HttpUtil;
import net.sourceforge.stripes.util.Log;

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
 * which an {@link ActionBean} is mapped using clean URLs. (Form more information on clean URLs, see
 * {@link UrlBinding}.) For example, if your {@code UserActionBean} is mapped to
 * {@code @UrlBinding("/user/{id}/{$event}")} and you have a static file at {@code /user/icon.gif},
 * then your icon will be delivered correctly because the initial request will not have returned a
 * {@code 404} error.
 * </p>
 * <p>
 * This filter accepts one init-param. {@code IncludeBufferSize} (optional, default 1024) sets the
 * number of characters to be buffered by {@link TempBufferWriter} for include requests. See
 * {@link TempBufferWriter} for more information.
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
 * @see UrlBinding
 */
public class DynamicMappingFilter implements Filter {
    /**
     * <p>
     * A {@link Writer} that passes characters to a {@link PrintWriter}. It buffers the first
     * {@code N} characters written to it and automatically overflows when the number of characters
     * written exceeds the limit. The size of the buffer defaults to 1024 characters, but it can be
     * changed using the {@code IncludeBufferSize} filter init-param in {@code web.xml}. If
     * {@code IncludeBufferSize} is zero or negative, then a {@link TempBufferWriter} will not be
     * used at all. This is only a good idea if your servlet container does not write an error
     * message to output when it can't find an included resource or if you only include resources
     * that do not depend on this filter to be delivered, such as other servlets, JSPs, static
     * resources, ActionBeans that are mapped with a prefix ({@code /action/*}) or suffix ({@code *.action}),
     * etc.
     * </p>
     * <p>
     * This writer is used to partially buffer the output of includes. Some (all?) servlet
     * containers write a message to the output stream indicating if an included resource is missing
     * because if the response has already been committed, they cannot send a 404 error. Since the
     * filter depends on getting a 404 before it attempts to dispatch an {@code ActionBean}, that
     * is problematic. So in using this writer, we assume that the length of the "missing resource"
     * message will be less than the buffer size and we discard that message if we're able to map
     * the included URL to an {@code ActionBean}. If there is no 404 then the output will be sent
     * normally. If there is a 404 and the URL does not match an ActionBean then the "missing
     * resource" message is sent through.
     * </p>
     * 
     * @author Ben Gunter
     * @since Stripes 1.5
     */
    public static class TempBufferWriter extends Writer {
        private StringWriter buffer;
        private PrintWriter out;

        public TempBufferWriter(PrintWriter out) {
            this.out = out;
            this.buffer = new StringWriter(includeBufferSize);
        }

        @Override
        public void close() throws IOException {
            flush();
            out.close();
        }

        @Override
        public void flush() throws IOException {
            overflow();
            out.flush();
        }

        @Override
        public void write(char[] chars, int offset, int length) throws IOException {
            if (buffer == null) {
                out.write(chars, offset, length);
            }
            else if (buffer.getBuffer().length() + length > includeBufferSize) {
                overflow();
                out.write(chars, offset, length);
            }
            else {
                buffer.write(chars, offset, length);
            }
        }

        /**
         * Write the contents of the buffer to the underlying writer. After a call to
         * {@link #overflow()}, all future writes to this writer will pass directly to the
         * underlying writer.
         */
        protected void overflow() {
            if (buffer != null) {
                out.print(buffer.toString());
                buffer = null;
            }
        }
    }

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
        private boolean include;
        private PrintWriter printWriter;
        private TempBufferWriter tempBufferWriter;

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

        @Override
        public PrintWriter getWriter() throws IOException {
            if (isInclude() && includeBufferSize > 0) {
                if (printWriter == null) {
                    tempBufferWriter = new TempBufferWriter(super.getWriter());
                    printWriter = new PrintWriter(tempBufferWriter);
                }
                return printWriter;
            }
            else {
                return super.getWriter();
            }
        }

        /** True if the currently executing request is an include. */
        public boolean isInclude() {
            return include;
        }

        /** Indicate if the currently executing request is an include. */
        public void setInclude(boolean include) {
            this.include = include;
        }

        /** Get the error code that was passed into {@code sendError(int, ..)} */
        public Integer getErrorCode() {
            return errorCode;
        }

        /** Clear error code and error message. */
        public void clearError() {
            this.errorCode = null;
            this.errorMessage = null;
        }

        /**
         * Send the error, if any, to the client. If {@code sendError(int, ..)} has not previously
         * been called, then do nothing.
         */
        public void proceed() throws IOException {
            // Explicitly overflow the buffer so the output gets written
            if (tempBufferWriter != null)
                tempBufferWriter.overflow();

            if (errorCode != null) {
                if (errorMessage == null)
                    super.sendError(errorCode);
                else
                    super.sendError(errorCode, errorMessage);
            }
        }
    }

    /**
     * The name of the init-param that can be used to set the size of the buffer used by
     * {@link TempBufferWriter} before it overflows.
     */
    private static final String INCLUDE_BUFFER_SIZE_PARAM = "IncludeBufferSize";

    /** The size of the buffer used by {@link TempBufferWriter} before it overflows. */
    private static int includeBufferSize = 1024;

    /** Logger */
    private static Log log = Log.getInstance(DynamicMappingFilter.class);

    private boolean initialized = false;
    private ServletContext servletContext;
    private StripesFilter stripesFilter;
    private DispatcherServlet stripesDispatcher;

    public void init(final FilterConfig config) throws ServletException {
        try {
            includeBufferSize = Integer.valueOf(config.getInitParameter(INCLUDE_BUFFER_SIZE_PARAM)
                    .trim());
            log.info(DynamicMappingFilter.class.getName(), " include buffer size is ",
                    includeBufferSize);
        }
        catch (NullPointerException e) {
            // ignore it
        }
        catch (Exception e) {
            log.warn(e, "Could not interpret '",
                    config.getInitParameter(INCLUDE_BUFFER_SIZE_PARAM),
                    "' as a number for init-param '", INCLUDE_BUFFER_SIZE_PARAM,
                    "'. Using default value of ", includeBufferSize, ".");
        }

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
        final ErrorTrappingResponseWrapper wrapper = new ErrorTrappingResponseWrapper(
                (HttpServletResponse) response);
        wrapper.setInclude(request.getAttribute(StripesConstants.REQ_ATTR_INCLUDE_PATH) != null);

        // Catch FileNotFoundException, which some containers (e.g. GlassFish) throw instead of setting SC_NOT_FOUND
        boolean fileNotFoundExceptionThrown = false;

        try {
          chain.doFilter(request, wrapper);
        }
        catch (FileNotFoundException exc) {
          fileNotFoundExceptionThrown = true;
        }

        // If a FileNotFoundException or SC_NOT_FOUND error occurred, then try to match an ActionBean to the URL
        Integer errorCode = wrapper.getErrorCode();
        if ((errorCode != null && errorCode == HttpServletResponse.SC_NOT_FOUND) || fileNotFoundExceptionThrown) {
            stripesFilter.doFilter(request, response, new FilterChain() {
                public void doFilter(ServletRequest request, ServletResponse response)
                        throws IOException, ServletException {
                    // Look for an ActionBean that is mapped to the URI
                    String uri = HttpUtil.getRequestedPath((HttpServletRequest) request);
                    Class<? extends ActionBean> beanType = StripesFilter.getConfiguration()
                            .getActionResolver().getActionBeanType(uri);

                    // If found then call the dispatcher directly. Otherwise, send the error.
                    if (beanType == null) {
                        wrapper.proceed();
                    }
                    else {
                        stripesDispatcher.service(request, response);
                    }
                }
            });
        }
        else {
            wrapper.proceed();
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
}
