package org.stripesframework.web.controller;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.stripesframework.web.action.ActionBeanContext;


/**
 * Used as the {@link java.lang.reflect.InvocationHandler} for a dynamic proxy that replaces the
 * {@link javax.servlet.http.HttpServletResponse} on {@link
 * ActionBeanContext}s in the flash scope after the current request
 * cycle has completed.
 *
 * @author Ben Gunter
 * @since Stripes 1.4.3
 */
public class FlashResponseInvocationHandler implements InvocationHandler, Serializable {

   private static final long serialVersionUID = 1L;

   @Override
   public Object invoke( Object object, Method method, Object[] objects ) throws Throwable {
      throw new IllegalStateException("Attempt to call " + method + " after the request cycle has completed. "
            + "This is most likely due to misuse of a flashed ActionBean or ActionBeanContext " + "on the ensuing request.");
   }
}
