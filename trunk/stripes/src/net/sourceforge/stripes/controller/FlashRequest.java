package net.sourceforge.stripes.controller;

import java.io.BufferedReader;
import java.io.Serializable;
import java.security.Principal;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Captures the state of an {@link javax.servlet.http.HttpServletRequest} so that the information
 * contained therein can be carried over to the next request for use by the flash scope. There are
 * several methods in here that cannot be faked and so must delegate to an active {@link
 * javax.servlet.http.HttpServletRequest} object, the {@link #delegate}. If one of these methods is
 * called and there is no delegate object set on the instance, they will throw a {@link
 * net.sourceforge.stripes.exception.StripesRuntimeException}. Unless this class is used outside its
 * intended context (during a live request processed through {@link StripesFilter}), you won't need
 * to worry about that.
 *
 * @author Ben Gunter
 * @since Stripes 1.4.3
 */
@SuppressWarnings("serial")
public class FlashRequest implements HttpServletRequest, Serializable {
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
	 * Finds the StripesRequestWrapper for the supplied request and swaps out the underlying
	 * request for an instance of FlashRequest.
	 *
	 * @param request the current HttpServletRequest
	 * @return the StripesRequestWrapper for this request with the "live" request replaced
	 */
    public static StripesRequestWrapper replaceRequest(HttpServletRequest request) {
        StripesRequestWrapper wrapper = StripesRequestWrapper.findStripesWrapper(request);
        wrapper.setRequest(new FlashRequest((HttpServletRequest) wrapper.getRequest()));
        return wrapper;
    }

    /**
     * Creates a new FlashRequest by copying all appropriate attributes from the prototype
     * request supplied.
     *
     * @param prototype the HttpServletRequest to create a disconnected copy of
     */
    @SuppressWarnings({ "unchecked", "deprecation" })
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
        for (String key : Collections.list((Enumeration<String>) prototype.getAttributeNames())) {
            attributes.put(key, prototype.getAttribute(key));
        }

        // copy headers
        for (String key : Collections.list((Enumeration<String>) prototype.getHeaderNames())) {
            headers.put(key, Collections.list(prototype.getHeaders(key)));
            try {
                dateHeaders.put(key, prototype.getDateHeader(key));
            }
            catch (Exception e) {
            }
        }

        // copy locales
        locales = Collections.list(prototype.getLocales());

        // copy parameters
        parameters.putAll(prototype.getParameterMap());
    }

    protected HttpServletRequest getDelegate() {
        if (delegate == null) {
            throw new IllegalStateException(
                    "Attempt to access a delegate method of " +
                    FlashRequest.class.getName() +
                    " but no delegate request has been set");
        }
        return delegate;
    }

    public void setDelegate(HttpServletRequest delegate) {
        this.delegate = delegate;
    }

    public String getAuthType() {
        return authType;
    }

    public Cookie[] getCookies() {
        return cookies;
    }

    public long getDateHeader(String name) {
        Long value = dateHeaders.get(name);
        return value == null ? 0 : value;
    }

    public String getHeader(String name) {
        List<String> values = headers.get(name);
        return values != null && values.size() > 0 ? values.get(0) : null;
    }

    public Enumeration<String> getHeaders(String name) {
        return Collections.enumeration(headers.get(name));
    }

    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(headers.keySet());
    }

    public int getIntHeader(String name) {
        try {
            return Integer.parseInt(getHeader(name));
        }
        catch (Exception e) {
            return 0;
        }
    }

    public String getMethod() {
        return method;
    }

    public String getPathInfo() {
        return pathInfo;
    }

    public String getPathTranslated() {
        return pathTranslated;
    }

    public String getContextPath() {
        return contextPath;
    }

    public String getQueryString() {
        return queryString;
    }

    public String getRemoteUser() {
        return remoteUser;
    }

    public boolean isUserInRole(String role) {
        return getDelegate().isUserInRole(role);
    }

    public Principal getUserPrincipal() {
        return getDelegate().getUserPrincipal();
    }

    public String getRequestedSessionId() {
        return requestedSessionId;
    }

    public String getRequestURI() {
        return requestURI;
    }

    public StringBuffer getRequestURL() {
        return new StringBuffer(requestURL.toString());
    }

    public String getServletPath() {
        return servletPath;
    }

    public HttpSession getSession(boolean create) {
        return getDelegate().getSession(create);
    }

    public HttpSession getSession() {
        return getDelegate().getSession();
    }

    public boolean isRequestedSessionIdValid() {
        return requestedSessionIdValid;
    }

    public boolean isRequestedSessionIdFromCookie() {
        return requestedSessionIdFromCookie;
    }

    public boolean isRequestedSessionIdFromURL() {
        return requestedSessionIdFromURL;
    }

    @Deprecated
    public boolean isRequestedSessionIdFromUrl() {
        return requestedSessionIdFromUrl;
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
    }

    public String getCharacterEncoding() {
        return characterEncoding;
    }

    public void setCharacterEncoding(String characterEncoding) {
        this.characterEncoding = characterEncoding;
    }

    public int getContentLength() {
        return 0;
    }

    public String getContentType() {
        return contentType;
    }

    public ServletInputStream getInputStream() {
        return null;
    }

    public String getParameter(String name) {
        String[] values = getParameterValues(name);
        return values != null && values.length > 0 ? values[0] : null;
    }

    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(parameters.keySet());
    }

    public String[] getParameterValues(String name) {
        return parameters.get(name);
    }

    public Map<String, String[]> getParameterMap() {
        return Collections.unmodifiableMap(parameters);
    }

    public String getProtocol() {
        return protocol;
    }

    public String getScheme() {
        return scheme;
    }

    public String getServerName() {
        return serverName;
    }

    public int getServerPort() {
        return serverPort;
    }

    public BufferedReader getReader() {
        return null;
    }

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    public Locale getLocale() {
        return locale;
    }

    public Enumeration<Locale> getLocales() {
        return Collections.enumeration(locales);
    }

    public boolean isSecure() {
        return secure;
    }

    public RequestDispatcher getRequestDispatcher(String name) {
        return getDelegate().getRequestDispatcher(name);
    }

    @Deprecated
    public String getRealPath(String name) {
        return getDelegate().getRealPath(name);
    }

    public int getRemotePort() {
        return remotePort;
    }

    public String getLocalName() {
        return localName;
    }

    public String getLocalAddr() {
        return localAddr;
    }

    public int getLocalPort() {
        return localPort;
    }
}
