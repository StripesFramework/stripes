/* Copyright 2010 Ben Gunter
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
package net.sourceforge.stripes.exception;

import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.controller.StripesConstants;

/**
 * A subclass of {@link IllegalStateException} that is thrown when validation errors are present on
 * a request and the source page cannot be determined.
 * 
 * @author Ben Gunter
 * @since Stripes 1.5.5
 */
public class SourcePageNotFoundException extends IllegalStateException {
    private ActionBeanContext actionBeanContext;

    /**
     * Construct a new instance for the given action bean context.
     * 
     * @param actionBeanContext The context.
     */
    public SourcePageNotFoundException(ActionBeanContext actionBeanContext) {
        // @formatter:off
        super(
                "Here's how it is. Someone (quite possibly the Stripes Dispatcher) needed " +
                "to get the source page resolution. But no source page was supplied in the " +
                "request, and unless you override ActionBeanContext.getSourcePageResolution() " +
                "you're going to need that value. When you use a <stripes:form> tag a hidden " +
                "field called '" + StripesConstants.URL_KEY_SOURCE_PAGE + "' is included. " +
                "If you write your own forms or links that could generate validation errors, " +
                "you must include a value  for this parameter. This can be done by calling " +
                "request.getServletPath().");
        // @formatter:on
        this.actionBeanContext = actionBeanContext;
    }

    /** Get the action bean context in which this exception occurred. */
    public ActionBeanContext getActionBeanContext() {
        return actionBeanContext;
    }
}
