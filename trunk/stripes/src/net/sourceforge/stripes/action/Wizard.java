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

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Documented;

/**
 * <p>Annotation that marks an ActionBean as representing a wizard user interface (i.e. one logical
 * form or operation spread across several pages/request cycles). ActionBeans that are marked
 * as Wizards are treated differently in the following ways:</p>
 *
 * <ul>
 *   <li>Data from previous request cycles is maintained automatically through hidden fields</li>
 *   <li>Required field validation is performed only on those fields present on the page</li>
 * </ul>
 *
 * @author Tim Fennell
 * @since Stripes 1.2
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Wizard {
}
