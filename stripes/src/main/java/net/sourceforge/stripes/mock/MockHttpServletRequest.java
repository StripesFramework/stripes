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

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.Principal;
import java.util.*;

/**
 * <p>
 * Mock implementation of an HttpServletRequest object. Allows for setting most values that are likely to be of interest
 * (and can always be subclassed to affect others). Of key interest and perhaps not completely obvious, the way to get
 * request parameters into an instance of MockHttpServletRequest is to fetch the parameter map using getParameterMap()
 * and use the put() and putAll() methods on it. Values must be String arrays. Examples follow:
 * </p>
 *
 * <pre>
 * MockHttpServletRequest req = new MockHttpServletRequest(&quot;/foo&quot;, &quot;/bar.action&quot;);
 * req.getParameterMap().put(&quot;param1&quot;, new String[] { &quot;value&quot; });
 * req.getParameterMap().put(&quot;param2&quot;, new String[] { &quot;value1&quot;, &quot;value2&quot; });
 * </pre>
 *
 * <p>
 * It should also be noted that unless you generate an instance of MockHttpSession (or another implementation of
 * HttpSession) and set it on the request, then your request will <i>never</i> have a session associated with it.
 * </p>
 *
 * @author Tim Fennell
 * @since Stripes 1.1.1
 */
public class MockHttpServletRequest implements HttpServletRequest {

    /**
     * The log.
     */
    private final Logger log = LogManager.getLogger(MockHttpServletRequest.class);

    /**
     * The auth type.
     */
    private String authType;

    /**
     * The cookies.
     */
    private Cookie[] cookies;

    /**
     * The headers.
     */
    private final Map<String, Object> headers = new HashMap<>();

    /**
     * The attributes.
     */
    private final Map<String, Object> attributes = new HashMap<>();

    /**
     * The parameters.
     */
    private final Map<String, String[]> parameters = new HashMap<>();

    /**
     * The method.
     */
    private String method = "POST";

    /**
     * The session.
     */
    private HttpSession session;

    /**
     * The chacarcter encoding.
     */
    private String chacarcterEncoding = "UTF-8";

    /**
     * The locales.
     */
    private final List<Locale> locales = new ArrayList<>();

    /**
     * The user principal.
     */
    private Principal userPrincipal;

    /**
     * The roles.
     */
    private Set<String> roles = new HashSet<>();

    /**
     * The forward url.
     */
    private String forwardUrl;

    /**
     * The included urls.
     */
    private final List<String> includedUrls = new ArrayList<>();

    /**
     * The request body.
     */
    private byte[] requestBody = new byte[0];

    /**
     * The protocol.
     */
    // All the bits of the URL
    private String protocol = "https";

    /**
     * The server name.
     */
    private String serverName = "localhost";

    /**
     * The server port.
     */
    private int serverPort = 8080;

    /**
     * The context path.
     */
    private String contextPath = "";

    /**
     * The servlet path.
     */
    private String servletPath = "";

    /**
     * The path info.
     */
    private String pathInfo = "";

    /**
     * The query string.
     */
    private String queryString = "";

    /**
     * The async context.
     */
    private MockAsyncContext asyncContext = null;

    /**
     * The servlet context.
     */
    private ServletContext servletContext = null;

    /**
     * Minimal constructor that makes sense. Requires a context path (should be the same as the name of the servlet
     * context, prepended with a '/') and a servlet path. E.g. new MockHttpServletRequest("/myapp",
     * "/actionType/foo.action").
     *
     * @param contextPath the context path
     * @param servletPath the servlet path
     */
    public MockHttpServletRequest(final String contextPath, final String servletPath) {
        this.contextPath = contextPath;
        this.servletPath = servletPath;
    }

    /**
     * Sets the auth type that will be reported by this request.
     */
    public void setAuthType(final String authType) {
        this.authType = authType;
    }

    /**
     * Gets the auth type being used by this request.
     */
    @Override
    public String getAuthType() {
        return this.authType;
    }

    /**
     * Sets the array of cookies that will be available from the request.
     */
    public void setCookies(final Cookie[] cookies) {
        this.cookies = cookies;
    }

    /**
     * Returns any cookies that are set on the request.
     */
    @Override
    public Cookie[] getCookies() {
        return this.cookies;
    }

    /**
     * Allows headers to be set on the request. These will be returned by the various getXxHeader() methods. If the header
     * is a date header it should be set with a Long. If the header is an Int header it should be set with an Integer.
     *
     * @param name  the name
     * @param value the value
     */
    public void addHeader(final String name, final Object value) {
        this.headers.put(name.toLowerCase(), value);
    }

    /**
     * Gets the named header as a long. Must have been set as a long with addHeader().
     */
    @Override
    public long getDateHeader(final String name) {
        return (Long) this.headers.get(name);
    }

    /**
     * Returns any header as a String if it exists.
     */
    @Override
    public String getHeader(String name) {
        if (name != null) {
            name = name.toLowerCase();
        }
        final Object header = this.headers.get(name);
        if (header != null) {
            return header.toString();
        } else {
            return null;
        }
    }

    /**
     * Returns an enumeration with single value of the named header, or an empty enum if no value.
     */
    @Override
    public Enumeration<String> getHeaders(final String name) {
        final String header = getHeader(name);
        final Collection<String> values = new ArrayList<>();
        if (header != null) {
            values.add(header);
        }
        return Collections.enumeration(values);
    }

    /**
     * Returns an enumeration containing all the names of headers supplied.
     */
    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(this.headers.keySet());
    }

    /**
     * Gets the named header as an int. Must have been set as an Integer with addHeader().
     */
    @Override
    public int getIntHeader(final String name) {
        String headerValue = getHeader(name);
        if (headerValue == null) {
            return -1;
        }
        return Integer.parseInt(headerValue);
    }

    /**
     * Sets the method used by the request. Defaults to POST.
     */
    public void setMethod(final String method) {
        this.method = method;
    }

    /**
     * Gets the method used by the request. Defaults to POST.
     */
    @Override
    public String getMethod() {
        return this.method;
    }

    /**
     * Sets the path info. Defaults to the empty string.
     */
    public void setPathInfo(final String pathInfo) {
        this.pathInfo = pathInfo;
    }

    /**
     * Returns the path info. Defaults to the empty string.
     */
    @Override
    public String getPathInfo() {
        return this.pathInfo;
    }

    /**
     * Always returns the same as getPathInfo().
     */
    @Override
    public String getPathTranslated() {
        return getPathInfo();
    }

    /**
     * Sets the context path. Defaults to the empty string.
     */
    public void setContextPath(final String contextPath) {
        this.contextPath = contextPath;
    }

    /**
     * Returns the context path. Defaults to the empty string.
     */
    @Override
    public String getContextPath() {
        return this.contextPath;
    }

    /**
     * Sets the query string set on the request; this value is not parsed for anything.
     */
    public void setQueryString(final String queryString) {
        this.queryString = queryString;
    }

    /**
     * Returns the query string set on the request.
     */
    @Override
    public String getQueryString() {
        return this.queryString;
    }

    /**
     * Returns the name from the user principal if one exists, otherwise null.
     */
    @Override
    public String getRemoteUser() {
        final Principal p = getUserPrincipal();
        return p == null ? null : p.getName();
    }

    /**
     * Sets the set of roles that the user is deemed to be in for the request.
     */
    public void setRoles(final Set<String> roles) {
        this.roles = roles;
    }

    /**
     * Returns true if the set of roles contains the role specified, false otherwise.
     */
    @Override
    public boolean isUserInRole(final String role) {
        return this.roles.contains(role);
    }

    /**
     * Sets the Principal for the current request.
     */
    public void setUserPrincipal(final Principal userPrincipal) {
        this.userPrincipal = userPrincipal;
    }

    /**
     * Returns the Principal if one is set on the request.
     */
    @Override
    public Principal getUserPrincipal() {
        return this.userPrincipal;
    }

    /**
     * Returns the ID of the session if one is attached to this request. Otherwise null.
     */
    @Override
    public String getRequestedSessionId() {
        if (this.session == null) {
            return null;
        }
        return this.session.getId();
    }

    /**
     * Returns the request URI as defined by the servlet spec.
     * <p>
     * FIX RRK missing servlet path.
     **/

    @Override
    public String getRequestURI() {
        return concatURI(concatURI(this.contextPath, this.servletPath), this.pathInfo);
    }

    /**
     * Returns (an attempt at) a reconstructed URL based on it's constituent parts.
     */
    @Override
    public StringBuffer getRequestURL() {
        return new StringBuffer().append(this.protocol).append("://").append(this.serverName).append(":")
                .append(this.serverPort)
                .append(this.contextPath).append(this.servletPath).append(this.pathInfo);
    }

    /**
     * Gets the part of the path which matched the servlet.
     */
    @Override
    public String getServletPath() {
        return this.servletPath;
    }

    /**
     * Gets the session object attached to this request.
     */
    @Override
    public HttpSession getSession(final boolean b) {
        return this.session;
    }

    /**
     * Gets the session object attached to this request.
     */
    @Override
    public HttpSession getSession() {
        return this.session;
    }

    /**
     * Allows a session to be associated with the request.
     */
    public void setSession(final HttpSession session) {
        this.session = session;
    }

    /**
     * Always returns true.
     */
    @Override
    public boolean isRequestedSessionIdValid() {
        return true;
    }

    /**
     * Always returns true.
     */
    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return true;
    }

    /**
     * Always returns false.
     */
    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }


    /**
     * Gets the named request attribute from an internal Map.
     *
     * @param key
     * @return
     */
    @Override
    public Object getAttribute(String key) {
        Object ret = attributes.get(key);
        if (log.isDebugEnabled() == true) {
            log.debug("MockupHttpServletRequest.getAttribute(" + key + ") => " + Objects.toString(ret, ""));
        }
        return ret;
    }


    /**
     * Gets an enumeration of all request attribute names.
     */
    @Override
    public Enumeration getAttributeNames() {
        return Collections.enumeration(this.attributes.keySet());
    }

    /**
     * Gets the character encoding, defaults to UTF-8.
     */
    @Override
    public String getCharacterEncoding() {
        return this.chacarcterEncoding;
    }

    /**
     * Sets the character encoding that will be returned by getCharacterEncoding().
     */
    @Override
    public void setCharacterEncoding(final String encoding) {
        this.chacarcterEncoding = encoding;
    }

    @Override
    public int getContentLength() {
        if (requestBody != null) {
            return requestBody.length;
        }
        return -1;
    }

    @Override
    public String getContentType() {
        return getHeader("content-type");
    }

    /**
     * Always returns null.
     *
     * @return
     * @throws java.io.IOException
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (requestBody == null) {
            return null;
        }
        final ByteArrayInputStream bis = new ByteArrayInputStream(requestBody);
        return new ServletInputStream() {

            @Override
            public int read() throws IOException {
                return bis.read();
            }

            @Override
            public void close() throws IOException {
                bis.close();
            }

            @Override
            public boolean isFinished() {
                return bis.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }

        };

    }

    /**
     * Gets the first value of the named parameter or null if a value does not exist.
     */
    @Override
    public String getParameter(final String name) {
        final String[] values = getParameterValues(name);
        if (values != null && values.length > 0) {
            return values[0];
        }

        return null;
    }

    /**
     * Gets an enumeration containing all the parameter names present.
     */
    @Override
    public Enumeration getParameterNames() {
        return Collections.enumeration(this.parameters.keySet());
    }

    /**
     * Returns an array of all values for a parameter, or null if the parameter does not exist.
     */
    @Override
    public String[] getParameterValues(final String name) {
        return this.parameters.get(name);
    }

    /**
     * Provides access to the parameter map. Note that this returns a reference to the live, modifiable parameter map. As
     * a result it can be used to insert parameters when constructing the request.
     */
    @Override
    public Map<String, String[]> getParameterMap() {
        return this.parameters;
    }

    /**
     * Sets the protocol for the request. Defaults to "https".
     */
    public void setProtocol(final String protocol) {
        this.protocol = protocol;
    }

    /**
     * Gets the protocol for the request. Defaults to "https".
     */
    @Override
    public String getProtocol() {
        return this.protocol;
    }

    /**
     * Always returns the same as getProtocol.
     */
    @Override
    public String getScheme() {
        return getProtocol();
    }

    /**
     * Sets the server name. Defaults to "localhost".
     */
    public void setServerName(final String serverName) {
        this.serverName = serverName;
    }

    /**
     * Gets the server name. Defaults to "localhost".
     */
    @Override
    public String getServerName() {
        return this.serverName;
    }

    /**
     * Sets the server port. Defaults to 8080.
     */
    public void setServerPort(final int serverPort) {
        this.serverPort = serverPort;
    }

    /**
     * Returns the server port. Defaults to 8080.
     */
    @Override
    public int getServerPort() {
        return this.serverPort;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), "UTF-8"));
    }

    /**
     * Aways returns "127.0.0.1".
     */
    @Override
    public String getRemoteAddr() {
        return "127.0.0.1";
    }

    /**
     * Always returns "localhost".
     */
    @Override
    public String getRemoteHost() {
        return "localhost";
    }

    /**
     * Sets the supplied value for the named request attribute.
     *
     * @param name
     * @param value
     */
    @Override
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
        if (log.isDebugEnabled() == true) {
            log.debug("MockupHttpServletRequest.setAttribute(" + name + ", " + Objects.toString(value, "") + ")");
        }
    }

    /**
     * Gets the request body
     */
    public byte[] getRequestBody() {
        return requestBody;
    }

    /**
     * Sets the body of the request
     *
     * @param requestBody
     */
    public void setRequestBody(String requestBody) {
        if (requestBody != null) {
            this.requestBody = requestBody.getBytes();
        }
    }

    /**
     * Sets the body of the request
     *
     * @param requestBody
     */
    public void setRequestBody(byte[] requestBody) {
        this.requestBody = requestBody;
    }

    /**
     * Removes any value for the named request attribute.
     *
     * @param name
     */
    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
        if (log.isDebugEnabled() == true) {
            log.debug("MockupHttpServletRequest.removeAttribute(" + name + ")");
        }
    }

    /**
     * Adds a Locale to the set of requested locales.
     *
     * @param locale the locale
     */
    public void addLocale(final Locale locale) {
        this.locales.add(locale);
    }

    /**
     * Returns the preferred locale. Defaults to the system locale.
     */
    @Override
    public Locale getLocale() {
        return getLocales().nextElement();
    }

    /**
     * Returns an enumeration of requested locales. Defaults to the system locale.
     */
    @Override
    public Enumeration<Locale> getLocales() {
        if (this.locales.size() == 0) {
            this.locales.add(Locale.getDefault());
        }

        return Collections.enumeration(this.locales);
    }

    /**
     * Returns true if the protocol is set to https (default), false otherwise.
     */
    @Override
    public boolean isSecure() {
        return this.protocol.equalsIgnoreCase("https");
    }

    /**
     * Returns an instance of MockRequestDispatcher that just records what URLs are forwarded to or included. The results
     * can be examined later by calling getForwardUrl() and getIncludedUrls().
     */
    @Override
    public MockRequestDispatcher getRequestDispatcher(final String url) {
        return new MockRequestDispatcher(url);
    }


    /**
     * Always returns 1088 (and yes, that was picked arbitrarily).
     */
    public int getRemotePort() {
        return 1088;
    }

    /**
     * Always returns the same value as getServerName().
     */
    public String getLocalName() {
        return getServerName();
    }

    /**
     * Always returns 127.0.0.1).
     */
    public String getLocalAddr() {
        return "127.0.0.1";
    }

    /**
     * Always returns the same value as getServerPort().
     */
    public int getLocalPort() {
        return getServerPort();
    }

    /**
     * Used by the request dispatcher to set the forward URL when a forward is invoked.
     */
    void setForwardUrl(final String url) {
        this.forwardUrl = url;
    }

    /**
     * Gets the URL that was forwarded to, if a forward was processed. Null otherwise.
     */
    public String getForwardUrl() {
        return this.forwardUrl;
    }

    /**
     * Used by the request dispatcher to record that a URL was included.
     *
     * @param url the url
     */
    void addIncludedUrl(final String url) {
        this.includedUrls.add(url);
    }

    /**
     * Gets the list (potentially empty) or URLs that were included during the request.
     */
    public List<String> getIncludedUrls() {
        return this.includedUrls;
    }

    @Override
    public String changeSessionId() {
        return null;
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        return false;
    }

    @Override
    public void login(String username, String password) throws ServletException {

    }

    @Override
    public void logout() throws ServletException {

    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return null;
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        return null;
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        return null;
    }

    @Override
    public long getContentLengthLong() {
        return 0;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /**
     * @return
     * @throws IllegalStateException
     */
    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        throw new UnsupportedOperationException("use request,response variant");
    }

    /**
     * @param servletRequest
     * @param servletResponse
     * @return
     * @throws IllegalStateException
     */
    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        if (asyncContext == null) {
            asyncContext = new MockAsyncContext(servletRequest, servletResponse);
        } else if (asyncContext.isCompleted()) {
            throw new IllegalStateException("Async Context already completed");
        }
        return asyncContext;
    }

    /**
     * @return
     */
    public boolean isAsyncStarted() {
        return asyncContext != null;
    }

    @Override
    public boolean isAsyncSupported() {
        return true;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return asyncContext;
    }

    public MockAsyncContext getMockAsyncContext() {
        return asyncContext;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return null;
    }

    public void setServletPath(String servletPath) {
        this.servletPath = servletPath;
    }

    private String concatURI(String f, String s) {
        if (s == null || s.length() == 0) {
            return f;
        }
        boolean fe = f.endsWith("/");
        boolean ss = s.startsWith("/");
        if (fe == true && ss == true) {
            return f + s.substring(1);
        }
        if (fe == false && ss == false) {
            return f + "/" + s;
        }
        return f + s;
    }


    @Override
    public String getRequestId() {
        return null;
    }

    @Override
    public String getProtocolRequestId() {
        return null;
    }

    @Override
    public ServletConnection getServletConnection() {
        return null;
    }
}
