/* Copyright 2007 John Newman
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

import java.util.Map;

import net.sourceforge.stripes.action.ActionBean;

/**
 * Thrown when the action resolver can not find an {@link ActionBean} bound to
 * the requested URL.
 *
 * @author John Newman
 * @since Stripes 1.5
 */
public class ActionBeanNotFoundException extends StripesServletException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs an action bean not found exception with the passed message.
     * 
     * @param message Message of the exception
     */
    public ActionBeanNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs an action bean not found exception with the passed message
     * and cause.
     * 
     * @param message Message of the exception
     * @param cause Cause of the exception
     */
    public ActionBeanNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an action bean not found exception with the passed cause.
     * 
     * @param cause - Cause of this exception
     */
    public ActionBeanNotFoundException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs an action bean not found exception for the passed URL and
     * associated beans.
     * 
     * @param requestedUrl - URL requested
     * @param registeredBeans - Beans registered for this exception.
     */
    public ActionBeanNotFoundException(String requestedUrl,
            Map<String, Class<? extends ActionBean>> registeredBeans) {
        super(buildMessage(requestedUrl, registeredBeans));
    }

    /**
     * Constructs an action bean not found exception for the passed URL and
     * associated beans and cause.
     * 
     * @param requestedUrl - URL requested
     * @param registeredBeans - Beans registered for this exception.
     * @param cause - Cause of the exception
     */
    public ActionBeanNotFoundException(String requestedUrl,
            Map<String, Class<? extends ActionBean>> registeredBeans, Throwable cause) {
        super(buildMessage(requestedUrl, registeredBeans), cause);
    }

    /**
     * Static method to build the message from the requested bean and the map of
     * registered beans.
     * 
     * @return Message built for the exception
     */
    private static String buildMessage(String requestedUrl,
            Map<String, Class<? extends ActionBean>> registeredBeans) {
        return "Could not locate an ActionBean that is bound to the URL [" + requestedUrl
                + "]. Common reasons for this include mis-matched URLs and forgetting "
                + "to implement ActionBean in your class. Registered ActionBeans are: "
                + registeredBeans;
    }
}
