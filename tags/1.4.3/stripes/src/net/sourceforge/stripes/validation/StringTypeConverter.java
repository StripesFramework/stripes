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
package net.sourceforge.stripes.validation;

import java.util.Locale;
import java.util.Collection;

/**
 * A dummy type converter that targets the String type by simply returning the input
 * String without any modifications.
 *
 * @author Tim Fennell
 * @since Stripes 1.4
 */
public class StringTypeConverter implements TypeConverter<String> {
    /** Does Nothing */
    public void setLocale(Locale locale) { }

    /** Simple returns the input String un-modified in any way. */
    public String convert(String input, Class<? extends String> targetType, Collection<ValidationError> errors) {
        return input;
    }
}
