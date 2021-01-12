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

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;
import javax.servlet.http.HttpServlet;


/**
 * <p>Mock implementation of a ServletContext.  Provides implementation the most commonly used
 * methods, namely those to manipulate init parameters and attributes.  Additional methods are
 * provided to allow the setting of initialization parameters etc.</p>
 *
 * <p>This mock implementation is meant only for testing purposes. As such there are certain
 * limitations:</p>
 *
 * <ul>
 *   <li>All configured Filters are applied to every request</li>
 *   <li>Only a single servlet is supported, and all requests are routed to it.</li>
 *   <li>Forwards, includes and redirects are recorded for posterity, but not processed.</li>
 *   <li>It may or may not be thread safe (not a priority since it is mainly for unit testing).</li>
 *   <li>You do your own session management (attach one to a request before executing).</li>
 * </ul>
 *
 * @author Tim Fennell
 * @since Stripes 1.1.1
 */
public class MockServletContext implements ServletContext {

   private final String                       _contextName;
   private final Map<String, String>          _initParameters = new HashMap<>();
   private final Map<String, Object>          _attributes     = new HashMap<>();
   private final List<Filter>                 _filters        = new ArrayList<>();
   private final List<ServletContextListener> _listeners      = new ArrayList<>();
   private       HttpServlet                  _servlet;

   /** Simple constructor that creates a new mock ServletContext with the supplied context name. */
   public MockServletContext( String contextName ) {
      _contextName = contextName;
   }

   /**
    * <p>Takes a request and response and runs them through the set of filters using a
    * MockFilterChain, which if everything goes well, will eventually execute the servlet
    * that is registered with this context.</p>
    *
    * <p>Any exceptions that are raised during the processing of the request are  simply
    * passed through to the caller. I.e. they will be thrown from this method.</p>
    */
   public void acceptRequest( MockHttpServletRequest request, MockHttpServletResponse response ) throws Exception {
      MockFilterChain chain = new MockFilterChain();
      chain.setServlet(_servlet);
      chain.addFilters(_filters);
      chain.doFilter(request, response);
   }

   /** Adds all the values in the supplied Map to the set of init parameters. */
   public void addAllInitParameters( Map<String, String> parameters ) {
      _initParameters.putAll(parameters);
   }

   /** Adds a filter to the end of filter chain that will be used to filter requests.*/
   public MockServletContext addFilter( Class<? extends Filter> filterClass, String filterName, Map<String, String> initParams ) {
      try {
         MockFilterConfig config = new MockFilterConfig();
         config.setFilterName(filterName);
         config.setServletContext(this);
         if ( initParams != null ) {
            config.addAllInitParameters(initParams);
         }

         Filter filter = filterClass.getDeclaredConstructor().newInstance();
         filter.init(config);
         _filters.add(filter);
         return this;
      }
      catch ( Exception e ) {
         throw new RuntimeException("Exception registering new filter with name " + filterName, e);
      }
   }

   @Override
   public FilterRegistration.Dynamic addFilter( String s, String s1 ) {
      return null;
   }

   @Override
   public FilterRegistration.Dynamic addFilter( String s, Filter filter ) {
      return null;
   }

   @Override
   public FilterRegistration.Dynamic addFilter( String s, Class<? extends Filter> aClass ) {
      return null;
   }

   /** Adds an init parameter to the mock servlet context. */
   public void addInitParameter( String name, String value ) {
      _initParameters.put(name, value);
   }

   /** Adds a {@link ServletContextListener} to this context and initializes it. */
   public MockServletContext addListener( ServletContextListener listener ) {
      ServletContextEvent event = new ServletContextEvent(this);
      listener.contextInitialized(event);
      _listeners.add(listener);
      return this;
   }

   @Override
   public void addListener( String s ) {

   }

   @Override
   public <T extends EventListener> void addListener( T t ) {

   }

   @Override
   public void addListener( Class<? extends EventListener> aClass ) {

   }

   @Override
   public ServletRegistration.Dynamic addServlet( String s, String s1 ) {
      return null;
   }

   @Override
   public ServletRegistration.Dynamic addServlet( String s, Servlet servlet ) {
      return null;
   }

   @Override
   public ServletRegistration.Dynamic addServlet( String s, Class<? extends Servlet> aClass ) {
      return null;
   }

   /**
    * Closes all filters and servlets for this context (application shutdown).
    */
   public void close() {
      removeListeners();
      removeFilters();
      Enumeration<?> servlets = getServlets();
      while ( servlets.hasMoreElements() ) {
         Object servlet = servlets.nextElement();
         if ( servlet instanceof Servlet ) {
            try {
               ((Servlet)servlet).destroy();
            }
            catch ( Exception e ) {
               log("Exception caught destroying servlet " + servlet + " contextName=" + _contextName, e);
            }
         }
      }
   }

   @Override
   public <T extends Filter> T createFilter( Class<T> aClass ) throws ServletException {
      return null;
   }

   @Override
   public <T extends EventListener> T createListener( Class<T> aClass ) throws ServletException {
      return null;
   }

   @Override
   public <T extends Servlet> T createServlet( Class<T> aClass ) throws ServletException {
      return null;
   }

   @Override
   public void declareRoles( String... strings ) {

   }

   /** Gets an attribute that has been set on the context (i.e. application) scope. */
   @Override
   public Object getAttribute( String name ) {
      return _attributes.get(name);
   }

   /** Returns an enumeration of all the names of attributes in the context. */
   @Override
   public Enumeration<String> getAttributeNames() {
      return Collections.enumeration(_attributes.keySet());
   }

   @Override
   public ClassLoader getClassLoader() {
      return null;
   }

   /** If the url is within this servlet context, returns this. Otherwise returns null. */
   @Override
   public ServletContext getContext( String url ) {
      if ( url.startsWith("/" + _contextName) ) {
         return this;
      } else {
         return null;
      }
   }

   /** Servlet 2.3 method. Returns the context name with a leading slash. */
   @Override
   public String getContextPath() {
      return "/" + _contextName;
   }

   @Override
   public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
      return null;
   }

   @Override
   public int getEffectiveMajorVersion() {
      return getMajorVersion();
   }

   @Override
   public int getEffectiveMinorVersion() {
      return getEffectiveMajorVersion();
   }

   @Override
   public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
      return null;
   }

   @Override
   public FilterRegistration getFilterRegistration( String s ) {
      return null;
   }

   @Override
   public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
      return null;
   }

   /** Provides access to the set of filters configured for this context. */
   public List<Filter> getFilters() {
      return _filters;
   }

   /** Gets the value of an init parameter with the specified name, if one exists. */
   @Override
   public String getInitParameter( String name ) {
      return _initParameters.get(name);
   }

   /** Returns an enumeration of all the initialization parameters in the context. */
   @Override
   public Enumeration<String> getInitParameterNames() {
      return Collections.enumeration(_initParameters.keySet());
   }

   @Override
   public JspConfigDescriptor getJspConfigDescriptor() {
      return null;
   }

   @Override
   public int getMajorVersion() { return 3; }

   /** Always returns null (i.e. don't know). */
   @Override
   public String getMimeType( String file ) { return null; }

   @Override
   public int getMinorVersion() { return 1; }

   /** Returns a MockRequestDispatcher for the named servlet provided. */
   @Override
   public RequestDispatcher getNamedDispatcher( String name ) {
      return new MockRequestDispatcher(name);
   }

   /** Always returns null as this is standard behaviour for WAR resources. */
   @Override
   public String getRealPath( String string ) { return null; }

   /** Returns a MockRequestDispatcher for the url provided. */
   @Override
   public RequestDispatcher getRequestDispatcher( String url ) {
      return new MockRequestDispatcher(url);
   }

   /** Uses the current classloader to fetch the resource if it can. */
   @Override
   public URL getResource( String name ) throws MalformedURLException {
      while ( name.startsWith("/") ) {
         name = name.substring(1);
      }
      return Thread.currentThread().getContextClassLoader().getResource(name);
   }

   /** Uses the current classloader to fetch the resource if it can. */
   @Override
   public InputStream getResourceAsStream( String name ) {
      while ( name.startsWith("/") ) {
         name = name.substring(1);
      }
      return Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
   }

   /** Always returns null (i.e. there are no resources under this path). */
   @Override
   public Set<String> getResourcePaths( String path ) {
      return null;
   }

   /** Returns a version string identifying the Mock implementation. */
   @Override
   public String getServerInfo() {
      return "Stripes Mock Servlet Environment, version 1.0.";
   }

   /** Deprecated method always returns null. */
   @Override
   public Servlet getServlet( String string ) throws ServletException { return null; }

   /** Returns the name of the mock context. */
   @Override
   public String getServletContextName() {
      return _contextName;
   }

   /** Deprecated method always returns an empty enumeration. */
   @Override
   public Enumeration<String> getServletNames() {
      return Collections.enumeration(Collections.emptySet());
   }

   @Override
   public ServletRegistration getServletRegistration( String s ) {
      return null;
   }

   @Override
   public Map<String, ? extends ServletRegistration> getServletRegistrations() {
      return null;
   }

   /** Deprecated method always returns an empty enumeration. */
   @Override
   public Enumeration<Servlet> getServlets() {
      return Collections.enumeration(Collections.emptySet());
   }

   @Override
   public SessionCookieConfig getSessionCookieConfig() {
      return null;
   }

   @Override
   public String getVirtualServerName() {
      return null;
   }

   /** Logs the message to System.out. */
   @Override
   public void log( String message ) {
      System.out.println("MockServletContext: " + message);
   }

   /** Logs the message and exception to System.out. */
   @Override
   public void log( Exception exception, String message ) {
      log(message, exception);
   }

   /** Logs the message and exception to System.out. */
   @Override
   public void log( String message, Throwable throwable ) {
      log(message);
      throwable.printStackTrace(System.out);
   }

   /** Removes the named attribute from the context. */
   @Override
   public void removeAttribute( String name ) {
      _attributes.remove(name);
   }

   /** Removes and destroys all registered filters. */
   public MockServletContext removeFilters() {
      for ( Filter each : _filters ) {
         try {
            each.destroy();
         }
         catch ( Exception e ) {
            log("Error while destroying filter " + each, e);
         }
      }
      _filters.clear();
      return this;
   }

   /**Removes and destroys all registered {@link ServletContextListener}. */
   public MockServletContext removeListeners() {
      ServletContextEvent e = new ServletContextEvent(this);
      for ( ServletContextListener l : _listeners ) {
         l.contextDestroyed(e);
      }
      _listeners.clear();
      return this;
   }

   /** Sets the supplied value for the attribute on the context. */
   @Override
   public void setAttribute( String name, Object value ) {
      _attributes.put(name, value);
   }

   @Override
   public boolean setInitParameter( String s, String s1 ) {
      return false;
   }

   /** Sets the servlet that will receive all requests in this servlet context. */
   public MockServletContext setServlet( Class<? extends HttpServlet> servletClass, String servletName, Map<String, String> initParams ) {
      try {
         MockServletConfig config = new MockServletConfig();
         config.setServletName(servletName);
         config.setServletContext(this);
         if ( initParams != null ) {
            config.addAllInitParameters(initParams);
         }

         _servlet = servletClass.getDeclaredConstructor().newInstance();
         _servlet.init(config);
         return this;
      }
      catch ( Exception e ) {
         throw new RuntimeException("Exception registering servlet with name " + servletName, e);
      }
   }

   @Override
   public void setSessionTrackingModes( Set<SessionTrackingMode> set ) {

   }
}
