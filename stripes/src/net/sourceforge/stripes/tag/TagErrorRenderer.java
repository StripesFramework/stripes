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

/**
 * <p>Implementations of this interface are used to apply formatting to form input
 * fields when there are associated errors.  TagErrorRenderers can modify attributes
 * of the tags output html before and/or after the tag renders itself.</p>
 *
 * <p>If the renderer modifies attributes of the form input tag, it is also responsible
 * for re-setting those values to their prior values in the doAfterEndTag() method. If
 * this is not done correctly and the tag is pooled by the container the results on the page
 * may be pretty unexpected!</p>
 *
 * @author Greg Hinkle
 */
public interface TagErrorRenderer {

    /**
     * Initialize this renderer for a specific tag instance
     * @param tag The InputTagSuppport subclass that will be modified
     */
    void init(InputTagSupport tag);

    /**
     * Executed before the start of rendering of the input tag.
     * The input tag attributes can be modifed here to be written
     * out with other html attributes.
     */
    void doBeforeStartTag();

    /**
     * Executed after the end of rendering of the input tag, including
     * its body and end tag.
     */
    void doAfterEndTag();
}
