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

/**
 * A parameter to a clean URL.
 * 
 * @author Ben Gunter
 * @since Stripes 1.5
 */
public class UrlBindingParameter {
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
    public UrlBindingParameter(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Create a new {@link UrlBindingParameter} with the given name, value and default value.
     * 
     * @param name parameter name
     * @param value parameter value
     * @param defaultValue default value to use if value is null
     */
    public UrlBindingParameter(String name, String value, String defaultValue) {
        super();
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
        this(prototype.name, prototype.value, prototype.defaultValue);
    }

    /**
     * Make a copy of the given {@link UrlBindingParameter} except that the parameter's value will
     * be set to <code>value</code>.
     * 
     * @param prototype a parameter
     * @param value the new parameter value
     */
    public UrlBindingParameter(UrlBindingParameter prototype, String value) {
        this(prototype.name, value, prototype.defaultValue);
    }

    /**
     * Get the parameter's default value. This value will be returned by {@link #getValue()} if the
     * parameter's actual value is null. The default value may be null.
     * 
     * @return the default value
     */
    public String getDefaultValue() {
        return defaultValue;
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
     * Return the parameter value that was extracted from a URI. If the value is null, then the
     * default value will be returned.
     * 
     * @return parameter value
     */
    public String getValue() {
        return value == null ? defaultValue : value;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UrlBindingParameter))
            return false;
        UrlBindingParameter that = (UrlBindingParameter) o;
        return this.value == that.value || ((this.value != null) && this.value.equals(that.value));
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
