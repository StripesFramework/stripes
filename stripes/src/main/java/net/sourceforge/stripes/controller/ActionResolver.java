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
import net.sourceforge.stripes.exception.StripesServletException;
import net.sourceforge.stripes.config.ConfigurableComponent;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * <p>Resolvers are responsible for locating ActionBean instances that can handle the submitted
 * request.  Once an appropriate ActionBean has been identified the ActionResolver is also
 * responsible for identifying the individual method on the ActionBean class that should handle
 * this specific request.</p>
 *
 * <p>Throughout this class two terms are used that refer to similar but not interchangeable
 * concepts. {@code UrlBinding} refers to the exact URL to which a bean is bound, e.g.
 * {@code /account/Profile.action}.  {@code Path} refers to the path segment of the requested
 * URL and is generally composed of the URL binding and possibly some additional information,
 * e.g. {@code /account/Profile.action/edit}.  In general the methods in this class are capable
 * of taking in a {@code path} and extracting the {@code UrlBinding} from it.</p>
 *
 * @author Tim Fennell
 */
public interface ActionResolver extends ConfigurableComponent {
    /**
     * Key that is to be used by ActionResolvers to store, as a request attribute, the
     * action that was resolved in the current request. The 'action' stored is always a String.
     */
    String RESOLVED_ACTION = "__stripes_resolved_action";

    /**
     * Returns the URL binding that is a substring of the path provided. For example, if there
     * is an ActionBean bound to {@code /user/Profile.action}, invoking
     * {@code getUrlBindingFromPath("/user/Profile.action/view"} should return
     * {@code "/user/Profile.action"}.
     *
     * @param path the path being used to access an ActionBean, either in a form or link tag,
     *        or in a request that is hitting the DispatcherServlet.
     * @return the UrlBinding of the ActionBean appropriate for the request, or null if the path
     *         supplied cannot be mapped to an ActionBean.
     */
    String getUrlBindingFromPath(String path);

    /**
     * Resolves the Class, implementing ActionBean, that should be used to handle the request.
     * If more than one class can be mapped to the request the results of this method are undefined -
     * implementations may return one of the implementations located or throw an exception.
     *
     * @param context the ActionBeanContext for the current request
     * @return an instance of ActionBean to handle the current request
     * @throws StripesServletException thrown if a ActionBean cannot be resolved for any reason
     */
    ActionBean getActionBean(ActionBeanContext context) throws StripesServletException;

    /**
     * Returns the ActionBean class that responds to the path provided.  If the path does
     * not contain a UrlBinding to which an ActionBean is bound a StripesServletException
     * will be thrown.
     *
     * @param context the current action bean context
     * @param path the path segment of the request (or link or action)
     * @return an instance of ActionBean that is bound to the UrlBinding contained within
     *         the path supplied
     * @throws StripesServletException if a matching ActionBean cannot be found
     */
    ActionBean getActionBean(ActionBeanContext context, String path)
            throws StripesServletException;

    /**
     * Fetches the Class representing the type of ActionBean that has been bound to
     * the URL contained within the path supplied.  Will not cause any ActionBean to be
     * instantiated. If no ActionBean has been bound to the URL then null will be returned.
     *
     * @param path the path segment of a request url or form action or link
     * @return the class object for the type of action bean bound to the url, or
     *         null if no bean is bound to that url
     */
    Class<? extends ActionBean> getActionBeanType(String path);

    /**
     * Takes a class that implements ActionBean and returns the URL binding of that class.
     * Essentially the inverse of the getActionBean() methods, this method allows you to find
     * out the URL binding of any ActionBean class. The binding can then be used to generate
     * URLs or for any other purpose.  If the bean is not bound, this method may return null.
     *
     * @param clazz a class that implements ActionBean
     * @return the UrlBinding or null if none can be determined
     * @since Stripes 1.2
     */
    String getUrlBinding(Class<? extends ActionBean> clazz);

    /**
     * Determines the name of the event fired by the front end.  Allows implementations to
     * easiliy vary their strategy for specifying event names (e.g. button names, hidden field
     * rewrites via JavaScript etc.).
     *
     * @param bean the ActionBean type that has been bound to the request
     * @param context the ActionBeanContext for the current request
     * @return the name of the event fired by the front end, or null if none is found
     */
    String getEventName(Class<? extends ActionBean> bean, ActionBeanContext context);

    /**
     * Resolves the Method which handles the named event.  If more than one method is declared as
     * able to handle the event the results of this method are undefined - implementations may
     * return one of the implementations or throw an exception.
     *
     * @param bean the ActionBean type that has been bound to the request
     * @param eventName the named event being handled by the ActionBean
     * @return a Method object representing the handling method - never null
     * @throws StripesServletException thrown if a method cannot be resolved for any reason,
     *         including but not limited to, when a Method does not exist that handles the event.
     */
    Method getHandler(Class<? extends ActionBean> bean, String eventName) throws StripesServletException;

    /**
     * Locates and returns the default handler method that should be invoked when no specific
     * event is named.  This occurs most often when a user submits a form via the Enter button
     * and no button or image button name is passed.
     *
     * @param bean the ActionBean type that has been bound to the request
     * @return a Method object representing the handling method - never null
     * @throws StripesServletException thrown if a default handler method cannot be found.
     */
    Method getDefaultHandler(Class<? extends ActionBean> bean) throws StripesServletException;

    /**
     * Returns the name of the event to which a given handler method responds. Primarily useful
     * when the default event is fired and it is necessary to figure out if the handler also
     * responds to a named event.
     *
     * @param handler the handler method who's event name to find
     * @return String the name of the event handled by this method, or null if an event is
     *         not mapped to this method.
     */
    String getHandledEvent(Method handler) throws StripesServletException;

    /**
     * Get all the classes implementing {@link ActionBean} that are recognized by this
     * {@link ActionResolver}. This method must return the full set of {@link ActionBean} classes
     * after the call to init().
     */
    Collection<Class<? extends ActionBean>> getActionBeanClasses();

    /**
     * Gets the {@link ActionBean} type that matches <code>actionBeanName</code>. Implementers may use different
     * strategies for naming {@link ActionBean}s. This method can return null if no
     * action beans exist with the given <code>actionBeanName</code> or multiple action beans resolve to the same
     * <code>actionBeanName</code>
     *
     * @param actionBeanName The name that identifies the {@link ActionBean}
     * @return the ActionBean class that matches actionBeanName, or null if an ActionBean can't be resolved for the name
     */
    Class<? extends ActionBean> getActionBeanByName(String actionBeanName);
}