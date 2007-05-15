package net.sourceforge.stripes.controller;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Used as the {@link java.lang.reflect.InvocationHandler} for a dynamic proxy that replaces the
 * {@link javax.servlet.http.HttpServletResponse} on {@link
 * net.sourceforge.stripes.action.ActionBeanContext}s in the flash scope after the current request
 * cycle has completed.
 * 
 * @author Ben Gunter
 * @since Stripes 1.4.3
 */
public class FlashResponseInvocationHandler implements InvocationHandler, Serializable {
    public Object invoke(Object object, Method method, Object[] objects) throws Throwable {
        throw new IllegalStateException(
                "Attempt to call " + method + " after the request cycle has completed. " +
                "This is most likely due to misuse of a flashed ActionBean or ActionBeanContext " +
                "on the ensuing request.");
    }
}
