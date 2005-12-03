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
package net.sourceforge.stripes.tag.layout;

import java.util.Map;
import java.util.HashMap;

/**
 * Used to move contextual information about a layout rendering between a LayoutRenderTag and
 * a LayoutDefinitionTag. Holds the set of overridden components and any parameters provided
 * to the render tag.
 *
 * @author Tim Fennell
 * @since Stripes 1.1
 */
public class LayoutContext {
    private Map<String,String> components = new HashMap<String,String>();
    private Map<String,Object> parameters = new HashMap<String,Object>();

    /**
     * Gets the Map of overridden components.  Will return an empty Map if no components were
     * overridden.
     */
    public Map<String, String> getComponents() {
        return components;
    }

    /** Gets the Map of parameters.  Will return an empty Map if none were provided. */
    public Map<String, Object> getParameters() {
        return parameters;
    }

    /** To String implementation the parameters, and the component names. */
    public String toString() {
        return "LayoutContext{" +
                "component names=" + components.keySet() +
                ", parameters=" + parameters +
                '}';
    }
}
