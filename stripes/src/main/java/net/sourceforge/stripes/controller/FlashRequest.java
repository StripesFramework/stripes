package net.sourceforge.stripes.controller;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.BufferedReader;
import java.io.Serial;
import java.io.Serializable;
import java.security.Principal;
import java.util.*;

/**
 * Captures the state of an {@link jakarta.servlet.http.HttpServletRequest} so that the information
 * contained therein can be carried over to the next request for use by the flash scope. There are
 * several methods in here that cannot be faked and so must delegate to an active {@link
 * jakarta.servlet.http.HttpServletRequest} object, the {@link #delegate}. If one of these methods
 * is called and there is no delegate object set on the instance, they will throw a {@link
 * net.sourceforge.stripes.exception.StripesRuntimeException}. Unless this class is used outside its
 * intended context (during a live request processed through {@link StripesFilter}), you won't need
 * to worry about that.
 *
 * @author Ben Gunter
 * @since Stripes 1.4.3
 */
public class FlashRequest implements HttpServletRequest, Serializable {
  @Serial private static final long serialVersionUID = 1L;

  private final Cookie[] cookies;
  private HttpServletRequest delegate;
  private final List<Locale> locales;
  private final Locale locale;
  private final Map<String, List<String>> headers = new HashMap<>();
  private final Map<String, Long> dateHeaders = new HashMap<>();
  private final Map<String, Object> attributes = new HashMap<>();
  private final Map<String, String[]> parameters = new HashMap<>();
  private final String authType;
  private String characterEncoding;
  private final String contentType;
  private final String contextPath;
  private final String localAddr;
  private final String localName;
  private final String method;
  private final String pathInfo;
  private final String pathTranslated;
  private final String protocol;
  private final String queryString;
  private final String remoteAddr;
  private final String remoteHost;
  private final String remoteUser;
  private final String requestURI;
  private final String requestedSessionId;
  private final String scheme;
  private final String serverName;
  private final String servletPath;
  private final StringBuffer requestURL;
  private final boolean requestedSessionIdFromCookie;
  private final boolean requestedSessionIdFromURL;
  private final boolean requestedSessionIdValid;
  private final boolean secure;
  private final int localPort;
  private final int remotePort;
  private final int serverPort;

  /**
   * Finds the StripesRequestWrapper for the supplied request and swaps out the underlying request
   * for an instance of FlashRequest.
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
   * Creates a new FlashRequest by copying all appropriate attributes from the prototype request
   * supplied.
   *
   * @param prototype the HttpServletRequest to create a disconnected copy of
   */
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
      } catch (Exception ignored) {
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
          "Attempt to access a delegate method of "
              + FlashRequest.class.getName()
              + " but no delegate request has been set");
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
    return values != null && !values.isEmpty() ? values.get(0) : null;
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
    } catch (Exception e) {
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

  @Override
  public String changeSessionId() {
    return null;
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

  @Override
  public boolean authenticate(HttpServletResponse httpServletResponse) {
    return false;
  }

  @Override
  public void login(String s, String s1) {}

  @Override
  public void logout() {}

  @Override
  public Collection<Part> getParts() {
    return null;
  }

  @Override
  public Part getPart(String s) {
    return null;
  }

  @Override
  public <T extends HttpUpgradeHandler> T upgrade(Class<T> aClass) {
    return null;
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

  @Override
  public long getContentLengthLong() {
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

  @Override
  public ServletContext getServletContext() {
    return null;
  }

  @Override
  public AsyncContext startAsync() throws IllegalStateException {
    return null;
  }

  @Override
  public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
      throws IllegalStateException {
    return null;
  }

  @Override
  public boolean isAsyncStarted() {
    return false;
  }

  @Override
  public boolean isAsyncSupported() {
    return false;
  }

  @Override
  public AsyncContext getAsyncContext() {
    return null;
  }

  @Override
  public DispatcherType getDispatcherType() {
    return null;
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
