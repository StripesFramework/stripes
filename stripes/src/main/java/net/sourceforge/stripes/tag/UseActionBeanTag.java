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
package net.sourceforge.stripes.tag;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.controller.ActionResolver;
import net.sourceforge.stripes.controller.DispatcherHelper;
import net.sourceforge.stripes.controller.ExecutionContext;
import net.sourceforge.stripes.controller.Interceptor;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.controller.DispatcherServlet;
import net.sourceforge.stripes.exception.StripesJspException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.JspException;

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
 * validation errors are produced.</p>
 *
 * <p>The binding of the ActionBean to the page scope happens whether the ActionBean
 * is created or not, making for a consistent variable to always use when referencing
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

    /** Indicates that validation should be executed. */
    private boolean validate = false;

    /** Indicates whether the event should be executed even if the bean was already present. */
    private boolean alwaysExecuteEvent = false;

    /** Indicates whether the resolution should be executed - false by default. */
    private boolean executeResolution = false;

    /**
     * The main work method of the tag. Looks up the action bean, instantiates it,
     * runs binding and then runs either the named event or the default.
     *
     * @return SKIP_BODY in all cases.
     * @throws JspException if the ActionBean could not be instantiate and executed
     */
    @Override
    public int doStartTag() throws JspException {
        // Check to see if the action bean already exists
        ActionBean actionBean = (ActionBean) getPageContext().findAttribute(binding);
        boolean beanNotPresent = actionBean == null;

        try {
            final Configuration config = StripesFilter.getConfiguration();
            final ActionResolver resolver = StripesFilter.getConfiguration().getActionResolver();
            final HttpServletRequest request = (HttpServletRequest) getPageContext().getRequest();
            final HttpServletResponse response = (HttpServletResponse) getPageContext().getResponse();
            Resolution resolution = null;
            ExecutionContext ctx = new ExecutionContext();

            // Lookup the ActionBean if we don't already have it
            if (beanNotPresent) {
                ActionBeanContext tempContext =
                        config.getActionBeanContextFactory().getContextInstance(request, response);
                tempContext.setServletContext(getPageContext().getServletContext());
                ctx.setLifecycleStage(LifecycleStage.ActionBeanResolution);
                ctx.setActionBeanContext(tempContext);

                // Run action bean resolution
                ctx.setInterceptors(config.getInterceptors(LifecycleStage.ActionBeanResolution));
                resolution = ctx.wrap( new Interceptor() {
                    public Resolution intercept(ExecutionContext ec) throws Exception {
                        ActionBean bean = resolver.getActionBean(ec.getActionBeanContext(), binding);
                        ec.setActionBean(bean);
                        return null;
                    }
                });
            }
            else {
                ctx.setActionBean(actionBean);
                ctx.setActionBeanContext(actionBean.getContext());
            }

            // Then, if and only if an event was specified, run handler resolution
            if (resolution == null && event != null && (beanNotPresent || this.alwaysExecuteEvent)) {
                ctx.setLifecycleStage(LifecycleStage.HandlerResolution);
                ctx.setInterceptors(config.getInterceptors(LifecycleStage.HandlerResolution));
                resolution = ctx.wrap( new Interceptor() {
                    public Resolution intercept(ExecutionContext ec) throws Exception {
                        ec.setHandler(resolver.getHandler(ec.getActionBean().getClass(), event));
                        ec.getActionBeanContext().setEventName(event);
                        return null;
                    }
                });
            }

            // Make the PageContext available during the validation stage so that we
            // can execute EL based expression validation
            try {
                DispatcherHelper.setPageContext(getPageContext());

                // Bind applicable request parameters to the ActionBean
                if (resolution == null && (beanNotPresent || this.validate == true)) {
                    resolution = DispatcherHelper.doBindingAndValidation(ctx, this.validate);
                }

                // Run custom validations if we're validating
                if (resolution == null && this.validate == true) {
                    String temp =  config.getBootstrapPropertyResolver().getProperty(
                                        DispatcherServlet.RUN_CUSTOM_VALIDATION_WHEN_ERRORS);
                    boolean validateWhenErrors = temp != null && Boolean.valueOf(temp);

                    resolution = DispatcherHelper.doCustomValidation(ctx, validateWhenErrors);
                }
            }
            finally {
                DispatcherHelper.setPageContext(null);
            }

            // Fill in any validation errors if they exist
            if (resolution == null && this.validate == true) {
                resolution = DispatcherHelper.handleValidationErrors(ctx);
            }

            // And (again) if an event was supplied, then run the handler
            if (resolution == null && event != null && (beanNotPresent || this.alwaysExecuteEvent)) {
                resolution = DispatcherHelper.invokeEventHandler(ctx);
            }

            DispatcherHelper.fillInValidationErrors(ctx);  // just in case!

            if (resolution != null && this.executeResolution) {
                DispatcherHelper.executeResolution(ctx, resolution);
            }

            // If a name was specified, bind the ActionBean into page context
            if (getVar() != null) {
                pageContext.setAttribute(getVar(), ctx.getActionBean());
            }

            return SKIP_BODY;
        }
        catch(Exception e) {
            throw new StripesJspException("Unabled to prepare ActionBean for JSP Usage",e);
        }
    }

    /**
     * Does nothing.
     * @return EVAL_PAGE in all cases.
     */
    @Override
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

    public boolean isValidate() { return validate; }

    public void setValidate(boolean validate) { this.validate = validate; }

    public boolean isAlwaysExecuteEvent() { return alwaysExecuteEvent; }

    public void setAlwaysExecuteEvent(boolean alwaysExecuteEvent) {
        this.alwaysExecuteEvent = alwaysExecuteEvent;
    }

    public boolean isExecuteResolution() { return executeResolution; }

    public void setExecuteResolution(boolean executeResolution) {
        this.executeResolution = executeResolution;
    }
}
