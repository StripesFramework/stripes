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
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationMetadata;

import java.util.List;

/**
 * <p>A Facade to the classes that perform expression based validation. Hides the fact that we
 * might be using one of many implementations to actually run expression validation. When the
 * {@link net.sourceforge.stripes.validation.expression.ExpressionExecutor} is first requested
 * an attempt is made to find the best working executor available.  The following classes will
 * be tried in turn until a working instance is found:</p>
 *
 * <ul>
 *   <li>{@link Jsp21ExpressionExecutor}</li>
 *   <li>{@link CommonsElExpressionExecutor}</li>
 *   <li>{@link Jsp20ExpressionExecutor</li>
 * </ul>
 *
 * @author Tim Fennell
 * @since Stripes 1.5
 */
public class ExpressionValidator {
    private static final Log log = Log.getInstance(ExpressionValidator.class);
    private static ExpressionExecutor executor;

    /**
     * Attempts to instantiate executor classes (as described in the class level javadoc)
     * until a working one is found.
     */
    public static void initialize() {
        try {
            executor = new Jsp21ExpressionExecutor();
        }
        catch (Throwable t) {
            try {
                executor = new CommonsElExpressionExecutor();
            }
            catch (Throwable t2) {
                executor = new Jsp20ExpressionExecutor();
            }
        }

        log.info("Expression validation will be performed using: " + executor.getClass().getName());
    }

    /**
     * Run expression validation on the bean property provided with the values provided.
     *
     * @param bean the ActionBean being validated
     * @param name the ParameterName object representing the parameter being validated
     * @param values the values to be validated (zero or more)
     * @param validationInfo the validation metadata for the named property
     * @param errors the ValidationErrors for the property, to be added to
     */
    public static void evaluate(final ActionBean bean, final ParameterName name, final List<Object> values,
                                final ValidationMetadata validationInfo, final ValidationErrors errors) {
        executor.evaluate(bean, name, values, validationInfo, errors);
    }

    /**
     * Gets the executor that will be used to run expression evaluation. If none is yet set
     * the {@link #initialize()} method will be run to set one up.
     *
     * @return an instance of ExpressionExecutor that can be used to execut validation expressions
     */
    public static ExpressionExecutor getExecutor() {
        if (executor == null) initialize();
        return executor;
    }

    /**
     * Allows anyone who is interested to substitute a different ExpressionExecutor instance
     * instead of the one picked by the ExpressionValidator.
     * @param executor the executor to use from now on
     */
    public static void setExecutor(final ExpressionExecutor executor) {
        ExpressionValidator.executor = executor;
    }
}
