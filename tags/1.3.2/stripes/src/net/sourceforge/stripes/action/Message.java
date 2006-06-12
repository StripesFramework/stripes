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
