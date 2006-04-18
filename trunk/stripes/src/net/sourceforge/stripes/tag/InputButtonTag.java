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
 * <p>Tag that generates HTML form fields of type {@literal <input type="button" ... />} which
 * render buttons for submitting forms.  The only capability offered above and beyond a pure
 * html tag is the ability to lookup the value of the button (i.e. the text on the button that the
 * user sees) from a localized resource bundle. For more details on operation see
 * {@link InputButtonSupportTag}.
 *
 * @author Tim Fennell
 */
public class InputButtonTag extends InputButtonSupportTag {
    /** Sets the input tag type to be button. */
    public InputButtonTag() {
        super();
        getAttributes().put("type", "button");
    }
}
