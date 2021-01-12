package net.sourceforge.stripes.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;


/**
 * Captures the state of an {@link javax.servlet.http.HttpServletRequest} so that the information
 * contained therein can be carried over to the next request for use by the flash scope. There are
 * several methods in here that cannot be faked and so must delegate to an active {@link
 * javax.servlet.http.HttpServletRequest} object, the {@link #_delegate}. If one of these methods is
 * called and there is no delegate object set on the instance, they will throw a {@link
 * net.sourceforge.stripes.exception.StripesRuntimeException}. Unless this class is used outside its
 * intended context (during a live request processed through {@link StripesFilter}), you won't need
 * to worry about that.
 *
 * @author Ben Gunter
 * @since Stripes 1.4.3
 */
public class FlashRequest implements HttpServletRequest, Serializable {

   private static final long serialVersionUID = 1L;

   /**
    * Finds the StripesRequestWrapper for the supplied request and swaps out the underlying
    * request for an instance of FlashRequest.
    *
    * @param request the current HttpServletRequest
    * @return the StripesRequestWrapper for this request with the "live" request replaced
    */
   public static StripesRequestWrapper replaceRequest( HttpServletRequest request ) {
      StripesRequestWrapper wrapper = StripesRequestWrapper.findStripesWrapper(request);
      wrapper.setRequest(new FlashRequest((HttpServletRequest)wrapper.getRequest()));
      return wrapper;
   }

   private final Cookie[]                  _cookies;
   private       HttpServletRequest        _delegate;
   private final List<Locale>              _locales;
   private final Locale                    _locale;
   private final Map<String, List<String>> _headers     = new HashMap<>();
   private final Map<String, Long>         _dateHeaders = new HashMap<>();
   private final Map<String, Object>       _attributes  = new HashMap<>();
   private final Map<String, String[]>     _parameters  = new HashMap<>();
   private final String                    _authType;
   private       String                    _characterEncoding;
   private final String                    _contentType;
   private final String                    _contextPath;
   private final String                    _localAddr;
   private final String                    _localName;
   private final String                    _method;
   private final String                    _pathInfo;
   private final String                    _pathTranslated;
   private final String                    _protocol;
   private final String                    _queryString;
   private final String                    _remoteAddr;
   private final String                    _remoteHost;
   private final String                    _remoteUser;
   private final String                    _requestURI;
   private final String                    _requestedSessionId;
   private final String                    _scheme;
   private final String                    _serverName;
   private final String                    _servletPath;
   private final StringBuffer              _requestURL;
   private final boolean                   _requestedSessionIdFromCookie;
   private final boolean                   _requestedSessionIdFromURL;
   private final boolean                   _requestedSessionIdFromUrl;
   private final boolean                   _requestedSessionIdValid;
   private final boolean                   _secure;
   private final int                       _localPort;
   private final int                       _remotePort;
   private final int                       _serverPort;

   /**
    * Creates a new FlashRequest by copying all appropriate attributes from the prototype
    * request supplied.
    *
    * @param prototype the HttpServletRequest to create a disconnected copy of
    */
   @SuppressWarnings({ "deprecation" })
   public FlashRequest( HttpServletRequest prototype ) {
      // copy properties
      _authType = prototype.getAuthType();
      _characterEncoding = prototype.getCharacterEncoding();
      _contentType = prototype.getContentType();
      _contextPath = prototype.getContextPath();
      _cookies = prototype.getCookies();
      _localAddr = prototype.getLocalAddr();
      _localName = prototype.getLocalName();
      _localPort = prototype.getLocalPort();
      _locale = prototype.getLocale();
      _method = prototype.getMethod();
      _pathInfo = prototype.getPathInfo();
      _pathTranslated = prototype.getPathTranslated();
      _protocol = prototype.getProtocol();
      _queryString = prototype.getQueryString();
      _remoteAddr = prototype.getRemoteAddr();
      _remoteHost = prototype.getRemoteHost();
      _remotePort = prototype.getRemotePort();
      _remoteUser = prototype.getRemoteUser();
      _requestURI = prototype.getRequestURI();
      _requestURL = prototype.getRequestURL();
      _requestedSessionId = prototype.getRequestedSessionId();
      _requestedSessionIdFromCookie = prototype.isRequestedSessionIdFromCookie();
      _requestedSessionIdFromURL = prototype.isRequestedSessionIdFromURL();
      _requestedSessionIdFromUrl = prototype.isRequestedSessionIdFromUrl();
      _requestedSessionIdValid = prototype.isRequestedSessionIdValid();
      _scheme = prototype.getScheme();
      _secure = prototype.isSecure();
      _serverName = prototype.getServerName();
      _serverPort = prototype.getServerPort();
      _servletPath = prototype.getServletPath();

      // copy attributes
      for ( String key : Collections.list(prototype.getAttributeNames()) ) {
         _attributes.put(key, prototype.getAttribute(key));
      }

      // copy headers
      for ( String key : Collections.list(prototype.getHeaderNames()) ) {
         _headers.put(key, Collections.list(prototype.getHeaders(key)));
         try {
            _dateHeaders.put(key, prototype.getDateHeader(key));
         }
         catch ( Exception e ) {
            // ignored
         }
      }

      // copy locales
      _locales = Collections.list(prototype.getLocales());

      // copy parameters
      _parameters.putAll(prototype.getParameterMap());
   }

   @Override
   public boolean authenticate( HttpServletResponse httpServletResponse ) throws IOException, ServletException {
      return false;
   }

   @Override
   public String changeSessionId() {
      return null;
   }

   @Override
   public AsyncContext getAsyncContext() {
      return getDelegate().getAsyncContext();
   }

   @Override
   public Object getAttribute( String name ) {
      return _attributes.get(name);
   }

   @Override
   public Enumeration<String> getAttributeNames() {
      return Collections.enumeration(_attributes.keySet());
   }

   @Override
   public String getAuthType() {
      return _authType;
   }

   @Override
   public String getCharacterEncoding() {
      return _characterEncoding;
   }

   @Override
   public int getContentLength() {
      return 0;
   }

   @Override
   public long getContentLengthLong() {
      return 0;
   }

   @Override
   public String getContentType() {
      return _contentType;
   }

   @Override
   public String getContextPath() {
      return _contextPath;
   }

   @Override
   public Cookie[] getCookies() {
      return _cookies;
   }

   @Override
   public long getDateHeader( String name ) {
      Long value = _dateHeaders.get(name);
      return value == null ? 0 : value;
   }

   @Override
   public DispatcherType getDispatcherType() {
      return getDelegate().getDispatcherType();
   }

   @Override
   public String getHeader( String name ) {
      List<String> values = _headers.get(name);
      return values != null && values.size() > 0 ? values.get(0) : null;
   }

   @Override
   public Enumeration<String> getHeaderNames() {
      return Collections.enumeration(_headers.keySet());
   }

   @Override
   public Enumeration<String> getHeaders( String name ) {
      return Collections.enumeration(_headers.get(name));
   }

   @Override
   public ServletInputStream getInputStream() {
      return null;
   }

   @Override
   public int getIntHeader( String name ) {
      try {
         return Integer.parseInt(getHeader(name));
      }
      catch ( Exception e ) {
         return 0;
      }
   }

   @Override
   public String getLocalAddr() {
      return _localAddr;
   }

   @Override
   public String getLocalName() {
      return _localName;
   }

   @Override
   public int getLocalPort() {
      return _localPort;
   }

   @Override
   public Locale getLocale() {
      return _locale;
   }

   @Override
   public Enumeration<Locale> getLocales() {
      return Collections.enumeration(_locales);
   }

   @Override
   public String getMethod() {
      return _method;
   }

   @Override
   public String getParameter( String name ) {
      String[] values = getParameterValues(name);
      return values != null && values.length > 0 ? values[0] : null;
   }

   @Override
   public Map<String, String[]> getParameterMap() {
      return Collections.unmodifiableMap(_parameters);
   }

   @Override
   public Enumeration<String> getParameterNames() {
      return Collections.enumeration(_parameters.keySet());
   }

   @Override
   public String[] getParameterValues( String name ) {
      return _parameters.get(name);
   }

   @Override
   public Part getPart( String s ) throws IOException, ServletException {
      return null;
   }

   @Override
   public Collection<Part> getParts() throws IOException, ServletException {
      return Collections.emptyList();
   }

   @Override
   public String getPathInfo() {
      return _pathInfo;
   }

   @Override
   public String getPathTranslated() {
      return _pathTranslated;
   }

   @Override
   public String getProtocol() {
      return _protocol;
   }

   @Override
   public String getQueryString() {
      return _queryString;
   }

   @Override
   public BufferedReader getReader() {
      return null;
   }

   @Override
   @Deprecated
   public String getRealPath( String name ) {
      return getDelegate().getRealPath(name);
   }

   @Override
   public String getRemoteAddr() {
      return _remoteAddr;
   }

   @Override
   public String getRemoteHost() {
      return _remoteHost;
   }

   @Override
   public int getRemotePort() {
      return _remotePort;
   }

   @Override
   public String getRemoteUser() {
      return _remoteUser;
   }

   @Override
   public RequestDispatcher getRequestDispatcher( String name ) {
      return getDelegate().getRequestDispatcher(name);
   }

   @Override
   public String getRequestURI() {
      return _requestURI;
   }

   @Override
   public StringBuffer getRequestURL() {
      return new StringBuffer(_requestURL.toString());
   }

   @Override
   public String getRequestedSessionId() {
      return _requestedSessionId;
   }

   @Override
   public String getScheme() {
      return _scheme;
   }

   @Override
   public String getServerName() {
      return _serverName;
   }

   @Override
   public int getServerPort() {
      return _serverPort;
   }

   @Override
   public ServletContext getServletContext() {
      return getDelegate().getServletContext();
   }

   @Override
   public String getServletPath() {
      return _servletPath;
   }

   @Override
   public HttpSession getSession( boolean create ) {
      return getDelegate().getSession(create);
   }

   @Override
   public HttpSession getSession() {
      return getDelegate().getSession();
   }

   @Override
   public Principal getUserPrincipal() {
      return getDelegate().getUserPrincipal();
   }

   @Override
   public boolean isAsyncStarted() {
      return getDelegate().isAsyncStarted();
   }

   @Override
   public boolean isAsyncSupported() {
      return getDelegate().isAsyncSupported();
   }

   @Override
   public boolean isRequestedSessionIdFromCookie() {
      return _requestedSessionIdFromCookie;
   }

   @Override
   public boolean isRequestedSessionIdFromURL() {
      return _requestedSessionIdFromURL;
   }

   @Override
   @Deprecated
   public boolean isRequestedSessionIdFromUrl() {
      return _requestedSessionIdFromUrl;
   }

   @Override
   public boolean isRequestedSessionIdValid() {
      return _requestedSessionIdValid;
   }

   @Override
   public boolean isSecure() {
      return _secure;
   }

   @Override
   public boolean isUserInRole( String role ) {
      return getDelegate().isUserInRole(role);
   }

   @Override
   public void login( String username, String password ) throws ServletException {
      getDelegate().login(username, password);
   }

   @Override
   public void logout() throws ServletException {
      getDelegate().logout();
   }

   @Override
   public void removeAttribute( String name ) {
      _attributes.remove(name);
   }

   @Override
   public void setAttribute( String name, Object value ) {
      _attributes.put(name, value);
   }

   @Override
   public void setCharacterEncoding( String characterEncoding ) {
      _characterEncoding = characterEncoding;
   }

   public void setDelegate( HttpServletRequest delegate ) {
      _delegate = delegate;
   }

   @Override
   public AsyncContext startAsync() throws IllegalStateException {
      return getDelegate().startAsync();
   }

   @Override
   public AsyncContext startAsync( ServletRequest servletRequest, ServletResponse servletResponse ) throws IllegalStateException {
      return getDelegate().startAsync(servletRequest, servletResponse);
   }

   @Override
   public <T extends HttpUpgradeHandler> T upgrade( Class<T> aClass ) throws IOException, ServletException {
      return getDelegate().upgrade(aClass);
   }

   protected HttpServletRequest getDelegate() {
      if ( _delegate == null ) {
         throw new IllegalStateException("Attempt to access a delegate method of " + FlashRequest.class.getName() + " but no delegate request has been set");
      }
      return _delegate;
   }
}
