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

import java.io.Serial;
import java.util.Map;
import net.sourceforge.stripes.action.ActionBean;

/**
 * Thrown when the action resolver can not find an {@link ActionBean} bound to the requested URL.
 *
 * @author John Newman
 * @since Stripes 1.5
 */
public class ActionBeanNotFoundException extends StripesServletException {
  @Serial private static final long serialVersionUID = 1L;

  /**
   * Constructs a new exception with the specified detail message.
   *
   * @param message the detail message.
   */
  public ActionBeanNotFoundException(String message) {
    super(message);
  }

  /**
   * Constructs a new exception with the specified detail message and cause.
   *
   * @param message the detail message.
   * @param cause the cause.
   */
  public ActionBeanNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new exception with the specified cause.
   *
   * @param cause the cause.
   */
  public ActionBeanNotFoundException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new exception with the specified detail message and cause.
   *
   * @param requestedUrl the requested URL.
   * @param registeredBeans the registered beans.
   */
  public ActionBeanNotFoundException(
      String requestedUrl, Map<String, Class<? extends ActionBean>> registeredBeans) {
    super(buildMessage(requestedUrl, registeredBeans));
  }

  /**
   * Constructs a new exception with the specified detail message and cause.
   *
   * @param requestedUrl the requested URL.
   * @param registeredBeans the registered beans.
   * @param cause the cause.
   */
  public ActionBeanNotFoundException(
      String requestedUrl,
      Map<String, Class<? extends ActionBean>> registeredBeans,
      Throwable cause) {
    super(buildMessage(requestedUrl, registeredBeans), cause);
  }

  /**
   * Static method to build the message from the requested bean and the map of registered beans.
   *
   * @param requestedUrl the requested URL.
   * @param registeredBeans the registered beans.
   * @return the message.
   */
  private static String buildMessage(
      String requestedUrl, Map<String, Class<? extends ActionBean>> registeredBeans) {
    return "Could not locate an ActionBean that is bound to the URL ["
        + requestedUrl
        + "]. Commons reasons for this include mis-matched URLs and forgetting "
        + "to implement ActionBean in your class. Registered ActionBeans are: "
        + registeredBeans;
  }
}
