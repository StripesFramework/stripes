package net.sourceforge.stripes.controller;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.exception.StripesServletException;

import java.lang.reflect.Method;

/**
 * Resolvers are responsible for locating ActionBean instances that map to short or user friendly
 * form names, as well as resolving the method on a given ActionBean that is responsible for handling
 * a specific event.
 *
 * @author Tim Fennell
 */
public interface ActionResolver {
    /**
     * This method should be implemented to return the name of the ActionBean that will be
     * returned for the given request.  Doing so will allow implementations of this interface to
     * be easily sub-classed to use different techniques to determine the name of the ActionBean.
     *
     * @param context the ActionBeanContext for the current request
     * @return String the name of the ActionBean that should be used to process the request
     * @throws StripesServletException thrown if a name cannot be determined.
     */
    String getActionBeanName(ActionBeanContext context) throws StripesServletException;

    /**
     * Resolves the Class, sublclassing ActionBean, that should be used to handle the request.
     * If more than one class implements the named form the results of this method are undefined -
     * implementations may return one of the implementations located or through an exception.
     *
     * @param actionName the logical name of the action bean to be resolved
     * @return a Class object representing a subclass of ActionBean - never null
     * @throws StripesServletException thrown if a ActionBean cannot be resolved for any reason,
     *         including, but not limited to, when a ActionBean cannot be found for the name
     *         supplied.
     */
    Class<ActionBean> getActionBean(String actionName) throws StripesServletException;

    /**
     * Determines the name of th event fired by the front end.  Allows implementations to
     * easiliy vary their strategy for specifying event names (e.g. button names, hidden field
     * rewrites via JavaScript etc.).
     *
     * @param bean the ActionBean type that has been bound to the request
     * @param context the ActionBeanContext for the current request
     * @return the name of the event fired by the front end, or null if none is found
     */
    String getEventName(Class<ActionBean> bean, ActionBeanContext context);

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
    Method getHandler(Class<ActionBean> bean, String eventName) throws StripesServletException;

    /**
     * Locates and returns the default handler method that should be invoked when no specific
     * event is named.  This occurs most often when a user submits a form via the Enter button
     * and no button or image button name is passed.
     *
     * @param bean the ActionBean type that has been bound to the request
     * @return a Method object representing the handling method - never null
     * @throws StripesServletException thrown if a default handler method cannot be found.
     */
    Method getDefaultHandler(Class<ActionBean> bean) throws StripesServletException;
}
