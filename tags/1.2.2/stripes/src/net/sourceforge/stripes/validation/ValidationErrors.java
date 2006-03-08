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

import net.sourceforge.stripes.controller.ParameterName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Container class for ValidationErrors that are tied to form fields.  All of the regular Map
 * methods are available and can be used.  In addition there are a number of utility methods which
 * are design to make it easier to interact with the data.
 *
 * @author Tim Fennell
 */
public class ValidationErrors extends HashMap<String, List<ValidationError>> {

    /** Key that is used to store global (i.e. non-field specific) errors. */
    public static final String GLOBAL_ERROR = "__stripes_global_error";

    /**
     * Adds an error message for the field specified.  Will lazily instantiate a
     * List&lt;ValidationError&gt; and insert it into the map if necessary.
     *
     * @param field the name of the field in error
     * @param error a ValidationError to add to that field
     */
    public void put(String field, ValidationError error) {
        List<ValidationError> errors = get(field);
        if (errors == null) {
            errors = new ArrayList<ValidationError>();
            put(field, errors);
        }

        error.setFieldName( new ParameterName(field).getStrippedName() );
        errors.add(error);
    }

    /**
     * Synonym for put(String field, ValidationError error). Adds an error message for the field
     * specified.  Will lazily instantiate a List&lt;ValidationError&gt; and insert it into the
     * map if necessary.
     *
     * @param field the name of the field in error
     * @param error a ValidationError to add to that field
     */
    public void add(String field, ValidationError error) {
        put(field, error);
    }


    /**
     * Add multiple errors for a particular field. Does not destroy or override any exisitng errors.
     * Purely a convenience method to avoid having to check and possibly instantiate the collection
     * of errors for a field if it does not already exist.
     *
     * @param field the name of the field in error
     * @param errors a non-null list of errors to add for the field
     */
    public void putAll(String field, List<ValidationError> errors) {
        for (ValidationError error : errors) {
            put(field, error);
        }
    }

    /**
     * Synonym for putAll().  Add multiple errors for a particular field. Does not destroy or
     * override any exisitng errors.
     * Purely a convenience method to avoid having to check and possibly instantiate the collection
     * of errors for a field if it does not already exist.
     *
     * @param field the name of the field in error
     * @param errors a non-null list of errors to add for the field
     */
    public void addAll(String field, List<ValidationError> errors) {
        putAll(field, errors);
    }

    /**
     * Allows for the addition of errors that are not tied to a specific field. Errors added in
     * this way will only be displayed as part of the complete list of errors, and never when
     * showing errors for a specific field.
     */
    public void addGlobalError(ValidationError error) {
        add(GLOBAL_ERROR, error);
    }

    /**
     * Replaces the list of errors for a given field with the list supplied.
     *
     * @param field the name of the field in error
     * @param errors the list of validation errors for the field
     * @return the previous errors for the field, or null if there were none
     */
    public List<ValidationError> put(String field, List<ValidationError> errors) {
        String strippedName = new ParameterName(field).getStrippedName();

        for (ValidationError error : errors) {
            error.setFieldName(strippedName);
        }

        return super.put(field, errors);
    }
}
