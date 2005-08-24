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
package net.sourceforge.stripes.exception;

import javax.servlet.ServletException;

/**
 * Stripes' implementation of a ServletException that is thrown by Stripes
 * wherever throws clauses are limited to ServletExceptions.
 *
 * @author Tim Fennell
 */
public class StripesServletException extends ServletException {
    public StripesServletException(String string) {
        super(string);
    }

    public StripesServletException(String string, Throwable throwable) {
        super(string, throwable);
    }

    public StripesServletException(Throwable throwable) {
        super(throwable);
    }
}
