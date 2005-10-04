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
