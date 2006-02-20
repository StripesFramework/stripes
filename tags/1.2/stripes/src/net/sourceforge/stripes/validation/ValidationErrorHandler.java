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

import net.sourceforge.stripes.action.Resolution;

/**
 * <p>Interface that can be implemented by ActionBeans to be notified of ValidationErrors that
 * occur during validation and binding.  In the case where an ActionBean does not implement
 * this interface, the errors are marshalled, the user is returned to the page from which
 * they came and the ActionBean instance is discarded.</p>
 *
 * <p>Implementing this interface gives ActionBeans the chance to modify what happens when the
 * binding phase generates errors. The handleValidationErrors method is invoked after binding, but
 * before any custom validation is invoked (if the ActionBean implements Validatable), and is
 * only invoked if there are errors. Also, note that setContext() will always have been
 * invoked prior to handleValidationErrors(), allowing the bean access to the event name
 * and other information.</p>
 *
 * <p>When the handleValidationErrors() method is invoked, the ActionBean may do one or more
 * of the following:</p>
 *
 * <ul>
 *   <li>Modify it's own internal state, e.g. unwind changes made by complex setter methods</li>
 *   <li>Remove validation errors from the ValidationErrors object</li>
 *   <li>Add new validation errors to the ValidationErrors object</li>
 *   <li>Modify errors in the ValidationErrors object</li>
 *   <li>(Optionally) Re-direct the flow of execution by returning a substitute Resolution</li>
 * </ul>
 *
 * <p>For example, if you want to override the validation service's results and continue
 * execution, you might invoke errors.clear() to remove all the errors.  Doing this makes it
 * as though the errors had never been generated!  Or perhaps for a specific ActionBean you'd
 * like to redirect to a different error page instead of the page the user came from, in that
 * case you can simple return a new ForwardResolution or RedirectResolution to change where the
 * user will be sent.</p>
 *
 * <p>Returning a Resolution from this method is stricly <b>optional</b>. If a Resolution is
 * returned it will be executed instead of the error resolution.  If null is returned then the
 * error resolution will be executed as normal.</p>
 *
 * @author Tim Fennell
 */
public interface ValidationErrorHandler {

    /**
     * Allows the ActionBean to influence what happens when validaiton errors occur during
     * validation and binding.  See class level javadoc for full description of behaviour.
     *
     * @param errors the set of validation errors generated during validation and binding
     * @return null, or a Resolution specifying what should happen next if non-standard
     *         behaviour is desired
     * @throws Exception may throw any exception, but this will generally result in a
     *         ServletException being thrown on up the stack
     */
    Resolution handleValidationErrors(ValidationErrors errors) throws Exception;
}
