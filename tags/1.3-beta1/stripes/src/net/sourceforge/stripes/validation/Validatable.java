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

/**
 * <p>Interface to be implemented by ActionBeans which need to perform some level of validation above
 * and beyond what Stripes can do through Annotations.  Allows for a clean separation of validation
 * logic and regular business logic.  This is especially useful since an ActionBean can have
 * multiple handler methods each responsible for handling a different event.</p>
 *
 * <p>By the time the validate() method is invoked Stripes has already performed all Annotated
 * validations and has converted and bound properties from the HttpServletRequest on to the
 * ActionBean.</p>
 *
 * <p>The default behaviour is Stripes is such that if validation errors arise from the annotated
 * validations (or type conversion), the validate() method will not be called (nor will the handler
 * method). This behaviour is configurable though.  Please see the Stripes documentation on
 * <a href="http://stripes.mc4j.org/confluence/display/stripes/Configuration+Reference">Configuration</a>
 * if you would prefer to have validate() called even when validation errors
 * have been generated by type conversion and annotation based validation.</p>
 *
 * @author Tim Fennell
 * @deprecated Beginning with Stripes 1.3 the preferred way to add custom validation logic to
 *             an ActionBean is with {@link ValidationMethod}. The Validatable interface will be
 *             removed in a future release. Converting is as easy as removing
 *             "implements Validatable" from your class and adding {@literal @ValidationMethod}
 *             to your validate() method.
 */
@Deprecated
public interface Validatable {

    /**
     * Internal validation of ActionBean state before invocation of a handler method should occur
     * here.  ActionBeans are free to do as they please, and have full access to the
     * ActionBeanContext in order to validate themselves.
     *
     * @param errors a ValidationErrors object to which errors should be added
     */
    void validate(ValidationErrors errors);
}