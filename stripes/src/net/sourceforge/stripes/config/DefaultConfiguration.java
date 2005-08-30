/* Copyright (C) 2005 Tim Fennell
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
package net.sourceforge.stripes.config;

import net.sourceforge.stripes.controller.ActionBeanPropertyBinder;
import net.sourceforge.stripes.controller.ActionResolver;
import net.sourceforge.stripes.controller.AnnotatedClassActionResolver;
import net.sourceforge.stripes.controller.OgnlActionBeanPropertyBinder;
import net.sourceforge.stripes.controller.ActionBeanContextFactory;
import net.sourceforge.stripes.controller.DefaultActionBeanContextFactory;
import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.localization.DefaultLocalizationBundleFactory;
import net.sourceforge.stripes.localization.LocalizationBundleFactory;
import net.sourceforge.stripes.localization.LocalePicker;
import net.sourceforge.stripes.localization.DefaultLocalePicker;
import net.sourceforge.stripes.validation.DefaultTypeConverterFactory;
import net.sourceforge.stripes.validation.TypeConverterFactory;
import net.sourceforge.stripes.tag.TagErrorRendererFactory;
import net.sourceforge.stripes.tag.DefaultTagErrorRendererFactory;
import net.sourceforge.stripes.format.FormatterFactory;
import net.sourceforge.stripes.format.DefaultFormatterFactory;

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
    private ActionBeanContextFactory actionBeanContextFactory;
    private TypeConverterFactory typeConverterFactory;
    private LocalizationBundleFactory localizationBundleFactory;
    private LocalePicker localePicker;
    private FormatterFactory formatterFactory;
    private TagErrorRendererFactory tagErrorRendererFactory;

    /** Gratefully accepts the BootstrapPropertyResolver handed to the Configuration. */
    public void setBootstrapPropertyResolver(BootstrapPropertyResolver resolver) {
        this.resolver = resolver;
    }

    /**
     * Creates and stores instances of the objects of the type that the Configuration is
     * responsible for providing, except where subclasses have already provided instances.
     */
    public void init() {
        try {
            if (this.actionResolver == null) {
                this.actionResolver = new AnnotatedClassActionResolver();
                this.actionResolver.init(this);
            }

            if (this.actionBeanPropertyBinder == null) {
                this.actionBeanPropertyBinder = new OgnlActionBeanPropertyBinder();
                this.actionBeanPropertyBinder.init(this);
            }

            if (this.actionBeanContextFactory == null) {
                this.actionBeanContextFactory = new DefaultActionBeanContextFactory();
                this.actionBeanContextFactory.init(this);
            }

            if (this.typeConverterFactory == null) {
                this.typeConverterFactory = new DefaultTypeConverterFactory();
                this.typeConverterFactory.init(this);
            }

            if (this.localizationBundleFactory == null) {
                this.localizationBundleFactory = new DefaultLocalizationBundleFactory();
                this.localizationBundleFactory.init(this);
            }

            if (this.localePicker == null) {
                this.localePicker = new DefaultLocalePicker();
                this.localePicker.init(this);
            }

            if (this.formatterFactory == null) {
                this.formatterFactory = new DefaultFormatterFactory();
                this.formatterFactory.init(this);
            }

            if (this.tagErrorRendererFactory == null) {
                this.tagErrorRendererFactory = new DefaultTagErrorRendererFactory();
                this.tagErrorRendererFactory.init(this);
            }
        }
        catch (Exception e) {
            throw new StripesRuntimeException
                    ("Problem instantiating default configuration objects.", e);
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
    public ActionResolver getActionResolver() {
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
    public ActionBeanPropertyBinder getActionBeanPropertyBinder() {
        return this.actionBeanPropertyBinder;
    }

    /** Allows subclasses to set the ActionBeanPropertyBinder instance to be used. */
    protected void setActionBeanPropertyBinder(ActionBeanPropertyBinder propertyBinder) {
        this.actionBeanPropertyBinder = propertyBinder;
    }

    /**
     * Returns the configured ActionBeanContextFactory. Unless a subclass has configured a custom
     * one, the instance will be a DefaultActionBeanContextFactory.
     *
     * @return ActionBeanContextFactory an instance of a factory for creating ActionBeanContexts
     */
    public ActionBeanContextFactory getActionBeanContextFactory() {
        return this.actionBeanContextFactory;
    }

    /** Allows subclasses to set the ActionBeanContextFactory instance to be used. */
    protected void setActionBeanContextFactory(ActionBeanContextFactory contextFactory) {
        this.actionBeanContextFactory = contextFactory;
    }

    /**
     * Will always return an instance of DefaultTypeConverterFactory.
     * @return TypeConverterFactory an instance of the default factory.
     */
    public TypeConverterFactory getTypeConverterFactory() {
        return this.typeConverterFactory;
    }

    /** Allows subclasses to set the TypeConverterFactory instance to be used. */
    protected void setTypeConverterFactory(TypeConverterFactory typeConverterFactory) {
        this.typeConverterFactory = typeConverterFactory;
    }

    /**
     * Returns an instance of a LocalizationBundleFactory.  By default this will be an instance of
     * DefaultLocalizationBundleFactory unless another type has been configured.
     */
    public LocalizationBundleFactory getLocalizationBundleFactory() {
        return this.localizationBundleFactory;
    }

    /** Allows subclasses to set the LocalizationBundleFactory instance to be used. */
    protected void setLocalizationBundleFactory(LocalizationBundleFactory localizationBundleFactory) {
        this.localizationBundleFactory = localizationBundleFactory;
    }

    /**
     * Returns an instance of a LocalePicker. Unless a subclass has picked another implementation
     * will return an instance of DefaultLocalePicker.
     */
    public LocalePicker getLocalePicker() { return this.localePicker; }

    /** Allows subclasses to set the LocalePicker instance to be used. */
    protected void setLocalePicker(LocalePicker localePicker) { this.localePicker = localePicker; }

    /**
     * Returns an instance of a FormatterFactory. Unless a subclass has picked another implementation
     * will return an instance of DefaultFormatterFactory.
     */
    public FormatterFactory getFormatterFactory() { return this.formatterFactory; }

    /** Allows subclasses to set the FormatterFactory instance to be used. */
    protected void setFormatterFactory(FormatterFactory formatterFactory) {
        this.formatterFactory = formatterFactory;
    }

    /**
     * Returns an instance of a TagErrorRendererFactory. Unless a subclass has picked another
     * implementation,  will return an instance of DefaultTagErrorRendererFactory.
     */
    public TagErrorRendererFactory getTagErrorRendererFactory() {
        return tagErrorRendererFactory;
    }

    /** Allows subclasses to set the TagErrorRendererFactory instance to be used. */
    protected void setTagErrorRendererFactory(TagErrorRendererFactory tagErrorRendererFactory) {
        this.tagErrorRendererFactory = tagErrorRendererFactory;
    }
}
