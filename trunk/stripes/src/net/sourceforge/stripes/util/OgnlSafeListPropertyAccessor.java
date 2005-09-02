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
package net.sourceforge.stripes.util;

import ognl.ListPropertyAccessor;
import ognl.OgnlException;

import java.util.Map;

/**
 * Used to override the default ListPropertyAccessor in Ognl to return nulls instead of throw
 * IndexOutOfBoundExceptions, when an attmept is made to access a List property that is not
 * within the bounds of the list.  Doing this allows the NullHandler a shot at filling in the
 * list (where possible).
 *
 * @author Tim Fennell
 */
public class OgnlSafeListPropertyAccessor extends ListPropertyAccessor {
    
    public Object getProperty(Map map, Object object, Object object1) throws OgnlException {
        try {
            return super.getProperty(map, object, object1);
        }
        catch (IndexOutOfBoundsException ioobe) {
            return null;
        }
    }
}
