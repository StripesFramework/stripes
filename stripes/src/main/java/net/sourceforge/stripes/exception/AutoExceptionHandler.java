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
package net.sourceforge.stripes.exception;


/**
 * <p>A marker interface for delegate exception handlers to be used with the
 * {@link DelegatingExceptionHandler}.  Note that the DelegatingExceptionHandler must be
 * configured as the {@link ExceptionHandler} for the application in order for AutoExceptionHandlers
 * to be discovered and used.</p>
 *
 * <p>AutoExceptionHandlers can define one or more methods to handle different kinds of exceptions.
 * Each method must have the following signature:</p>
 *
 *<pre>public Resolution handle(Type exception, HttpServletRequest req, HttpServletResponse res);</pre>
 *
 * <p>where <tt>Type</tt> can be any subclass of {@link java.lang.Throwable}.  Handler methods do
 * not have to follow any naming convention. In the above example 'handle' is used, but any
 * other name, e.g. 'run', 'handleException' etc. would have worked as well. The return type is only
 * loosely enforced; if the method returns an object and it is a
 * {@link net.sourceforge.stripes.action.Resolution} then it will be executed, otherwise it
 * will be ignored.</p>
 *
 * @author Jeppe Cramon
 * @since Stripes 1.3
 */
public interface AutoExceptionHandler { }
