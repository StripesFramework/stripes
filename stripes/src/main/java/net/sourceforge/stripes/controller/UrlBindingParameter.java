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

import java.lang.reflect.Method;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.exception.StripesServletException;

/**
 * A parameter to a clean URL.
 * 
 * @author Ben Gunter
 * @since Stripes 1.5
 */
public class UrlBindingParameter {
    /** The special parameter name for the event to execute */
    public static final String PARAMETER_NAME_EVENT = "$event";

    protected Class<? extends ActionBean> beanClass;
    protected String name;
    protected String value;
    protected String defaultValue;

    /**
     * Create a new {@link UrlBindingParameter} with the given name and value. The
     * {@link #defaultValue} will be null.
     * 
     * @param name parameter name
     * @param value parameter value
     */
    public UrlBindingParameter(Class<? extends ActionBean> beanClass, String name, String value) {
        this(beanClass, name, value, null);
    }

    /**
     * Create a new {@link UrlBindingParameter} with the given name, value and default value.
     * 
     * @param name parameter name
     * @param value parameter value
     * @param defaultValue default value to use if value is null
     */
    public UrlBindingParameter(Class<? extends ActionBean> beanClass, String name, String value, String defaultValue) {
        this.beanClass = beanClass;
        this.name = name;
        this.value = value;
        this.defaultValue = defaultValue;
    }

    /**
     * Make an exact copy of the given {@link UrlBindingParameter}.
     * 
     * @param prototype a parameter
     */
    public UrlBindingParameter(UrlBindingParameter prototype) {
        this(prototype.beanClass, prototype.name, prototype.value, prototype.defaultValue);
    }

    /**
     * Make a copy of the given {@link UrlBindingParameter} except that the parameter's value will
     * be set to <code>value</code>.
     * 
     * @param prototype a parameter
     * @param value the new parameter value
     */
    public UrlBindingParameter(UrlBindingParameter prototype, String value) {
        this(prototype.beanClass, prototype.name, value, prototype.defaultValue);
    }

    /** Get the {@link ActionBean} class to which the {@link UrlBinding} applies. */
    public Class<? extends ActionBean> getBeanClass() {
        return beanClass;
    }

    /**
     * Get the parameter's default value, which may be null.
     * 
     * @return the default value
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Ensure the default event name is set if the binding uses the $event parameter.
     * Can only be done safely after the event mappings have been processed.
     * see http://www.stripesframework.org/jira/browse/STS-803
     */
    void initDefaultValueWithDefaultHandlerIfNeeded(ActionResolver actionResolver) {
        if (PARAMETER_NAME_EVENT.equals(name)) {
            Method defaultHandler;
            try {
                defaultHandler = actionResolver.getDefaultHandler(beanClass);
            } catch (StripesServletException e) {
                throw new StripesRuntimeException("Caught an exception trying to get default handler for ActionBean '" + beanClass.getName() +
                        "'. Make sure this ActionBean has a default handler.", e);
            }
            HandlesEvent annotation = defaultHandler.getAnnotation(HandlesEvent.class);
            if (annotation != null) {
                this.defaultValue = annotation.value();
            } else {
                this.defaultValue = defaultHandler.getName();
            }
        }
    }

    /**
     * Get the parameter name.
     * 
     * @return parameter name
     */
    public String getName() {
        return name;
    }

    /**
     * Return the parameter value that was extracted from a URI.
     * 
     * @return parameter value
     */
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UrlBindingParameter))
            return false;

        UrlBindingParameter that = (UrlBindingParameter) o;
        return this.value == null ? that.value == null : this.value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public String toString() {
        if (defaultValue == null)
            return name;
        else
            return name + "=" + defaultValue;
    }
}
