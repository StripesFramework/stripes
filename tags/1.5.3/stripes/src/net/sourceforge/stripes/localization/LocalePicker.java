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

    /**
     * Picks the character encoding to use for the current request using the specified
     * Locale. The character encoding will be set on both the request and the response. If the
     * LocalePicker does not wish to change or specify a character encoding then this
     * method should return null.
     *
     * @param request the current HttpServletRequest
     * @param locale the Locale picked by the LocalePicker for this request
     * @return the name of the character encoding to use, or null to use the default
     */
    String pickCharacterEncoding(HttpServletRequest request, Locale locale);
}
