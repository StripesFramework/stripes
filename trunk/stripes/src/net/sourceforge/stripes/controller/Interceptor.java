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

import net.sourceforge.stripes.action.Resolution;

/**
 * <p>Interface for classes which wish to intercept the processing of a request at various
 * stages in the Stripes lifecycle. To denote the {@link LifecycleStage} (or stages) at
 * which an interceptor should run, the class should be marked with an {@link Intercepts}
 * annotation declaring one or more lifecycle stages.</p>
 *
 * <p>{@code Interceptors} execute <i>around</i> the intercepted lifecycle stage. Assuming for
 * simplicity's sake that a single interceptor is configured, any code in the
 * {@link #intercept(ExecutionContext)} method prior to calling {@code context.proceed()} will
 * be executed immediately prior to the lifecycle code.  Any code after calling
 * {@code context.proceed()} will be executed immediately after the lifecycle code. For example
 * the following implementation would print out a message before and after validation and
 * binding occur:</p>
 *
 *<pre>
 *{@literal @}Intercepts(LifecycleStage.BindingAndValidation)
 *public class NoisyInterceptor implements Interceptor {
 *    public Resolution intercept(ExecutionContext context) {
 *        System.out.println("Before validation and binding!");
 *        Resolution r = context.proceed();
 *        System.out.println("After validation and binding!");
 *        return r;
 *    }
 *}
 *</pre>
 *
 * <p>Interceptors can, in addition to adding behaviour, divert the flow of execution. They do
 * this by returning a {@link Resolution}.  If an interceptor returns a Resolution Stripes will
 * abort processing of the current request and immediately execute the Resolution.</p>
 *
 * <p>Interceptor developers must be careful to ensure that interceptors are well behaved. To
 * continue normal processing interceptors <b>must invoke {@code context.proceed()}</b>. Since a
 * given interceptor may be part of a stack of interceptors, or the lifecycle code may return
 * a resolution, the interceptor must return the Resolution produced by {@code context.proceed()}
 * unless it explicitly wishes to alter the flow of execution.</p>
 *
 * <p>Interceptors gain access to information about the current execution environment through
 * the {@link ExecutionContext}.  Through this they can access the ActionBean, the handler
 * Method, the lifecycle stage etc.  Care must be taken to ensure that information is available
 * before using it.  For example interceptors which execute around ActionBeanResolution will not
 * have access to the current ActionBean until after calling context.proceed() and will not have
 * access to the event name or handler method at all (HandlerResolution occurs after
 * ActionBeanResolution).</p>
 *
 * <p>Optionally, Interceptor classes may implement the
 * {@link net.sourceforge.stripes.config.ConfigurableComponent} interface. If implemented,
 * the Interceptor will have it's {@code init(Configuration)} method called after instantiation
 * and before being placed into service.</p>
 *
 * <p>Interceptors are located by Stripes through it's
 * {@link net.sourceforge.stripes.config.Configuration}.  To configure interceptors you can either
 * implement your own Configuration (probably by subclassing
 * {@link net.sourceforge.stripes.config.RuntimeConfiguration}), or more likely by listing out
 * the interceptors desired in the web.xml as specified in the documentation for
 * {@link net.sourceforge.stripes.config.RuntimeConfiguration#initInterceptors()}.</p>
 *
 * @author Tim Fennell
 * @since Stripes 1.3
 */
public interface Interceptor {
    /**
     * Invoked when intercepting the flow of execution.
     *
     * @param context the ExecutionContext of the request currently being processed
     * @return the result of calling context.proceed(), or if the interceptor wishes to change
     *         the flow of execution, a Resolution
     * @throws Exception if any non-recoverable errors occur
     */
    Resolution intercept(ExecutionContext context) throws Exception;
}
