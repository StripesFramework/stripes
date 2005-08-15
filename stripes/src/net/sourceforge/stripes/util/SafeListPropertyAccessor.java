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
public class SafeListPropertyAccessor extends ListPropertyAccessor {
    
    public Object getProperty(Map map, Object object, Object object1) throws OgnlException {
        try {
            return super.getProperty(map, object, object1);
        }
        catch (IndexOutOfBoundsException ioobe) {
            return null;
        }
    }
}
