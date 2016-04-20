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
 * <p>One or more An HTTP request method annotations can be placed on an event 
 * handler method to specify exactly what types of request methods it services.
 * If no HTTP request methods are specified on an event handling method, then @GET is
 * presumed by default.  A StripesRuntimeException will be thrown if a caller attempts
 * to execute an event method on a RestActionBean which does not support the HTTP
 * request method specified by the caller.</p>
 * 
 * <p>For example:</p>
 * <pre>
 * {@literal @}RestActionBean
 * {@literal @}UrlBinding( "/person" )
 * public class PersonActionBean implements ActionBean {
 * 
 *   {@literal @}GET
 *   {@literal @}HandlesEvent( "retrieve" ) 
 *   public Resolution getPerson() {
 *     ...
 *   }
 * 
 *   {@literal @}PUT
 *   {@literal @}HandlesEvent( "update" )
 *   public Resolution updatePerson() {
 *     ...
 *   }
 * 
 *   ...
 * 
 * }
 * </pre>
 * 
 * @author Rick Grashel
 */
@Target( ElementType.METHOD )
@Retention( RetentionPolicy.RUNTIME )
@Documented
public @interface TRACE
{
}
