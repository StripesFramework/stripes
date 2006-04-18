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
