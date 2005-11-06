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

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.controller.StripesConstants;
import net.sourceforge.stripes.validation.ValidationErrors;

/**
 * <p>Mock object that attempts to make it easier to use the other Mock objects in this package
 * to interact with Stripes and to interrogate the results.  Everything that is done in this class
 * is do-able without this class! It simply exists to make things a bit easier. As a result all
 * the methods in this class simply manipulate one or more of the underlying Mock objects. If
 * some needed capability is not exposed through the MockRoundtrip it is always possible to fetch
 * the underlying request, response and context and interact with them directly.</p>
 *
 * <p>It is worth noting that the Mock system <b>does not process forwards, includes and
 *  redirects</b>. When an ActionBean (or other object) invokes the servlet APIs for any of these
 * actions it is recorded so that it can be reported and verified later. In the majority of cases
 * it should be sufficient to test ActionBeans in isolation and verify that they produced the
 * expected output data and/or forward/redirect. If your ActionBeans depend on being able to include
 * other resources before continuing, sorry - you're on your own!</p>
 *
 * <p>An example usage of this class might look like:</p>
 *
 * <pre>
 * MockServletContext context = ...;
 * MockRoundtrip trip = new MockRoundtrip(context, CalculatorActionBean.class);
 * trip.setParameter("numberOne", "2");
 * trip.setParameter("numberTwo", "2");
 * trip.execute();
 * CalculatorActionBean bean = trip.getActionBean(CalculatorActionBean.class);
 * Assert.assertEquals(bean.getResult(), 4, "two plus two should equal four");
 * Assert.assertEquals(trip.getDestination(), ""/quickstart/index.jsp");
 * </pre>
 *
 * @author Tim Fennell
 * @since Stripes 1.1.1
 */
public class MockRoundtrip {
    /** Default value for the source page that generated this round trip request. */
    public static final String DEFAULT_SOURCE_PAGE = "_default_source_page_";

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockServletContext context;

    /**
     * Preferred constructor that will manufacture a request. Uses the ServletContext to ensure
     * that the request's context path matches. Pulls the UrlBinding of the ActionBean and uses
     * that as the requst URL. Constructs a new session for the request.
     *
     * @param context the MockServletContext that will receive this request
     * @param beanType a Class object representing the ActionBean that should receive the request
     */
    public MockRoundtrip(MockServletContext context, Class<? extends ActionBean> beanType) {
        this(context, beanType.getAnnotation(UrlBinding.class).value() );
    }

    /**
     * Preferred constructor that will manufacture a request. Uses the ServletContext to ensure
     * that the request's context path matches. Pulls the UrlBinding of the ActionBean and uses
     * that as the requst URL. Constructs a new session for the request.
     *
     * @param context the MockServletContext that will receive this request
     * @param beanType a Class object representing the ActionBean that should receive the request
     */
    public MockRoundtrip(MockServletContext context,
                         Class<? extends ActionBean> beanType,
                         MockHttpSession session) {
        this(context, beanType.getAnnotation(UrlBinding.class).value(), session);
    }

    /**
     * Constructor that will create a requeset suitable for the provided servlet context and
     * URL. Note that in general the contructors that take an ActionBean Class object are preferred
     * over those that take a URL.  Constructs a new session for the request.
     *
     * @param context the MockServletContext that will receive this request
     * @param actionBeanUrl the url binding of the action bean
     */
    public MockRoundtrip(MockServletContext context, String actionBeanUrl) {
        this(context, actionBeanUrl, new MockHttpSession(context));
    }

    /**
     * Constructor that will create a requeset suitable for the provided servlet context and
     * URL. Note that in general the contructors that take an ActionBean Class object are preferred
     * over those that take a URL.  The request will use the provided session instead of creating
     * a new one.
     *
     * @param context the MockServletContext that will receive this request
     * @param actionBeanUrl the url binding of the action bean
     * @param session an instance of MockHttpSession to use for the request
     */
    public MockRoundtrip(MockServletContext context, String actionBeanUrl, MockHttpSession session) {
        this.context = context;
        this.request = new MockHttpServletRequest("/" + context.getServletContextName(),
                                                  actionBeanUrl);
        this.request.setSession(session);
        this.response = new MockHttpServletResponse();
        setSourcePage(DEFAULT_SOURCE_PAGE);
    }

    /** Provides access to the request when direct access is needed. */
    public MockHttpServletRequest getRequest() { return this.request; }

    /** Provides access to the response when direct access is needed. */
    public MockHttpServletResponse getResponse() { return this.response; }

    /**
     * Sets the named request parameter to the value or values provided. Any existing values are
     * wiped out and replaced with the value(s) provided.
     */
    public void setParameter(String name, String... value) {
        this.request.getParameterMap().put(name, value);
    }

    /**
     * Adds the value provided to the set of values for the named request parameter. If one or
     * more values already exist they will be retained, and the new value will be appended to the
     * set of values.
     */
    public void addParameter(String name, String... value) {
        if (this.request.getParameterValues(name) == null) {
            setParameter(name, value);
        }
        else {
            String[] oldValues = this.request.getParameterMap().get(name);
            String[] combined = new String[oldValues.length + value.length];
            System.arraycopy(oldValues, 0, combined, 0, oldValues.length);
            System.arraycopy(value, 0, combined, oldValues.length, value.length);
        }
    }

    /**
     * All requests to Stripes that can generate validation errors are required to supply a
     * request parameter telling Stripes where the request came from. If you do not supply a
     * value for this parameter then the value of MockRoundTrip.DEFAULT_SOURCE_PAGE will be used.
     */
    public void setSourcePage(String url) {
        setParameter(StripesConstants.URL_KEY_SOURCE_PAGE, url);
    }

    /**
     * Executes the request in the servlet context that was provided in the constructor. If the
     * request throws an Exception then that will be thrown from this method. Otherwise, once the
     * execution has completed you can use the other methods on this class to examine the outcome.
     */
    public void execute() throws Exception {
        this.context.acceptRequest(this.request, this.response);
    }

    /**
     * Executes the request in the servlet context that was provided in the constructor. Sets up
     * the request so that it mimics the submission of a specific event, named by the 'event'
     * parameter to this method. If the request throws an Exception then that will be thrown from
     * this method. Otherwise, once the execution has completed you can use the other methods on
     * this class to examine the outcome.
     */
    public void execute(String event) throws Exception {
        setParameter(event, "");
        execute();
    }

    /**
     * Gets the instance of the ActionBean type provided that was instantiated by Stripes to
     * handle the request. If a bean of this type was not instantiated, this method will
     * return null.
     *
     * @param type the Class object representing the ActionBean type expected
     * @return the instance of the ActionBean that was created by Stripes
     */
    public <A extends ActionBean> A getActionBean(Class<A> type) {
        return (A) this.request.getAttribute(type.getAnnotation(UrlBinding.class).value());
    }

    /**
     * Gets the (potentially empty) set of Validation Errors that were produced by the request.
     */
    public ValidationErrors getValidationErrors() {
        ActionBean bean = (ActionBean) this.request.getAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN);
        return bean.getContext().getValidationErrors();
    }

    /**
     * Gets, as bytes, any data that was written to the output stream associated with the
     * request. Note that since the Mock system does not write standard HTTP response information
     * (headers etc.) to the output stream, this will be exactly what was written by the
     * ActionBean.
     */
    public byte[] getOutputBytes() {
        return this.response.getOutputBytes();
    }

    /**
     * Gets, as a String, any data that was written to the output stream associated with the
     * request. Note that since the Mock system does not write standard HTTP response information
     * (headers etc.) to the output stream, this will be exactly what was written by the
     * ActionBean.
     */
    public String getOutputString() {
        return this.response.getOutputString();
    }

    /**
     * Gets the URL to which Stripes was directed after invoking the ActionBean. Assumes that
     * the request was either forwarded or redirected exactly once. If the request was forwarded
     * then the forwarded URL will be returned verbatim.  If the response was redirected and the
     * redirect URL was within the same web application, then the URL returned will exclude the
     * context path.  I.e. the URL returned will be the same regardless of whether the page was
     * forwarded to or redirected to.
     */
    public String getDestination() {
        String forward = this.request.getForwardUrl();
        String redirect = this.response.getRedirectUrl();

        if (forward != null) {
            return forward;
        }
        else if (redirect != null && redirect.startsWith(this.request.getContextPath())) {
            redirect = redirect.substring( redirect.indexOf('/', 1));
        }

        return redirect;
    }

    /** If the request resulted in a forward, returns the URL that was forwarded to. */
    public String getForwardUrl() {
        return this.request.getForwardUrl();
    }

    /**
     * If the request resulted in a redirect, returns the URL that was redirected to. Unlike
     * getDestination(), the URL in this case will be the exact URL that would have been sent to
     * the browser (i.e. including the servlet context).
     */
    public String getRedirectUrl() {
        return this.response.getRedirectUrl();
    }
}
