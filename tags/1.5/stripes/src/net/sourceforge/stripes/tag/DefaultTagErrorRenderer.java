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
 * <p>This default implementation of the TagErrorRenderer interface sets the html class
 * attribute to 'error'.  More specifically, if the tag had no previous CSS class, it
 * will have its class attribute set to error. If it previously had a CSS class attribute,
 * e.g. class="foo", then it's class attribute will be re-written as class="foo error",
 * which instructs the browser to apply both styles, with error taking precedence. The
 * use of a single class name allows applications to define a single style for all input
 * fields, and then override it for specific fields as they choose.</p>
 *
 * <p>An example of the css definition to set backgrounds to yellow by default, but
 * to red for checkboxes and radio buttons follows:</p>

 * {@code
 *   input.error { background-color: yellow; }
 *   input[type="checkbox"].error, input[type="radio"].error {background-color: red; }
 * }
 * @author Greg Hinkle, Tim Fennell
 */
public class DefaultTagErrorRenderer implements TagErrorRenderer {
    private InputTagSupport tag;
    private String oldCssClass;

    /** Simply stores the tag passed in. */
    public void init(InputTagSupport tag) {
        this.tag = tag;
    }

    /**
     * Returns the tag which is being rendered. Useful mostly when subclassing the default
     * renderer to add further functionality.
     *
     * @return the input tag being rendered
     */
    protected InputTagSupport getTag() {
        return this.tag;
    }

    /**
     * Ensures that the tag's list of CSS classes includes the "error" class.
     */
    public void doBeforeStartTag() {
        this.oldCssClass = tag.getCssClass();
        if (this.oldCssClass != null && this.oldCssClass.length() > 0) {
            tag.setCssClass("error " + this.oldCssClass);
        }
        else {
            tag.setCssClass("error");
        }
    }

    /**
     * Resets the tag's class attribute to it's original value in case the tag gets pooled.
     */
    public void doAfterEndTag() {
        tag.setCssClass(oldCssClass);
    }
}
