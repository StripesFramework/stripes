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

import net.sourceforge.stripes.examples.bugzooky.biz.Bug;
import net.sourceforge.stripes.examples.bugzooky.biz.BugManager;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.TypeConverter;
import net.sourceforge.stripes.validation.ValidationError;

/**
 * A {@link TypeConverter} that parses its input string to an integer and queries the
 * {@link BugManager} for a {@link Bug} object with that ID. If no {@link Bug} with the ID is found,
 * then it simply returns null. If the input string cannot be parsed as an integer, then this
 * {@link TypeConverter} adds a validation error and returns null.
 * 
 * @author Ben Gunter
 */
public class BugTypeConverter implements TypeConverter<Bug> {
    /**
     * Attempt to parse the input string to an integer and look up the {@link Bug} with that ID
     * using a {@link BugManager}.
     * 
     * @param input The input string to be parsed as the Bug ID.
     * @param targetType The type of object we're supposed to be returning.
     * @param errors The validation errors for this request. If the input string cannot be parsed,
     *            then we will add a new {@link ValidationError} to this collection and return null.
     */
    public Bug convert(String input, Class<? extends Bug> targetType,
            Collection<ValidationError> errors) {
        Bug bug = null;

        try {
            int id = Integer.valueOf(input);
            BugManager bugManager = new BugManager();
            bug = bugManager.getBug(id);
        }
        catch (NumberFormatException e) {
            errors.add(new SimpleError("The number {0} is not a valid Bug ID", input));
        }

        return bug;
    }

    /** This is specified in the {@link TypeConverter} interface, but it is not used here. */
    public void setLocale(Locale locale) {
    }
}
