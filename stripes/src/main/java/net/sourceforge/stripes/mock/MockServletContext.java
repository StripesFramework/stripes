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
import jakarta.servlet.descriptor.JspConfigDescriptor;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Mock implementation of a ServletContext. Provides implementation the most
 * commonly used methods, namely those to manipulate init parameters and
 * attributes. Additional methods are provided to allow the setting of
 * initialization parameters etc.</p>
 *
 * <p>
 * This mock implementation is meant only for testing purposes. As such there
 * are certain limitations:</p>
 *
 * <ul>
 * <li>All configured Filters are applied to every request</li>
 * <li>Only a single servlet is supported, and all requests are routed to
 * it.</li>
 * <li>Forwards, includes and redirects are recorded for posterity, but not
 * processed.</li>
 * <li>It may or may not be thread safe (not a priority since it is mainly for
 * unit testing).</li>
 * <li>You do your own session management (attach one to a request before
 * executing).</li>
 * </ul>
 *
 * @author Tim Fennell
 * @since Stripes 1.1.1
 */
public class MockServletContext implements ServletContext {

    private String contextName;
    private Map<String, String> initParameters = new HashMap<String, String>();
    private Map<String, Object> attributes = new HashMap<String, Object>();
    private List<Filter> filters = new ArrayList<Filter>();
    private List<ServletContextListener> listeners = new ArrayList<ServletContextListener>();
    private HttpServlet servlet;

    /**
     * Simple constructor that creates a new mock ServletContext with the
     * supplied context name.
     * @param contextName
     */
    public MockServletContext(String contextName) {
        this.contextName = contextName;
    }

    /**
     * If the url is within this servlet context, returns this. Otherwise
     * returns null.
     * @param url
     * @return
     */
    public ServletContext getContext(String url) {
        if (url.startsWith("/" + this.contextName)) {
            return this;
        } else {
            return null;
        }
    }

    /**
     * Servlet 2.3 method. Returns the context name with a leading slash.
     * @return
     */
    public String getContextPath() {
        return "/" + this.contextName;
    }

    /**
     * Always returns 2.
     * @return
     */
    public int getMajorVersion() {
        return 2;
    }

    /**
     * Always returns 4.
     * @return
     */
    public int getMinorVersion() {
        return 4;
    }

    /**
     * Always returns null (i.e. don't know).
     * @param file
     * @return
     */
    public String getMimeType(String file) {
        return null;
    }

    /**
     * Always returns null (i.e. there are no resources under this path).
     * @param path
     * @return
     */
    public Set<String> getResourcePaths(String path) {
        return null;
    }

    /**
     * Uses the current classloader to fetch the resource if it can.
     * @param name
     * @return
     * @throws java.net.MalformedURLException
     */
    public URL getResource(String name) throws MalformedURLException {
        while (name.startsWith("/")) {
            name = name.substring(1);
        }
        return Thread.currentThread().getContextClassLoader().getResource(name);
    }

    /**
     * Uses the current classloader to fetch the resource if it can.
     * @param name
     * @return
     */
    public InputStream getResourceAsStream(String name) {
        while (name.startsWith("/")) {
            name = name.substring(1);
        }
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
    }

    /**
     * Returns a MockRequestDispatcher for the url provided.
     * @param url
     * @return
     */
    public RequestDispatcher getRequestDispatcher(String url) {
        return new MockRequestDispatcher(url);
    }

    /**
     * Returns a MockRequestDispatcher for the named servlet provided.
     * @param name
     * @return
     */
    public RequestDispatcher getNamedDispatcher(String name) {
        return new MockRequestDispatcher(name);
    }

    /**
     * Deprecated method always returns null.
     * @param string
     * @return
     * @throws jakarta.servlet.ServletException
     */
    public Servlet getServlet(String string) throws ServletException {
        return null;
    }

    /**
     * Deprecated method always returns an empty enumeration.
     * @return
     */
    public Enumeration<Servlet> getServlets() {
        return Collections.enumeration(Collections.<Servlet>emptySet());
    }

    /**
     * Deprecated method always returns an empty enumeration.
     * @return
     */
    public Enumeration<String> getServletNames() {
        return Collections.enumeration(Collections.<String>emptySet());
    }

    /**
     * Logs the message to System.out.
     * @param message
     */
    public void log(String message) {
        System.out.println("MockServletContext: " + message);
    }

    /**
     * Logs the message and exception to System.out.
     * @param exception
     * @param message
     */
    public void log(Exception exception, String message) {
        log(message, exception);
    }

    /**
     * Logs the message and exception to System.out.
     * @param message
     * @param throwable
     */
    public void log(String message, Throwable throwable) {
        log(message);
        throwable.printStackTrace(System.out);
    }

    /**
     * Always returns null as this is standard behaviour for WAR resources.
     * @param string
     * @return
     */
    public String getRealPath(String string) {
        return null;
    }

    /**
     * Returns a version string identifying the Mock implementation.
     * @return
     */
    public String getServerInfo() {
        return "Stripes Mock Servlet Environment, version 1.0.";
    }

    /**
     * Adds an init parameter to the mock servlet context.
     * @param name
     * @param value
     */
    public void addInitParameter(String name, String value) {
        this.initParameters.put(name, value);
    }

    /**
     * Adds all the values in the supplied Map to the set of init parameters.
     * @param parameters
     */
    public void addAllInitParameters(Map<String, String> parameters) {
        this.initParameters.putAll(parameters);
    }

    /**
     * Gets the value of an init parameter with the specified name, if one
     * exists.
     * @param name
     * @return
     */
    public String getInitParameter(String name) {
        return this.initParameters.get(name);
    }

    /**
     * Returns an enumeration of all the initialization parameters in the
     * context.
     * @return
     */
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(this.initParameters.keySet());
    }

    /**
     * Gets an attribute that has been set on the context (i.e. application)
     * scope.
     * @param name
     * @return
     */
    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    /**
     * Returns an enumeration of all the names of attributes in the context.
     * @return
     */
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(this.attributes.keySet());
    }

    /**
     * Sets the supplied value for the attribute on the context.
     * @param name
     * @param value
     */
    public void setAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }

    /**
     * Removes the named attribute from the context.
     * @param name
     */
    public void removeAttribute(String name) {
        this.attributes.remove(name);
    }

    /**
     * Returns the name of the mock context.
     * @return
     */
    public String getServletContextName() {
        return this.contextName;
    }

    /**
     * Adds a filter to the end of filter chain that will be used to filter requests.
     * @param filterClass
     * @param filterName
     * @param initParams
     * @return
     */
    public MockServletContext addFilter(Class<? extends Filter> filterClass,
            String filterName,
            Map<String, String> initParams) {
        try {
            MockFilterConfig config = new MockFilterConfig();
            config.setFilterName(filterName);
            config.setServletContext(this);
            if (initParams != null) {
                config.addAllInitParameters(initParams);
            }

            Filter filter = filterClass.newInstance();
            filter.init(config);
            this.filters.add(filter);
            return this;
        } catch (Exception e) {
            throw new RuntimeException("Exception registering new filter with name " + filterName, e);
        }
    }

    /**
     * Removes and destroys all registered filters.
     * @return
     */
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

    /**
     * Provides access to the set of filters configured for this context.
     * @return
     */
    public List<Filter> getFilters() {
        return this.filters;
    }

    /**
     * Adds a {@link ServletContextListener} to this context and initializes it.
     * @param listener
     * @return
     */
    public MockServletContext addListener(ServletContextListener listener) {
        ServletContextEvent event = new ServletContextEvent(this);
        listener.contextInitialized(event);
        listeners.add(listener);
        return this;
    }

    /**
     * Removes and destroys all registered {@link ServletContextListener}.
     * @return
     */
    public MockServletContext removeListeners() {
        ServletContextEvent e = new ServletContextEvent(this);
        for (ServletContextListener l : listeners) {
            l.contextDestroyed(e);
        }
        listeners.clear();
        return this;
    }

    /**
     * Sets the servlet that will receive all requests in this servlet context.
     * @param servletClass
     * @param servletName
     * @param initParams
     * @return
     */
    public MockServletContext setServlet(Class<? extends HttpServlet> servletClass,
            String servletName,
            Map<String, String> initParams) {
        try {
            MockServletConfig config = new MockServletConfig();
            config.setServletName(servletName);
            config.setServletContext(this);
            if (initParams != null) {
                config.addAllInitParameters(initParams);
            }

            this.servlet = servletClass.newInstance();
            this.servlet.init(config);
            return this;
        } catch (Exception e) {
            throw new RuntimeException("Exception registering servlet with name " + servletName, e);
        }
    }

    /**
     * <p>
     * Takes a request and response and runs them through the set of filters
     * using a MockFilterChain, which if everything goes well, will eventually
     * execute the servlet that is registered with this context.</p>
     *
     * <p>
     * Any exceptions that are raised during the processing of the request are
     * simply passed through to the caller. I.e. they will be thrown from this
     * method.</p>
     * @param request
     * @param response
     * @throws java.lang.Exception
     */
    public void acceptRequest(MockHttpServletRequest request, MockHttpServletResponse response)
            throws Exception {
        copyCookies(request, response);
        MockFilterChain chain = new MockFilterChain();
        chain.setServlet(this.servlet);
        chain.addFilters(this.filters);
        chain.doFilter(request, response);
        // wait for any async context to finish (block)
        if (request.isAsyncStarted()) {
            MockAsyncContext asyncContext = (MockAsyncContext) request.getAsyncContext();
            asyncContext.waitForCompletion();
        }

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

    /**
     * Closes all filters and servlets for this context (application shutdown).
     */
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

    /**
     *
     * @return
     */
    public int getEffectiveMajorVersion() {
        return 0;
    }

    /**
     *
     * @return
     */
    public int getEffectiveMinorVersion() {
        return 0;
    }

    /**
     *
     * @param name
     * @param value
     * @return
     */
    public boolean setInitParameter(String name, String value) {
        return false;
    }

    /**
     *
     * @param servletName
     * @param className
     * @return
     */
    public ServletRegistration.Dynamic addServlet(String servletName, String className) {
        return null;
    }

    /**
     *
     * @param servletName
     * @param servlet
     * @return
     */
    public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
        return null;
    }

    /**
     *
     * @param servletName
     * @param servletClass
     * @return
     */
    public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
        return null;
    }

    @Override
    public ServletRegistration.Dynamic addJspFile(String s, String s1) {
        return null;
    }

    /**
     *
     * @param <T>
     * @param clazz
     * @return
     * @throws ServletException
     */
    public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
        return null;
    }

    /**
     *
     * @param servletName
     * @return
     */
    public ServletRegistration getServletRegistration(String servletName) {
        return null;
    }

    /**
     *
     * @return
     */
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        return null;
    }

    /**
     *
     * @param filterName
     * @param className
     * @return
     */
    public FilterRegistration.Dynamic addFilter(String filterName, String className) {
        return null;
    }

    /**
     *
     * @param filterName
     * @param filter
     * @return
     */
    public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
        return null;
    }

    /**
     *
     * @param filterName
     * @param filterClass
     * @return
     */
    public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
        return null;
    }

    /**
     *
     * @param <T>
     * @param clazz
     * @return
     * @throws ServletException
     */
    public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
        return null;
    }

    /**
     *
     * @param filterName
     * @return
     */
    public FilterRegistration getFilterRegistration(String filterName) {
        return null;
    }

    /**
     *
     * @return
     */
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        return null;
    }

    /**
     *
     * @return
     */
    public SessionCookieConfig getSessionCookieConfig() {
        return null;
    }

    /**
     *
     * @param sessionTrackingModes
     */
    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {

    }

    /**
     *
     * @return
     */
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        return null;
    }

    /**
     *
     * @return
     */
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        return null;
    }

    /**
     *
     * @param className
     */
    public void addListener(String className) {

    }

    /**
     *
     * @param <T>
     * @param t
     */
    public <T extends EventListener> void addListener(T t) {

    }

    /**
     *
     * @param listenerClass
     */
    public void addListener(Class<? extends EventListener> listenerClass) {

    }

    /**
     *
     * @param <T>
     * @param clazz
     * @return
     * @throws ServletException
     */
    public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
        return null;
    }

    /**
     *
     * @return
     */
    public JspConfigDescriptor getJspConfigDescriptor() {
        return null;
    }

    /**
     *
     * @return
     */
    public ClassLoader getClassLoader() {
        return null;
    }

    /**
     *
     * @param roleNames
     */
    public void declareRoles(String... roleNames) {

    }

    /**
     *
     * @return
     */
    public String getVirtualServerName() {
        return null;
    }

    @Override
    public int getSessionTimeout() {
        return 0;
    }

    @Override
    public void setSessionTimeout(int i) {

    }

    @Override
    public String getRequestCharacterEncoding() {
        return null;
    }

    @Override
    public void setRequestCharacterEncoding(String s) {

    }

    @Override
    public String getResponseCharacterEncoding() {
        return null;
    }

    @Override
    public void setResponseCharacterEncoding(String s) {

    }
}
