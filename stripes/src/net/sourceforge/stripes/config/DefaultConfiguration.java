package net.sourceforge.stripes.config;

import net.sourceforge.stripes.exception.StripesServletException;
import net.sourceforge.stripes.controller.ActionResolver;
import net.sourceforge.stripes.controller.AnnotatedClassActionResolver;
import net.sourceforge.stripes.controller.ActionBeanPropertyBinder;
import net.sourceforge.stripes.controller.OgnlActionBeanPropertyBinder;
import net.sourceforge.stripes.validation.TypeConverterFactory;
import net.sourceforge.stripes.validation.DefaultTypeConverterFactory;

/**
 * <p>Centralized location for defaults for all Configuration properties.  This implementation does
 * not lookup configuration information anywhere!  It returns hard-coded defaults that will result
 * in a working system without any user intervention.</p>
 *
 * <p>The DefaultConfiguration is designed to be easily extended as needed. Subclasses should simply
 * implement their own init() method which instantiates the ConfigurableComponents in the manner
 * required and then call super.setXXX() to set them on the configuration.  The last step should be
 * to call super.init(), which will allow the DefaultConfiguration to fill in any implementations
 * that were not set by the child class.</p>
 *
 * @author Tim Fennell
 */
public class DefaultConfiguration implements Configuration {
    private BootstrapPropertyResolver resolver;
    private ActionResolver actionResolver;
    private ActionBeanPropertyBinder actionBeanPropertyBinder;
    private TypeConverterFactory typeConverterFactory;

    /** Gratefully accepts the BootstrapPropertyResolver handed to the Configuration. */
    public void setBootstrapPropertyResolver(BootstrapPropertyResolver resolver) {
        this.resolver = resolver;
    }

    /**
     * Creates and stores instances of the objects of the type that the Configuration is
     * responsible for providing, except where subclasses have already provided instances.
     */
    public void init() throws StripesServletException {
        try {
            if (this.actionResolver == null) {
                this.actionResolver = new AnnotatedClassActionResolver();
                this.actionResolver.init(this);
            }

            if (this.actionBeanPropertyBinder == null) {
                this.actionBeanPropertyBinder = new OgnlActionBeanPropertyBinder();
                this.actionBeanPropertyBinder.init(this);
            }

            if (this.typeConverterFactory == null) {
                this.typeConverterFactory = new DefaultTypeConverterFactory();
                this.typeConverterFactory.init(this);
            }
        }
        catch (Exception e) {
            throw new StripesServletException(e);
        }
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
        return this.actionResolver;
    }

    /** Allows subclasses to set the ActionResolver instance to be used. */
    protected void setActionResolver(ActionResolver actionResolver) {
        this.actionResolver = actionResolver;
    }

    /**
     * Will always return an OgnlActionBeanPropertyBinder
     * @return OgnlActionBeanPropertyBinder an instance of the default binder
     */
    public ActionBeanPropertyBinder getActionBeanPropertyBinder() throws StripesServletException {
        return this.actionBeanPropertyBinder;
    }

    /** Allows subclasses to set the ActionBeanPropertyBinder instance to be used. */
    protected void setActionBeanPropertyBinder(ActionBeanPropertyBinder propertyBinder) {
        this.actionBeanPropertyBinder = propertyBinder;
    }

    /**
     * Will always return an instance of DefaultTypeConverterFactory.
     * @return TypeConverterFactory an instance of the default factory.
     */
    public TypeConverterFactory getTypeConverterFactory() throws StripesServletException {
        return this.typeConverterFactory;
    }

    /** Allows subclasses to set the TypeConverterFactory instance to be used. */
    protected void setTypeConverterFactory(TypeConverterFactory typeConverterFactory) {
        this.typeConverterFactory = typeConverterFactory;
    }

}
