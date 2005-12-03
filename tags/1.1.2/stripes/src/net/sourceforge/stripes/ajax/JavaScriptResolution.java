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
package net.sourceforge.stripes.ajax;

import net.sourceforge.stripes.action.Resolution;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>Resolution that will convert a Java object web to a web of JavaScript objects and arrays, and
 * stream the JavaScript back to the client.  The output of this resolution can be evaluated in
 * JavaScript using the eval() function, and will return a reference to the top level JavaScript
 * object.  For more information see {@link JavaScriptBuilder}</p>
 *
 * @author Tim Fennell
 * @since Stripes 1.1
 */
public class JavaScriptResolution implements Resolution {
    private JavaScriptBuilder builder;

    /**
     * Constructs a new JavaScriptResolution that will convert the supplied object to JavaScript.
     *
     * @param rootObject an Object of any type supported by {@link JavaScriptBuilder}. In most cases
     *        this will either be a JavaBean, Map, Collection or Array, but may also be any one of
     *        the basic Java types including String, Date, Number etc.
     */
    public JavaScriptResolution(Object rootObject, Class<?>... userTypesExcluded) {
        this.builder = new JavaScriptBuilder(rootObject, userTypesExcluded);
    }

    /**
     * Converts the object passed in to JavaScript and streams it back to the client.
     */
    public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        this.builder.build(response.getWriter());
        response.flushBuffer();
    }
}
