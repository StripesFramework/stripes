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
package net.sourceforge.stripes.tag;

import net.sourceforge.stripes.exception.StripesJspException;
import net.sourceforge.stripes.config.ConfigurableComponent;

/**
 * Interface that implements the logic to determine how to populate/repopulate an input tag.
 * Generally, population strategies will need to determine whether to pull the tag's value from
 * the current request's parameters, from an ActionBean (if one is present), or from a value
 * provided for the tag on the JSP.
 *
 * @author Tim Fennell 
 */
public interface PopulationStrategy extends ConfigurableComponent {
    Object getValue(InputTagSupport tag) throws StripesJspException ;
}
