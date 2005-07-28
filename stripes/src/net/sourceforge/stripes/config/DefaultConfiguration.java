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

    /** Stores a reference to the resolver handed in by the dispatcher. */
    public void init(BootstrapPropertyResolver resolver) throws StripesServletException {
        this.resolver = resolver;
    }

    /** Returns a reference to the resolver supplied at initialziation time. */
    public BootstrapPropertyResolver getBootstrapPropertyResolver() {
        return this.resolver;
    }

    /**
     * Will always return an instance of AnnotatedClassActionResolver
     * @return AnnotatedClassActionResolver an instance of the default resolver
     */
    public ActionResolver getActionResolver() throws StripesServletException {
        try {
            ActionResolver resolver = new AnnotatedClassActionResolver();
            resolver.init(this);
            return resolver;
        }
        catch (Exception e) {
            throw new StripesServletException(e);
        }
    }

    /**
     * Will always return an OgnlActionBeanPropertyBinder
     * @return OgnlActionBeanPropertyBinder an instance of the default binder
     */
    public ActionBeanPropertyBinder getActionBeanPropertyBinder() throws StripesServletException {
        try {
            ActionBeanPropertyBinder binder = new OgnlActionBeanPropertyBinder();
            binder.init(this);
            return binder;
        }
        catch (Exception e) {
            throw new StripesServletException(e);
        }
    }
}
