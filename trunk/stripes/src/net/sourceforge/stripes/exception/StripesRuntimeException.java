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

/**
 * Stripes' version of a RuntimeException that is to be preferred in Stripes
 * code to throwing plain RuntimeExceptions.
 *
 * @author Tim Fennell
 */
public class StripesRuntimeException extends RuntimeException {
    public StripesRuntimeException(String message) {
        super(message);
    }

    public StripesRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public StripesRuntimeException(Throwable cause) {
        super(cause);
    }
}
