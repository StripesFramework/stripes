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
