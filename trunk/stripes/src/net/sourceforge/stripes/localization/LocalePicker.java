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
package net.sourceforge.stripes.localization;

import net.sourceforge.stripes.config.ConfigurableComponent;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * <p>A LocalePicker is a class that determines what Locale a particular request will use. At first
 * this may seem odd given that the request already has a method called getLocale(), but ask
 * yourself this: if your site only supports English, and the user's browser requests the
 * Japanese locale, in what locale should you accept their input?</p>
 *
 * <p>The LocalPicker is given access to the request and can use any mechanism it chooses to
 * decide upon a Locale.  However, it must return a valid locale.  It is suggested that if a locale
 * cannot be chosen that the picker return the system locale.</p>
 *
 * @author Tim Fennell
 */
public interface LocalePicker extends ConfigurableComponent {

    /**
     * Picks a locale for the HttpServletRequest supplied.  Given that the request could be a
     * regular request or a form upload request it is suggested that the LocalePicker only rely
     * on the headers in the request, and perhaps the session, and not look for parameters.
     *
     * @param request the current HttpServletRequest
     * @return Locale the locale to be used throughout the request for input parsing and
     *         localized output
     */
    Locale pickLocale(HttpServletRequest request);
}
