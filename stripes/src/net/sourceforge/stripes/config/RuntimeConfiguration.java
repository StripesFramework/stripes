package net.sourceforge.stripes.config;

import net.sourceforge.stripes.exception.StripesServletException;
import net.sourceforge.stripes.controller.ActionResolver;
import net.sourceforge.stripes.controller.ActionBeanPropertyBinder;

// TODO: implement this class!

/**
 *
 */
public class RuntimeConfiguration implements Configuration {
    private BootstrapPropertyResolver resolver;

    public void init(BootstrapPropertyResolver resolver) throws StripesServletException {
        this.resolver = resolver;
    }

    public BootstrapPropertyResolver getBootstrapPropertyResolver() {
        return this.resolver;
    }

    public Class<? extends ActionResolver> getActionResolver() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Class<? extends ActionBeanPropertyBinder> getActionBeanPropertyBinder() throws StripesServletException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}

