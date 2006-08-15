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
 * Simple type converter that converts the input String to a Character by returning
 * the first character in the String.
 *
 * @author Tim Fennell
 * @since Stripes 1.4
 */
public class CharacterTypeConverter implements TypeConverter<Character> {
    /** Does nothing. */
    public void setLocale(Locale locale) { }

    /**
     * Converts the input String to a Character by taking the first character in the
     * String and returning it. If the String is null or empty (this should never happen)
     * then it will return the Character represented by ordinal 0, aka the null character.
     *
     * @param input the String to convert into a single Character
     * @param targetType the type to convert to
     */
    public Character convert(String input, Class<? extends Character> targetType, Collection<ValidationError> errors) {
        if (input != null && !"".equals(input)) {
            return input.charAt(0);
        }
        else {
            return '\0';
        }
    }
}
