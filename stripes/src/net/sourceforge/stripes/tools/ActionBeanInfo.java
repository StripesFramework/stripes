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
