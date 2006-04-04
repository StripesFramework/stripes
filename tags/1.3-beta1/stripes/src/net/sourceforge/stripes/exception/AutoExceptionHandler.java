/* Copyright (C) 2006 Tim Fennell
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the license with this software. If not,
 * it can be found online at http://www.fsf.org/licensing/licenses/lgpl.html
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
