/* Copyright 2009 Ben Gunter
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
package net.sourceforge.stripes.examples.bugzooky.ext;

import java.util.Collection;
import java.util.Locale;

import net.sourceforge.stripes.examples.bugzooky.biz.Person;
import net.sourceforge.stripes.examples.bugzooky.biz.PersonManager;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.TypeConverter;
import net.sourceforge.stripes.validation.ValidationError;

/**
 * A {@link TypeConverter} that parses its input string to an integer and queries the
 * {@link PersonManager} for a {@link Person} object with that ID. If no {@link Person} with the ID
 * is found, then it simply returns null. If the input string cannot be parsed as an integer, then
 * this {@link TypeConverter} adds a validation error and returns null.
 * 
 * @author Ben Gunter
 */
public class PersonTypeConverter implements TypeConverter<Person> {
    /**
     * Attempt to parse the input string to an integer and look up the {@link Person} with that ID
     * using a {@link PersonManager}.
     * 
     * @param input The input string to be parsed as the Person ID.
     * @param targetType The type of object we're supposed to be returning.
     * @param errors The validation errors for this request. If the input string cannot be parsed,
     *            then we will add a new {@link ValidationError} to this collection and return null.
     */
    public Person convert(String input, Class<? extends Person> targetType,
            Collection<ValidationError> errors) {
        Person person = null;

        try {
            int id = Integer.valueOf(input);
            PersonManager personManager = new PersonManager();
            person = personManager.getPerson(id);
        }
        catch (NumberFormatException e) {
            errors.add(new SimpleError("The number {0} is not a valid Person ID", input));
        }

        return person;
    }

    /** This is specified in the {@link TypeConverter} interface, but it is not used here. */
    public void setLocale(Locale locale) {
    }
}
