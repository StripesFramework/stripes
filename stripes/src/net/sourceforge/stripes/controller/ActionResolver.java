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
     * @return a Class object representing a subclass of ActionBean - never null
     * @throws StripesServletException thrown if a ActionBean cannot be resolved for any reason
     */
    Class<ActionBean> getActionBean(ActionBeanContext context) throws StripesServletException;

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
