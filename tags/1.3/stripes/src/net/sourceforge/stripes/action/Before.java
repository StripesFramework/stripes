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
 * public void doStuff() {
 *    ...
 * }
 *
 * // Runs before binding and validation are executed
 * {@literal @Before(LifecycleStage.BindingAndValidation)}
 * public void doPreValidationStuff() {
 *    ...
 * }
 *
 * // Runs twice, once before each validation-related stage
 * {@literal @}Before({LifecycleStage.BindingAndValidation, LifecycleStage.CustomValidation})
 * public void doMorePreValidationStuff() {
 *    ...
 * }
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
