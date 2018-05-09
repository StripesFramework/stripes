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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 * <p>
 * Mock implementation of an HttpServletRequest object. Allows for setting most
 * values that are likely to be of interest (and can always be subclassed to
 * affect others). Of key interest and perhaps not completely obvious, the way
 * to get request parameters into an instance of MockHttpServletRequest is to
 * fetch the parameter map using getParameterMap() and use the put() and
 * putAll() methods on it. Values must be String arrays. Examples follow:</p>
 *
 * <pre>
 * MockHttpServletRequest req = new MockHttpServletRequest("/foo", "/bar.action");
 * req.getParameterMap().put("param1", new String[] {"value"});
 * req.getParameterMap().put("param2", new String[] {"value1", "value2"});
 * </pre>
 *
 * <p>
 * It should also be noted that unless you generate an instance of
 * MockHttpSession (or another implementation of HttpSession) and set it on the
 * request, then your request will
 * <i>never</i> have a session associated with it.</p>
 *
 * @author Tim Fennell
 * @since Stripes 1.1.1
 */
public class MockHttpServletRequest implements HttpServletRequest {

    private String authType;
    private Cookie[] cookies;
    private Map<String, Object> headers = new HashMap<String, Object>();
    private Map<String, Object> attributes = new HashMap<String, Object>();
    private Map<String, String[]> parameters = new HashMap<String, String[]>();
    private String method = "POST";
    private HttpSession session;
    private String characterEncoding = "UTF-8";
    private List<Locale> locales = new ArrayList<Locale>();
    private Principal userPrincipal;
    private Set<String> roles = new HashSet<String>();
    private String forwardUrl;
    private List<String> includedUrls = new ArrayList<String>();
    private byte[] requestBody = new byte[0];

    // All the bits of the URL
    private String protocol = "https";
    private String serverName = "localhost";
    private int serverPort = 8080;
    private String contextPath = "";
    private String servletPath = "";
    private String pathInfo = "";
    private String queryString = "";

    private MockAsyncContext asyncContext = null;

    /**
     * Minimal constructor that makes sense. Requires a context path (should be
     * the same as the name of the servlet context, prepended with a '/') and a
     * servlet path. E.g. new MockHttpServletRequest("/myapp",
     * "/actionType/foo.action").
     *
     * @param contextPath
     * @param servletPath
     */
    public MockHttpServletRequest(
            String contextPath,
            String servletPath) {
        this.contextPath = contextPath;
        this.servletPath = servletPath;
    }

    /**
     * Sets the auth type that will be reported by this request.
     * @param authType
     */
    public void setAuthType(String authType) {
        this.authType = authType;
    }

    /**
     * Gets the auth type being used by this request.
     * @return 
     */
    public String getAuthType() {
        return this.authType;
    }

    /**
     * Sets the array of cookies that will be available from the request.
     * @param cookies
     */
    public void setCookies(Cookie[] cookies) {
        this.cookies = cookies;
    }

    /**
     * Returns any cookies that are set on the request.
     * @return 
     */
    public Cookie[] getCookies() {
        return this.cookies;
    }

    /**
     * Allows headers to be set on the request. These will be returned by the
     * various getXxHeader() methods. If the header is a date header it should
     * be set with a Long. If the header is an Int header it should be set with
     * an Integer.
     * @param name
     * @param value
     */
    public void addHeader(String name, Object value) {
        this.headers.put(name.toLowerCase(), value);
    }

    /**
     * Gets the named header as a long. Must have been set as a long with
     * addHeader().
     * @param name
     * @return 
     */
    public long getDateHeader(String name) {
        return (Long) this.headers.get(name);
    }

    /**
     * Returns any header as a String if it exists.
     * @param name
     * @return 
     */
    public String getHeader(String name) {
        final Object header = this.headers.get(name == null ? null : name.toLowerCase());
        return header == null ? null : header.toString();
    }

    /**
     * Returns an enumeration with single value of the named header, or an empty
     * enum if no value.
     * @param name
     * @return 
     */
    public Enumeration<String> getHeaders(String name) {
        String header = getHeader(name);
        Collection<String> values = new ArrayList<String>();
        if (header != null) {
            values.add(header);
        }
        return Collections.enumeration(values);
    }

    /**
     * Returns an enumeration containing all the names of headers supplied.
     * @return 
     */
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(headers.keySet());
    }

    /**
     * Gets the named header as an int. Must have been set as an Integer with
     * addHeader().
     * @param name
     * @return 
     */
    public int getIntHeader(String name) {
        String headerValue = getHeader(name);
        if (headerValue == null) {
            return -1;
        }
        return Integer.parseInt(headerValue);
    }

    /**
     * Sets the method used by the request. Defaults to POST.
     * @param method
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * Gets the method used by the request. Defaults to POST.
     * @return 
     */
    public String getMethod() {
        return this.method;
    }

    /**
     * Sets the path info. Defaults to the empty string.
     * @param pathInfo
     */
    public void setPathInfo(String pathInfo) {
        this.pathInfo = pathInfo;
    }

    /**
     * Returns the path info. Defaults to the empty string.
     * @return 
     */
    public String getPathInfo() {
        return this.pathInfo;
    }

    /**
     * Always returns the same as getPathInfo().
     * @return 
     */
    public String getPathTranslated() {
        return getPathInfo();
    }

    /**
     * Sets the context path. Defaults to the empty string.
     * @param contextPath
     */
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    /**
     * Returns the context path. Defaults to the empty string.
     * @return 
     */
    public String getContextPath() {
        return this.contextPath;
    }

    /**
     * Sets the query string set on the request; this value is not parsed for
     * anything.
     * @param queryString
     */
    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    /**
     * Returns the query string set on the request.
     * @return 
     */
    public String getQueryString() {
        return this.queryString;
    }

    /**
     * Returns the name from the user principal if one exists, otherwise null.
     * @return 
     */
    public String getRemoteUser() {
        Principal p = getUserPrincipal();
        return p == null ? null : p.getName();
    }

    /**
     * Sets the set of roles that the user is deemed to be in for the request.
     * @param roles
     */
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    /**
     * Returns true if the set of roles contains the role specified, false
     * otherwise.
     * @param role
     * @return 
     */
    public boolean isUserInRole(String role) {
        return this.roles.contains(role);
    }

    /**
     * Sets the Principal for the current request.
     * @param userPrincipal
     */
    public void setUserPrincipal(Principal userPrincipal) {
        this.userPrincipal = userPrincipal;
    }

    /**
     * Returns the Principal if one is set on the request.
     * @return 
     */
    public Principal getUserPrincipal() {
        return this.userPrincipal;
    }

    /**
     * Returns the ID of the session if one is attached to this request.
     * Otherwise null.
     * @return 
     */
    public String getRequestedSessionId() {
        if (this.session == null) {
            return null;
        }
        return this.session.getId();
    }

    /**
     * Returns the request URI as defined by the servlet spec.
     * @return 
     */
    public String getRequestURI() {
        return this.contextPath + this.servletPath + this.pathInfo;
    }

    /**
     * Returns (an attempt at) a reconstructed URL based on its constituent
     * parts.
     * @return 
     */
    public StringBuffer getRequestURL() {
        return new StringBuffer().append(this.protocol)
                .append("://")
                .append(this.serverName)
                .append(":")
                .append(this.serverPort)
                .append(this.contextPath)
                .append(this.servletPath)
                .append(this.pathInfo);
    }

    /**
     * Gets the part of the path which matched the servlet.
     * @return 
     */
    public String getServletPath() {
        return this.servletPath;
    }

    /**
     * Gets the session object attached to this request.
     * @param b
     * @return 
     */
    public HttpSession getSession(boolean b) {
        return this.session;
    }

    /**
     * Gets the session object attached to this request.
     * @return 
     */
    public HttpSession getSession() {
        return this.session;
    }

    /**
     * Allows a session to be associated with the request.
     * @param session
     */
    public void setSession(HttpSession session) {
        this.session = session;
    }

    /**
     * Always returns true.
     * @return 
     */
    public boolean isRequestedSessionIdValid() {
        return true;
    }

    /**
     * Always returns true.
     * @return 
     */
    public boolean isRequestedSessionIdFromCookie() {
        return true;
    }

    /**
     * Always returns false.
     * @return 
     */
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    /**
     * Always returns false.
     * @return 
     */
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    /**
     * Gets the named request attribute from an internal Map.
     * @param key
     * @return 
     */
    public Object getAttribute(String key) {
        return this.attributes.get(key);
    }

    /**
     * Gets an enumeration of all request attribute names.
     * @return 
     */
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(this.attributes.keySet());
    }

    /**
     * Gets the character encoding, defaults to UTF-8.
     * @return 
     */
    public String getCharacterEncoding() {
        return this.characterEncoding;
    }

    /**
     * Sets the character encoding that will be returned by
     * getCharacterEncoding().
     * @param encoding
     */
    public void setCharacterEncoding(String encoding) {
        this.characterEncoding = encoding;
    }

    /**
     * Always returns -1 (unknown).
     * @return 
     */
    public int getContentLength() {
        return requestBody.length;
    }

    /**
     * Always returns null.
     * @return 
     */
    public String getContentType() {
        return getHeader("content-type");
    }

    /**
     * Always returns null.
     * @return 
     * @throws java.io.IOException
     */
    public ServletInputStream getInputStream() throws IOException {
        return new ServletInputStream() {

            ByteArrayInputStream wrappedStream = new ByteArrayInputStream(requestBody);

            public final InputStream getWrappedInputStream() {
                return wrappedStream;
            }

            @Override
            public int read() throws IOException {
                return wrappedStream.read();
            }

            @Override
            public void close() throws IOException {
                wrappedStream.close();
            }

            @Override
            public boolean isFinished() {
                return wrappedStream.available() == 0;
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
     * Gets the first value of the named parameter or null if a value does not
     * exist.
     * @param name
     * @return 
     */
    public String getParameter(String name) {
        String[] values = getParameterValues(name);
        if (values != null && values.length > 0) {
            return values[0];
        }

        return null;
    }

    /**
     * Gets an enumeration containing all the parameter names present.
     * @return 
     */
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(this.parameters.keySet());
    }

    /**
     * Returns an array of all values for a parameter, or null if the parameter
     * does not exist.
     * @param name
     * @return 
     */
    public String[] getParameterValues(String name) {
        return this.parameters.get(name);
    }

    /**
     * Provides access to the parameter map. Note that this returns a reference
     * to the live, modifiable parameter map. As a result it can be used to
     * insert parameters when constructing the request.
     * @return 
     */
    public Map<String, String[]> getParameterMap() {
        return this.parameters;
    }

    /**
     * Sets the protocol for the request. Defaults to "https".
     * @param protocol
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * Gets the protocol for the request. Defaults to "https".
     * @return 
     */
    public String getProtocol() {
        return this.protocol;
    }

    /**
     * Always returns the same as getProtocol.
     * @return 
     */
    public String getScheme() {
        return getProtocol();
    }

    /**
     * Sets the server name. Defaults to "localhost".
     * @param serverName
     */
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    /**
     * Gets the server name. Defaults to "localhost".
     * @return 
     */
    public String getServerName() {
        return this.serverName;
    }

    /**
     * Sets the server port. Defaults to 8080.
     * @param serverPort
     */
    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    /**
     * Returns the server port. Defaults to 8080.
     * @return 
     */
    public int getServerPort() {
        return this.serverPort;
    }

    /**
     * Always returns null.
     * @return 
     * @throws java.io.IOException
     */
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    /**
     * Aways returns "127.0.0.1".
     * @return 
     */
    public String getRemoteAddr() {
        return "127.0.0.1";
    }

    /**
     * Always returns "localhost".
     * @return 
     */
    public String getRemoteHost() {
        return "localhost";
    }

    /**
     * Sets the supplied value for the named request attribute.
     * @param name
     * @param value
     */
    public void setAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }

    /**
     * Sets the body of the request
     * @param requestBody
     */
    public void setRequestBody(String requestBody) {
        if (requestBody != null) {
            this.requestBody = requestBody.getBytes();
        }
    }

    /**
     * Removes any value for the named request attribute.
     * @param name
     */
    public void removeAttribute(String name) {
        this.attributes.remove(name);
    }

    /**
     * Adds a Locale to the set of requested locales.
     * @param locale
     */
    public void addLocale(Locale locale) {
        this.locales.add(locale);
    }

    /**
     * Returns the preferred locale. Defaults to the system locale.
     * @return 
     */
    public Locale getLocale() {
        return getLocales().nextElement();
    }

    /**
     * Returns an enumeration of requested locales. Defaults to the system
     * locale.
     * @return 
     */
    public Enumeration<Locale> getLocales() {
        if (this.locales.size() == 0) {
            this.locales.add(Locale.getDefault());
        }

        return Collections.enumeration(this.locales);
    }

    /**
     * Returns true if the protocol is set to https (default), false otherwise.
     * @return 
     */
    public boolean isSecure() {
        return this.protocol.equalsIgnoreCase("https");
    }

    /**
     * Returns an instance of MockRequestDispatcher that just records what URLs
     * are forwarded to or included. The results can be examined later by
     * calling getForwardUrl() and getIncludedUrls().
     * @param url
     * @return 
     */
    public MockRequestDispatcher getRequestDispatcher(String url) {
        return new MockRequestDispatcher(url);
    }

    /**
     * Always returns the path passed in without any alteration.
     * @param path
     * @return 
     */
    public String getRealPath(String path) {
        return path;
    }

    /**
     * Always returns 1088 (and yes, that was picked arbitrarily).
     * @return 
     */
    public int getRemotePort() {
        return 1088;
    }

    /**
     * Always returns the same value as getServerName().
     * @return 
     */
    public String getLocalName() {
        return getServerName();
    }

    /**
     * Always returns 127.0.0.1).
     * @return 
     */
    public String getLocalAddr() {
        return "127.0.0.1";
    }

    /**
     * Always returns the same value as getServerPort().
     * @return 
     */
    public int getLocalPort() {
        return getServerPort();
    }

    /**
     * Used by the request dispatcher to set the forward URL when a forward is
     * invoked.
     */
    void setForwardUrl(String url) {
        this.forwardUrl = url;
    }

    /**
     * Gets the URL that was forwarded to, if a forward was processed. Null
     * otherwise.
     * @return 
     */
    public String getForwardUrl() {
        return this.forwardUrl;
    }

    /**
     * Used by the request dispatcher to record that a URL was included.
     */
    void addIncludedUrl(String url) {
        this.includedUrls.add(url);
    }

    /**
     * Gets the list (potentially empty) or URLs that were included during the
     * request.
     * @return 
     */
    public List<String> getIncludedUrls() {
        return this.includedUrls;
    }

    /**
     *
     * @return
     */
    public String changeSessionId() {
        return null;
    }

    /**
     *
     * @param response
     * @return
     * @throws IOException
     * @throws ServletException
     */
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        return false;
    }

    /**
     *
     * @param username
     * @param password
     * @throws ServletException
     */
    public void login(String username, String password) throws ServletException {

    }

    /**
     *
     * @throws ServletException
     */
    public void logout() throws ServletException {

    }

    /**
     *
     * @return
     * @throws IOException
     * @throws ServletException
     */
    public Collection<Part> getParts() throws IOException, ServletException {
        return null;
    }

    /**
     *
     * @param name
     * @return
     * @throws IOException
     * @throws ServletException
     */
    public Part getPart(String name) throws IOException, ServletException {
        return null;
    }

    /**
     *
     * @param <T>
     * @param handlerClass
     * @return
     * @throws IOException
     * @throws ServletException
     */
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        return null;
    }

    /**
     *
     * @return
     */
    public long getContentLengthLong() {
        return 0;
    }

    /**
     *
     * @return
     */
    public ServletContext getServletContext() {
        return null;
    }

    /**
     *
     * @return
     * @throws IllegalStateException
     */
    public AsyncContext startAsync() throws IllegalStateException {
        throw new UnsupportedOperationException("use request,response variant");
    }

    /**
     *
     * @param servletRequest
     * @param servletResponse
     * @return
     * @throws IllegalStateException
     */
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        if (asyncContext == null) {
            asyncContext = new MockAsyncContext(servletRequest, servletResponse);
        } else if (asyncContext.isCompleted()) {
            throw new IllegalStateException("Async Context already completed");
        }
        return asyncContext;
    }

    /**
     *
     * @return
     */
    public boolean isAsyncStarted() {
        return asyncContext != null;
    }

    /**
     *
     * @return
     */
    public boolean isAsyncSupported() {
        return true;
    }

    /**
     *
     * @return
     */
    public MockAsyncContext getAsyncContext() {
        return asyncContext;
    }

    /**
     *
     * @return
     */
    public DispatcherType getDispatcherType() {
        return null;
    }
}
