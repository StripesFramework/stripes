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
 * Annotation used to bind ActionBean classes to a specific path within the web appliction.
 * The AnnotatedClassActionResolver will examine the URL submitted and extract the section
 * that is relative to the web-app root.  That will be compared with the URL specified in
 * the UrlBinding annotation, to find the ActionBean that should process the chosen request.
 *
 * @author Tim Fennell
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface UrlBinding {
    /** The web-app relative URL that the ActionBean will respond to. */
    String value();
}
