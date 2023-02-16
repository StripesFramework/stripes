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

import net.sourceforge.stripes.config.ConfigurableComponent;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Factory for classes that implement {@link ContentTypeRequestWrapper}. The
 * factory may chose to always supply the same kind of wrapper, or vary the
 * implementation request by request as it sees fit.
 *
 * @author Rick Grashel
 */
public interface ContentTypeRequestWrapperFactory extends ConfigurableComponent {

    /**
     * Wraps the request in an appropriate implementation of ContentTypeWrapper
     * that is capable of providing access to request parameters
     *
     * @param request an active HttpServletRequest
     * @return an implementation of the appropriate wrapper
     * @throws java.lang.Exception If an error occurs while constructing the
     * wrapper
     */
    ContentTypeRequestWrapper wrap(HttpServletRequest request) throws Exception;
}
