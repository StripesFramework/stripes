/*
 * Copyright 2014 Rick Grashel.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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

/**
 * <p>
 * All Stripes REST action beans should add the @RestActionBean annotation at
 * the class level. Doing this will trigger Stripes to handle REST calls
 * properly according to their HTTP request method and provide
 * semantically-proper responses from event handler methods.</p>
 *
 * <p>
 * By default, the SingleResourceStrategy is the mechanism through which REST
 * handler event methods are resolved and executed on a given RestActionBean. In
 * this strategy, the event method executed is based solely upon the raw HTTP
 * request method sent by the caller. So if the HTTP request method is "GET",
 * then the get() method will be executed on the action bean. If "HEAD", then
 * the head() method will be executed... et cetera. In this strategy the
 * "DefaultHandler" method is always "get".</p>
 *
 * @author Rick Grashel
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RestActionBean {
}
