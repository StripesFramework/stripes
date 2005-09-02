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
     * Changes the tag's class attribute to "error".
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
