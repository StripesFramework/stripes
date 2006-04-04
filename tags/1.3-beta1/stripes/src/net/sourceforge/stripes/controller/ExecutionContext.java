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
package net.sourceforge.stripes.controller;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.util.Log;

import java.util.Collection;
import java.util.Iterator;
import java.lang.reflect.Method;

/**
 * <p>Holds the execution context for processing a single request. The ExecutionContext is made
 * available to {@link Interceptor} classes that are interleaved with the regular request
 * processing lifecycle.</p>
 *
 * <p>The ExecutionContext is not populated all at once, but in pieces as the request progreses.
 * Check the accessor method for each item for information on when that item becomes available
 * in the reuest processing lifecycle.</p>
 *
 * @author Tim Fennell
 * @since Stripes 1.3
 */
public class ExecutionContext {
    private static final Log log = Log.getInstance(ExecutionContext.class);

    private Collection<Interceptor> interceptors;
    private Iterator<Interceptor> iterator;
    private Interceptor target;
    private ActionBeanContext actionBeanContext;
    private ActionBean actionBean;
    private Method handler;
    private LifecycleStage lifecycleStage;

    /**
     * Used by the {@link DispatcherServlet} to initialize and/or swap out the list of
     * {@link Interceptor} instances which should wrap the current {@link LifecycleStage}.
     *
     * @param stack a non-null (though possibly empty) ordered collection of interceptors
     */
    public void setInterceptors(Collection<Interceptor> stack) {
        this.interceptors = stack;
    }

    /**
     * Used by the {@link DispatcherServlet} to wrap a block of lifecycle code in
     * {@link Interceptor} calls.
     *
     * @param target a block of lifecycle/request processing code that is contained inside
     *        a class that implements Interceptor
     * @return a Resolution instance or null depending on what is returned by the lifecycle
     *         code and any interceptors which intercept the execution
     * @throws Exception if the lifecycle code or an interceptor throws an Exception
     */
    public Resolution wrap(Interceptor target) throws Exception {
        this.target = target;
        this.iterator = null;
        return proceed();
    }

    /**
     * Retreives the ActionBeanContext associated with the current request. Available to all
     * interceptors regardless of {@link LifecycleStage}.
     *
     * @return the current ActionBeanContext
     */
    public ActionBeanContext getActionBeanContext() {
        return actionBeanContext;
    }

    /** Sets the ActionBeanContext for the current request. */
    public void setActionBeanContext(ActionBeanContext actionBeanContext) {
        this.actionBeanContext = actionBeanContext;
    }

    /**
     * Retrieves the ActionBean instance that is associated with the current request. Available
     * to interceptors only after {@link LifecycleStage#ActionBeanResolution} has occurred.
     *
     * @return the current ActionBean instance, or null if not yet resolved
     */
    public ActionBean getActionBean() { return actionBean; }

    /** Sets the ActionBean associated with the current request. */
    public void setActionBean(ActionBean actionBean) { this.actionBean = actionBean; }

    /**
     * Retrieves the handler Method that is targetted by the current request. Available
     * to interceptors only after {@link LifecycleStage#HandlerResolution} has occurred.
     *
     * @return the current ActionBean instance, or null if not yet resolved
     */
    public Method getHandler() { return handler; }

    /** Sets the handler method that will be invoked to process the current request. */
    public void setHandler(Method handler) { this.handler = handler; }

    /**
     * Gets the current LifecycleStage being processed. This is always set to the appropriate
     * lifecycle stage before invoking any interceptors or lifecycle code, so that interceptors
     * that intercept at multiple lifecycle stages can be aware of which stage is being
     * intercepted.
     *
     * @return the LifecycleStage currently being processed/intercepted
     */
    public LifecycleStage getLifecycleStage() { return lifecycleStage; }

    /** Sets the current stage in the request processing lifecycle. */
    public void setLifecycleStage(LifecycleStage lifecycleStage) {
        this.lifecycleStage = lifecycleStage;
    }

    /**
     * Continues the flow of execution. If there are more interceptors in the stack intercepting
     * the current lifecycle stage then the flow continues by calling the next interceptor. If
     * there are no more interceptors then the lifecycle code is invoked.
     *
     * @return a Resolution if the lifecycle code or one of the interceptors returns one
     * @throws Exception if the lifecycle code or one of the interceptors throws one
     */
    public Resolution proceed() throws Exception {
        if (this.iterator == null) {
            log.debug("Transitioning to lifecycle stage ", lifecycleStage);
            this.iterator = this.interceptors.iterator();
        }

        if (this.iterator.hasNext()) {
            return this.iterator.next().intercept(this);
        }
        else {
            return this.target.intercept(this);
        }
    }
}
