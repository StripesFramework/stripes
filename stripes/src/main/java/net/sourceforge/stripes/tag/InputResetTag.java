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
 * Tag that generates HTML form fields of type {@literal <input type="reset" ... />} which render
 * buttons for resetting forms. The only capability offered above and beyond a pure html tag is the
 * ability to look up the value of the button (i.e. the text on the button that the user sees) from
 * a localized resource bundle. For more details on operation see {@link
 * net.sourceforge.stripes.tag.InputButtonSupportTag}.
 *
 * @author Tim Fennell
 */
public class InputResetTag extends InputButtonSupportTag {
  /** Sets the input tag type to be reset. */
  public InputResetTag() {
    getAttributes().put("type", "reset");
  }
}
