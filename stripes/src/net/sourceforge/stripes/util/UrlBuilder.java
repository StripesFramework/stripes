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
package net.sourceforge.stripes.util;

import net.sourceforge.stripes.exception.StripesRuntimeException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Map;

/**
 * <p>Simple class that encapsulates the process of building up a URL from a path fragment
 * and a zero or more parameters.  Parameters can be single valued, array valued or
 * collection valued.  In the case of arrays and collections, each value in the array or
 * collection will be added as a separate URL parameter (with the same name). The assembled
 * URL can then be retrieved by calling toString().</p>
 *
 * <p>While not immediately obvious, it is possible to add a single parameter with multiple
 * values by invoking the addParameter() method that uses varargs, and supplying a Collection as
 * the single parameter value to the method.</p>
 *
 * @author Tim Fennell
 * @since Stripes 1.1.2
 */
public class UrlBuilder {
    private StringBuilder url = new StringBuilder(256);
    boolean seenQuestionMark = false;
    private String parameterSeparator;

    /**
     * Constructs a UrlBuilder with the path to a resource. Parameters can be added
     * later using addParameter().  If the link is to be used in a page then the ampersand
     * character usually used to separate parameters will be escaped using the XML entity
     * for ampersand.
     *
     * @param url the path part of the URL
     * @param isForPage true if the URL is to be embedded in a page (e.g. in an anchor of img
     *        tag), false if for some other purpose.
     */
    public UrlBuilder(String url, boolean isForPage) {
        this.url.append(url);
        this.seenQuestionMark = this.url.indexOf("?") != -1;

        if (isForPage) {
            this.parameterSeparator = "&amp;";
        }
        else {
            this.parameterSeparator = "&";
        }
    }

    /**
     * Returns the string that will be used to separate parameters in the query string.
     * Will usually be either '&amp;' for query strings that will be embedded in HTML
     * pages and '&' otherwise.
     */
    public String getParameterSeparator() { return parameterSeparator; }

    /**
     * Sets the string that will be used to separate parameters. By default the values is a
     * single ampersand character. If the URL is to be embedded in a page the value should be
     * set to the XML ampersand entity.
     */
    public void setParameterSeparator(String parameterSeparator) {
        this.parameterSeparator = parameterSeparator;
    }

    /**
     * <p>Appends one or more values of a parameter to the URL. Checks to see if each value is
     * null, and if so generates a parameter with no value.  URL Encodes the parameter values
     * to make sure that it is safe to insert into the URL.</p>
     *
     * <p>If a single parameter value is passed, and the value is a Collection, then a parameter
     * will be added to the URL for each entry in the collection.</p.
     *
     * @param name the name of the request parameter being added
     * @param values one or more scalar values for the parameter supplied, or a single Collection
     */
    public void addParameter(String name, Object... values) {
        try {
            // Do a little special case checking to see if we were really passed
            // a collection instead of one or more scalar values
            if (values != null && values.length == 1 && values[0] instanceof Collection) {
                values = ((Collection) values[0]).toArray();
            }

            // If values is null or empty, then simply sub in a single empty string
            if (values == null || values.length == 0) {
                values = Literal.array("");
            }

            for (Object v : values) {
                // Figure out whether we already have params or not
                if (!this.seenQuestionMark) {
                    this.url.append('?');
                    this.seenQuestionMark = true;
                }
                else {
                    this.url.append(this.parameterSeparator);
                }

                this.url.append(name);
                this.url.append('=');
                if (v != null) {
                    this.url.append( URLEncoder.encode(v.toString(), "UTF-8") );
                }
            }
        }
        catch (UnsupportedEncodingException uee) {
            throw new StripesRuntimeException("Unsupported encoding?  UTF-8?  That's unpossible.");
        }
    }

    /**
     * Appends one or more parameters to the URL.  Various assumptions are made about the Map
     * parameter. Firstly, that the keys are all either Strings, or objects that can be safely
     * toString()'d to yield parameter names.  Secondly that the values either toString() to form
     * a single parameter value, or are arrays or collections that contain toString()'able
     * objects.
     *
     * @param parameters a non-null Map as described above
     */
    public void addParameters(Map<? extends Object,? extends Object> parameters) {
        for (Map.Entry<? extends Object,? extends Object> parameter : parameters.entrySet()) {
            String name = parameter.getKey().toString();
            Object valueOrValues = parameter.getValue();

            if (valueOrValues == null) {
                addParameter(name, (Object) null);
            }
            else if (valueOrValues.getClass().isArray()) {
                Object[] values = (Object[]) valueOrValues;
                addParameter(name, values);
            }
            else if (valueOrValues instanceof Collection) {
                Collection values = (Collection) valueOrValues;
                addParameter(name, values);
            }
            else {
                addParameter(name, valueOrValues);
            }
        }
    }

    /**
     * Returns the URL composed thus far as a String.  All paramter values will have been
     * URL encoded and appended to the URL before returning it.
     */
    @Override
    public String toString() {
        return this.url.toString();
    }
}
