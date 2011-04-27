/* Copyright 2008 Tim Fennell
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
package net.sourceforge.stripes.validation.expression;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.controller.ParameterName;
import net.sourceforge.stripes.validation.ValidationMetadata;
import net.sourceforge.stripes.validation.ValidationErrors;

import java.util.List;

/**
 * <p>Simple interface that specifies how Stripes will invoke expression based validation.
 * Generally used via the ExpressionValidator which will pick an appropriate implementation
 * based on the current environment.</p>
 *
 * <p>Implementations should throw an exception from their default constructor if they
 * are unable to operate due to class versioning of availability issues.</p>
 *
 * @author Tim Fennell
 * @since Stripes 1.5
 */
public interface ExpressionExecutor {
    /** The default scope to use when constructing errors. */
    String ERROR_DEFAULT_SCOPE = "validation.expression";

    /** The error key to use when constructing errors. */
    String ERROR_KEY = "valueFailedExpression";

    /** The special name given to the field that the expression is annotated on. */
    String THIS = "this";

    /**
     * Performs validation of an ActionBean property using the expression contained
     * within the validation metadata. If the expression does not evaluate to true
     * then an error will be added to the validation errors. Otherwise there are no
     * side effects.
     *
     * @param bean the ActionBean instance owning the field being validated
     * @param name the name of the field being validated
     * @param values the List of values (post type conversion), each to be validated
     * @param validationInfo the validation metadata for the field
     * @param errors the ValidationErrors object into which to place any errors
     */
    public void evaluate(ActionBean bean, ParameterName name, List<Object> values,
                         ValidationMetadata validationInfo, ValidationErrors errors);
}
