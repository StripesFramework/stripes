package net.sourceforge.stripes.controller;

import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.exception.StripesServletException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Implements an ActionBeanContextFactory that allows for instantiation of application specific
 * ActionBeanContext classes. Looks for a configuration parameters called "ActionBeanContext.Class".
 * If the property is present, the named class with be instantiated and returned from the
 * getContextInstance() method.  If no class is named, then the default class, ActionBeanContext
 * will be instanted.
 *
 * @author Tim Fennell
 */
public class DefaultActionBeanContextFactory implements ActionBeanContextFactory {
    /** The name of the configuration property used for the context class name. */
    public static final String CONTEXT_CLASS_NAME = "ActionBeanContext.Class";

    private Configuration configuration;
    private Class<? extends ActionBeanContext> contextClass;

    /** Stores the configuration, and looks up the ActionBeanContext class specified. */
    public void init(Configuration configuration) throws Exception {
        this.configuration = configuration;

        String name = configuration.getBootstrapPropertyResolver().getProperty(CONTEXT_CLASS_NAME);
        if (name != null) {
            this.contextClass = (Class<? extends ActionBeanContext>) Class.forName(name);
        }
        else {
            this.contextClass = ActionBeanContext.class;
        }
    }

    /**
     * Returns a new instance of the configured class, or ActionBeanContext if a class is
     * not specified.
     */
    public ActionBeanContext getContextInstance(HttpServletRequest request,
                                                HttpServletResponse response) throws ServletException {
        try {
            ActionBeanContext context = this.contextClass.newInstance();
            context.setRequest(request);
            context.setResponse(response);
            return context;
        }
        catch (Exception e) {
            throw new StripesServletException("Could not instantiate configured " +
            "ActionBeanContext class: " + this.contextClass, e);
        }
    }
}
