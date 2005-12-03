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
package net.sourceforge.stripes.tools;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Encapsulates meta-information about a single event within an ActionBean. This class is not
 * used at runtime in Stripes, but is used by the {@link SiteStructureTool} to collect and
 * report on information about ActionBeans.
 *
 * @author Tim Fennell
 * @since Stripes 1.1.2
 */
public class EventInfo {
    private String name;
    private String methodName;
    private boolean defaultEvent;
    private Collection<String> resolutions = new ArrayList<String>();

    /** The name of the event (possibly null in the case of default events). */
    public String getName() { return name; }

    /** The name of the event (possibly null in the case of default events). */
    public void setName(String name) { this.name = name; }

    /** The name of the method in the ActionBean that handles the event. */
    public String getMethodName() { return methodName; }

    /** The name of the method in the ActionBean that handles the event. */
    public void setMethodName(String methodName) { this.methodName = methodName; }

    /** True if the event is the default event for the ActionBean, false otherwise. */
    public boolean isDefaultEvent() { return defaultEvent; }

    /** True if the event is the default event for the ActionBean, false otherwise. */
    public void setDefaultEvent(boolean defaultEvent) { this.defaultEvent = defaultEvent; }

    /** Adds a resolution to the set of possible resolutions for the event. */
    public void addResolution(String outcome) { this.resolutions.add(outcome); }

    /** The set of all possible resolutions for the event. */
    public Collection<String> getResolutions() { return resolutions; }

    /** The set of all possible resolutions for the event. */
    public void setResolutions(Collection<String> resolutions) { this.resolutions = resolutions; }
}
