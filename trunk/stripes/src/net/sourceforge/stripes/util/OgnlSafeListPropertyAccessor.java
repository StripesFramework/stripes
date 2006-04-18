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
