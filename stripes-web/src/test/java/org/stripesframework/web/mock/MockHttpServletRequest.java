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
package org.stripesframework.web.mock;

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

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
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
public class MockHttpServletRequest implements HttpServletRequest {

   private       String                _authType;
   private       Cookie[]              _cookies;
   private final Map<String, Object>   _headers           = new HashMap<>();
   private final Map<String, Object>   _attributes        = new HashMap<>();
   private final Map<String, String[]> _parameters        = new HashMap<>();
   private       String                _method            = "POST";
   private       HttpSession           _session;
   private       String                _characterEncoding = "UTF-8";
   private final List<Locale>          _locales           = new ArrayList<>();
   private       Principal             _userPrincipal;
   private       Set<String>           _roles             = new HashSet<>();
   private       String                _forwardUrl;
   private final List<String>          _includedUrls      = new ArrayList<>();

   // All the bits of the URL
   private String _protocol    = "https";
   private String _serverName  = "localhost";
   private int    _serverPort  = 8080;
   private String _contextPath = "";
   private String _servletPath = "";
   private String _pathInfo    = "";
   private String _queryString = "";

   /**
    * Minimal constructor that makes sense. Requires a context path (should be the same as
    * the name of the servlet context, prepended with a '/') and a servlet path. E.g.
    * new MockHttpServletRequest("/myapp", "/actionType/foo.action").
    *
    * @param contextPath
    * @param servletPath
    */
   public MockHttpServletRequest( String contextPath, String servletPath ) {
      _contextPath = contextPath;
      _servletPath = servletPath;
   }

   /**
    * Allows headers to be set on the request. These will be returned by the various getXxHeader()
    * methods. If the header is a date header it should be set with a Long. If the header is an
    * Int header it should be set with an Integer.
    */
   public void addHeader( String name, Object value ) {
      _headers.put(name.toLowerCase(), value);
   }

   /** Adds a Locale to the set of requested locales. */
   public void addLocale( Locale locale ) { _locales.add(locale); }

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
      return null;
   }

   /** Gets the named request attribute from an internal Map. */
   @Override
   public Object getAttribute( String key ) { return _attributes.get(key); }

   /** Gets an enumeration of all request attribute names. */
   @Override
   public Enumeration<String> getAttributeNames() {
      return Collections.enumeration(_attributes.keySet());
   }

   /** Gets the auth type being used by this request. */
   @Override
   public String getAuthType() { return _authType; }

   /** Gets the character encoding, defaults to UTF-8. */
   @Override
   public String getCharacterEncoding() { return _characterEncoding; }

   /** Always returns -1 (unknown). */
   @Override
   public int getContentLength() { return -1; }

   @Override
   public long getContentLengthLong() {
      return 0;
   }

   /** Always returns null. */
   @Override
   public String getContentType() { return null; }

   /** Returns the context path. Defaults to the empty string. */
   @Override
   public String getContextPath() { return _contextPath; }

   /** Returns any cookies that are set on the request. */
   @Override
   public Cookie[] getCookies() { return _cookies; }

   /** Gets the named header as a long. Must have been set as a long with addHeader(). */
   @Override
   public long getDateHeader( String name ) { return (Long)_headers.get(name); }

   @Override
   public DispatcherType getDispatcherType() {
      return null;
   }

   /** Gets the URL that was forwarded to, if a forward was processed. Null otherwise. */
   public String getForwardUrl() { return _forwardUrl; }

   /** Returns any header as a String if it exists. */
   @Override
   public String getHeader( String name ) {
      final Object header = _headers.get(name == null ? null : name.toLowerCase());
      return header == null ? null : header.toString();
   }

   /** Returns an enumeration containing all the names of headers supplied. */
   @Override
   public Enumeration<String> getHeaderNames() { return Collections.enumeration(_headers.keySet()); }

   /** Returns an enumeration with single value of the named header, or an empty enum if no value. */
   @Override
   public Enumeration<String> getHeaders( String name ) {
      String header = getHeader(name);
      Collection<String> values = new ArrayList<>();
      if ( header != null ) {
         values.add(header);
      }
      return Collections.enumeration(values);
   }

   /** Gets the list (potentially empty) or URLs that were included during the request. */
   public List<String> getIncludedUrls() { return _includedUrls; }

   /** Always returns null. */
   @Override
   public ServletInputStream getInputStream() throws IOException { return null; }

   /** Gets the named header as an int. Must have been set as an Integer with addHeader(). */
   @Override
   public int getIntHeader( String name ) {
      String headerValue = getHeader(name);
      if ( headerValue == null ) {
         return -1;
      }
      return Integer.parseInt(headerValue);
   }

   /** Always returns 127.0.0.1). */
   @Override
   public String getLocalAddr() { return "127.0.0.1"; }

   /** Always returns the same value as getServerName(). */
   @Override
   public String getLocalName() { return getServerName(); }

   /** Always returns the same value as getServerPort(). */
   @Override
   public int getLocalPort() { return getServerPort(); }

   /** Returns the preferred locale. Defaults to the system locale. */
   @Override
   public Locale getLocale() { return getLocales().nextElement(); }

   /** Returns an enumeration of requested locales. Defaults to the system locale. */
   @Override
   public Enumeration<Locale> getLocales() {
      if ( _locales.size() == 0 ) {
         _locales.add(Locale.getDefault());
      }

      return Collections.enumeration(_locales);
   }

   /** Gets the method used by the request. Defaults to POST. */
   @Override
   public String getMethod() { return _method; }

   /** Gets the first value of the named parameter or null if a value does not exist. */
   @Override
   public String getParameter( String name ) {
      String[] values = getParameterValues(name);
      if ( values != null && values.length > 0 ) {
         return values[0];
      }

      return null;
   }

   /**
    * Provides access to the parameter map. Note that this returns a reference to the live,
    * modifiable parameter map. As a result it can be used to insert parameters when constructing
    * the request.
    */
   @Override
   public Map<String, String[]> getParameterMap() {
      return _parameters;
   }

   /** Gets an enumeration containing all the parameter names present. */
   @Override
   public Enumeration<String> getParameterNames() {
      return Collections.enumeration(_parameters.keySet());
   }

   /** Returns an array of all values for a parameter, or null if the parameter does not exist. */
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
      return null;
   }

   /** Returns the path info. Defaults to the empty string. */
   @Override
   public String getPathInfo() { return _pathInfo; }

   /** Always returns the same as getPathInfo(). */
   @Override
   public String getPathTranslated() { return getPathInfo(); }

   /** Gets the protocol for the request. Defaults to "https". */
   @Override
   public String getProtocol() { return _protocol; }

   /** Returns the query string set on the request. */
   @Override
   public String getQueryString() { return _queryString; }

   /** Always returns null. */
   @Override
   public BufferedReader getReader() throws IOException { return null; }

   /** Always returns the path passed in without any alteration. */
   @Override
   public String getRealPath( String path ) { return path; }

   /** Aways returns "127.0.0.1". */
   @Override
   public String getRemoteAddr() { return "127.0.0.1"; }

   /** Always returns "localhost". */
   @Override
   public String getRemoteHost() { return "localhost"; }

   /** Always returns 1088 (and yes, that was picked arbitrarily). */
   @Override
   public int getRemotePort() { return 1088; }

   /** Returns the name from the user principal if one exists, otherwise null. */
   @Override
   public String getRemoteUser() {
      Principal p = getUserPrincipal();
      return p == null ? null : p.getName();
   }

   /**
    * Returns an instance of MockRequestDispatcher that just records what URLs are forwarded
    * to or included. The results can be examined later by calling getForwardUrl() and
    * getIncludedUrls().
    */
   @Override
   public MockRequestDispatcher getRequestDispatcher( String url ) {
      return new MockRequestDispatcher(url);
   }

   /** Returns the request URI as defined by the servlet spec. */
   @Override
   public String getRequestURI() { return _contextPath + _servletPath + _pathInfo; }

   /** Returns (an attempt at) a reconstructed URL based on it's constituent parts. */
   @Override
   public StringBuffer getRequestURL() {
      return new StringBuffer().append(_protocol)
            .append("://")
            .append(_serverName)
            .append(":")
            .append(_serverPort)
            .append(_contextPath)
            .append(_servletPath)
            .append(_pathInfo);
   }

   /** Returns the ID of the session if one is attached to this request. Otherwise null. */
   @Override
   public String getRequestedSessionId() {
      if ( _session == null ) {
         return null;
      }
      return _session.getId();
   }

   /** Always returns the same as getProtocol. */
   @Override
   public String getScheme() { return getProtocol(); }

   /** Gets the server name. Defaults to "localhost". */
   @Override
   public String getServerName() { return _serverName; }

   /** Returns the server port. Defaults to 8080. */
   @Override
   public int getServerPort() { return _serverPort; }

   @Override
   public ServletContext getServletContext() {
      return null;
   }

   /** Gets the part of the path which matched the servlet. */
   @Override
   public String getServletPath() { return _servletPath; }

   /** Gets the session object attached to this request. */
   @Override
   public HttpSession getSession( boolean b ) { return _session; }

   /** Gets the session object attached to this request. */
   @Override
   public HttpSession getSession() { return _session; }

   /** Returns the Principal if one is set on the request. */
   @Override
   public Principal getUserPrincipal() { return _userPrincipal; }

   @Override
   public boolean isAsyncStarted() {
      return false;
   }

   @Override
   public boolean isAsyncSupported() {
      return false;
   }

   /** Always returns true. */
   @Override
   public boolean isRequestedSessionIdFromCookie() { return true; }

   /** Always returns false. */
   @Override
   public boolean isRequestedSessionIdFromURL() { return false; }

   /** Always returns false. */
   @Override
   public boolean isRequestedSessionIdFromUrl() { return false; }

   /** Always returns true. */
   @Override
   public boolean isRequestedSessionIdValid() { return true; }

   /** Returns true if the protocol is set to https (default), false otherwise. */
   @Override
   public boolean isSecure() {
      return _protocol.equalsIgnoreCase("https");
   }

   /** Returns true if the set of roles contains the role specified, false otherwise. */
   @Override
   public boolean isUserInRole( String role ) {
      return _roles.contains(role);
   }

   @Override
   public void login( String s, String s1 ) throws ServletException {

   }

   @Override
   public void logout() throws ServletException {

   }

   /** Removes any value for the named request attribute. */
   @Override
   public void removeAttribute( String name ) { _attributes.remove(name); }

   /** Sets the supplied value for the named request attribute. */
   @Override
   public void setAttribute( String name, Object value ) {
      _attributes.put(name, value);
   }

   /** Sets the auth type that will be reported by this request. */
   public void setAuthType( String authType ) { _authType = authType; }

   /** Sets the character encoding that will be returned by getCharacterEncoding(). */
   @Override
   public void setCharacterEncoding( String encoding ) { _characterEncoding = encoding; }

   /** Sets the context path. Defaults to the empty string. */
   public void setContextPath( String contextPath ) { _contextPath = contextPath; }

   /** Sets the array of cookies that will be available from the request. */
   public void setCookies( Cookie[] cookies ) { _cookies = cookies; }

   /** Sets the method used by the request. Defaults to POST. */
   public void setMethod( String method ) { _method = method; }

   /** Sets the path info. Defaults to the empty string. */
   public void setPathInfo( String pathInfo ) { _pathInfo = pathInfo; }

   /** Sets the protocol for the request. Defaults to "https". */
   public void setProtocol( String protocol ) { _protocol = protocol; }

   /** Sets the query string set on the request; this value is not parsed for anything. */
   public void setQueryString( String queryString ) { _queryString = queryString; }

   /** Sets the set of roles that the user is deemed to be in for the request. */
   public void setRoles( Set<String> roles ) { _roles = roles; }

   /** Sets the server name. Defaults to "localhost". */
   public void setServerName( String serverName ) { _serverName = serverName; }

   /** Sets the server port. Defaults to 8080. */
   public void setServerPort( int serverPort ) { _serverPort = serverPort; }

   /** Allows a session to be associated with the request. */
   public void setSession( HttpSession session ) { _session = session; }

   /** Sets the Principal for the current request. */
   public void setUserPrincipal( Principal userPrincipal ) { _userPrincipal = userPrincipal; }

   @Override
   public AsyncContext startAsync() throws IllegalStateException {
      return null;
   }

   @Override
   public AsyncContext startAsync( ServletRequest servletRequest, ServletResponse servletResponse ) throws IllegalStateException {
      return null;
   }

   @Override
   public <T extends HttpUpgradeHandler> T upgrade( Class<T> aClass ) throws IOException, ServletException {
      return null;
   }

   /** Used by the request dispatcher to record that a URL was included. */
   void addIncludedUrl( String url ) { _includedUrls.add(url); }

   /** Used by the request dispatcher to set the forward URL when a forward is invoked. */
   void setForwardUrl( String url ) { _forwardUrl = url; }
}
