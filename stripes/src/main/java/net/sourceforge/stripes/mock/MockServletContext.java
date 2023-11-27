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

import jakarta.servlet.Filter;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.SessionTrackingMode;
import jakarta.servlet.descriptor.JspConfigDescriptor;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Mock implementation of a ServletContext. Provides implementation the most commonly used methods,
 * namely those to manipulate init parameters and attributes. Additional methods are provided to
 * allow the setting of initialization parameters etc.
 *
 * <p>This mock implementation is meant only for testing purposes. As such there are certain
 * limitations:
 *
 * <ul>
 *   <li>All configured Filters are applied to every request
 *   <li>Only a single servlet is supported, and all requests are routed to it.
 *   <li>Forwards, includes and redirects are recorded for posterity, but not processed.
 *   <li>It may or may not be thread safe (not a priority since it is mainly for unit testing).
 *   <li>You do your own session management (attach one to a request before executing).
 * </ul>
 *
 * @author Tim Fennell
 * @since Stripes 1.1.1
 */
public class MockServletContext implements ServletContext {
  private final String contextName;
  private final Map<String, String> initParameters = new HashMap<>();
  private final Map<String, Object> attributes = new HashMap<>();
  private final List<Filter> filters = new ArrayList<>();
  private final List<ServletContextListener> listeners = new ArrayList<>();
  private HttpServlet servlet;

  /** Simple constructor that creates a new mock ServletContext with the supplied context name. */
  public MockServletContext(String contextName) {
    this.contextName = contextName;
  }

  /** If the url is within this servlet context, returns this. Otherwise, returns null. */
  public ServletContext getContext(String url) {
    if (url.startsWith("/" + this.contextName)) {
      return this;
    } else {
      return null;
    }
  }

  /** Servlet 2.3 method. Returns the context name with a leading slash. */
  public String getContextPath() {
    return "/" + this.contextName;
  }

  /** Always returns 2. */
  public int getMajorVersion() {
    return 4;
  }

  /** Always returns 4. */
  public int getMinorVersion() {
    return 0;
  }

  @Override
  public int getEffectiveMajorVersion() {
    return 0;
  }

  @Override
  public int getEffectiveMinorVersion() {
    return 0;
  }

  /** Always returns null (i.e. don't know). */
  public String getMimeType(String file) {
    return null;
  }

  /** Always returns null (i.e. there are no resources under this path). */
  public Set<String> getResourcePaths(String path) {
    return null;
  }

  /** Uses the current classloader to fetch the resource if it can. */
  public URL getResource(String name) {
    while (name.startsWith("/")) name = name.substring(1);
    return Thread.currentThread().getContextClassLoader().getResource(name);
  }

  /** Uses the current classloader to fetch the resource if it can. */
  public InputStream getResourceAsStream(String name) {
    while (name.startsWith("/")) name = name.substring(1);
    return Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
  }

  /** Returns a MockRequestDispatcher for the url provided. */
  public RequestDispatcher getRequestDispatcher(String url) {
    return new MockRequestDispatcher(url);
  }

  /** Returns a MockRequestDispatcher for the named servlet provided. */
  public RequestDispatcher getNamedDispatcher(String name) {
    return new MockRequestDispatcher(name);
  }

  /** Deprecated method always returns null. */
  public Servlet getServlet(String string) {
    return null;
  }

  /** Deprecated method always returns an empty enumeration. */
  public Enumeration<?> getServlets() {
    return Collections.enumeration(Collections.emptySet());
  }

  /** Deprecated method always returns an empty enumeration. */
  public Enumeration<?> getServletNames() {
    return Collections.enumeration(Collections.emptySet());
  }

  /** Logs the message to System.out. */
  public void log(String message) {
    System.out.println("MockServletContext: " + message);
  }

  /** Logs the message and exception to System.out. */
  public void log(Exception exception, String message) {
    log(message, exception);
  }

  /** Logs the message and exception to System.out. */
  public void log(String message, Throwable throwable) {
    log(message);
    throwable.printStackTrace(System.out);
  }

  /** Always returns null as this is standard behaviour for WAR resources. */
  public String getRealPath(String string) {
    return null;
  }

  /** Returns a version string identifying the Mock implementation. */
  public String getServerInfo() {
    return "Stripes Mock Servlet Environment, version 1.0.";
  }

  /** Adds an init parameter to the mock servlet context. */
  public void addInitParameter(String name, String value) {
    this.initParameters.put(name, value);
  }

  /** Adds all the values in the supplied Map to the set of init parameters. */
  public void addAllInitParameters(Map<String, String> parameters) {
    this.initParameters.putAll(parameters);
  }

  /** Gets the value of an init parameter with the specified name, if one exists. */
  public String getInitParameter(String name) {
    return this.initParameters.get(name);
  }

  /** Returns an enumeration of all the initialization parameters in the context. */
  public Enumeration<String> getInitParameterNames() {
    return Collections.enumeration(this.initParameters.keySet());
  }

  @Override
  public boolean setInitParameter(String name, String value) {
    return false;
  }

  /** Gets an attribute that has been set on the context (i.e. application) scope. */
  public Object getAttribute(String name) {
    return this.attributes.get(name);
  }

  /** Returns an enumeration of all the names of attributes in the context. */
  public Enumeration<String> getAttributeNames() {
    return Collections.enumeration(this.attributes.keySet());
  }

  /** Sets the supplied value for the attribute on the context. */
  public void setAttribute(String name, Object value) {
    this.attributes.put(name, value);
  }

  /** Removes the named attribute from the context. */
  public void removeAttribute(String name) {
    this.attributes.remove(name);
  }

  /** Returns the name of the mock context. */
  public String getServletContextName() {
    return this.contextName;
  }

  @Override
  public ServletRegistration.Dynamic addServlet(String servletName, String className) {
    return null;
  }

  @Override
  public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
    return null;
  }

  @Override
  public ServletRegistration.Dynamic addServlet(
      String servletName, Class<? extends Servlet> servletClass) {
    return null;
  }

  @Override
  public ServletRegistration.Dynamic addJspFile(String servletName, String jspFile) {
    return null;
  }

  @Override
  public <T extends Servlet> T createServlet(Class<T> clazz) {
    return null;
  }

  @Override
  public ServletRegistration getServletRegistration(String servletName) {
    return null;
  }

  @Override
  public Map<String, ? extends ServletRegistration> getServletRegistrations() {
    return null;
  }

  @Override
  public FilterRegistration.Dynamic addFilter(String filterName, String className) {
    return null;
  }

  @Override
  public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
    return null;
  }

  @Override
  public FilterRegistration.Dynamic addFilter(
      String filterName, Class<? extends Filter> filterClass) {
    return null;
  }

  @Override
  public <T extends Filter> T createFilter(Class<T> clazz) {
    return null;
  }

  @Override
  public FilterRegistration getFilterRegistration(String filterName) {
    return null;
  }

  @Override
  public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
    return null;
  }

  @Override
  public SessionCookieConfig getSessionCookieConfig() {
    return null;
  }

  @Override
  public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {}

  @Override
  public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
    return null;
  }

  @Override
  public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
    return null;
  }

  @Override
  public void addListener(String className) {}

  @Override
  public <T extends EventListener> void addListener(T t) {}

  @Override
  public void addListener(Class<? extends EventListener> listenerClass) {}

  @Override
  public <T extends EventListener> T createListener(Class<T> clazz) {
    return null;
  }

  @Override
  public JspConfigDescriptor getJspConfigDescriptor() {
    return null;
  }

  @Override
  public ClassLoader getClassLoader() {
    return null;
  }

  @Override
  public void declareRoles(String... roleNames) {}

  @Override
  public String getVirtualServerName() {
    return null;
  }

  @Override
  public int getSessionTimeout() {
    return 0;
  }

  @Override
  public void setSessionTimeout(int sessionTimeout) {}

  @Override
  public String getRequestCharacterEncoding() {
    return null;
  }

  @Override
  public void setRequestCharacterEncoding(String encoding) {}

  @Override
  public String getResponseCharacterEncoding() {
    return null;
  }

  @Override
  public void setResponseCharacterEncoding(String encoding) {}

  /** Adds a filter to the end of filter chain that will be used to filter requests. */
  public MockServletContext addFilter(
      Class<? extends Filter> filterClass, String filterName, Map<String, String> initParams) {
    try {
      MockFilterConfig config = new MockFilterConfig();
      config.setFilterName(filterName);
      config.setServletContext(this);
      if (initParams != null) config.addAllInitParameters(initParams);

      Filter filter = filterClass.getDeclaredConstructor().newInstance();
      filter.init(config);
      this.filters.add(filter);
      return this;
    } catch (Exception e) {
      throw new RuntimeException("Exception registering new filter with name " + filterName, e);
    }
  }

  /** Removes and destroys all registered filters. */
  public MockServletContext removeFilters() {
    for (Filter each : filters) {
      try {
        each.destroy();
      } catch (Exception e) {
        log("Error while destroying filter " + each, e);
      }
    }
    filters.clear();
    return this;
  }

  /** Provides access to the set of filters configured for this context. */
  public List<Filter> getFilters() {
    return this.filters;
  }

  /** Adds a {@link ServletContextListener} to this context and initializes it. */
  public MockServletContext addListener(ServletContextListener listener) {
    ServletContextEvent event = new ServletContextEvent(this);
    listener.contextInitialized(event);
    listeners.add(listener);
    return this;
  }

  /** Removes and destroys all registered {@link ServletContextListener}. */
  public MockServletContext removeListeners() {
    ServletContextEvent e = new ServletContextEvent(this);
    for (ServletContextListener l : listeners) {
      l.contextDestroyed(e);
    }
    listeners.clear();
    return this;
  }

  /** Sets the servlet that will receive all requests in this servlet context. */
  public MockServletContext setServlet(
      Class<? extends HttpServlet> servletClass,
      String servletName,
      Map<String, String> initParams) {
    try {
      MockServletConfig config = new MockServletConfig();
      config.setServletName(servletName);
      config.setServletContext(this);
      if (initParams != null) config.addAllInitParameters(initParams);

      this.servlet = servletClass.getDeclaredConstructor().newInstance();
      this.servlet.init(config);
      return this;
    } catch (Exception e) {
      throw new RuntimeException("Exception registering servlet with name " + servletName, e);
    }
  }

  /**
   * Takes a request and response and runs them through the set of filters using a MockFilterChain,
   * which, if everything goes well, will eventually execute the servlet that is registered with
   * this context.
   *
   * <p>Any exceptions that are raised during the processing of the request are simply passed
   * through to the caller. I.e. they will be thrown from this method.
   */
  public void acceptRequest(MockHttpServletRequest request, MockHttpServletResponse response)
      throws Exception {
    copyCookies(request, response);
    MockFilterChain chain = new MockFilterChain();
    chain.setServlet(this.servlet);
    chain.addFilters(this.filters);
    chain.doFilter(request, response);
  }

  /**
   * Copies cookies from the request to the response.
   *
   * @param request The request.
   * @param response The response.
   */
  public void copyCookies(MockHttpServletRequest request, MockHttpServletResponse response) {
    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        response.addCookie(cookie);
      }
    }
  }

  /** Closes all filters and servlets for this context (application shutdown). */
  public void close() {
    removeListeners();
    removeFilters();
    Enumeration<?> servlets = getServlets();
    while (servlets.hasMoreElements()) {
      Object servlet = servlets.nextElement();
      if (servlet instanceof Servlet) {
        try {
          ((Servlet) servlet).destroy();
        } catch (Exception e) {
          log("Exception caught destroying servlet " + servlet + " contextName=" + contextName, e);
        }
      }
    }
  }
}
