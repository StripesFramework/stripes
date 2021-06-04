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
package org.stripesframework.web.controller;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;

import org.stripesframework.web.action.ActionBean;
import org.stripesframework.web.action.ActionBeanContext;
import org.stripesframework.web.action.Resolution;
import org.stripesframework.web.util.Log;


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

   private static final ThreadLocal<ExecutionContext> currentContext = new ThreadLocal<>();

   /** Get the execution context for the current thread. */
   public static ExecutionContext currentContext() {
      return currentContext.get();
   }

   private Collection<Interceptor> _interceptors;
   private Iterator<Interceptor>   _iterator;
   private Interceptor             _target;
   private ActionBeanContext       _actionBeanContext;
   private ActionBean              _actionBean;
   private Method                  _handler;
   private Resolution              _resolution;
   private LifecycleStage          _lifecycleStage;
   private boolean                 _resolutionFromHandler = false;

   /**
    * Retrieves the ActionBean instance that is associated with the current request. Available
    * to interceptors only after {@link LifecycleStage#ActionBeanResolution} has occurred.
    *
    * @return the current ActionBean instance, or null if not yet resolved
    */
   public ActionBean getActionBean() { return _actionBean; }

   /**
    * Retrieves the ActionBeanContext associated with the current request. Available to all
    * interceptors regardless of {@link LifecycleStage}.
    *
    * @return the current ActionBeanContext
    */
   public ActionBeanContext getActionBeanContext() {
      return _actionBeanContext;
   }

   /**
    * Retrieves the handler Method that is targeted by the current request. Available
    * to interceptors only after {@link LifecycleStage#HandlerResolution} has occurred.
    *
    * @return the current handler method, or null if not yet resolved
    */
   public Method getHandler() { return _handler; }

   /**
    * Gets the current LifecycleStage being processed. This is always set to the appropriate
    * lifecycle stage before invoking any interceptors or lifecycle code, so that interceptors
    * that intercept at multiple lifecycle stages can be aware of which stage is being
    * intercepted.
    *
    * @return the LifecycleStage currently being processed/intercepted
    */
   public LifecycleStage getLifecycleStage() { return _lifecycleStage; }

   /**
    * Gets the Resolution that will be executed at the end of the execution. This value
    * is generally not populated until just prior to {@link LifecycleStage#ResolutionExecution}.
    *
    * @return the Resolution associated with this execution
    */
   public Resolution getResolution() { return _resolution; }

   public boolean isResolutionFromHandler() {
      return _resolutionFromHandler;
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
      if ( _iterator == null ) {
         log.debug("Transitioning to lifecycle stage ", _lifecycleStage);
         _iterator = _interceptors.iterator();
      }

      if ( _iterator.hasNext() ) {
         return _iterator.next().intercept(this);
      } else {
         return _target.intercept(this);
      }
   }

   /** Sets the ActionBean associated with the current request. */
   public void setActionBean( ActionBean actionBean ) { _actionBean = actionBean; }

   /** Sets the ActionBeanContext for the current request. */
   public void setActionBeanContext( ActionBeanContext actionBeanContext ) {
       _actionBeanContext = actionBeanContext;
   }

   /** Sets the handler method that will be invoked to process the current request. */
   public void setHandler( Method handler ) { _handler = handler; }

   /**
    * Used by the {@link DispatcherServlet} to initialize and/or swap out the list of
    * {@link Interceptor} instances which should wrap the current {@link LifecycleStage}.
    *
    * @param stack a non-null (though possibly empty) ordered collection of interceptors
    */
   public void setInterceptors( Collection<Interceptor> stack ) {
      _interceptors = stack;
   }

   /** Sets the current stage in the request processing lifecycle. */
   public void setLifecycleStage( LifecycleStage lifecycleStage ) {
       _lifecycleStage = lifecycleStage;
   }

   /** Sets the Resolution that will be executed to terminate this execution. */
   public void setResolution( Resolution resolution ) { _resolution = resolution; }

   public void setResolutionFromHandler( boolean resolutionFromHandler ) {
       _resolutionFromHandler = resolutionFromHandler;
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
   public Resolution wrap( Interceptor target ) throws Exception {
       _target = target;
      _iterator = null;

      // Before executing RequestInit, set this as the current execution context
      if ( _lifecycleStage == LifecycleStage.RequestInit ) {
         currentContext.set(this);
      }

      try {
         return proceed();
      }
      finally {
         // Make sure the current execution context gets cleared after RequestComplete
         if ( LifecycleStage.RequestComplete == getLifecycleStage() ) {
            currentContext.set(null);
         }
      }
   }
}
