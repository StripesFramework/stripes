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
