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

import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
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

/**
 * <p>Mock implementation of an HttpServletRequest object.  Allows for setting most values that
 * are likely to be of interest (and can always be subclassed to affect others). Of key interest
 * and perhaps not completely obvious, the way to get request parameters into an instance of
 * MockHttpServletRequest is to fetch the parameter map using getParameterMap() and use the
 * put() and putAll() methods on it.  Values must be String arrays.  Examples follow:</p>
 *
 * <pre>
 * MockHttpServletRequest req = new MockHttpServletRequest("/foo", "/bar.action");
 * req.getParameterMap().put("param1", new String[] {"value"});
 * req.getParameterMap().put("param2", new String[] {"value1", "value2"});
 * </pre>
 *
 * <p>It should also be noted that unless you generate an instance of MockHttpSession (or
 * another implementation of HttpSession) and set it on the request, then your request will
 * <i>never</i> have a session associated with it.</p>
 *
 * @author Tim Fennell
 * @since Stripes 1.1.1
 */
@SuppressWarnings("deprecation")
public class MockHttpServletRequest implements HttpServletRequest {
    private String authType;
    private Cookie[] cookies;
    private Map<String,Object> headers = new HashMap<String,Object>();
    private Map<String,Object> attributes = new HashMap<String,Object>();
    private Map<String,String[]> parameters = new HashMap<String,String[]>();
    private String method = "POST";
    private HttpSession session;
    private String characterEncoding = "UTF-8";
    private List<Locale> locales = new ArrayList<Locale>();
    private Principal userPrincipal;
    private Set<String> roles = new HashSet<String>();
    private String forwardUrl;
    private List<String> includedUrls = new ArrayList<String>();

    // All the bits of the URL
    private String protocol = "https";
    private String serverName = "localhost";
    private int    serverPort = 8080;
    private String contextPath = "";
    private String servletPath = "";
    private String pathInfo    = "";
    private String queryString = "";

    /**
     * Minimal constructor that makes sense. Requires a context path (should be the same as
     * the name of the servlet context, prepended with a '/') and a servlet path. E.g.
     * new MockHttpServletRequest("/myapp", "/actionType/foo.action").
     *
     * @param contextPath
     * @param servletPath
     */
    public MockHttpServletRequest(String contextPath, String servletPath) {
        this.contextPath = contextPath;
        this.servletPath = servletPath;
    }

    /** Sets the auth type that will be reported by this request. */
    public void setAuthType(String authType) { this.authType = authType; }

    /** Gets the auth type being used by this request. */
    public String getAuthType() { return this.authType; }

    /** Sets the array of cookies that will be available from the request. */
    public void setCookies(Cookie[] cookies) { this.cookies = cookies; }

    /** Returns any cookies that are set on the request. */
    public Cookie[] getCookies() { return this.cookies; }

    /**
     * Allows headers to be set on the request. These will be returned by the various getXxHeader()
     * methods. If the header is a date header it should be set with a Long. If the header is an
     * Int header it should be set with an Integer.
     */
    public void addHeader(String name, Object value) {
        this.headers.put(name.toLowerCase(), value);
    }

    /** Gets the named header as a long. Must have been set as a long with addHeader(). */
    public long getDateHeader(String name) { return (Long) this.headers.get(name); }

    /** Returns any header as a String if it exists. */
    public String getHeader(String name) {
        if (name != null)
            name = name.toLowerCase();
        Object header = this.headers.get(name);
        if (header != null) {
            return header.toString();
        }
        else {
            return null;
        }
    }

    /** Returns an enumeration with single value of the named header, or an empty enum if no value. */
    public Enumeration getHeaders(String name) {
        String header = getHeader(name);
        Collection<String> values = new ArrayList<String>();
        if (header != null) {
            values.add(header);
        }
        return Collections.enumeration(values);
    }

    /** Returns an enumeration containing all the names of headers supplied. */
    public Enumeration getHeaderNames() { return Collections.enumeration(headers.keySet()); }

    /** Gets the named header as an int. Must have been set as an Integer with addHeader(). */
    public int getIntHeader(String name) {
        return (Integer) this.headers.get(name);
    }

    /** Sets the method used by the request. Defaults to POST. */
    public void setMethod(String method) { this.method = method; }

    /** Gets the method used by the request. Defaults to POST. */
    public String getMethod() { return this.method; }

    /** Sets the path info. Defaults to the empty string. */
    public void setPathInfo(String pathInfo) { this.pathInfo = pathInfo; }

    /** Returns the path info. Defaults to the empty string. */
    public String getPathInfo() { return this.pathInfo; }

    /** Always returns the same as getPathInfo(). */
    public String getPathTranslated() { return getPathInfo(); }

    /** Sets the context path. Defaults to the empty string. */
    public void setContextPath(String contextPath) { this.contextPath = contextPath; }

    /** Returns the context path. Defaults to the empty string. */
    public String getContextPath() { return this.contextPath; }

    /** Sets the query string set on the request; this value is not parsed for anything. */
    public void setQueryString(String queryString) { this.queryString = queryString; }

    /** Returns the query string set on the request. */
    public String getQueryString() { return this.queryString; }

    /** Returns the name from the user principal if one exists, otherwise null. */
    public String getRemoteUser() {
        Principal p = getUserPrincipal();
        return p == null ? null : p.getName();
    }

    /** Sets the set of roles that the user is deemed to be in for the request. */
    public void setRoles(Set<String> roles) { this.roles = roles; }

    /** Returns true if the set of roles contains the role specified, false otherwise. */
    public boolean isUserInRole(String role) {
        return this.roles.contains(role);
    }

    /** Sets the Principal for the current request. */
    public void setUserPrincipal(Principal userPrincipal) { this.userPrincipal = userPrincipal; }

    /** Returns the Principal if one is set on the request. */
    public Principal getUserPrincipal() { return this.userPrincipal; }

    /** Returns the ID of the session if one is attached to this request. Otherwise null. */
    public String getRequestedSessionId() {
        if (this.session == null) {
            return null;
        }
        return this.session.getId();
    }

    /** Returns the request URI as defined by the servlet spec. */
    public String getRequestURI() { return this.contextPath + this.pathInfo; }

    /** Returns (an attempt at) a reconstructed URL based on it's constituent parts. */
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

    /** Gets the part of the path which matched the servlet. */
    public String getServletPath() { return this.servletPath; }

    /** Gets the session object attached to this request. */
    public HttpSession getSession(boolean b) { return this.session; }

    /** Gets the session object attached to this request. */
    public HttpSession getSession() { return this.session; }

    /** Allows a session to be associated with the request. */
    public void setSession(HttpSession session) { this.session = session; }

    /** Always returns true. */
    public boolean isRequestedSessionIdValid() { return true; }

    /** Always returns true. */
    public boolean isRequestedSessionIdFromCookie() { return true; }

    /** Always returns false. */
    public boolean isRequestedSessionIdFromURL() { return false; }

    /** Always returns false. */
    public boolean isRequestedSessionIdFromUrl() { return false; }

    /** Gets the named request attribute from an internal Map. */
    public Object getAttribute(String key) { return this.attributes.get(key); }

    /** Gets an enumeration of all request attribute names. */
    public Enumeration getAttributeNames() {
        return Collections.enumeration(this.attributes.keySet());
    }

    /** Gets the character encoding, defaults to UTF-8. */
    public String getCharacterEncoding() { return this.characterEncoding; }

    /** Sets the character encoding that will be returned by getCharacterEncoding(). */
    public void setCharacterEncoding(String encoding) { this.characterEncoding = encoding; }

    /** Always returns -1 (unknown). */
    public int getContentLength() { return -1; }

    /** Always returns null. */
    public String getContentType() { return null; }

    /** Always returns null. */
    public ServletInputStream getInputStream() throws IOException { return null; }

    /** Gets the first value of the named parameter or null if a value does not exist. */
    public String getParameter(String name) {
        String[] values = getParameterValues(name);
        if (values != null && values.length > 0) {
            return values[0];
        }

        return null;
    }

    /** Gets an enumeration containing all the parameter names present. */
    public Enumeration getParameterNames() {
        return Collections.enumeration(this.parameters.keySet());
    }

    /** Returns an array of all values for a parameter, or null if the parameter does not exist. */
    public String[] getParameterValues(String name) {
        return this.parameters.get(name);
    }

    /**
     * Provides access to the parameter map. Note that this returns a reference to the live,
     * modifiable parameter map. As a result it can be used to insert parameters when constructing
     * the request.
     */
    public Map<String,String[]> getParameterMap() {
        return this.parameters;
    }

    /** Sets the protocol for the request. Defaults to "https". */
    public void setProtocol(String protocol) { this.protocol = protocol; }

    /** Gets the protocol for the request. Defaults to "https". */
    public String getProtocol() { return this.protocol; }

    /** Always returns the same as getProtocol. */
    public String getScheme() { return getProtocol(); }

    /** Sets the server name. Defaults to "localhost". */
    public void setServerName(String serverName) { this.serverName = serverName; }

    /** Gets the server name. Defaults to "localhost". */
    public String getServerName() { return this.serverName; }

    /** Sets the server port. Defaults to 8080. */
    public void setServerPort(int serverPort) { this.serverPort = serverPort; }

    /** Returns the server port. Defaults to 8080. */
    public int getServerPort() { return this.serverPort; }

    /** Always returns null. */
    public BufferedReader getReader() throws IOException { return null; }

    /** Aways returns "127.0.0.1". */
    public String getRemoteAddr() { return "127.0.0.1"; }

    /** Always returns "localhost". */
    public String getRemoteHost() { return "localhost"; }

    /** Sets the supplied value for the named request attribute. */
    public void setAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }

    /** Removes any value for the named request attribute. */
    public void removeAttribute(String name) { this.attributes.remove(name); }

    /** Adds a Locale to the set of requested locales. */
    public void addLocale(Locale locale) { this.locales.add(locale); }

    /** Returns the preferred locale. Defaults to the system locale. */
    public Locale getLocale() { return getLocales().nextElement(); }

    /** Returns an enumeration of requested locales. Defaults to the system locale. */
    public Enumeration<Locale> getLocales() {
        if (this.locales.size() == 0) {
            this.locales.add( Locale.getDefault() );
        }

        return Collections.enumeration(this.locales);
    }

    /** Returns true if the protocol is set to https (default), false otherwise. */
    public boolean isSecure() {
        return this.protocol.equalsIgnoreCase("https");
    }

    /**
     * Returns an instance of MockRequestDispatcher that just records what URLs are forwarded
     * to or included. The results can be examined later by calling getForwardUrl() and
     * getIncludedUrls().
     */
    public MockRequestDispatcher getRequestDispatcher(String url) {
        return new MockRequestDispatcher(url);
    }

    /** Always returns the path passed in without any alteration. */
    public String getRealPath(String path) { return path; }

    /** Always returns 1088 (and yes, that was picked arbitrarily). */
    public int getRemotePort() { return 1088; }

    /** Always returns the same value as getServerName(). */
    public String getLocalName() { return getServerName(); }

    /** Always returns 127.0.0.1). */
    public String getLocalAddr() { return "127.0.0.1"; }

    /** Always returns the same value as getServerPort(). */
    public int getLocalPort() { return getServerPort(); }

    /** Used by the request dispatcher to set the forward URL when a forward is invoked. */
    void setForwardUrl(String url) { this.forwardUrl = url; }

    /** Gets the URL that was forwarded to, if a forward was processed. Null otherwise. */
    public String getForwardUrl() { return this.forwardUrl; }

    /** Used by the request dispatcher to record that a URL was included. */
    void addIncludedUrl(String url) { this.includedUrls.add(url); }

    /** Gets the list (potentially empty) or URLs that were included during the request. */
    public List<String> getIncludedUrls() { return this.includedUrls; }
}
