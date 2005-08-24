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

import javax.servlet.jsp.JspException;

/**
 * Stripes' version of the JspException that is used where only JspExceptions
 * can be thrown.
 *
 * @author Tim Fennell
 */
public class StripesJspException extends JspException {
    public StripesJspException(String string) {
        super(string);
    }

    public StripesJspException(String string, Throwable throwable) {
        super(string, throwable);
    }

    public StripesJspException(Throwable throwable) {
        super(throwable);
    }
}
