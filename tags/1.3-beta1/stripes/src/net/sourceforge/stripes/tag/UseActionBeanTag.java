/* Copyright (C) 2005 Greg Hinkle, Tim Fennell
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
package net.sourceforge.stripes.tag;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.controller.ActionResolver;
import net.sourceforge.stripes.controller.ExecutionContext;
import net.sourceforge.stripes.controller.Interceptor;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.exception.StripesJspException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;

/**
 * <p>This tag supports the use of Stripes ActionBean classes as view helpers.
 * It allows for the use of actions as the controller and then their reuse
 * on the page, creating it if it does not exist. A typical usage pattern would
 * be for a page that contains two types of information, the interaction with each being
 * handled by separate ActionBean implementation. Some page events route to the first
 * action and others to the second, but the page still requires data from both in
 * order to render. This tag would define both ActionBeans in the page scope, creating
 * the one that wasn't executing the event.</p>
 *
 * <p>This class will bind parameters to a created ActionBean just as the execution of
 * an event on an ActionBean would. It does not rebind values to ActionBeans that
 * were previously created for execution of the action. Validation is not done
 * during this binding, except the type conversion required for binding, and no
 * validaiton errors are produced.</p>
 *
 * <p>The binding of the ActionBean to the page scope happens whether the ActionBean
 * is created or not, making for a consistent variable to always use when referncing
 * the ActionBean.</p>
 *
 * @author Greg Hinkle, Tim Fennell
 */
public class UseActionBeanTag extends StripesTagSupport {

    /** The UrlBinding of the ActionBean to create */
    private String binding;

    /** The event, if any, to execute when creating */
    private String event;

    /** A page scope variable to which to bind the ActionBean */
    private String var;

    /**
     * The main work method of the tag. Looks up the action bean, instantiates it,
     * runs binding and then runs either the named event or the default.
     *
     * @return SKIP_BODY in all cases.
     * @throws JspException if the ActionBean could not be instantiate and executed
     */
    public int doStartTag() throws JspException {
        // Check to see if the action bean already exists
        ActionBean actionBean = (ActionBean) pageContext.getRequest().getAttribute(binding);
        if (actionBean == null) {
            try {
                final Configuration config = StripesFilter.getConfiguration();
                HttpServletRequest request = (HttpServletRequest) getPageContext().getRequest();
                HttpServletResponse response = (HttpServletResponse) getPageContext().getResponse();

                // Setup the context stuff needed to emulate the dispatcher
                ActionBeanContext tempContext =
                        config.getActionBeanContextFactory().getContextInstance(request, response);
                ExecutionContext ctx = new ExecutionContext();
                ctx.setLifecycleStage(LifecycleStage.ActionBeanResolution);
                ctx.setActionBeanContext(tempContext);

                // Run action bean resolution
                final ActionResolver resolver = StripesFilter.getConfiguration().getActionResolver();
                ctx.setInterceptors(config.getInterceptors(LifecycleStage.ActionBeanResolution));
                ctx.wrap( new Interceptor() {
                    public Resolution intercept(ExecutionContext ec) throws Exception {
                        ActionBean bean = resolver.getActionBean(ec.getActionBeanContext(), binding);
                        ec.setActionBean(bean);
                        return null;
                    }
                });

                // Then, if and only if an event was specified, run handler resolution
                if (event != null) {
                    ctx.setLifecycleStage(LifecycleStage.HandlerResolution);
                    ctx.setInterceptors(config.getInterceptors(LifecycleStage.HandlerResolution));
                    ctx.wrap( new Interceptor() {
                        public Resolution intercept(ExecutionContext ec) throws Exception {
                            ec.setHandler(resolver.getHandler(ec.getActionBean().getClass(), event));
                            return null;
                        }
                    });
                }

                // Bind applicable request parameters to the ActionBean
                ctx.setLifecycleStage(LifecycleStage.BindingAndValidation);
                ctx.setInterceptors(config.getInterceptors(LifecycleStage.BindingAndValidation));
                ctx.wrap( new Interceptor() {
                    public Resolution intercept(ExecutionContext ec) throws Exception {
                        config.getActionBeanPropertyBinder()
                                .bind(ec.getActionBean(), ec.getActionBeanContext(), false);
                        return null;
                    }
                });

                // And (again) if an event was supplied, then run the handler
                if (event != null) {
                    ctx.setLifecycleStage(LifecycleStage.EventHandling);
                    ctx.setInterceptors(config.getInterceptors(LifecycleStage.EventHandling));
                    ctx.wrap( new Interceptor() {
                        public Resolution intercept(ExecutionContext ec) throws Exception {
                            return (Resolution) ec.getHandler().invoke(ec.getActionBean());
                        }
                    });
                }

                // Put the ActionBean in it's home in the request
                actionBean = ctx.getActionBean();
                request.setAttribute(binding, actionBean);
            }
            catch(Exception e) {
                throw new StripesJspException("Unabled to prepare ActionBean for JSP Usage",e);
            }
        }

        // If a name was specified, bind the ActionBean into page context
        if (getVar() != null) {
            pageContext.setAttribute(getVar(), actionBean);
        }

        return SKIP_BODY;
    }

    /**
     * Does nothing.
     * @return EVAL_PAGE in all cases.
     */
    public int doEndTag() { return EVAL_PAGE; }

    /**
     * Sets the binding attribute by figuring out what ActionBean class is identified
     * and then in turn finding out the appropriate URL for the ActionBean.
     *
     * @param beanclass the FQN of an ActionBean class, or a Class object for one.
     */
    public void setBeanclass(Object beanclass) throws StripesJspException {
        String url = getActionBeanUrl(beanclass);
        if (url == null) {
            throw new StripesJspException("The 'beanclass' attribute provided could not be " +
                "used to identify a valid and configured ActionBean. The value supplied was: " +
                beanclass);
        }
        else {
            this.binding = url;
        }
    }

    /** Get the UrlBinding of the requested ActionBean */
    public String getBinding() { return binding; }

    /** Set the UrlBinding of the requested ActionBean */
    public void setBinding(String binding) { this.binding = binding; }

    /** The event name, if any to execute. */
    public String getEvent() { return event; }

    /** The event name, if any to execute. */
    public void setEvent(String event) { this.event = event; }

    /** Gets the name of the page scope variable to which the ActionBean will be bound. */
    public String getVar() { return var; }

    /** Sets the name of the page scope variable to which the ActionBean will be bound. */
    public void setVar(String var) { this.var = var; }

    /** Alias for getVar() so that the JSTL and jsp:useBean style are allowed. */
    public String getId() { return getVar(); }

    /** Alias for setVar() so that the JSTL and jsp:useBean style are allowed. */
    public void setId(String id) { setVar(id); }
}
