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
 * <p>The ExecutionContext is not populated all at once, but in pieces as the request progresses.
 * Check the accessor method for each item for information on when that item becomes available
 * in the request processing lifecycle.</p>
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
    private Resolution resolution;
    private LifecycleStage lifecycleStage;
    private boolean resolutionFromHandler = false;

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
     * Retrieves the ActionBeanContext associated with the current request. Available to all
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
     * Retrieves the handler Method that is targeted by the current request. Available
     * to interceptors only after {@link LifecycleStage#HandlerResolution} has occurred.
     *
     * @return the current ActionBean instance, or null if not yet resolved
     */
    public Method getHandler() { return handler; }

    /** Sets the handler method that will be invoked to process the current request. */
    public void setHandler(Method handler) { this.handler = handler; }

    /**
     * Gets the Resolution that will be executed at the end of the execution. This value
     * is generally not populated until just prior to {@link LifecycleStage#ResolutionExecution}.
     *
     * @return the Resolution associated with this execution
     */
    public Resolution getResolution() { return resolution; }

    /** Sets the Resolution that will be executed to terminate this execution. */
    public void setResolution(Resolution resolution) { this.resolution = resolution; }

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

    public boolean isResolutionFromHandler() {
        return resolutionFromHandler;
    }

    public void setResolutionFromHandler(boolean resolutionFromHandler) {
        this.resolutionFromHandler = resolutionFromHandler;
    }
}
