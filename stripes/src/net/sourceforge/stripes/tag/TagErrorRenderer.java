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
