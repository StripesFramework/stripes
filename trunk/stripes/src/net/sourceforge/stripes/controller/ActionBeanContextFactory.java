package net.sourceforge.stripes.controller;

import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.config.ConfigurableComponent;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * Interface for classes that can instantiate and supply new instances of the
 * ActionBeanContext class, or subclasses thereof.
 *
 * @author Tim Fennell
 */
public interface ActionBeanContextFactory extends ConfigurableComponent {


    /**
     * Creates and returns a new instance of ActionBeanContext or a subclass.
     *
     * @param request the current HttpServletRequest
     * @param response the current HttpServletResponse
     * @return a new instance of ActionBeanContext
     * @throws ServletException if the ActionBeanContext class configured cannot be instantiated
     */
    ActionBeanContext getContextInstance(HttpServletRequest request,
                                         HttpServletResponse response) throws ServletException;
}