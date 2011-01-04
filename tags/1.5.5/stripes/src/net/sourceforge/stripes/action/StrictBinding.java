/* Copyright 2007 Ben Gunter
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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;

/**
 * <p>
 * When applied to an {@link ActionBean}, this annotation turns on binding access controls. The
 * default policy is to deny binding to all properties. To enable binding on any given property, the
 * preferred method is to apply a {@link Validate} annotation to the property. (For nested
 * properties, use {@link ValidateNestedProperties}.) Even if validation is not necessary for the
 * property in question, a naked {@link Validate} annotation may still be used to enable binding.
 * Alternatively, binding can be enabled or disabled through the use of the {@link #allow()} and
 * {@link #deny()} elements of this annotation.
 * </p>
 * <p>
 * Properties may be named explicitly or by using globs. A single star (*) matches any property of
 * an element. Two stars (**) indicate any property of an element, including properties of that
 * property and so on. For security reasons, partial matches are not allowed so globs like
 * user.pass* will never match anything. Some examples:
 * <ul>
 * <li>{@code *} - any property of the {@link ActionBean} itself</li>
 * <li>{@code **} - any property of the {@link ActionBean} itself or its properties or their
 * properties, and so on</li>
 * <li>{@code user.username, user.email} - the username and email property of the user property of
 * the {@link ActionBean}</li>
 * <li>{@code user, user.*} - the user property and any property of the user
 * </ul>
 * </p>
 * <p>
 * The {@link #allow()} and {@link #deny()} elements are of type String[], but each string in the
 * array may be a comma-separated list of properties. Thus the
 * {@code @StrictBinding(allow="user, user.*")} is equivalent to
 * {@code @StrictBinding(allow={ "user", "user.*" }}.
 * </p>
 * 
 * @author Ben Gunter
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.TYPE })
@Documented
public @interface StrictBinding {
    /**
     * The options for the {@link StrictBinding#defaultPolicy()} element.
     */
    public enum Policy {
        /** In the event of a conflict, binding is allowed */
        ALLOW,

        /** In the event of a conflict, binding is denied */
        DENY
    }

    /**
     * The policy to observe when a property name matches both the deny and allow lists, or when a
     * property name does not match either list.
     */
    Policy defaultPolicy() default Policy.DENY;

    /** The list of properties that may be bound. */
    String[] allow() default "";

    /** The list of properties that may <em>not</em> be bound. */
    String[] deny() default "";
}
