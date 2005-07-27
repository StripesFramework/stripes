package net.sourceforge.stripes.util;

import ognl.OgnlContext;
import ognl.OgnlRuntime;
import ognl.ObjectNullHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.lang.reflect.Method;
import java.lang.reflect.Array;

/**
 * NullHandler implementation used to replace the default one that ships with Ognl. During setValue
 * operations it will ensure that the object graph is fully formed so that the target property can
 * be set.  During get operations it will create intermediary objects as necessary, but not add
 * them to the object graph.  This allows callers to discover information about the object graph
 * during get operations without mutating the object graph.
 *
 * @author Tim Fennell
 */
public class OgnlCustomNullHandler extends ObjectNullHandler {
    /** Log object used by the class to log messages. */
    private Log log = LogFactory.getLog(OgnlCustomNullHandler.class);

    /**
     * Figures out what to do when Ognl encounters a null value in one of the expressions that it
     * is evaluating.  The rules followed are as follows:
     * <ul>
     *   <li>If the expression is being evaluated to set an value, the null intermediate object is
     *       instantiated and set on its parent object.
     *   </li>
     *   <li>If the expression is a get expression and the null value is an intermediary value then
     *       the intermediary is <em>temporarily</em> instantiated but the object is never set on
     *       it's parent object
     *   </li>
     *   <li>If the expression is a get expression and the null value is the result of the final
     *       node in the expression, null is returned.
     *   </li>
     * </ul>
     *
     * @param context the OgnlContext, actually of type OgnlContext
     * @param target the object on which the null property was found
     * @param property really a String, the name of the property being retrieved
     * @return the swapped in value if one was created otherwise null
     */
    public Object nullPropertyValue(Map context, Object target, Object property) {
        Object result = null;
        OgnlContext ctx = (OgnlContext) context;
        String propertyName = (String) property;

        try {
            int indexInParent = ctx.getCurrentEvaluation().getNode().getIndexInParent();
            int maxIndex = ctx.getRootEvaluation().getNode().jjtGetNumChildren() -1 ;

            // If the null value isn't the terminal value in the expression...
            if (indexInParent != maxIndex) {
                // Get the set method, determine the type of object that was null, and make one!
                Method method =
                    OgnlRuntime.getSetMethod(ctx, target.getClass(), propertyName);
                Class clazzes[] = method.getParameterTypes();

                // If the target type is an array we have to instantiate it a little differently
                if (clazzes[0].isArray()) {
                    result = Array.newInstance(clazzes[0].getComponentType(), 0);
                }
                else if (clazzes[0].isEnum()) {
                    result = clazzes[0].getEnumConstants()[0];
                }
                else {
                    result = clazzes[0].newInstance();
                }

                // Now, if the caller was doing a set operation, lets make this change permanent
                if (ctx.getRootEvaluation().isSetOperation()) {
                    Object[] args = new Object[1];
                    args[0] = result;
                    method.invoke(target, args);
                }
            }
        }
        catch (Exception e) { // There's really not a lot we can do about this.
            log.info("Problem encountered trying to create and set property [" + propertyName +
                "] on object of type [" + target.getClass().getName() + "].");
        }

        return result;
    }
}