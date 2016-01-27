/*
 * Copyright 2014 Rick Grashel.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sourceforge.stripes.examples.todolist;

import java.util.Collection;
import java.util.Locale;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.TypeConverter;
import net.sourceforge.stripes.validation.ValidationError;

/**
 * This is a type converter for Todo objects.
 */
public class TodoTypeConverter implements TypeConverter< Todo> {

    @Override
    public void setLocale(Locale locale) {
    }

    /**
     * Takes the passed identifier and tries to look up a corresponding Todo
     * in the datastore and return it.
     *
     * @param todoIdentifier
     * @param targetType
     * @param errors
     * @return
     */
    @Override
    public Todo convert(String todoIdentifier,
            Class<? extends Todo> targetType,
            Collection<ValidationError> errors) {
        // If no identifier is passed, just return null.
        if (todoIdentifier == null) {
            return null;
        }
        
        Todo todo = null;

        try {
            todo = Datastore.getById(Long.parseLong(todoIdentifier));
        } catch (NumberFormatException nfe) {
            // We don't care about this.  It is the same result.
        }

        // If the todo is not found by the TypeConverter, then throw an error.
        if (todo == null) {
            errors.add(new SimpleError("Unable to find artist by identifier passed => " + todoIdentifier));
        }

        return todo;
    }
}
