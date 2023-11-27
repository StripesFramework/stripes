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
package net.sourceforge.stripes.validation;

import net.sourceforge.stripes.action.Resolution;

/**
 * Interface that can be implemented by ActionBeans to be notified of ValidationErrors that occur
 * during validation and binding. In the case where an ActionBean does not implement this interface,
 * the errors are marshalled, the user is returned to the page from which they came and the
 * ActionBean instance is discarded.
 *
 * <p>Implementing this interface gives ActionBeans the chance to modify what happens when the
 * binding and/or validation phase(s) generate errors. The handleValidationErrors method is invoked
 * after all validation has completed - i.e. after annotation based validation and any {@link
 * ValidationMethod}s that are applicable for the current request. Invocation only happens when one
 * or more validation errors exist. Also, note that {@code setContext()} will always have been
 * invoked prior to {@link #handleValidationErrors(ValidationErrors)}, allowing the bean access to
 * the event name and other information.
 *
 * <p>When the {@link #handleValidationErrors(ValidationErrors)} method is invoked, the {@link
 * net.sourceforge.stripes.action.ActionBean} may do one or more of the following:
 *
 * <ul>
 *   <li>Modify it's own internal state, e.g. unwind changes made by complex setter methods
 *   <li>Remove validation errors from the ValidationErrors object
 *   <li>Add new validation errors to the ValidationErrors object
 *   <li>Modify errors in the ValidationErrors object
 *   <li>Any other operation, e.g. rollback a transaction, log an audit message etc.
 *   <li>(Optionally) Re-direct the flow of execution by returning a substitute Resolution
 * </ul>
 *
 * <p>For example, if you want to override the validation service's results and continue execution,
 * you might invoke {@code errors.clear()} to remove all the errors. Doing this makes it as though
 * the errors had never been generated! Or perhaps for a specific ActionBean you'd like to redirect
 * to a different error page instead of the page the user came from, in that case you can simply
 * return a new {@link net.sourceforge.stripes.action.ForwardResolution} or {@link
 * net.sourceforge.stripes.action.RedirectResolution} to change where the user will be sent.
 *
 * <p>Returning a {@link Resolution} from this method is stricly <b>optional</b>. If a Resolution is
 * returned it will be executed instead of the error resolution. If null is returned (and one or
 * more errors persist) then the error resolution will be executed as normal.
 *
 * @author Tim Fennell
 */
public interface ValidationErrorHandler {

  /**
   * Allows the ActionBean to influence what happens when validation errors occur during validation
   * and binding. See class level javadoc for full description of behaviour.
   *
   * @param errors the set of validation errors generated during validation and binding
   * @return null, or a Resolution specifying what should happen next if non-standard behaviour is
   *     desired
   * @throws Exception may throw any exception, but this will generally result in a ServletException
   *     being thrown on up the stack
   */
  Resolution handleValidationErrors(ValidationErrors errors) throws Exception;
}
