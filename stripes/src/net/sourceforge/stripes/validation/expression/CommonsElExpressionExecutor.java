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

import net.sourceforge.stripes.util.ReflectUtil;
import net.sourceforge.stripes.exception.StripesRuntimeException;

import javax.servlet.jsp.el.ExpressionEvaluator;

/**
 * An implementation of {@link ExpressionExecutor} that relies on the Apache Commons
 * EL implementation being available in the classpath. This is the case with Tomcat
 * 5.5 and can be made so with other containers by including commons-el.jar in the web
 * application's classpath.
 *
 * @author Tim Fennell
 * @since Stripes 1.5
 */
@SuppressWarnings("deprecation")
public class CommonsElExpressionExecutor extends ExpressionExecutorSupport {
    /** The FQN of the expression evaluator class in commons-el. */
    public static final String COMMONS_CLASS = "org.apache.commons.el.ExpressionEvaluatorImpl";

    /**
     * Default constructor that checks to make sure this class can work, and if not throws
     * an exception.
     */
    public CommonsElExpressionExecutor() {
        if (getEvaluator() == null) {
            throw new StripesRuntimeException("Apache commons EL does not appear to be available.");
        }
    }

    /**
     * Attempts to create an expression evaluator by reflecting to find the implementation
     * in the apache commons-el project.
     *
     * @return an instance of ExpressionEvaluatorImpl if it can, null otherwise
     */
    protected ExpressionEvaluator getEvaluator() {
        try {
            return (ExpressionEvaluator)
                    ReflectUtil.findClass(COMMONS_CLASS).newInstance();
        }
        catch (Exception e) {
            return null;
        }
    }
}
