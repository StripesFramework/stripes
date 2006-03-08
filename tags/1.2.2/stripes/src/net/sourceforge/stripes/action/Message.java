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

import java.util.Locale;

/**
 * Represents a message that can be displayed to the user.  Encapsulates commonalities
 * between error messages produced as part of validation and other types of user messages
 * such as warnings or feedback messages.
 *
 * @author Tim Fennell
 */
public interface Message {
    /**
     * Provides a message that can be displayed to the user. The message must be a String,
     * and should be in the language and locale appropriate for the user.
     *
     * @param locale the Locale picked for the current interaction with the user
     * @return String the String message that will be displayed to the user
     */
    String getMessage(Locale locale);
}
