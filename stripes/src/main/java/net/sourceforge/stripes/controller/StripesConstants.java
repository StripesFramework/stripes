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

import java.util.Collections;
import java.util.Set;
import net.sourceforge.stripes.util.Literal;

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
   * The name of a URL parameter that is used to hold the names of certain inputs that are present
   * in a form if those inputs might not be submitted with the form, such as checkboxes.
   */
  String URL_KEY_FIELDS_PRESENT = "__fp";

  /**
   * The name of a URL parameter that is used as a last resort to attempt to determine the name of
   * the event that is being fired by the browser.
   */
  String URL_KEY_EVENT_NAME = "_eventName";

  /**
   * The name of a URL parameter that is used to tell Stripes that a flash scope exists for the
   * current request.
   */
  String URL_KEY_FLASH_SCOPE_ID = "__fsk";

  /**
   * An immutable set of URL keys or request parameters that have special meaning to Stripes and as
   * a result should not be referenced in binding, validation or other other places that work on the
   * full set of request parameters.
   */
  Set<String> SPECIAL_URL_KEYS =
      Collections.unmodifiableSet(
          Literal.set(
              StripesConstants.URL_KEY_SOURCE_PAGE,
              StripesConstants.URL_KEY_FIELDS_PRESENT,
              StripesConstants.URL_KEY_FLASH_SCOPE_ID,
              StripesConstants.URL_KEY_EVENT_NAME));

  /**
   * The name under which the ActionBean for a request is stored as a request attribute before
   * forwarding to the JSP.
   */
  String REQ_ATTR_ACTION_BEAN = "actionBean";

  /**
   * The name of a request attribute in which a Stack of action beans is some times stored when a
   * single request involves includes of action beans.
   */
  String REQ_ATTR_ACTION_BEAN_STACK = "__stripes_actionBeanStack";

  /**
   * The attribute key that is used to store the default set of non-error messages for display to
   * the user.
   */
  String REQ_ATTR_MESSAGES = "_stripes_defaultMessages";

  /**
   * The attribute key that is used to store the tag stack during page processing. The tag stack is
   * a Stack of Stripes tags so that parent tag relationships can work across includes etc.
   */
  String REQ_ATTR_TAG_STACK = "__stripes_tag_stack";

  /**
   * The name of a request parameter that holds a Map of flash scopes keyed by the hash code of the
   * request that generated them.
   */
  String REQ_ATTR_FLASH_SCOPE_LOCATION = "__flash_scopes";

  /** The name of a request attribute that holds the lookup key of the current flash scope. */
  String REQ_ATTR_CURRENT_FLASH_SCOPE = "__current_flash_scope";

  /**
   * The name of a request attribute that is checked first to determine the name of the event that
   * should fire.
   */
  String REQ_ATTR_EVENT_NAME = "__stripes_event_name";

  /**
   * Request attribute key defined by the servlet spec for storing the included servlet path when
   * processing a server side include.
   */
  String REQ_ATTR_INCLUDE_PATH = "javax.servlet.include.servlet_path";

  /**
   * Request attribute key defined by the servlet spec for storing the included path info when
   * processing a server side include.
   */
  String REQ_ATTR_INCLUDE_PATH_INFO = "javax.servlet.include.path_info";
}
