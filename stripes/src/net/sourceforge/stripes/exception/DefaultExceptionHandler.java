/* Copyright (C) 2006 Tim Fennell
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
