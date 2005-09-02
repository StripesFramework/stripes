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
