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
package net.sourceforge.stripes.action;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Type that is designed to be returned by &quot;handler&quot; methods in ActionBeans. The
 * Resolution is responsible for executing the next step after the ActionBean has handled the
 * user's request.  In most cases this will likely be to forward the user on to the next page.
 *
 * @see ForwardResolution
 * @author Tim Fennell
 */
public interface Resolution {
    /**
     * Called by the Stripes dispatcher to invoke the Resolution.  Should use the request and
     * response provided to direct the user to an appropriate view.
     *
     * @param request the current HttpServletRequest
     * @param response the current HttpServletResponse
     * @throws Exception exceptions of any type may be thrown if the Resolution cannot be
     *         executed as intended
     */
    void execute(HttpServletRequest request, HttpServletResponse response)
        throws Exception;
}
