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
