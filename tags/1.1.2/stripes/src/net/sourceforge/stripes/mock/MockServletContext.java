/* Copyright (C) 2005 Tim Fennell
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the license with this software. If not,
 * it can be found online at http://www.fsf.org/licensing/licenses/lgpl.html
 */
package net.sourceforge.stripes.mock;

import javax.servlet.Filter;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private String contextName;
    private Map<String,String> initParameters = new HashMap<String,String>();
    private Map<String,Object> attributes = new HashMap<String,Object>();
    private List<Filter> filters = new ArrayList<Filter>();
    private HttpServlet servlet;

    /** Simple constructor that creates a new mock ServletContext with the supplied context name. */
    public MockServletContext(String contextName) {
        this.contextName = contextName;
    }

    /** If the url is within this servlet context, returns this. Otherwise returns null. */
    public ServletContext getContext(String url) {
        if (url.startsWith("/" + this.contextName)) {
            return this;
        }
        else {
            return null;
        }
    }

    /** Always returns 2. */
    public int getMajorVersion() { return 2; }

    /** Always returns 4. */
    public int getMinorVersion() { return 4; }

    /** Always returns null (i.e. don't know). */
    public String getMimeType(String file) { return null; }

    /** Always returns null (i.e. there are no resources under this path). */
    public Set getResourcePaths(String path) {
        return null;
    }

    /** Uses the current classloader to fetch the resource if it can. */
    public URL getResource(String name) throws MalformedURLException {
        return Thread.currentThread().getContextClassLoader().getResource(name);
    }

    /** Uses the current classloader to fetch the resource if it can. */
    public InputStream getResourceAsStream(String name) {
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
    public Servlet getServlet(String string) throws ServletException { return null; }

    /** Deprecated method always returns an empty enumeration. */
    public Enumeration getServlets() {
        return Collections.enumeration( Collections.emptySet() );
    }

    /** Deprecated method always returns an empty enumeration. */
    public Enumeration getServletNames() {
        return Collections.enumeration( Collections.emptySet() );
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
    public String getRealPath(String string) { return null; }

    /** Returns a version string identifying the Mock implementation. */
    public String getServerInfo() {
        return "Stripes Mock Servlet Environment, version 1.0.";
    }

    /** Adds an init parameter to the mock servlet context. */
    public void addInitParameter(String name, String value) {
        this.initParameters.put(name, value);
    }

    /** Adds all the values in the supplied Map to the set of init parameters. */
    public void addAllInitParameters(Map<String,String> parameters) {
        this.initParameters.putAll(parameters);
    }

    /** Gets the value of an init parameter with the specified name, if one exists. */
    public String getInitParameter(String name) {
        return this.initParameters.get(name);
    }

    /** Returns an enumeration of all the initialization parameters in the context. */
    public Enumeration getInitParameterNames() {
        return Collections.enumeration( this.initParameters.keySet() );
    }

    /** Gets an attribute that has been set on the context (i.e. application) scope. */
    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    /** Returns an enumeration of all the names of attributes in the context. */
    public Enumeration getAttributeNames() {
        return Collections.enumeration( this.attributes.keySet() );
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

    /** Adds a filter to the end of filter chain that will be used to filter requests.*/
    public void addFilter(Class<? extends Filter> filterClass,
                          String filterName,
                          Map<String,String> initParams) {
        try {
            MockFilterConfig config = new MockFilterConfig();
            config.setFilterName(filterName);
            config.setServletContext(this);
            if (initParams != null) config.addAllInitParameters(initParams);

            Filter filter = filterClass.newInstance();
            filter.init(config);
            this.filters.add(filter);
        }
        catch (Exception e) {
            throw new RuntimeException("Exception registering new filter with name " + filterName, e);
        }
    }

    /** Sets the servlet that will receive all requests in this servlet context. */
    public void setServlet(Class<? extends HttpServlet> servletClass,
                           String servletName,
                           Map<String,String> initParams) {
        try {
            MockServletConfig config = new MockServletConfig();
            config.setServletName(servletName);
            config.setServletContext(this);
            if (initParams != null) config.addAllInitParameters(initParams);

            this.servlet = servletClass.newInstance();
            this.servlet.init(config);
        }
        catch (Exception e) {
            throw new RuntimeException("Exception registering servlet with name " + servletName, e);
        }
    }

    /**
     * <p>Takes a request and response and runs them through the set of filters using a
     * MockFilterChain, which if everything goes well, will eventually execute the servlet
     * that is registered with this context.</p>
     *
     * <p>Any exceptions that are raised during the processing of the request are  simply
     * passed through to the caller. I.e. they will be thrown from this method.</p> 
     */
    public void acceptRequest(MockHttpServletRequest request, MockHttpServletResponse response)
    throws Exception {
        MockFilterChain chain = new MockFilterChain();
        chain.setServlet(this.servlet);
        chain.addFilters(this.filters);
        chain.doFilter(request, response);
    }
}
