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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.exception.StripesServletException;
import net.sourceforge.stripes.util.HttpUtil;
import net.sourceforge.stripes.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * <p>
 * A servlet filter that dynamically maps URLs to {@link ActionBean}s. This filter can be used to
 * allow Stripes to dispatch requests to {@link ActionBean}s based on their URL binding, even if the
 * URL to which they are bound is not explicitly mapped in {@code web.xml}.
 * </p>
 * <p>
 * One caveat must be observed when using this filter. This filter <em>MUST</em> be the last filter
 * in the filter chain. When it dynamically maps an {@link ActionBean} to a URL, the filter chain is
 * interrupted.
 * </p>
 * <p>
 * {@link StripesFilter} and/or {@link DispatcherServlet} may be declared in {@code web.xml}, but
 * neither is required for this filter to work. If you choose not to declare {@link StripesFilter}
 * in {@code web.xml}, then this filter should be configured the way you would normally configure
 * {@link StripesFilter}. However, some resources, such as JSPs, may require access to the Stripes
 * {@link Configuration} through {@link StripesFilter}. If you intend to access JSPs directly, then
 * {@link StripesFilter} should be explicitly mapped to {@code *.jsp}.
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
 * which an {@link ActionBean} is mapped using clean URLs. (For more information on clean URLs, see
 * {@link UrlBinding}.) For example, if your {@code UserActionBean} is mapped to
 * {@code @UrlBinding("/user/{id}/{$event}")} and you have a static file at {@code /user/icon.gif},
 * then your icon will be delivered correctly because the initial request will not have returned a
 * {@code 404} error.
 * </p>
 * <p>
 * The {@code IncludeBufferSize} initialization parameter (optional, default 1024) sets the number
 * of characters to be buffered by {@link TempBufferWriter} for include requests. See
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
 *      &lt;init-param&gt;
 *          &lt;param-name&gt;ActionResolver.Packages&lt;/param-name&gt;
 *          &lt;param-value&gt;com.yourcompany.stripes.action&lt;/param-value&gt;
 *      &lt;/init-param&gt;
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
    public static final String INCLUDE_BUFFER_SIZE_PARAM = "IncludeBufferSize";

    /**
     * The attribute name used to store a reference to {@link StripesFilter} in the servlet context.
     */
    public static final String CONTEXT_KEY_STRIPES_FILTER = StripesFilter.class.getName();

    /**
     * Request header that indicates that the current request is part of the process of trying to
     * force initialization of {@link StripesFilter}. If this header is present then
     * {@link DynamicMappingFilter} makes no attempt to map the request to an {@link ActionBean}.
     */
    private static final String REQ_HEADER_INIT_FLAG = "X-Dynamic-Mapping-Filter-Init";

    /** The size of the buffer used by {@link TempBufferWriter} before it overflows. */
    private static int includeBufferSize = 1024;

    /** Logger */
    private static Log log = Log.getInstance(DynamicMappingFilter.class);

    private FilterConfig filterConfig;
    private ServletContext servletContext;
    private StripesFilter stripesFilter;
    private DispatcherServlet stripesDispatcher;
    private boolean stripesFilterIsInternal, initializing;

    public void init(final FilterConfig config) throws ServletException {
        try {
            String value = config.getInitParameter(INCLUDE_BUFFER_SIZE_PARAM);
            if (value != null) {
                includeBufferSize = Integer.valueOf(value.trim());
                log.info(getClass().getSimpleName(), " include buffer size is ", includeBufferSize);
            }
        }
        catch (Exception e) {
            log.warn(e, "Could not interpret '",
                    config.getInitParameter(INCLUDE_BUFFER_SIZE_PARAM),
                    "' as a number for init-param '", INCLUDE_BUFFER_SIZE_PARAM,
                    "'. Using default value ", includeBufferSize, ".");
        }

        this.filterConfig = config;
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
        try {
            if (stripesDispatcher != null)
                stripesDispatcher.destroy();
        }
        finally {
            stripesDispatcher = null;

            try {
                if (stripesFilterIsInternal && stripesFilter != null)
                    stripesFilter.destroy();
            }
            finally {
                stripesFilter = null;
            }
        }
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
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

        // Check the instance field as well as request header for initialization request
        boolean initializing = this.initializing
                || ((HttpServletRequest) request).getHeader(REQ_HEADER_INIT_FLAG) != null;

        // If a FileNotFoundException or SC_NOT_FOUND error occurred, then try to match an ActionBean to the URL
        Integer errorCode = wrapper.getErrorCode();
        if (!initializing && (errorCode != null && errorCode == HttpServletResponse.SC_NOT_FOUND)
                || fileNotFoundExceptionThrown) {
            // Get a reference to a StripesFilter instance
            StripesFilter sf = getStripesFilter();
            if (sf == null) {
                initStripesFilter((HttpServletRequest) request, wrapper);
                sf = getStripesFilter();
            }

            sf.doFilter(request, response, new FilterChain() {
                public void doFilter(ServletRequest request, ServletResponse response)
                        throws IOException, ServletException {
                    // Look for an ActionBean that is mapped to the URI
                    String uri = HttpUtil.getRequestedPath((HttpServletRequest) request);
                    Class<? extends ActionBean> beanType = getStripesFilter()
                            .getInstanceConfiguration().getActionResolver().getActionBeanType(uri);

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
     * Get a reference to {@link StripesFilter}. The first time this method is called, the reference
     * will be looked up in the servlet context and cached in the {@link #stripesFilter} field.
     */
    protected StripesFilter getStripesFilter() {
        if (stripesFilter == null) {
            stripesFilter = (StripesFilter) servletContext.getAttribute(CONTEXT_KEY_STRIPES_FILTER);
            if (stripesFilter != null) {
                log.debug("Found StripesFilter in the servlet context.");
            }
        }

        return stripesFilter;
    }

    /**
     * The servlet spec allows a container to wait until a filter is required to process a request
     * before it initializes the filter. Since we need to get a reference to {@link StripesFilter}
     * from the servlet context, we really need {@link StripesFilter} to have been initialized at
     * the time we process our first request. If that didn't happen automatically, this method does
     * its best to force it to happen.
     * 
     * @param request The current request
     * @param response The current response
     * @throws ServletException If anything goes wrong that simply can't be ignored.
     */
    protected synchronized void initStripesFilter(HttpServletRequest request,
            HttpServletResponse response) throws ServletException {
        try {
            // Check if another thread got into this method before the current thread
            if (getStripesFilter() != null)
                return;

            log.info("StripesFilter not initialized. Checking the situation in web.xml ...");
            Document document = parseWebXml();
            NodeList filterNodes = eval("/web-app/filter/filter-class[text()='"
                    + StripesFilter.class.getName() + "']/..", document, XPathConstants.NODESET);
            if (filterNodes == null || filterNodes.getLength() != 1) {
                String msg;
                if (filterNodes == null || filterNodes.getLength() < 1) {
                    msg = "StripesFilter is not declared in web.xml. ";
                }
                else {
                    msg = "StripesFilter is declared multiple times in web.xml; refusing to use either one. ";
                }

                log.info(msg, "Initializing with \"", filterConfig.getFilterName(),
                        "\" configuration.");
                createStripesFilter(filterConfig);
            }
            else {
                Node filterNode = filterNodes.item(0);
                final String name = eval("filter-name", filterNode, XPathConstants.STRING);
                log.debug("Found StripesFilter declared as ", name, " in web.xml");

                List<String> patterns = getFilterUrlPatterns(filterNode);
                if (patterns.isEmpty()) {
                    log.info("StripesFilter is declared but not mapped in web.xml. ",
                            "Initializing with \"", name, "\" configuration from web.xml.");

                    final Map<String, String> parameters = getFilterParameters(filterNode);
                    createStripesFilter(new FilterConfig() {
                        public ServletContext getServletContext() {
                            return servletContext;
                        }

                        public Enumeration<String> getInitParameterNames() {
                            return Collections.enumeration(parameters.keySet());
                        }

                        public String getInitParameter(String name) {
                            return parameters.get(name);
                        }

                        public String getFilterName() {
                            return name;
                        }
                    });
                }
                else {
                    issueRequests(patterns, request, response);
                }
            }
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new StripesServletException(
                    "Unhandled exception trying to force initialization of StripesFilter", e);
        }

        // Blow up if no StripesFilter instance could be acquired or created
        if (getStripesFilter() == null) {
            String msg = "There is no StripesFilter instance available in the servlet context, "
                    + "and DynamicMappingFilter was unable to initialize one. See previous log "
                    + "messages for more information.";
            log.error(msg);
            throw new StripesServletException(msg);
        }
    }

    /**
     * Parse the application's {@code web.xml} file and return a DOM {@link Document}.
     * 
     * @throws ParserConfigurationException If thrown by the XML parser
     * @throws IOException If thrown by the XML parser
     * @throws SAXException If thrown by the XML parser
     */
    protected Document parseWebXml() throws SAXException, IOException, ParserConfigurationException {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                servletContext.getResourceAsStream("/WEB-INF/web.xml"));
    }

    /**
     * Evaluate an xpath expression against a DOM {@link Node} and return the result.
     * 
     * @param expression The expression to evaluate
     * @param source The node against which the expression will be evaluated
     * @param returnType One of the constants defined in {@link XPathConstants}
     * @return The result returned by {@link XPath#evaluate(String, Object, QName)}
     * @throws XPathExpressionException If the xpath expression is invalid
     */
    @SuppressWarnings("unchecked")
    protected <T> T eval(String expression, Node source, QName returnType)
            throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        return (T) xpath.evaluate(expression, source, returnType);
    }

    /**
     * Get all the URL patterns to which a filter is mapped in {@code web.xml}. This includes direct
     * mappings using {@code filter-mapping/url-pattern} and indirect mappings using
     * {@code filter-mapping/servlet-name} and {@code servlet-mapping/url-pattern}.
     * 
     * @param filterNode The DOM ({@code &lt;filter&gt;}) {@link Node} containing the filter
     *            declaration from {@code web.xml}
     * @return A list of all the patterns to which the filter is mapped
     * @throws XPathExpressionException In case of failure evaluating an xpath expression
     */
    protected List<String> getFilterUrlPatterns(Node filterNode) throws XPathExpressionException {
        String filterName = eval("filter-name", filterNode, XPathConstants.STRING);
        Document document = filterNode.getOwnerDocument();

        NodeList urlMappings = eval("/web-app/filter-mapping/filter-name[text()='" + filterName
                + "']/../url-pattern", document, XPathConstants.NODESET);
        NodeList servletMappings = eval("/web-app/filter-mapping/filter-name[text()='" + filterName
                + "']/../servlet-name", document, XPathConstants.NODESET);

        List<String> patterns = new ArrayList<String>();
        if (urlMappings != null && urlMappings.getLength() > 0) {
            for (int i = 0; i < urlMappings.getLength(); i++) {
                patterns.add(urlMappings.item(i).getTextContent().trim());
            }
        }

        if (servletMappings != null && servletMappings.getLength() > 0) {
            for (int i = 0; i < servletMappings.getLength(); i++) {
                String servletName = servletMappings.item(i).getTextContent().trim();
                urlMappings = eval("/web-app/servlet-mapping/servlet-name[text()='" + servletName
                        + "']/../url-pattern", document, XPathConstants.NODESET);
                for (int j = 0; j < urlMappings.getLength(); j++) {
                    patterns.add(urlMappings.item(j).getTextContent().trim());
                }
            }
        }

        log.debug("Filter ", filterName, " maps to ", patterns);
        return patterns;
    }

    /**
     * Get the initialization parameters for a filter declared in {@code web.xml}.
     * 
     * @param filterNode The DOM ({@code &lt;filter&gt;}) {@link Node} containing the filter
     *            declaration from {@code web.xml}
     * @return A map of parameter names to parameter values
     * @throws XPathExpressionException In case of failure evaluation an xpath expression
     */
    protected Map<String, String> getFilterParameters(Node filterNode)
            throws XPathExpressionException {
        Map<String, String> params = new LinkedHashMap<String, String>();
        NodeList paramNodes = eval("init-param", filterNode, XPathConstants.NODESET);
        for (int i = 0; i < paramNodes.getLength(); i++) {
            Node node = paramNodes.item(i);
            String key = eval("param-name", node, XPathConstants.STRING);
            String value = eval("param-value", node, XPathConstants.STRING);
            params.put(key, value);
        }
        return params;
    }

    /**
     * Create and initialize an instance of {@link StripesFilter} with the given configuration.
     * 
     * @param config The filter configuration
     * @throws ServletException If initialization of the filter fails
     */
    protected void createStripesFilter(FilterConfig config) throws ServletException {
        StripesFilter filter = new StripesFilter();
        filter.init(config);
        this.stripesFilter = filter;
        this.stripesFilterIsInternal = true;
    }

    /**
     * Issue a series of requests in an attempt to force an invocation (and initialization) of
     * {@link StripesFilter} in the application context. All patterns will be requested first with
     * an internal forward, then an include and finally with a brand new request to the address and
     * port returned by {@link HttpServletRequest#getLocalAddr()} and
     * {@link HttpServletRequest#getLocalPort()}, respectively.
     * 
     * @param patterns The list of patterns to request, as specified by {@code url-pattern} elements
     *            in {@code web.xml}
     * @param request The current request, required to process a forward or include
     * @param response The current response, required to process a forward or include
     */
    protected void issueRequests(List<String> patterns, HttpServletRequest request,
            HttpServletResponse response) {
        // Replace globs in the patterns with a random string
        String random = "stripes-dmf-request-" + UUID.randomUUID();
        List<String> uris = new ArrayList<String>(patterns.size());
        for (String pattern : patterns) {
            String uri = pattern.replace("*", random);
            if (!uri.startsWith("/"))
                uri = "/" + uri;
            uris.add(uri);
        }

        // Set the HTTP method to something generally harmless
        HttpServletRequestWrapper req = new HttpServletRequestWrapper(request) {
            @Override
            public String getMethod() {
                return "OPTIONS";
            }
        };

        // Response swallows all output
        HttpServletResponseWrapper rsp = new HttpServletResponseWrapper(response) {
            @Override
            public ServletOutputStream getOutputStream() throws IOException {
                return new ServletOutputStream() {
                    @Override
                    public void write(int b) throws IOException {
                        // No output
                    }
                };
            }

            @Override
            public PrintWriter getWriter() throws IOException {
                return new PrintWriter(getOutputStream());
            }
        };

        // Try forward first
        log.info("Found StripesFilter declared and mapped in web.xml but not yet initialized.");
        Iterator<String> iterator = uris.iterator();
        while (getStripesFilter() == null && iterator.hasNext()) {
            String uri = iterator.next();
            log.info("Try to force initialization of StripesFilter with forward to ", uri);
            try {
                initializing = true;
                RequestDispatcher dispatcher = servletContext.getRequestDispatcher(uri);
                dispatcher.forward(req, rsp);
            }
            catch (Exception e) {
                log.debug(e, "Ignored exception during forward");
            }
            finally {
                initializing = false;
                response.reset();
            }
        }

        // If forward failed, try include
        iterator = uris.iterator();
        while (getStripesFilter() == null && iterator.hasNext()) {
            String uri = iterator.next();
            log.info("Try to force initialization of StripesFilter with include of ", uri);
            try {
                initializing = true;
                RequestDispatcher dispatcher = servletContext.getRequestDispatcher(uri);
                dispatcher.forward(req, rsp);
            }
            catch (Exception e) {
                log.debug(e, "Ignored exception during forward");
            }
            finally {
                initializing = false;
                response.reset();
            }
        }

        // If both forward and include failed, then do something truly abominable ...
        iterator = uris.iterator();
        while (getStripesFilter() == null && iterator.hasNext()) {
            try {
                String uri = iterator.next();
                log.info("Try to force initialization of StripesFilter with request to ", uri);
                requestRemotely(request, uri);
            }
            catch (Exception e) {
                log.debug(e, "Ignored exception during request");
            }
        }
    }

    /**
     * Issue a new request to a path relative to the request's context. The connection is made to
     * the address and port returned by {@link HttpServletRequest#getLocalAddr()} and
     * {@link HttpServletRequest#getLocalPort()}, respectively.
     * 
     * @param request The current request
     * @param relativePath The context-relative path to request
     */
    @SuppressWarnings("unchecked")
    public void requestRemotely(HttpServletRequest request, String relativePath) {
        HttpURLConnection cxn = null;
        try {
            // Create a new URL using the current request's protocol, port and context
            String protocol = new URL(request.getRequestURL().toString()).getProtocol();
            String file = request.getContextPath() + relativePath;
            URL url = new URL(protocol, request.getLocalAddr(), request.getLocalPort(), file);
            cxn = (HttpURLConnection) url.openConnection();

            // Set the HTTP method to something generally harmless
            cxn.setRequestMethod("OPTIONS");

            // Copy all the request headers to the new request
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String hdr = headerNames.nextElement();
                cxn.setRequestProperty(hdr, request.getHeader(hdr));
            }

            // Set a flag to let DMF know not to process the request
            cxn.setRequestProperty(REQ_HEADER_INIT_FLAG, "true");

            // Log the HTTP status
            log.debug(cxn.getResponseCode(), " ", cxn.getResponseMessage(), " (", cxn
                    .getContentLength(), " bytes) from ", url);
        }
        catch (Exception e) {
            log.debug(e, "Request failed trying to force initialization of StripesFilter");
        }
        finally {
            try {
                cxn.disconnect();
            }
            catch (Exception e) {
                // Ignore
            }
        }
    }
}
