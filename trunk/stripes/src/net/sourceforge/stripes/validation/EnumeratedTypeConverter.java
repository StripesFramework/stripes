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

import java.util.Collection;
import java.util.Locale;

/**
 * Converts the String form of an Enumerated type into the Enum value that it represents. If the
 * String does not represent one of the values in the Enum a validation error will be set.
 *
 * @author Tim Fennell
 */
public class EnumeratedTypeConverter implements TypeConverter<Enum> {

    /**
     * Does nothing at present due to the fact that enumerated types don't support localization
     * all that well. 
     */
    public void setLocale(Locale locale) {
        // Do nothing
    }

    public Enum convert(String input,
                        Class<? extends Enum> targetType,
                        Collection<ValidationError> errors) {

        try {
            return Enum.valueOf(targetType, input);
        }
        catch (IllegalArgumentException iae) {
            errors.add(new ScopedLocalizableError("converter.enum", "notAnEnumeratedValue"));
            return null;
        }
    }
}
