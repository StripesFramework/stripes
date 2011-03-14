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

import javax.servlet.FilterConfig;

/**
 * Mock implementation of the FilterConfig interface from the Http Servlet spec.
 *
 * @author Tim Fennell
 * @since Stripes 1.1.1
 */
public class MockFilterConfig extends MockBaseConfig implements FilterConfig {
    private String filterName;

    /** Sets the filter name that will be retrieved by getFilterName(). */
    public void setFilterName(String filterName) { this.filterName = filterName; }

    /** Returns the name of the filter for which this is the config. */
    public String getFilterName() { return this.filterName; }
}
