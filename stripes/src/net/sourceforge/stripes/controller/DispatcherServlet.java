package net.sourceforge.stripes.controller;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.config.BootstrapPropertyResolver;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.config.DefaultConfiguration;
import net.sourceforge.stripes.exception.StripesServletException;
import net.sourceforge.stripes.validation.Validatable;
import net.sourceforge.stripes.validation.ValidationError;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.util.Log;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Servlet that controls how requests to the Stripes framework are processed.  Uses an instance of
 * the ActionResolver interface to locate the bean and method used to handle the current request and
 * then delegates processing to the bean.
 *
 * @author Tim Fennell
 */
public class DispatcherServlet extends HttpServlet {
    /** Key used to lookup the name of the Configuration class used to configure Stripes. */
    public static final String CONFIG_CLASS = "stripes.config";

    /** Path to a temporary directory that will be used to process file uploads. */
    protected static String temporaryDirectoryPath;

    /** Log used throughout the class. */
    private static Log log = Log.getInstance(DispatcherServlet.class);

    protected ActionResolver actionResolver;
    protected ActionBeanPropertyBinder propertyBinder;
    protected Configuration configuration;

    /**
     * Performs the necessary initialization for the Stripes controller servlet, including:
     * <ul>
     *  <li>Loading the specified Configuration classes</li>
     *  <li>Setting up a ActionResolver instance.</li>
     * </ul>
     *
     * @throws ServletException thrown if a problem is encountered initializing Stripes
     */
    public void init() throws ServletException {
        BootstrapPropertyResolver bootstrap = new BootstrapPropertyResolver(getServletConfig());
        String configurationClassName = bootstrap.getProperty(CONFIG_CLASS);

        // Set up the Configuration - if one isn't found by the bootstrapper then
        // we'll just use the DefaultConfiguration
        if (configurationClassName != null) {
            try {
                Class clazz = Class.forName(configurationClassName);
                this.configuration = (Configuration) clazz.newInstance();
            }
            catch (Exception e) {
                log.fatal(e, "Could not instantiate specified Configuration. Class name specified was ",
                          "[", configurationClassName, "].");
                throw new StripesServletException("Could not instantiate specified Configuration. " +
                    "Class name specified was [" + configurationClassName + "].", e);
            }
        }
        else {
            this.configuration = new DefaultConfiguration();
        }

        // Try to instantiate an ActionResolver
        try {
            this.actionResolver = this.configuration.getActionResolver().newInstance();
        }
        catch (Exception e) {
            log.fatal(e, "Could not instantiate specified ActionResolver. Configuration supplied ",
                "class [", this.configuration.getActionResolver(), "].");
            throw new StripesServletException("Could not instantiate specified ActionResolver. " +
                "Configuration supplied class [" + this.configuration.getActionResolver() + "].", e);
        }

        // Try to instantiate an ActionBeanPropertyBinder
        try {
            this.propertyBinder = this.configuration.getActionBeanPropertyBinder().newInstance();
            this.propertyBinder.init();
        }
        catch (Exception e) {
            log.fatal(e, "Could not instantiate specified ActionBeanPropertyBinder. ",
            "Configuration supplied class [", this.configuration.getActionBeanPropertyBinder(), "].");
            throw new StripesServletException("Could not instantiate specified ActionBeanPropertyBinder. " +
                "Configuration supplied class [" + this.configuration.getActionResolver() + "].", e);
        }

        // Figure out where the temp directory is
        File tempDir = (File) getServletContext().getAttribute("javax.servlet.context.tempdir");
        if (tempDir != null) {
            DispatcherServlet.temporaryDirectoryPath = tempDir.getAbsolutePath();
        }
        else {
            DispatcherServlet.temporaryDirectoryPath = System.getProperty("java.io.tmpdir");
        }
    }

    /** Implemented as a simple call to doPost(request, response). */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        doPost(request, response);
    }

    /**
     * Uses the configured actionResolver to locate the appropriate ActionBean type and method to handle
     * the current request.  Instantiates the ActionBean, provides it references to the request and
     * response and then invokes the handler method.
     *
     * @param servletRequest the HttpServletRequest handed to the class by the container
     * @param response the HttpServletResponse paired to the request
     * @throws ServletException thrown when the system fails to process the request in any way
     */
    protected void doPost(HttpServletRequest servletRequest, HttpServletResponse response)
        throws ServletException {

        try {
            StripesRequestWrapper request = wrapRequest(servletRequest);

            // Lookup the bean class, handler method and hook everything together
            ActionBeanContext context = createActionBeanContext(request, response);

            String beanName = this.actionResolver.getActionBeanName(context);
            Class<ActionBean> clazz = this.actionResolver.getActionBean(beanName);
            String eventName = this.actionResolver.getEventName(clazz, context);
            context.setEventName(eventName);

            Method handler = null;
            if (eventName != null) {
                handler = this.actionResolver.getHandler(clazz, eventName);
            }
            else {
                handler = this.actionResolver.getDefaultHandler(clazz);
            }

            ActionBean bean = clazz.newInstance();
            bean.setContext(context);
            request.setAttribute(beanName, bean);
            request.setAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN, bean);

            // Bind the value to the bean - this includes performing field level validation
            ValidationErrors errors = bindValues(bean, context);

            if (errors.size() == 0 && bean instanceof Validatable) {
                ((Validatable) bean).validate(errors);
            }

            if (errors.size() > 0) {
                /** Since we don't pass form name down the stack, we add it to the errors here. */
                for (List<ValidationError> listOfErrors : errors.values()) {
                    for (ValidationError error : listOfErrors) {
                        error.setFormName(beanName);
                    }
                }
                bean.getContext().setValidationErrors(errors);
                getErrorResolution(request).execute(request, response);
            }
            else  {
                Object returnValue = handler.invoke(bean);

                if (returnValue != null && returnValue instanceof Resolution) {
                    Resolution resolution = (Resolution) returnValue;
                    resolution.execute(request, response);
                }
                else {
                    log.warn("Expected handler method ", handler.getName(), " on class ",
                             clazz.getSimpleName(), " to return a Resolution. Instead it ",
                             "returned: ", returnValue);
                }
            }
        }
        catch (ServletException se) { throw se; }
        catch (InvocationTargetException ite) {
            if (ite.getTargetException() instanceof ServletException) {
                throw (ServletException) ite.getTargetException();
            }
            else {
                throw new StripesServletException
                    ("ActionBean execution threw an exception.", ite.getTargetException());
            }
        }
        catch (Exception e) {
            throw new StripesServletException("Exception encountered processing request.", e);
        }
    }

    /**
     * Wraps the HttpServletRequest with a StripesServletRequest.  This is done to ensure that any
     * form posts that contain file uploads get handled appropriately.
     *
     * @param servletRequest the HttpServletRequest handed to the dispatcher by the container
     * @return an instance of StripesRequestWrapper, which is an HttpServletRequestWrapper
     * @throws StripesServletException if the wrapper cannot be constructed
     */
    protected StripesRequestWrapper wrapRequest(HttpServletRequest servletRequest)
        throws StripesServletException {
        String tempDirPath = getTempDirectoryPath();

        StripesRequestWrapper request =
            new StripesRequestWrapper(servletRequest, tempDirPath, Integer.MAX_VALUE);
        return request;
    }

    /** Returns the path to the temporary directory that is used to store file uploads. */
    protected String getTempDirectoryPath() {
        return DispatcherServlet.temporaryDirectoryPath;
    }

    /**
     * Invokes the configured property binder in order to populate the bean's properties from the
     * values contained in the request.
     *
     * @param bean the bean to be populated
     * @param context the ActionBeanContext containing the request and other information
     */
    protected ValidationErrors bindValues(ActionBean bean, ActionBeanContext context) {
        return this.propertyBinder.bind(bean, context);
    }

    /**
     * Creates the ActionBeanContext for the current request.
     */
    protected ActionBeanContext createActionBeanContext(HttpServletRequest request,
                                                        HttpServletResponse response) {
        ActionBeanContext context = new ActionBeanContext();
        context.setRequest(request);
        context.setResponse(response);
        return context;
    }

    /**
     * Determines the page to send the user to (and how) in case of validation errors.
     */
    protected Resolution getErrorResolution(HttpServletRequest request) {
        return new ForwardResolution(request.getParameter(StripesConstants.URL_KEY_SOURCE_PAGE));
    }
}
