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
package net.sourceforge.stripes.action;

import net.sourceforge.stripes.controller.LifecycleStage;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Specifies that the annotated method should be run <i>after</i> the specified
 * {@link LifecycleStage}(s). More than one LifecycleStage can be specified, in which case the
 * method will be run after each stage completes. If no LifecycleStage is specified then the
 * default is to execute the method after {@link LifecycleStage#EventHandling}.
 * {@link LifecycleStage#RequestInit} <b>cannot</b> be specified because there is no ActionBean
 * to run a method on before the ActionBean has been resolved!</p>
 *
 * <p>The method may have any name, any access specifier (public, private etc.) and must take
 * no arguments. Methods may return values; if the value is a
 * {@link net.sourceforge.stripes.action.Resolution} it will be used immediately to terminate
 * the request.  Any other values returned will be ignored.</p>
 *
 * <p>Examples:</p>
 *<pre>
 * // Runs only after the event handling method has been run
 * {@literal @After}
 * public void doStuff() {
 *    ...
 * }
 *
 * // Runs after binding and validation have completed
 * {@literal @After(stages = LifecycleStage.BindingAndValidation)}
 * public void doPostValidationStuff() {
 *    ...
 * }
 *
 * // Runs twice, once after each validation-related stage
 * {@literal @}After(stages = {LifecycleStage.BindingAndValidation, LifecycleStage.CustomValidation})
 * public void doMorePostValidationStuff() {
 *    ...
 * }
 * </pre>
 *
 * @see net.sourceforge.stripes.action.Before
 * @see net.sourceforge.stripes.controller.BeforeAfterMethodInterceptor
 * @author Jeppe Cramon
 * @since Stripes 1.3
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Inherited
@Documented
public @interface After {
	/** One or more lifecycle stages after which the method should be called. */
	LifecycleStage[] stages() default LifecycleStage.EventHandling;

    /**
     * Allows the method to be restricted to one or more events. By default the method will
     * be executed on all events. Can be used to specify one or more events to apply the method
     * to (e.g. on={"save", "update"}),  or to specify one or more events <i>not</i> to apply
     * the method to (e.g. on="!delete").
     */
    String[] on() default {};
}
