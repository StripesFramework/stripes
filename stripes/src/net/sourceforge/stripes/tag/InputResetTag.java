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
package net.sourceforge.stripes.tag;

/**
 * <p>Tag that generates HTML form fields of type {@literal <input type="reset" ... />} which
 * render buttons for resetting forms.  The only capability offered above and beyond a pure
 * html tag is the ability to lookup the value of the button (i.e. the text on the button that the
 * user sees) from a localized resource bundle. For more details on operation see
 * {@link net.sourceforge.stripes.tag.InputButtonSupportTag}.
 *
 * @author Tim Fennell
 */
public class InputResetTag extends InputButtonSupportTag {
    /** Sets the input tag type to be reset. */
    public InputResetTag() {
        super();
        getAttributes().put("type", "reset");
    }
}
