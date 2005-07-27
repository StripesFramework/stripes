package net.sourceforge.stripes.config;

import net.sourceforge.stripes.exception.StripesServletException;
import net.sourceforge.stripes.controller.ActionResolver;
import net.sourceforge.stripes.controller.AnnotatedClassActionResolver;
import net.sourceforge.stripes.controller.ActionBeanPropertyBinder;
import net.sourceforge.stripes.controller.OgnlActionBeanPropertyBinder;

/**
 * Centralized location for defaults for all Configuration properties.  This implementation does
 * not lookup configuration information anywhere!  It returns hard-coded defaults that will result
 * in a working system without any user intervention.
 *
 * @author Tim Fennell
 */
public class DefaultConfiguration implements Configuration {
    private BootstrapPropertyResolver resolver;

    // Interface method
    public void init(BootstrapPropertyResolver resolver) throws StripesServletException {
        this.resolver = resolver;
    }

    // Interface method
    public BootstrapPropertyResolver getBootstrapPropertyResolver() {
        return this.resolver;
    }

    /**
     * Will always return the class representing the AnnotatedClassActionResolver.
     * @return AnnotatedClassActionResolver.class
     */
    public Class<? extends ActionResolver> getActionResolver() {
        return AnnotatedClassActionResolver.class;
    }

    /**
     * Will always return the class representing OgnlActionBeanPropertyBinder.
     * @return OgnlActionBeanPropertyBinder.class
     */
    public Class<? extends ActionBeanPropertyBinder> getActionBeanPropertyBinder() {
        return OgnlActionBeanPropertyBinder.class;
    }
}
