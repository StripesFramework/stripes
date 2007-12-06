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
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * This annotation can be applied to an event handler method or to an {@link ActionBean} class to
 * suggest to the HTTP client how it should cache the response. Classes will inherit this annotation
 * from their superclass. Method-level annotations override class-level annotations. This means, for
 * example, that applying {@code @HttpCache(allow=false)} to an {@link ActionBean} class turns off
 * client-side caching for all events except those that are annotated with
 * {@code @HttpCache(allow=true)}.
 * </p>
 * <p>
 * Some examples:
 * <ul>
 * <li>{@code @HttpCache} - Same behavior as if the annotation were not present. No headers are
 * set.</li>
 * <li>{@code @HttpCache(allow=true)} - Same as above.</li>
 * <li>{@code @HttpCache(allow=false)} - Set headers to disable caching and immediately expire the
 * document.</li>
 * <li>{@code @HttpCache(expires=3600)} - Caching is allowed. The document expires in 10 minutes.</li>
 * </ul>
 * </p>
 * 
 * @author Ben Gunter
 * @since Stripes 1.5
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.METHOD, ElementType.TYPE })
@Inherited
@Documented
public @interface HttpCache {
    /** Indicates whether the response should be cached by the client. */
    boolean allow() default true;

    /**
     * The number of seconds into the future that the response should expire. If {@link #allow()} is
     * false, then this value is ignored and zero is used. If {@link #allow()} is true and this
     * value is less than zero, then no Expires header is sent.
     */
    int expires() default 0;
}
