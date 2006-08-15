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
package net.sourceforge.stripes.controller;

/**
 * <p>Describes the major stages that form the Stripes request processing lifecycle. These stages
 * are enumerated here primarily because they are the points around which execution can be
 * intercepted using the Stripes {@link Interceptor} system.</p>
 *
 * @author Tim Fennell
 * @since Stripes 1.3
 */
public enum LifecycleStage {
    /**
     * First major lifecycle stage.  Involves the location of the ActionBean class that
     * is bound to the URL being requested, and usually also the creation of a new instance
     * of that class.
     */
    ActionBeanResolution,

    /**
     * Second major lifecycle stage.  Involves the determination of the event name in the
     * request (if there is one), and the location of the Method which handles the even.
     */
    HandlerResolution,

    /**
     * Third major lifecycle stage. Involves the processing of all validations specied through
     * {@literal} @Validate annotations as well as the type conversion of request parameters
     * and their binding to the ActionBean.
     */
    BindingAndValidation,

    /**
     * Fourth major lifecycle stage. Involves the execution of any custom validation logic
     * exposed by the ActionBean.
     */
    CustomValidation,

    /**
     * Fifth major lifecycle stage.  The actual execution of the event handler method. Only
     * occurs when the prior stages have produced no persistent validation errors.
     */
    EventHandling,

    /**
     * Sixth major lifecycle stage. Is executed any time a Resolution is executed, either
     * as the outcome of an event handler, or because some other mechanism short circuits
     * processing by returning a Resolution. 
     */
    ResolutionExecution
}
