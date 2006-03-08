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
import net.sourceforge.stripes.exception.StripesServletException;
import net.sourceforge.stripes.config.ConfigurableComponent;

import java.lang.reflect.Method;

/**
 * Resolvers are responsible for locating ActionBean instances that can handle the submitted
 * request.  Once an appropriate ActionBean has been identified the ActionResolver is also
 * responsible for identifying the individual method on the ActionBean class that should handle
 * this specific request.
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
     * Resolves the Class, sublclassing ActionBean, that should be used to handle the request.
     * If more than one class can be mapped to the request the results of this method are undefined -
     * implementations may return one of the implementations located or throw an exception.
     *
     * @param context the ActionBeanContext for the current request
     * @return an instance of ActionBean to handle the current request
     * @throws StripesServletException thrown if a ActionBean cannot be resolved for any reason
     */
    ActionBean getActionBean(ActionBeanContext context) throws StripesServletException;

    /**
     * Returns the ActionBean class that is bound to the UrlBinding supplied.
     *
     * @param context the current action bean context
     * @param urlBinding the URL to which the ActionBean has been bound
     * @return an instance of ActionBean that is bound to the URL supplied
     * @throws StripesServletException if the UrlBinding does not match an ActionBean binding
     */
    ActionBean getActionBean(ActionBeanContext context, String urlBinding)
            throws StripesServletException;

    /**
     * Fetches the Class representing the type of ActionBean that has been bound to
     * the URL supplied.  Will not cause any ActionBean to be instantiated. If no
     * ActionBean has been bound to the URL supplied will return null.
     *
     * @param urlBinding the url to find the bound bean type
     * @return the class object for the type of action bean bound to the url, or
     *         null if no bean is bound to that url
     */
    Class<? extends ActionBean> getActionBeanType(String urlBinding);

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
     * Determines the name of th event fired by the front end.  Allows implementations to
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
}
