/* Copyright (C) 2005 Tim Fennell
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
package net.sourceforge.stripes.controller;

import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.config.ConfigurableComponent;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * Interface for classes that can instantiate and supply new instances of the
 * ActionBeanContext class, or subclasses thereof.
 *
 * @author Tim Fennell
 */
public interface ActionBeanContextFactory extends ConfigurableComponent {


    /**
     * Creates and returns a new instance of ActionBeanContext or a subclass.
     *
     * @param request the current HttpServletRequest
     * @param response the current HttpServletResponse
     * @return a new instance of ActionBeanContext
     * @throws ServletException if the ActionBeanContext class configured cannot be instantiated
     */
    ActionBeanContext getContextInstance(HttpServletRequest request,
                                         HttpServletResponse response) throws ServletException;
}