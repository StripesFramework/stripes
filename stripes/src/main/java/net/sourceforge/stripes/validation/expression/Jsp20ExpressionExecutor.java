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

import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.controller.DispatcherHelper;

import jakarta.servlet.jsp.el.ExpressionEvaluator;
import jakarta.servlet.jsp.PageContext;

/**
 * An implementation of {@link ExpressionExecutor} that uses the container's built in
 * JSP2.0 EL implementation. This requires that the DispatcherServlet allocates a
 * {@link jakarta.servlet.jsp.PageContext} object earlier in the request cycle in order
 * to gain access to the ExpressionEvaluator.  This can cause problems in some containers.
 *
 * @author Tim Fennell
 * @since Stripes 1.5
 */
@SuppressWarnings("deprecation")
public class Jsp20ExpressionExecutor extends ExpressionExecutorSupport {
    private static final Log log = Log.getInstance(Jsp20ExpressionExecutor.class);

    /**
     * Attempts to get the PageContext object stashed away in the DispatcherHelper
     *  and use it to generate an ExpressionEvaluator.
     *
     * @return an ExpressionEvaluator if possible, or null otherwise
     */
    @Override
    protected ExpressionEvaluator getEvaluator() {
        final PageContext context = DispatcherHelper.getPageContext();

        if (context == null) {
            log.error("Could not process expression based validation. It would seem that ",
                      "your servlet container is being mean and will not let the dispatcher ",
                      "servlet manufacture a PageContext object through the JSPFactory. The ",
                      "result of this is that expression validation will be disabled. Sorry.");
            return null;
        }
        else {
            return context.getExpressionEvaluator();
        }
    }
}