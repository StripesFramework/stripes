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
package net.sourceforge.stripes.integration.spring;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Documented;

/**
 * <p>Annotation used for injecting Spring managed beans into objects within Stripes
 * (usually ActionBeans).  The value of the annotation  represents the name of the bean
 * in the Spring application context to inject. If the value is omitted then Stripes
 * will attempt to auto-wire first by property/field name and then by type.</p>
 *
 * <p>Both methods and fields can be annotated.  If a field is annotated Stripes will use
 * field access to attempt to inject the bean into the field.  If a method is annotated Stripes
 * will attempt to invoke the method and supply it the value to inject.  In both cases
 * non-public fields/methods are supported (i.e. values can be injected into private fields
 * and through private methods).</p>
 *
 * <p>For a more details description of the injection process and how auto-wiring occurs
 * when explicit bean names are omitted see the {@link SpringHelper} class.</p>
 *
 * @author Dan Hayes
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
@Documented
public @interface SpringBean {
    String value() default "";
}
