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
import javax.servlet.FilterChain;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Mock implementation of a filter chain that allows a number of filters to be called
 * before finally invoking the servlet that is the target of the request.
 *
 * @author Tim Fennell
 * @since Stripes 1.1.1
 */
public class MockFilterChain implements FilterChain {
    private List<Filter> filters = new ArrayList<Filter>();
    private Iterator<Filter> iterator;
    private Servlet servlet;

    /** Adds a filter to the set of filters to be run. */
    public void addFilter(Filter filter) {
        this.filters.add(filter);
    }

    /** Adds an ordered list of filters to the filter chain. */
    public void addFilters(Collection<Filter> filters) {
        this.filters.addAll(filters);
    }

    /** Sets the servlet that will receive the request after all filters are processed. */
    public void setServlet(Servlet servlet) {
        this.servlet = servlet;
    }

    /** Used to coordinate the execution of the filters. */
    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        if (this.iterator == null) {
            this.iterator = this.filters.iterator();
        }

        if (this.iterator.hasNext()) {
            this.iterator.next().doFilter(request, response, this);
        }
        else {
            this.servlet.service(request, response);
        }
    }
}
