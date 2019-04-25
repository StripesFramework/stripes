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
package net.sourceforge.stripes.controller;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * Interface which must be implemented by classes which provide the ability to
 * parse request body content for a given content-type and return parameter
 * names and values to the StripesRequestWrapper.
 *
 * @author Rick Grashel
 */
public interface ContentTypeRequestWrapper {

    /**
     * Pseudo-constructor that allows the class to perform any initialization
     * necessary.
     *
     * @param request an HttpServletRequest that has a content-type of
     * multipart.
     * @throws IOException if an error occurs reading the request body
     */
    void build(HttpServletRequest request) throws IOException;

    /**
     * Fetches the names of all non-file parameters in the request. Directly
     * analogous to the method of the same name in HttpServletRequest when the
     * request is non-multipart.
     *
     * @return an Enumeration of all non-file parameter names in the request
     */
    Enumeration<String> getParameterNames();

    /**
     * Fetches all values of a specific parameter in the request. To simulate
     * the HTTP request style, the array should be null for non-present
     * parameters, and values in the array should never be null - the empty
     * String should be used when there is value.
     *
     * @param name the name of the request parameter
     * @return an array of non-null parameters or null
     */
    String[] getParameterValues(String name);
}
