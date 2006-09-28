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
package net.sourceforge.stripes.action;

import net.sourceforge.stripes.util.UrlBuilder;
import net.sourceforge.stripes.controller.StripesFilter;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Abstract class that provides a consistent API for all Resolutions that send the user onward to
 * another view - either by forwarding, redirecting or some other mechanism.  Provides methods
 * for getting and setting the path that the user should be sent to next.</p>
 *
 * <p>The rather odd looking generic declaration on this class is called a self-bounding generic
 * type. The declaration allows methods in this class like {@link #addParameter(String, Object...)}
 * to return the appropriate type when accessed through subclasses.  I.e.
 * {@code RedirectResolution.addParameter(String, Object...)} will return a reference of type
 * RedirectResolution instead of OnwardResolution.</p>
 *
 * @author Tim Fennell
 */
public abstract class OnwardResolution<T extends OnwardResolution<T>> {
    protected String path;
    Map<String,Object> parameters = new HashMap<String,Object>();

    /**
     * Default constructor that takes the supplied path and stores it for use.
     * @param path the path to which the resolution should navigate
     */
    public OnwardResolution(String path) {
        this.path = path;
    }

    /**
     * Constructor that will extract the url binding for the ActionBean class supplied and
     * use that as the path for the resolution.
     *
     * @param beanType a Class that represents an ActionBean
     */
    public OnwardResolution(Class<? extends ActionBean> beanType) {
        this(StripesFilter.getConfiguration().getActionResolver().getUrlBinding(beanType));
    }

    /**
     * Constructor that will extract the url binding for the ActionBean class supplied and
     * use that as the path for the resolution and adds a parameter to ensure that the
     * specified event is invoked.
     *
     * @param beanType a Class that represents an ActionBean
     * @param event the String name of the event to trigger on navigation
     */
    public OnwardResolution(Class<? extends ActionBean> beanType, String event) {
        this(beanType);
        addParameter(event);
    }

    /** Accessor for the path that the user should be sent to. */
    public String getPath() {
        return path;
    }

    /** Setter for the path that the user should be sent to. */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Method that will work for this class and subclasses; returns a String containing the
     * class name, and the path to which it will send the user.
     */
    public String toString() {
        return getClass().getSimpleName() + "{" +
            "path='" + path + "'" +
            "}";
    }

    /**
     * Adds a request parameter with zero or more values to the URL.  Values may
     * be supplied using varargs, or alternatively by suppling a single value parameter which is
     * an instance of Collection.
     *
     * @param name the name of the URL parameter
     * @param values zero or more scalar values, or a single Collection
     * @return this Resolution so that methods can be chained
     */
    public T addParameter(String name, Object... values) {
        this.parameters.put(name, values);
        return (T) this;
    }

    /**
     * Bulk adds one or more request parameters to the URL. Each entry in the Map
     * represents a single named parameter, with the values being either a scalar value,
     * an array or a Collection.
     *
     * @param parameters a Map of parameters as described above
     * @return this Resolution so that methods can be chained
     */
    public T addParameters(Map<String,Object> parameters) {
        this.parameters.putAll(parameters);
        return (T) this;
    }

    /**
     * Constructs the URL for the resolution by taking the path and appending any parameters
     * supplied.
     */
    public String getUrl() {
        UrlBuilder builder = new UrlBuilder(path, false);
        builder.addParameters(this.parameters);
        return builder.toString();
    }
}
