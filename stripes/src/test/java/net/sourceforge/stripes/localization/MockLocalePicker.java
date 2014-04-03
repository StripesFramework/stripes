/* Copyright 2010 Marcus Krassmann
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

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.stripes.config.Configuration;

/**
 * Simple locale picker that just uses the locale of the passed HttpServletRequest. This should be
 * used for locale dependent test cases.
 * 
 * @author Marcus Krassmann
 */
public class MockLocalePicker implements LocalePicker {
    public Locale pickLocale(HttpServletRequest request) {
        return request.getLocale() == null ? Locale.getDefault() : request.getLocale();
    }

    public String pickCharacterEncoding(HttpServletRequest request, Locale locale) {
        return "UTF-8";
    }

    public void init(Configuration configuration) throws Exception {
    }
}