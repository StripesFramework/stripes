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
package net.sourceforge.stripes.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
     * response provided to direct the user to an approprirate view.
     *
     * @param request the current HttpServletRequest
     * @param response the current HttpServletResponse
     * @throws Exception exceptions of any type may be thrown if the Resolution cannot be
     *         executed as intended
     */
    void execute(HttpServletRequest request, HttpServletResponse response)
        throws Exception;
}
