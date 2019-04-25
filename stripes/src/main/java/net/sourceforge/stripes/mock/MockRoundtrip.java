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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.Filter;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.Message;
import net.sourceforge.stripes.controller.ActionResolver;
import net.sourceforge.stripes.controller.AnnotatedClassActionResolver;
import net.sourceforge.stripes.controller.StripesConstants;
import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.controller.UrlBindingFactory;
import net.sourceforge.stripes.util.CryptoUtil;
import net.sourceforge.stripes.validation.ValidationErrors;

/**
 * <p>
 * Mock object that attempts to make it easier to use the other Mock objects in
 * this package to interact with Stripes and to interrogate the results.
 * Everything that is done in this class is do-able without this class! It
 * simply exists to make things a bit easier. As a result all the methods in
 * this class simply manipulate one or more of the underlying Mock objects. If
 * some needed capability is not exposed through the MockRoundtrip it is always
 * possible to fetch the underlying request, response and context and interact
 * with them directly.</p>
 *
 * <p>
 * It is worth noting that the Mock system <b>does not process forwards,
 * includes and redirects</b>. When an ActionBean (or other object) invokes the
 * servlet APIs for any of these actions it is recorded so that it can be
 * reported and verified later. In the majority of cases it should be sufficient
 * to test ActionBeans in isolation and verify that they produced the expected
 * output data and/or forward/redirect. If your ActionBeans depend on being able
 * to include other resources before continuing, sorry - you're on your own!</p>
 *
 * <p>
 * An example usage of this class might look like:</p>
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

    /**
     * Default value for the source page that generated this round trip request.
     */
    public static final String DEFAULT_SOURCE_PAGE = "_default_source_page_";

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockServletContext context;

    /**
     * Preferred constructor that will manufacture a request. Uses the
     * ServletContext to ensure that the request's context path matches. Pulls
     * the UrlBinding of the ActionBean and uses that as the requst URL.
     * Constructs a new session for the request.
     *
     * @param context the MockServletContext that will receive this request
     * @param beanType a Class object representing the ActionBean that should
     * receive the request
     */
    public MockRoundtrip(MockServletContext context, Class<? extends ActionBean> beanType) {
        this(context, beanType, new MockHttpSession(context));
    }

    /**
     * Preferred constructor that will manufacture a request. Uses the
     * ServletContext to ensure that the request's context path matches. Pulls
     * the UrlBinding of the ActionBean and uses that as the requst URL.
     * Constructs a new session for the request.
     *
     * @param context the MockServletContext that will receive this request
     * @param beanType a Class object representing the ActionBean that should
     * receive the request
     * @param session
     */
    public MockRoundtrip(MockServletContext context,
            Class<? extends ActionBean> beanType,
            MockHttpSession session) {
        this(context, getUrlBindingStub(beanType, context), session);
    }

    /**
     * Constructor that will create a request suitable for the provided servlet
     * context and URL. Note that in general the constructors that take an
     * ActionBean Class object are preferred over those that take a URL.
     * Constructs a new session for the request.
     *
     * @param context the MockServletContext that will receive this request
     * @param actionBeanUrl the url binding of the action bean
     */
    public MockRoundtrip(MockServletContext context, String actionBeanUrl) {
        this(context, actionBeanUrl, new MockHttpSession(context));
    }

    /**
     * Constructor that will create a request suitable for the provided servlet
     * context and URL. Note that in general the contructors that take an
     * ActionBean Class object are preferred over those that take a URL. The
     * request will use the provided session instead of creating a new one.
     *
     * @param context the MockServletContext that will receive this request
     * @param actionBeanUrl the url binding of the action bean
     * @param session an instance of MockHttpSession to use for the request
     */
    public MockRoundtrip(MockServletContext context, String actionBeanUrl, MockHttpSession session) {
        // Look for a query string and parse out the parameters if one is present
        String path = actionBeanUrl;
        SortedMap<String, List<String>> parameters = null;
        int qmark = actionBeanUrl.indexOf("?");
        if (qmark > 0) {
            path = actionBeanUrl.substring(0, qmark);
            if (qmark < actionBeanUrl.length()) {
                String query = actionBeanUrl.substring(qmark + 1);
                if (query != null && query.length() > 0) {
                    parameters = new TreeMap<String, List<String>>();
                    for (String kv : query.split("&")) {
                        String[] parts = kv.split("=");
                        String key, value;
                        if (parts.length == 1) {
                            key = parts[0];
                            value = null;
                        } else if (parts.length == 2) {
                            key = parts[0];
                            value = parts[1];
                        } else {
                            key = value = null;
                        }

                        if (key != null) {
                            List<String> values = parameters.get(key);
                            if (values == null) {
                                values = new ArrayList<String>();
                            }
                            values.add(value);
                            parameters.put(key, values);
                        }
                    }
                }
            }
        }

        this.context = context;
        this.request = new MockHttpServletRequest("/" + context.getServletContextName(), path);
        this.request.setSession(session);
        this.response = new MockHttpServletResponse();
        setSourcePage(DEFAULT_SOURCE_PAGE);

        // Add any parameters that were embedded in the given URL
        if (parameters != null) {
            for (Map.Entry<String, List<String>> entry : parameters.entrySet()) {
                for (String value : entry.getValue()) {
                    addParameter(entry.getKey(), value);
                }
            }
        }
    }

    /**
     * Get the servlet request object to be used by this round trip
     * @return 
     */
    public MockHttpServletRequest getRequest() {
        return request;
    }

    /**
     * Set the servlet request object to be used by this round trip
     * @param request
     */
    protected void setRequest(MockHttpServletRequest request) {
        this.request = request;
    }

    /**
     * Get the servlet response object to be used by this round trip
     * @return 
     */
    public MockHttpServletResponse getResponse() {
        return response;
    }

    /**
     * Set the servlet response object to be used by this round trip
     * @param response
     */
    protected void setResponse(MockHttpServletResponse response) {
        this.response = response;
    }

    /**
     * Get the ActionBean context to be used by this round trip
     * @return 
     */
    public MockServletContext getContext() {
        return context;
    }

    /**
     * Set the ActionBean context to be used by this round trip
     * @param context
     */
    protected void setContext(MockServletContext context) {
        this.context = context;
    }

    /**
     * Sets the named request parameter to the value or values provided. Any
     * existing values are wiped out and replaced with the value(s) provided.
     * @param name
     * @param value
     */
    public void setParameter(String name, String... value) {
        this.request.getParameterMap().put(name, value);
    }

    /**
     * Adds the value provided to the set of values for the named request
     * parameter. If one or more values already exist they will be retained, and
     * the new value will be appended to the set of values.
     * @param name
     * @param value
     */
    public void addParameter(String name, String... value) {
        if (this.request.getParameterValues(name) == null) {
            setParameter(name, value);
        } else {
            String[] oldValues = this.request.getParameterMap().get(name);
            String[] combined = new String[oldValues.length + value.length];
            System.arraycopy(oldValues, 0, combined, 0, oldValues.length);
            System.arraycopy(value, 0, combined, oldValues.length, value.length);
            setParameter(name, combined);
        }
    }

    /**
     * All requests to Stripes that can generate validation errors are required
     * to supply a request parameter telling Stripes where the request came
     * from. If you do not supply a value for this parameter then the value of
     * MockRoundTrip.DEFAULT_SOURCE_PAGE will be used.
     * @param url
     */
    public void setSourcePage(String url) {
        if (url != null) {
            url = CryptoUtil.encrypt(url);
        }
        setParameter(StripesConstants.URL_KEY_SOURCE_PAGE, url);
    }

    /**
     * Executes the request in the servlet context that was provided in the
     * constructor. If the request throws an Exception then that will be thrown
     * from this method. Otherwise, once the execution has completed you can use
     * the other methods on this class to examine the outcome.
     * @throws java.lang.Exception
     */
    public void execute() throws Exception {
        this.context.acceptRequest(this.request, this.response);
    }

    /**
     * Executes the request in the servlet context that was provided in the
     * constructor. Sets up the request so that it mimics the submission of a
     * specific event, named by the 'event' parameter to this method. If the
     * request throws an Exception then that will be thrown from this method.
     * Otherwise, once the execution has completed you can use the other methods
     * on this class to examine the outcome.
     * @param event
     * @throws java.lang.Exception
     */
    public void execute(String event) throws Exception {
        setParameter(event, "");
        execute();
    }

    /**
     * Gets the instance of the ActionBean type provided that was instantiated
     * by Stripes to handle the request. If a bean of this type was not
     * instantiated, this method will return null.
     *
     * @param <A>
     * @param type the Class object representing the ActionBean type expected
     * @return the instance of the ActionBean that was created by Stripes
     */
    @SuppressWarnings("unchecked")
    public <A extends ActionBean> A getActionBean(Class<A> type) {
        A bean = (A) this.request.getAttribute(getUrlBinding(type, this.context));
        if (bean == null) {
            bean = (A) this.request.getSession().getAttribute(getUrlBinding(type, this.context));
        }
        return bean;
    }

    /**
     * Gets the (potentially empty) set of Validation Errors that were produced
     * by the request.
     * @return 
     */
    public ValidationErrors getValidationErrors() {
        ActionBean bean = (ActionBean) this.request.getAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN);
        return bean.getContext().getValidationErrors();
    }

    /**
     * Gets the {@link List} of {@link Message}s that were produced by the
     * request. This should be used instead of obtaining the messages from the
     * {@link net.sourceforge.stripes.action.ActionBeanContext} as the context
     * is bound to the {@link net.sourceforge.stripes.controller.FlashScope}.
     *
     * @return
     */
    public List<Message> getMessages() {
        Object attribute = this.request.getAttribute(StripesConstants.REQ_ATTR_MESSAGES);
        if (attribute == null) {
            return null;
        }

        return (List<Message>) attribute;
    }

    /**
     * Gets, as bytes, any data that was written to the output stream associated
     * with the request. Note that since the Mock system does not write standard
     * HTTP response information (headers etc.) to the output stream, this will
     * be exactly what was written by the ActionBean.
     * @return 
     */
    public byte[] getOutputBytes() {
        return this.response.getOutputBytes();
    }

    /**
     * Gets, as a String, any data that was written to the output stream
     * associated with the request. Note that since the Mock system does not
     * write standard HTTP response information (headers etc.) to the output
     * stream, this will be exactly what was written by the ActionBean.
     * @return 
     */
    public String getOutputString() {
        return this.response.getOutputString();
    }

    /**
     * Gets the URL to which Stripes was directed after invoking the ActionBean.
     * Assumes that the request was either forwarded or redirected exactly once.
     * If the request was forwarded then the forwarded URL will be returned
     * verbatim. If the response was redirected and the redirect URL was within
     * the same web application, then the URL returned will exclude the context
     * path. I.e. the URL returned will be the same regardless of whether the
     * page was forwarded to or redirected to.
     * @return 
     */
    public String getDestination() {
        String forward = this.request.getForwardUrl();
        String redirect = this.response.getRedirectUrl();

        if (forward != null) {
            return forward;
        } else if (redirect != null) {
            String contextPath = this.request.getContextPath();
            if (contextPath.length() > 1 && redirect.startsWith(contextPath + '/')) {
                redirect = redirect.substring(contextPath.length());
            }
        }

        return redirect;
    }

    /**
     * If the request resulted in a forward, returns the URL that was forwarded
     * to.
     * @return 
     */
    public String getForwardUrl() {
        return this.request.getForwardUrl();
    }

    /**
     * If the request resulted in a redirect, returns the URL that was
     * redirected to. Unlike getDestination(), the URL in this case will be the
     * exact URL that would have been sent to the browser (i.e. including the
     * servlet context).
     * @return 
     */
    public String getRedirectUrl() {
        return this.response.getRedirectUrl();
    }

    /**
     * Find and return the {@link AnnotatedClassActionResolver} for the given
     * context.
     */
    private static AnnotatedClassActionResolver getActionResolver(MockServletContext context) {
        for (Filter filter : context.getFilters()) {
            if (filter instanceof StripesFilter) {
                ActionResolver resolver = ((StripesFilter) filter).getInstanceConfiguration()
                        .getActionResolver();
                if (resolver instanceof AnnotatedClassActionResolver) {
                    return (AnnotatedClassActionResolver) resolver;
                }
            }
        }

        return null;
    }

    /**
     * Find and return the {@link UrlBindingFactory} for the given context.
     */
    private static UrlBindingFactory getUrlBindingFactory(MockServletContext context) {
        ActionResolver resolver = getActionResolver(context);
        if (resolver instanceof AnnotatedClassActionResolver) {
            return ((AnnotatedClassActionResolver) resolver).getUrlBindingFactory();
        }

        return null;
    }

    /**
     * A helper method that fetches the UrlBinding of a class in the manner it
     * would be interpreted by the current context configuration.
     */
    private static String getUrlBinding(Class<? extends ActionBean> clazz,
            MockServletContext context) {
        return getActionResolver(context).getUrlBinding(clazz);
    }

    /**
     * Get the URL binding for an {@link ActionBean} class up to the first
     * parameter.
     */
    private static String getUrlBindingStub(Class<? extends ActionBean> clazz,
            MockServletContext context) {
        return getUrlBindingFactory(context).getBindingPrototype(clazz).getPath();
    }
}
