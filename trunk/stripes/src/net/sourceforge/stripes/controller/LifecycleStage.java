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
package net.sourceforge.stripes.controller;

/**
 * <p>Describes the major stages that form the Stripes request processing lifecycle. These stages
 * are enumerated here primarily because they are the points around which execution can be
 * intercepted using the Stripes {@link Interceptor} system.</p>
 *
 * @author Tim Fennell
 * @since Stripes 1.3
 */
public enum LifecycleStage {
    /**
     * First major lifecycle stage.  Involves the location of the ActionBean class that
     * is bound to the URL being requested, and usually also the creation of a new instance
     * of that class.
     */
    ActionBeanResolution,

    /**
     * Second major lifecycle stage.  Involves the determination of the event name in the
     * request (if there is one), and the location of the Method which handles the even.
     */
    HandlerResolution,

    /**
     * Third major lifecycle stage. Involves the processing of all validations specied through
     * {@literal} @Validate annotations as well as the type conversion of request parameters
     * and their binding to the ActionBean.
     */
    BindingAndValidation,

    /**
     * Fourth major lifecycle stage. Involves the execution of any custom validation logic
     * exposed by the ActionBean.
     */
    CustomValidation,

    /**
     * Fifth major lifecycle stage.  The actual execution of the event handler method. Only
     * occurs when the prior stages have produced no persistent validation errors.
     */
    EventHandling
}
