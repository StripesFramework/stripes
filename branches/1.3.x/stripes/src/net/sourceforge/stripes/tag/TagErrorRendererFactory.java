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

import net.sourceforge.stripes.config.ConfigurableComponent;

/**
 * Constructs and returns an instance of TagErrorRenderer to handle the
 * error output of a specific form input tag.
 *
 * @author Greg Hinkle
 */
public interface TagErrorRendererFactory extends ConfigurableComponent {


    /**
     * Returns a new instance of a TagErrorRenderer that is utilized
     * by the supplied tag.
     * @param tag The tag that needs to be error renderered
     * @return TagErrorRenderer the error renderer to render the error output
     */
    public TagErrorRenderer getTagErrorRenderer(InputTagSupport tag);

}
