/* Copyright 2007 Ben Gunter
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.stripes.action.ActionBean;

/**
 * Represents a URL binding as declared by a {@link net.sourceforge.stripes.action.UrlBinding}
 * annotation on an {@link ActionBean} class.
 * 
 * @author Ben Gunter
 * @since Stripes 1.5
 */
public class UrlBinding {
    protected Class<? extends ActionBean> beanType;
    protected String path, suffix;
    protected List<Object> components;
    protected List<UrlBindingParameter> parameters;

    /**
     * Create a new instance with all its members. Collections passed in will be made immutable.
     * 
     * @param beanType the {@link ActionBean} class to which this binding applies
     * @param path the path to which the action is mapped
     * @param components list of literal strings that separate the parameters
     */
    public UrlBinding(Class<? extends ActionBean> beanType, String path, List<Object> components) {
        this.beanType = beanType;
        this.path = path;
        if (components != null)
            this.components = Collections.unmodifiableList(components);

        this.parameters = new ArrayList<UrlBindingParameter>(this.components.size());
        for (Object component : components) {
            if (component instanceof UrlBindingParameter) {
                this.parameters.add((UrlBindingParameter) component);
            }
        }

        if (this.parameters.size() > 0) {
            Object last = this.components.get(this.components.size() - 1);
            if (last instanceof String) {
                this.suffix = (String) last;
            }
        }
    }

    /**
     * Create a new instance that takes no parameters.
     * 
     * @param beanType
     * @param path
     */
    public UrlBinding(Class<? extends ActionBean> beanType, String path) {
        this.beanType = beanType;
        this.path = path;
        this.components = Collections.emptyList();
        this.parameters = Collections.emptyList();
    }

    /**
     * Get the {@link ActionBean} class to which this binding applies.
     */
    public Class<? extends ActionBean> getBeanType() {
        return beanType;
    }

    /**
     * Get the list of components that comprise this binding. The components are returned in the
     * order in which they appear in the binding definition.
     */
    public List<Object> getComponents() {
        return components;
    }

    /**
     * Get the list of parameters for this binding.
     */
    public List<UrlBindingParameter> getParameters() {
        return parameters;
    }

    /**
     * Get the path for this binding. The path is the string of literal characters in the pattern up
     * to the first parameter definition.
     */
    public String getPath() {
        return path;
    }

    /**
     * If this binding includes one or more parameters and the last component is a {@link String},
     * then this method will return that last component. Otherwise, it returns null.
     */
    public String getSuffix() {
        return suffix;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(getPath());
        for (Object component : getComponents()) {
            if (component instanceof String) {
                buf.append(component);
            }
            else if (component instanceof UrlBindingParameter) {
                buf.append('{').append(component).append('}');
            }
        }
        return buf.toString();
    }
}
