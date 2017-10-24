package net.sourceforge.stripes.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.security.Principal;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Captures the state of an {@link javax.servlet.http.HttpServletRequest} so
 * that the information contained therein can be carried over to the next
 * request for use by the flash scope. There are several methods in here that
 * cannot be faked and so must delegate to an active {@link
 * javax.servlet.http.HttpServletRequest} object, the {@link #delegate}. If one
 * of these methods is called and there is no delegate object set on the
 * instance, they will throw a {@link
 * net.sourceforge.stripes.exception.StripesRuntimeException}. Unless this class
 * is used outside its intended context (during a live request processed through
 * {@link StripesFilter}), you won't need to worry about that.
 *
 * @author Ben Gunter
 * @since Stripes 1.4.3
 */
public class FlashRequest implements HttpServletRequest, Serializable {

    private static final long serialVersionUID = 1L;

    private Cookie[] cookies;
    private HttpServletRequest delegate;
    private List<Locale> locales;
    private Locale locale;
    private Map<String, List<String>> headers = new HashMap<String, List<String>>();
    private Map<String, Long> dateHeaders = new HashMap<String, Long>();
    private Map<String, Object> attributes = new HashMap<String, Object>();
    private Map<String, String[]> parameters = new HashMap<String, String[]>();
    private String authType;
    private String characterEncoding;
    private String contentType;
    private String contextPath;
    private String localAddr;
    private String localName;
    private String method;
    private String pathInfo;
    private String pathTranslated;
    private String protocol;
    private String queryString;
    private String remoteAddr;
    private String remoteHost;
    private String remoteUser;
    private String requestURI;
    private String requestedSessionId;
    private String scheme;
    private String serverName;
    private String servletPath;
    private StringBuffer requestURL;
    private boolean requestedSessionIdFromCookie;
    private boolean requestedSessionIdFromURL;
    private boolean requestedSessionIdFromUrl;
    private boolean requestedSessionIdValid;
    private boolean secure;
    private int localPort;
    private int remotePort;
    private int serverPort;

    /**
     * Finds the StripesRequestWrapper for the supplied request and swaps out
     * the underlying request for an instance of FlashRequest.
     *
     * @param request the current HttpServletRequest
     * @return the StripesRequestWrapper for this request with the "live"
     * request replaced
     */
    public static StripesRequestWrapper replaceRequest(HttpServletRequest request) {
        StripesRequestWrapper wrapper = StripesRequestWrapper.findStripesWrapper(request);
        wrapper.setRequest(new FlashRequest((HttpServletRequest) wrapper.getRequest()));
        return wrapper;
    }

    /**
     * Creates a new FlashRequest by copying all appropriate attributes from the
     * prototype request supplied.
     *
     * @param prototype the HttpServletRequest to create a disconnected copy of
     */
    @SuppressWarnings({"unchecked", "deprecation"})
    public FlashRequest(HttpServletRequest prototype) {
        // copy properties
        authType = prototype.getAuthType();
        characterEncoding = prototype.getCharacterEncoding();
        contentType = prototype.getContentType();
        contextPath = prototype.getContextPath();
        cookies = prototype.getCookies();
        localAddr = prototype.getLocalAddr();
        localName = prototype.getLocalName();
        localPort = prototype.getLocalPort();
        locale = prototype.getLocale();
        method = prototype.getMethod();
        pathInfo = prototype.getPathInfo();
        pathTranslated = prototype.getPathTranslated();
        protocol = prototype.getProtocol();
        queryString = prototype.getQueryString();
        remoteAddr = prototype.getRemoteAddr();
        remoteHost = prototype.getRemoteHost();
        remotePort = prototype.getRemotePort();
        remoteUser = prototype.getRemoteUser();
        requestURI = prototype.getRequestURI();
        requestURL = prototype.getRequestURL();
        requestedSessionId = prototype.getRequestedSessionId();
        requestedSessionIdFromCookie = prototype.isRequestedSessionIdFromCookie();
        requestedSessionIdFromURL = prototype.isRequestedSessionIdFromURL();
        requestedSessionIdFromUrl = prototype.isRequestedSessionIdFromUrl();
        requestedSessionIdValid = prototype.isRequestedSessionIdValid();
        scheme = prototype.getScheme();
        secure = prototype.isSecure();
        serverName = prototype.getServerName();
        serverPort = prototype.getServerPort();
        servletPath = prototype.getServletPath();

        // copy attributes
        for (String key : Collections.list(prototype.getAttributeNames())) {
            attributes.put(key, prototype.getAttribute(key));
        }

        // copy headers
        for (String key : Collections.list(prototype.getHeaderNames())) {
            headers.put(key, Collections.list(prototype.getHeaders(key)));
            try {
                dateHeaders.put(key, prototype.getDateHeader(key));
            } catch (Exception e) {
                // dunno
            }
        }

        // copy locales
        locales = Collections.list(prototype.getLocales());

        // copy parameters
        parameters.putAll(prototype.getParameterMap());
    }

    /**
     * Returns the HTTP servlet request that this flash request delegates to.
     * 
     * @return the HTTP servlet request that this flash request delegates to.
     */
    protected HttpServletRequest getDelegate() {
        if (delegate == null) {
            throw new IllegalStateException(
                    "Attempt to access a delegate method of "
                    + FlashRequest.class.getName()
                    + " but no delegate request has been set");
        }
        return delegate;
    }

    /**
     * Sets the http servlet request that this flash request delegates to.
     * 
     * @param delegate - the http servlet request that this flash request delegates to.
     */
    public void setDelegate(HttpServletRequest delegate) {
        this.delegate = delegate;
    }

    /**
     * Gets the authentication type.
     * 
     * @return The authentication type.
     */
    @Override
    public String getAuthType() {
        return authType;
    }

    /**
     * Returns the cookies associated with this request
     * 
     * @return The cookies associated with this request
     */
    @Override
    public Cookie[] getCookies() {
        return cookies;
    }

    /**
     * Returns the date header for the passed in name.
     * 
     * @param name - Name to return date header for.
     * @return Date header value for the passed name
     */
    @Override
    public long getDateHeader(String name) {
        Long value = dateHeaders.get(name);
        return value == null ? 0 : value;
    }

    /**
     * Return the header value for the passed name
     * 
     * @param name - Name to return header value for
     * @return Header value for passed name
     */
    @Override
    public String getHeader(String name) {
        List<String> values = headers.get(name);
        return values != null && values.size() > 0 ? values.get(0) : null;
    }

    /**
     * Returns enumeration of headers associated with the passed name.
     * 
     * @param name - Header name
     * @return enumeration of headers associated with the passed name.
     */
    @Override
    public Enumeration<String> getHeaders(String name) {
        return Collections.enumeration(headers.get(name));
    }

    /**
     * Return an enumeration of all header names.
     * 
     * @return Enumeration of all header names.
     */
    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(headers.keySet());
    }

    /**
     * Returns int header value for the name passed in.
     * 
     * @param name - Name to return int header value for.
     * @return Int value of header name passed in
     */
    @Override
    public int getIntHeader(String name) {
        try {
            return Integer.parseInt(getHeader(name));
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Return the HTTP method for this request.
     * 
     * @return HTTP method for this request.
     */
    @Override
    public String getMethod() {
        return method;
    }

    /**
     * Returns path info for this request.
     * 
     * @return Path info for this request
     */
    @Override
    public String getPathInfo() {
        return pathInfo;
    }

    /**
     * Returns path translated for this request.
     * 
     * @return Path translated for this request.
     */
    @Override
    public String getPathTranslated() {
        return pathTranslated;
    }

    /**
     * Returns the context path for this request.
     * @return the context path for this request.
     */
    @Override
    public String getContextPath() {
        return contextPath;
    }

    /**
     * Returns the query string for this request
     * @return The query string for this request
     */
    @Override
    public String getQueryString() {
        return queryString;
    }

    /**
     * Returns the remote user of the request.
     * 
     * @return Remote user of the request
     */
    @Override
    public String getRemoteUser() {
        return remoteUser;
    }

    /**
     * Returns whether or not the user is in the role passed in.
     * 
     * @param role - Role to check user to see if they are a member of.
     * @return Whether or not the user is in the role passed in.
     */
    @Override
    public boolean isUserInRole(String role) {
        return getDelegate().isUserInRole(role);
    }

    /**
     * Returns the user principal for this request.
     * 
     * @return User principal for this request
     */
    @Override
    public Principal getUserPrincipal() {
        return getDelegate().getUserPrincipal();
    }

    /**
     * The session ID of this request
     * @return Session ID of this request
     */
    @Override
    public String getRequestedSessionId() {
        return requestedSessionId;
    }

    /**
     * Return the URI of this request.
     * @return URI of this request.
     */
    @Override
    public String getRequestURI() {
        return requestURI;
    }

    /**
     * The request URL
     * @return The request URL
     */
    @Override
    public StringBuffer getRequestURL() {
        return new StringBuffer(requestURL.toString());
    }

    /**
     * Returns the servlet path
     * @return The servlet path
     */
    @Override
    public String getServletPath() {
        return servletPath;
    }

    /**
     * Returns the session and creates a new one if necessary (and indicated)
     * @param create - Whether or not to create a new session if one does not already exist
     * @return HTTP session object associated with this request
     */
    @Override
    public HttpSession getSession(boolean create) {
        return getDelegate().getSession(create);
    }

    /**
     * Returns the session and creates a new one if necessary 
     * @return HTTP session object associated with this request
     */
    @Override
    public HttpSession getSession() {
        return getDelegate().getSession();
    }

    /**
     * Returns whether or not the requested session ID is valid.
     * @return whether or not the requested session ID is valid.
     */
    @Override
    public boolean isRequestedSessionIdValid() {
        return requestedSessionIdValid;
    }

    /**
     * Returns whether or not the session ID is from the cookie.
     * @return whether or not the session ID is from the cookie.
     */
    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return requestedSessionIdFromCookie;
    }

    /**
     * Returns whether or not the session ID is from the URL
     * @return whether or not the session ID is from the URL
     */
    @Override
    public boolean isRequestedSessionIdFromURL() {
        return requestedSessionIdFromURL;
    }

    /**
     * Returns whether or not the session ID is from the URL
     * @return whether or not the session ID is from the URL
     * @deprecated
     */
    @Deprecated
    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return requestedSessionIdFromUrl;
    }

    /**
     * Returns the attribute value associated with the passed attribute name.
     * 
     * @param name - Name to return attribute value for.
     * @return The attribute value for the passed name.
     */
    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    /**
     * Returns an enumeration of all of the attribute names associated with this
     * request.
     * 
     * @return an enumeration of all of the attribute names associated with this
     * request.
     */
    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
    }

    /**
     * Returns the character encoding of this request.
     * 
     * @return Character encoding of this request.
     */
    @Override
    public String getCharacterEncoding() {
        return characterEncoding;
    }

    /**
     * Sets the character encoding for this request.
     * 
     * @param characterEncoding - Character encoding of this request
     */
    @Override
    public void setCharacterEncoding(String characterEncoding) {
        this.characterEncoding = characterEncoding;
    }

    /**
     * Return the length of the content for this flash request.  This is always
     * zero.
     * 
     * @return Length of content for this request.  Always zero for flash requests.
     */
    @Override
    public int getContentLength() {
        return 0;
    }

    /**
     * Returns the content type of this request.
     * 
     * @return Content type of this request.
     */
    @Override
    public String getContentType() {
        return contentType;
    }

    /**
     * Returns the servlet input strem for this request.  This is always null
     * for a flash request.
     * 
     * @return the servlet input strem for this request.  This is always null
     * for a flash request.
     */
    @Override
    public ServletInputStream getInputStream() {
        return null;
    }

    /**
     * Returns the values associated with the parameter name passed.
     *  
     * @param name - Name to return values for
     * @return the values associated with the parameter name passed.
     */
    @Override
    public String getParameter(String name) {
        String[] values = getParameterValues(name);
        return values != null && values.length > 0 ? values[0] : null;
    }

    /**
     * Returns enumeration of all parameter names.
     * 
     * @return Enumeration of all parameter names.
     */
    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(parameters.keySet());
    }

    /**
     * Returns an array of values for the passed parameter name.
     * 
     * @param name - Parameter name to return values for
     * @return Values for the passed parameter name
     */
    @Override
    public String[] getParameterValues(String name) {
        return parameters.get(name);
    }

    /**
     * Returns map of parameter names to values.
     * @return map of parameter names to values.
     */
    @Override
    public Map<String, String[]> getParameterMap() {
        return Collections.unmodifiableMap(parameters);
    }

    /**
     * Returns the protocol for this HTTP request.
     * 
     * @return Protocol for this request.
     */
    @Override
    public String getProtocol() {
        return protocol;
    }

    /**
     * Returns the scheme of this request.
     * 
     * @return Scheme of this request.
     */
    @Override
    public String getScheme() {
        return scheme;
    }

    /**
     * Returns the server name associated with this request.
     * 
     * @return the server name associated with this request.
     */
    @Override
    public String getServerName() {
        return serverName;
    }

    /**
     * Returns the port associated with this request.
     * @return the port associated with this request.
     */
    @Override
    public int getServerPort() {
        return serverPort;
    }

    /**
     * Returns the reader associated with this request.  Since flash requests
     * are simple wrappers, there is no reader associated with it.
     * 
     * @return Null reader for flash request.
     */
    @Override
    public BufferedReader getReader() {
        return null;
    }

    /**
     * Returns remote address associated with this request.
     * @return remote address associated with this request.
     */
    @Override
    public String getRemoteAddr() {
        return remoteAddr;
    }

    /**
     * Returns remote host associated with this request.
     * @return remote host associated with this request.
     */
    @Override
    public String getRemoteHost() {
        return remoteHost;
    }

    /**
     * Sets an attribute name/value on the request.
     * 
     * @param name - Name of the attribute
     * @param value - Value of the attribute
     */
    @Override
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    /**
     * Removes the attributes from the request with the passed name.
     * @param name - Name of attribute to remove
     */
    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    /**
     * Returns the locale of the request.
     * 
     * @return The locale of the request.
     */
    @Override
    public Locale getLocale() {
        return locale;
    }

    /**
     * Returns the locales associated with this request.
     * @return the locales associated with this request.
     */
    @Override
    public Enumeration<Locale> getLocales() {
        return Collections.enumeration(locales);
    }

    /**
     * Returns whether or not this request is secure.
     * @return whether or not this request is secure.
     */
    @Override
    public boolean isSecure() {
        return secure;
    }

    /**
     * Returns the dispatcher for the passed name
     * @param name - Request dispatcher name to return dispatcher for
     * @return the dispatcher for the passed name
     */
    @Override
    public RequestDispatcher getRequestDispatcher(String name) {
        return getDelegate().getRequestDispatcher(name);
    }

    /**
     * Returns the real path for the passed resource name.
     * 
     * @param name - Name of resource to return real path for
     * @return the real path for the passed resource name.
     * @deprecated
     */
    @Override
    @Deprecated
    public String getRealPath(String name) {
        return getDelegate().getRealPath(name);
    }

    /**
     * Returns the remote port for the request.
     * 
     * @return the remote port for the request.
     */
    @Override
    public int getRemotePort() {
        return remotePort;
    }

    /**
     * Returns the local name of the request.
     * 
     * @return Local name of the request
     */
    @Override
    public String getLocalName() {
        return localName;
    }

    /**
     * Returns the local address of the request.
     * 
     * @return Local address of the request.
     */
    @Override
    public String getLocalAddr() {
        return localAddr;
    }

    /**
     * Returns the local port of the request.
     * 
     * @return Local port of the request.
     */
    @Override
    public int getLocalPort() {
        return localPort;
    }

    /**
     * Changes the session ID for the request.
     * 
     * @return The new session ID.
     */
    @Override
    public String changeSessionId() {
        return getDelegate().changeSessionId();
    }

    /**
     * Authenticates the user in the request.
     * 
     * @param response The response object
     * @return Whether or not the user authentication succeeded.
     * @throws IOException If an error occurs during authentication
     * @throws ServletException If another type of servlet error occurs during authentication
     */
    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        return delegate.authenticate(response);
    }

    /**
     * Logs in with the passed username and password
     * 
     * @param username - Username to log in as
     * @param password - Password to log in as
     * @throws ServletException If the login fails
     */
    @Override
    public void login(String username, String password) throws ServletException {
        delegate.login(username, password);
    }

    /**
     * Logs out the currently logged in user.
     * 
     * @throws ServletException If logout fails
     */
    @Override
    public void logout() throws ServletException {
        delegate.logout();
    }

    /**
     * Returns the request parts for this request.
     * 
     * @return The request parts for this request
     * @throws IOException If an IO error happens retrieving the request parts.
     * @throws ServletException If another error happens retrieving request parts.
     */
    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return delegate.getParts();
    }

    /**
     * Returns the request part with the passed name.
     * 
     * @param name - Name of part to return part for
     * @return Request part
     * @throws IOException If an IO error happens returning the requested part
     * @throws ServletException If another error happens returning the requested part
     */
    @Override
    public Part getPart(String name) throws IOException, ServletException {
        return delegate.getPart(name);
    }

    /**
     * Returns the upgrade handler to upgrade to for the passed handler class.
     * 
     * @param <T> Type of upgrade handler to upgrade to
     * @param handlerClass The current handler class
     * @return Instance of upgrade handler to upgrade to
     * @throws IOException If an error occurs during retrieval of upgrade handler
     * @throws ServletException If an error occurs during retrieval of upgrade handler
     */
    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        return delegate.upgrade(handlerClass);
    }

    /**
     * Returns the length of the content as a long.  Always zero for flash request.
     * @return the length of the content as a long.  Always zero for flash request.
     */
    @Override
    public long getContentLengthLong() {
        return 0;
    }

    /**
     * Returns the servlet context.
     * 
     * @return The servlet context.
     */
    @Override
    public ServletContext getServletContext() {
        return delegate.getServletContext();
    }

    /**
     * Returns the async context.
     * 
     * @return the async context
     * @throws IllegalStateException If this is not an async-able request.
     */
    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return delegate.startAsync();
    }

    /**
     * Beging an asynchronous request context.
     * 
     * @param servletRequest - Servlet request object
     * @param servletResponse - Servlet response object
     * @return Async context object
     * @throws IllegalStateException If the async cannot be started
     */
    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        return delegate.startAsync(servletRequest, servletResponse);
    }

    /**
     * Returns whether or not async has been started for this request.
     * 
     * @return whether or not async has been started for this request.
     */
    @Override
    public boolean isAsyncStarted() {
        return delegate.isAsyncStarted();
    }

    /**
     * Returns whether or not async is supported for this request.
     * 
     * @return whether or not async is supported for this request.
     */
    @Override
    public boolean isAsyncSupported() {
        return delegate.isAsyncSupported();
    }

    /**
     * Returns the async context for this request.
     * 
     * @return the async context for this request.
     */
    @Override
    public AsyncContext getAsyncContext() {
        return delegate.getAsyncContext();
    }

    /**
     * Returns the dispatcher type object for this request.
     * 
     * @return the dispatcher type object for this request.
     */
    @Override
    public DispatcherType getDispatcherType() {
        return delegate.getDispatcherType();
    }
}
