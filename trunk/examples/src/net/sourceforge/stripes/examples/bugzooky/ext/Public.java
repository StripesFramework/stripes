/* Copyright 2009 Ben Gunter
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
package net.sourceforge.stripes.examples.bugzooky.ext;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.sourceforge.stripes.action.ActionBean;

/**
 * Works in conjunction with {@link SecurityInterceptor} to prevent unauthenticated users from
 * viewing information they are not allowed to view. Apply this annotation to an {@link ActionBean}
 * class to indicate that users are allowed to access it even if they are not authenticated. Most of
 * this application's {@link ActionBean}s require authentication so it's easier to flag those that
 * do <em>not</em> require authentication.
 * 
 * @author Ben Gunter
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Public {
}
