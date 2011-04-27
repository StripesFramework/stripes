/* Copyright 2008 Ben Gunter
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
package net.sourceforge.stripes.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.sourceforge.stripes.exception.AutoExceptionHandler;
import net.sourceforge.stripes.format.Formatter;
import net.sourceforge.stripes.validation.TypeConverter;
import net.sourceforge.stripes.validation.Validate;

/**
 * When applied to a Stripes extension class (e.g., one that implements {@link Formatter},
 * {@link TypeConverter}, {@link AutoExceptionHandler}, etc.), this annotation indicates that the
 * class should <em>not</em> be loaded via autodiscovery. This is useful, for example, when you
 * have a {@link TypeConverter} that is applied in special cases via {@link Validate#converter()}
 * but should not be used for all the type conversions to which it applies.
 * 
 * @author Ben Gunter
 * @since Stripes 1.5
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.TYPE })
@Documented
public @interface DontAutoLoad {
}
