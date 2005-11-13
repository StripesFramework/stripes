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

import net.sourceforge.stripes.action.UrlBinding;

import java.util.Map;
import java.util.TreeMap;

/**
 * Encapsulates meta-information about an ActionBean, namely it's UrlBinding and set of events
 * to which it responds. This class is not used at runtime in Stripes, but is used by the
 * {@link SiteStructureTool} to collect and report on information about ActionBeans.
 *
 * @author Tim Fennell
 * @since Stripes 1.1.2
 */
public class ActionBeanInfo {
    private String className;
    private UrlBinding urlBinding;
    private Map<String,EventInfo> events = new TreeMap<String,EventInfo>();
    private EventInfo defaultEvent;

    /** The class name of the ActionBean that this object describes. */
    public String getClassName() { return className; }

    /** The class name of the ActionBean that this object describes. */
    public void setClassName(String className) { this.className = className; }

    /** The UrlBinding extracted from the ActionBean. */
    public UrlBinding getUrlBinding() { return urlBinding; }

    /** The UrlBinding extracted from the ActionBean. */
    public void setUrlBinding(UrlBinding urlBinding) { this.urlBinding = urlBinding; }

    /**
     * Adds the supplied event to the events for this ActionBean. If the event is the default
     * event it will be stored separately, and not as part of the Map of events.
     */
    public void addEvent(EventInfo eventInfo) {
        if (eventInfo.isDefaultEvent()) {
            this.defaultEvent = eventInfo;
        }
        else {
            this.events.put(eventInfo.getName(), eventInfo);
        }
    }

    /** Gets a Map of event name to EventInfo, excluding the default event. */
    public Map<String, EventInfo> getEvents() { return events; }

    /** Gets the default event for this ActionBean, if there is one. */
    public EventInfo getDefaultEvent() {
        return this.defaultEvent;
    }
}
