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
