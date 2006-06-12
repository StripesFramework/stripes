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

import net.sourceforge.stripes.config.Configuration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

/**
 * Default ExceptionHandler implementation that simply rethrows any ServletExceptions and
 * wraps and rethrows other exceptions, letting the container deal with them.
 *
 * @author Tim Fennell
 * @since Stripes 1.3
 */
public class DefaultExceptionHandler implements ExceptionHandler {

    /** Simply rethrows the exception passed in. */
    public void handle(Throwable throwable,
                       HttpServletRequest request,
                       HttpServletResponse response) throws ServletException {
        if (throwable instanceof ServletException) {
            throw (ServletException) throwable;
        }
        else {
            throw new StripesServletException
                    ("Unhandled exception caught by the default exception handler.", throwable);
        }

    }

    /** Does nothing. */
    public void init(Configuration configuration) throws Exception { }
}
