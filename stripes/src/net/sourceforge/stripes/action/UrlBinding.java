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
 * <p>
 * Annotation used to bind ActionBean classes to a specific path within the web application. The
 * AnnotatedClassActionResolver will examine the URL submitted and extract the section that is
 * relative to the web-app root. That will be compared with the URL specified in the UrlBinding
 * annotation, to find the ActionBean that should process the chosen request.
 * </p>
 * <p>
 * Stripes supports "Clean URLs" through the {@link UrlBinding} annotation. Parameters may be
 * embedded in the URL by placing the parameter name inside braces ({}). For example,
 * {@code @UrlBinding("/foo/{bar}/{baz}")} maps the action to "/foo" and indicates that the "bar"
 * and "baz" parameters may be embedded in the URL. In this case, the URL /foo/abc/123 would invoke
 * the action with bar set to "abc" and baz set to "123". The literal strings between parameters can
 * be any string.
 * </p>
 * <p>
 * The special parameter name $event may be used to embed the event name in a clean URL. For
 * example, given {@code @UrlBinding("/foo/{$event}")} the "bar" event could be invoked with the
 * URL /foo/bar.
 * </p>
 * <p>
 * Clean URL parameters can be assigned default values using the {@code =} operator. For example,
 * {@code @UrlBinding("/foo/{bar=abc}/{baz=123}")}. If a parameter with a default value is missing
 * from a request URL, it will still be made available as a request parameter with the default
 * value. Default values are automatically embedded when building URLs with the Stripes JSP tags.
 * The default value for $event can be automatically determined from the {@link DefaultHandler}
 * annotation or it can be explicitly set like any other parameter.
 * </p>
 * <p>
 * Clean URLs support both prefix mapping ({@code /action/foo/{bar}}) and extension mapping ({@code /foo/{bar}.action}).
 * Any number of parameters and/or literals may be omitted from the end of a request URL.
 * </p>
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
