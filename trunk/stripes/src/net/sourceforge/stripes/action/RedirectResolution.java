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
package net.sourceforge.stripes.action;

import net.sourceforge.stripes.controller.FlashScope;
import net.sourceforge.stripes.controller.StripesConstants;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

/**
 * <p>Resolution that uses the Servlet API to <em>redirect</em> the user to another path by issuing
 * a client side redirect. Unlike the ForwardResolution the RedirectResolution can send the user to
 * any URL anywhere on the web - though it is more commonly used to send the user to a location
 * within the same application.<p>
 *
 * <p>By default the RedirectResolution will prepend the context path of the web application to
 * any URL before redirecting the request. To prevent the context path from being prepended
 * use the constructor: {@code RedirectResolution(String,boolean)}.</p>
 *
 * <p>It is also possible to append paramters to the URL to which the user will be redirected.
 * This can be done by manually adding parameters with the addParameter() and addParameters()
 * methods, and by invoking includeRequestParameters() which will cause all of the current
 * request parameters to be included into the URL.</p>
 *
 * @see ForwardResolution
 * @author Tim Fennell
 */
public class RedirectResolution extends OnwardResolution<RedirectResolution> implements Resolution {
    private boolean prependContext = true;
    private boolean includeRequestParameters;
    private Collection<ActionBean> beans; // used to flash action beans

    /**
     * Simple constructor that takes the URL to which to forward the user. Defaults to
     * prepending the context path to the url supplied before redirecting.
     *
     * @param url the URL to which the user's browser should be re-directed.
     */
    public RedirectResolution(String url) {
        this(url, true);
    }

    /**
     * Constructor that allows explicit control over whether or not the context path is
     * prepended to the URL before redirecting.
     *
     * @param url the URL to which the user's browser should be re-directed.
     * @param prependContext true if the context should be prepended, false otherwise
     */
    public RedirectResolution(String url, boolean prependContext) {
        super(url);
        this.prependContext = prependContext;
    }

    /**
     * Constructs a RedirectResolution that will redirect to the URL appropriate for
     * the ActionBean supplied.  This constructor should be preferred when redirecting
     * to an ActionBean as it will ensure the correct URL is always used.
     *
     * @param beanType the Class object representing the ActionBean to redirect to
     */
    public RedirectResolution(Class<? extends ActionBean> beanType) {
        super(beanType);
    }

    /**
     * Constructs a RedirectResolution that will redirect to the URL appropriate for
     * the ActionBean supplied.  This constructor should be preferred when redirecting
     * to an ActionBean as it will ensure the correct URL is always used.
     *
     * @param beanType the Class object representing the ActionBean to redirect to
     * @param event the event that should be triggered on the redirect
     */
    public RedirectResolution(Class<? extends ActionBean> beanType, String event) {
        super(beanType, event);
    }

    /**
     * If set to true, will cause absolutely all request parameters present in the current request
     * to be appended to the redirect URL that will be sent to the browser. Since some browsers
     * and servers cannot handle extremely long URLs, care should be taken when using this
     * method with large form posts.
     *
     * @param inc whether or not current request parameters should be included in the redirect
     * @return RedirectResolution, this resolution so that methods can be chained
     */
    public RedirectResolution includeRequestParameters(boolean inc) {
        this.includeRequestParameters = inc;
        return this;
    }

    /**
     * Causes the ActionBean supplied to be added to the Flash scope and made available
     * during the next request cycle.
     *
     * @param bean the ActionBean to be added to flash scope
     * @since Stripes 1.2
     */
    public RedirectResolution flash(ActionBean bean) {
        if (this.beans == null) {
            this.beans = new HashSet<ActionBean>();
        }

        this.beans.add(bean);
        return this;
    }

    /**
     * Attempts to redirect the user to the specified URL.
     *
     * @throws ServletException thrown when the Servlet container encounters an error
     * @throws IOException thrown when the Servlet container encounters an error
     */
    public void execute(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        if (this.includeRequestParameters) {
            addParameters(request.getParameterMap());
        }

        // Add any beans to the flash scope
        if (this.beans != null) {
            FlashScope flash = FlashScope.getCurrent(request, true);
            for (ActionBean bean : this.beans) {
                flash.put(bean);
            }
        }

        // If a flash scope exists, add the parameter to the request
        FlashScope flash = FlashScope.getCurrent(request, false);
        if (flash != null) {
            addParameter(StripesConstants.URL_KEY_FLASH_SCOPE_ID, flash.key());
        }

        // Prepend the context path if required
        String url = getUrl();
        if (this.prependContext) {
            url = request.getContextPath() + url;
        }

        response.sendRedirect( response.encodeRedirectURL(url) );
    }
}
