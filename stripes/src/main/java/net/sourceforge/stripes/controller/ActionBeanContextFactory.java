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

import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.config.ConfigurableComponent;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;

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