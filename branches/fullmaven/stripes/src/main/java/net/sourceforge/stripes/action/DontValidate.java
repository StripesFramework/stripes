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

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;

/**
 * Specify that the event handled by the annotated method should not have validation run on it
 * before the handler is invoked. Note that even if there are no normal validation errors for a
 * request, there may still be errors during type conversion and binding. Such errors are also
 * ignored by default. That behavior can be modified using the {@link #ignoreBindingErrors()}
 * element of this annotation.
 * 
 * @author Tim Fennell
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface DontValidate {
    /**
     * If true (the default) then any validation errors that might occur during type conversion and
     * binding will be ignored. If false then Stripes will forward back to the source page as it
     * normally would when it encounters validation errors. In either case, any errors that occur
     * during binding will be present in the {@link ActionBeanContext}.
     * 
     * @see ActionBeanContext#getValidationErrors()
     */
    boolean ignoreBindingErrors() default true;
}
