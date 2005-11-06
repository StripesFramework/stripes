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

import javax.servlet.ServletContext;
import java.util.Map;
import java.util.Enumeration;
import java.util.Collections;
import java.util.HashMap;

/**
 * Common parent class for both MockServletConfig and MockFilterConfig since they are both
 * essentially the same with a couple of method names changed.
 *
 * @author Tim Fennell
 * @since Stripes 1.1.1
 */
public class MockBaseConfig {
    private ServletContext servletContext;
    private Map<String,String> initParameters = new HashMap<String,String>();

    /** Sets the ServletContext that will be returned by getServletContext(). */
    public void setServletContext(ServletContext ctx) { this.servletContext = ctx; }

    /** Gets the ServletContext in whiich the filter is running. */
    public ServletContext getServletContext() { return this.servletContext; }

    /** Adds a value to the set of init parameters. */
    public void addInitParameter(String name, String value) {
        this.initParameters.put(name, value);
    }

    /** Adds all the values in the provided Map to the set of init parameters. */
    public void addAllInitParameters(Map<String,String> parameters) {
        this.initParameters.putAll(parameters);
    }

    /** Gets the named init parameter if it exists, or null if it doesn't. */
    public String getInitParameter(String name) {
        return this.initParameters.get(name);
    }

    /** Gets an enumeration of all the init parameter names present. */
    public Enumeration getInitParameterNames() {
        return Collections.enumeration( this.initParameters.keySet() );
    }
}
