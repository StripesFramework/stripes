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
import java.util.List;

/**
 * Used to override the default ListPropertyAccessor in Ognl to return nulls instead of throw
 * IndexOutOfBoundExceptions, when an attmept is made to access a List property that is not
 * within the bounds of the list.  Doing this allows the NullHandler a shot at filling in the
 * list (where possible).
 *
 * @author Tim Fennell
 */
public class OgnlSafeListPropertyAccessor extends ListPropertyAccessor {

    /**
     * Simply wraps the parent list accessor and returns null when an index out of
     * bounds exception occurrs.
     */
    public Object getProperty(Map map, Object object, Object object1) throws OgnlException {
        try {
            return super.getProperty(map, object, object1);
        }
        catch (IndexOutOfBoundsException ioobe) {
            return null;
        }
    }

    /**
     * Auto-expands the list until it is large enough for set(index) to be called on it,
     * then delegates to the parent ListPropertyAccessor to do the rest.
     *
     * @param map the OgnlContext
     * @param target the List into which an item is being inserted
     * @param property the index at which an item is being inserted
     * @param parent the parent object which contains the List
     */
    public void setProperty(Map map, Object target, Object property, Object parent) throws OgnlException {
        List list = (List) target;
        int index = (Integer) property;

        for (int i=list.size(); i<=index; ++i) {
            list.add(null);
        }

        super.setProperty(map, target, property, parent);
    }
}
