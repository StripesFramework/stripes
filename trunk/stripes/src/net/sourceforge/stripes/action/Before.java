/* Copyright (C) 2006 Tim Fennell
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
package net.sourceforge.stripes.action;

import net.sourceforge.stripes.controller.LifecycleStage;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Specifies that the annotated method should be run <i>before</i> the specified
 * {@link LifecycleStage}(s). More than one LifecycleStage can be specified, in which case the
 * method will be run before each stage. If no LifecycleStage is specified then the
 * default is to execute the method after {@link LifecycleStage#EventHandling}.
 * {@link LifecycleStage#ActionBeanResolution} <b>cannot</b> be specified because there is
 * no ActionBean to run a method on before the ActionBean has been resolved!</p>
 *
 * <p>The method may have any name, but must by public and take no arguments. Methods may return
 * values; if the value is a {@link net.sourceforge.stripes.action.Resolution} it will be used
 * immediately to terminate the request.  Any other values returned will be ignored.</p>
 *
 * <p>Examples:</p>
 *<pre>
 * // Runs before the event handling method has been run
 * {@literal @Before}
 * public void doStuff() { ... }
 *
 * // Runs before binding and validation are executed
 * {@literal @Before(LifecycleStage.BindingAndValidation)}
 * public void doPreValidationStuff() { ... }
 *
 * // Runs twice, once before each validation-related stage
 * {@literal @}Before({LifecycleStage.BindingAndValidation, LifecycleStage.CustomValidation})
 * public void doMorePreValidationStuff() { ... }
 * </pre>
 *
 * @see net.sourceforge.stripes.action.After
 * @see net.sourceforge.stripes.controller.BeforeAfterMethodInterceptor
 * @author Jeppe Cramon
 * @since Stripes 1.3
 */@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Inherited
@Documented
public @interface Before {
	/** One or more lifecycle stages before which the method should be called. */
	LifecycleStage[] value() default LifecycleStage.EventHandling; 
}
