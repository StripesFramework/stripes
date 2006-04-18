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
package net.sourceforge.stripes.mock;

import javax.servlet.ServletConfig;

/**
 * Mock implementation of a Servlet Config.
 *
 * @author Tim Fennell
 * @since Stripes 1.1.1
 */
public class MockServletConfig extends MockBaseConfig implements ServletConfig {
    private String servletName;

    /** Returns the name of the servlet for which this is the config. */
    public String getServletName() {
        return this.servletName;
    }

    /** Sets the name of the servlet for which this is the config. */
    public void setServletName(String name) {
        this.servletName = name;
    }
}
