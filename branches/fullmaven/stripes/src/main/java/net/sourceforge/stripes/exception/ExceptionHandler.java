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
package net.sourceforge.stripes.exception;

import net.sourceforge.stripes.config.ConfigurableComponent;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * <p>Component that is delegated to in order to handle any exceptions that are raised
 * during the processing of a request which is processed through the Stripes Filter.
 * Implementations have two options for handling an exception:</p>
 *
 * <ul>
 *   <li>Handle the exception and return</li>
 *   <li>Rethrown the exception if it cannot be handled</li>
 * </ul>
 *
 * <p>In the first case it is up to the exception handler to provide an appropriate response
 * to the user.  This might involve forwarding or redirecting the user to an error page, or
 * providing a streaming response in the case of an AJAX client.</p>
 *
 * <p>If the ExceptionHandler elects not to handle an Exception and re-throws it then the
 * exception will percolate up and the container will handle it using whatever error pages
 * are configured.</p>
 *
 * @author Tim Fennell
 * @since Stripes 1.3
 */
public interface ExceptionHandler extends ConfigurableComponent {

    /**
     * Responsible for handling any exceptions that arise as described in the class
     * level javadoc.
     *
     * @param throwable the exception/throwable being handled
     * @param request the current request. Notably, if the request progressed as far as
     *        ActionBeanResolution the ActionBean can be retreived by calling
     *       {@code request.getAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN)}.
     * @param response the current response.
     * @throws ServletException if the exception passed in cannot be handled
     */
    void handle(Throwable throwable,
                HttpServletRequest request,
                HttpServletResponse response) throws ServletException, IOException ;
}
