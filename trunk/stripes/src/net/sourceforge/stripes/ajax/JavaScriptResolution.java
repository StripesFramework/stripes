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
     * @param objectsToExclude Classes and/or property names to exclude from the output.
     */
    public JavaScriptResolution(Object rootObject, Object... objectsToExclude) {
        this.builder = new JavaScriptBuilder(rootObject, objectsToExclude);
    }

    /**
     * Converts the object passed in to JavaScript and streams it back to the client.
     */
    public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("text/javascript");
        this.builder.build(response.getWriter());
        response.flushBuffer();
    }
}
