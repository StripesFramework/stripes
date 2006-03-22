/* Copyright (C) 2006 Jeppe Cramon
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
 * Interface that exception handlers must implement.
 * It's currently only used as a marker interface which is used by the default ExceptionHandlerResolver, DelegatingExceptionHandler, to find
 * available ExceptionHandlers.<p/>
 *
 * Since there're no exception handler methods required by the interface, each AutoExceptionHandler implementation
 * can contain several exception handler methods.
 * It's the implementation of the ExceptionHandlerResolver that defines how exception handler methods are defined.
 *
 * @author Jeppe Cramon
 */
public interface AutoExceptionHandler { }
