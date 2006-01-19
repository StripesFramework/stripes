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
package net.sourceforge.stripes.controller;

/**
 * Container for constant values that are used across more than one class in Stripes.
 *
 * @author Tim Fennell
 */
public interface StripesConstants {
    /**
     * The name of a URL parameter that is used to hold the path (relative to the web application
     * root) from which the current form submission was made.
     */
    String URL_KEY_SOURCE_PAGE = "_sourcePage";

    /**
     * The name of a URL parameter that is used to hold the path (relative to the web application
     * root) from which the current form submission was made.
     */
    String URL_KEY_FIELDS_PRESENT = "__fp";

    /**
     * The name under which the ActionBean for a request is stored as a request attribute before
     * forwarding to the JSP.
     */
    String REQ_ATTR_ACTION_BEAN = "actionBean";

    /**
     * The attribute key that is used to store the default set of non-error messages for
     * display to the user.
     */
    String REQ_ATTR_MESSAGES = "_stripes_defaultMessages";

    /**
     * The attribute key that is used to store the tag stack during page processing. The tag
     * stack is a Stack of Stripes tags so that parent tag relationships can work across
     * includes etc.
     */
    String REQ_ATTR_TAG_STACK = "__stripes_tag_stack";

    /**
     * The name of a URL parameter that is used to tell Stripes that a flash scope exists
     * for the current request.
     */
    String URL_KEY_FLASH_SCOPE_ID = "__fsk";

    /**
     * The name of a request parameter that holds a Map of flash scopes keyed by the
     * hashcode of the request that generated them.
     */
    String REQ_ATTR_FLASH_SCOPE_LOCATION = "__flash_scopes";

}
