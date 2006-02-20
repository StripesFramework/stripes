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
