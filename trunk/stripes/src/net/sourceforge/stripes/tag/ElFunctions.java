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
package net.sourceforge.stripes.tag;

import java.util.List;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.validation.ValidationError;
import net.sourceforge.stripes.validation.ValidationErrors;

/**
 * A collection of static functions that are included in the Stripes tag library.  In most
 * cases these are not functions that are specific to Stripes, but simply functions that
 * make doing web development (especially with Java 5 constructs) easier.
 *
 * @author Tim Fennell
 * @since Stripes 1.1
 */
public class ElFunctions {

    /** Gets the name of the supplied enumerated value. */
    public static String name(Enum<?> e) {
        return e.name();
    }

    /** Indicates if validation errors exist for the given field of the given {@link ActionBean}. */
    public static boolean hasErrors(ActionBean actionBean, String field) {
        if (actionBean == null || field == null)
            return false;

        ActionBeanContext context = actionBean.getContext();
        if (context == null)
            return false;

        ValidationErrors errors = context.getValidationErrors();
        if (errors == null || errors.isEmpty())
            return false;

        List<ValidationError> fieldErrors = errors.get(field);
        return fieldErrors != null && fieldErrors.size() > 0;
    }

}
