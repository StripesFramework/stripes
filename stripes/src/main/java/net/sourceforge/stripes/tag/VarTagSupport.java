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
package net.sourceforge.stripes.tag;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Provides support for tags that allow assigning a value in a named scope. The
 * "var" and "scope" properties are provided, as is an {@link #export(Object)}
 * method that assigns a value to the given name in the given scope.
 * 
 * @author Ben Gunter
 */
public abstract class VarTagSupport extends StripesTagSupport {
    protected String var;
    protected String scope;

    /** Get the scope in which the value will be stored */
    public String getScope() {
        return scope;
    }

    /** Set the scope in which the value will be stored */
    public void setScope(String scope) {
        this.scope = scope;
    }

    /** Get the name of the variable to which the value will be assigned */
    public String getVar() {
        return var;
    }

    /** Set the name of the variable to which the value will be assigned */
    public void setVar(String var) {
        this.var = var;
    }

    /**
     * Assigns the <code>value</code> to an attribute named by
     * <code>var</code> in the named <code>scope</code>.
     * 
     * @param value
     *            the object to be exported
     */
    protected void export(Object value) {
        if ("request".equals(scope)) {
            pageContext.getRequest().setAttribute(var, value);
        }
        else if ("session".equals(scope)) {
            ((HttpServletRequest) pageContext.getRequest()).getSession()
                    .setAttribute(var, value);
        }
        else if ("application".equals(scope)) {
            pageContext.getServletContext().setAttribute(var, value);
        }
        else {
            pageContext.setAttribute(var, value);
        }
    }
}
