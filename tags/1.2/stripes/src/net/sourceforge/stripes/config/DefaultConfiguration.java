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
import net.sourceforge.stripes.tag.PopulationStrategy;
import net.sourceforge.stripes.tag.DefaultPopulationStrategy;
import net.sourceforge.stripes.format.FormatterFactory;
import net.sourceforge.stripes.format.DefaultFormatterFactory;

/**
 * <p>Centralized location for defaults for all Configuration properties.  This implementation does
 * not lookup configuration information anywhere!  It returns hard-coded defaults that will result
 * in a working system without any user intervention.</p>
 *
 * <p>Despite it's name the DefaultConfiguration is not in fact the default Configuration
 * implementation in Stripes!  Instead it is the retainer of default configuration values. The
 * Configuration implementation that is used when no alternative is configured is the
 * {@link RuntimeConfiguration}, which is a direct subclass of DefaultConfiguration, and when no
 * further configuration properties are supplied behaves identically to the DefaultConfiguration.</p>
 *
 * <p>The DefaultConfiguration is designed to be easily extended as needed.  The init() method
 * ensures that compoments are initialized in the correct order (taking dependencies into account),
 * and should generally not be overridden. It invokes a number of initXXX() methods, one per
 * configurable component. Subclasses should override any of the initXXX() methods desirable to
 * return a fully initialized instance of the relevant component type, or null if the default is
 * desired.</p>
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
    private PopulationStrategy populationStrategy;

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
            this.actionResolver = initActionResolver();
            if (this.actionResolver == null) {
                this.actionResolver = new AnnotatedClassActionResolver();
                this.actionResolver.init(this);
            }

            this.actionBeanPropertyBinder = initActionBeanPropertyBinder();
            if (this.actionBeanPropertyBinder == null) {
                this.actionBeanPropertyBinder = new OgnlActionBeanPropertyBinder();
                this.actionBeanPropertyBinder.init(this);
            }

            this.actionBeanContextFactory = initActionBeanContextFactory();
            if (this.actionBeanContextFactory == null) {
                this.actionBeanContextFactory = new DefaultActionBeanContextFactory();
                this.actionBeanContextFactory.init(this);
            }

            this.typeConverterFactory = initTypeConverterFactory();
            if (this.typeConverterFactory == null) {
                this.typeConverterFactory = new DefaultTypeConverterFactory();
                this.typeConverterFactory.init(this);
            }

            this.localizationBundleFactory = initLocalizationBundleFactory();
            if (this.localizationBundleFactory == null) {
                this.localizationBundleFactory = new DefaultLocalizationBundleFactory();
                this.localizationBundleFactory.init(this);
            }

            this.localePicker = initLocalePicker();
            if (this.localePicker == null) {
                this.localePicker = new DefaultLocalePicker();
                this.localePicker.init(this);
            }

            this.formatterFactory = initFormatterFactory();
            if (this.formatterFactory == null) {
                this.formatterFactory = new DefaultFormatterFactory();
                this.formatterFactory.init(this);
            }

            this.tagErrorRendererFactory = initTagErrorRendererFactory();
            if (this.tagErrorRendererFactory == null) {
                this.tagErrorRendererFactory = new DefaultTagErrorRendererFactory();
                this.tagErrorRendererFactory.init(this);
            }

            this.populationStrategy = initPopulationStrategy();
            if (this.populationStrategy == null) {
                this.populationStrategy = new DefaultPopulationStrategy();
                this.populationStrategy.init(this);
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

    /** Allows subclasses to initialize a non-default ActionResovler. */
    protected ActionResolver initActionResolver() { return null; }

    /**
     * Will always return an OgnlActionBeanPropertyBinder
     * @return OgnlActionBeanPropertyBinder an instance of the default binder
     */
    public ActionBeanPropertyBinder getActionBeanPropertyBinder() {
        return this.actionBeanPropertyBinder;
    }

    /** Allows subclasses to initizlize a non-default ActionBeanPropertyBinder. */
    protected ActionBeanPropertyBinder initActionBeanPropertyBinder() { return null; }

    /**
     * Returns the configured ActionBeanContextFactory. Unless a subclass has configured a custom
     * one, the instance will be a DefaultActionBeanContextFactory.
     *
     * @return ActionBeanContextFactory an instance of a factory for creating ActionBeanContexts
     */
    public ActionBeanContextFactory getActionBeanContextFactory() {
        return this.actionBeanContextFactory;
    }

    /** Allows subclasses to initialize a non-default ActionBeanContextFactory. */
    protected ActionBeanContextFactory initActionBeanContextFactory() { return null; }

    /**
     * Will always return an instance of DefaultTypeConverterFactory.
     * @return TypeConverterFactory an instance of the default factory.
     */
    public TypeConverterFactory getTypeConverterFactory() {
        return this.typeConverterFactory;
    }

    /** Allows subclasses to initizlize a non-default TypeConverterFactory. */
    protected TypeConverterFactory initTypeConverterFactory() { return null; }

    /**
     * Returns an instance of a LocalizationBundleFactory.  By default this will be an instance of
     * DefaultLocalizationBundleFactory unless another type has been configured.
     */
    public LocalizationBundleFactory getLocalizationBundleFactory() {
        return this.localizationBundleFactory;
    }

    /** Allows subclasses to initialize a non-default LocalizationBundleFactory. */
    protected LocalizationBundleFactory initLocalizationBundleFactory() { return null; }

    /**
     * Returns an instance of a LocalePicker. Unless a subclass has picked another implementation
     * will return an instance of DefaultLocalePicker.
     */
    public LocalePicker getLocalePicker() { return this.localePicker; }

    /** Allows subclasses to initialize a non-default LocalePicker. */
    protected LocalePicker initLocalePicker() { return null; }

    /**
     * Returns an instance of a FormatterFactory. Unless a subclass has picked another implementation
     * will return an instance of DefaultFormatterFactory.
     */
    public FormatterFactory getFormatterFactory() { return this.formatterFactory; }

    /** Allows subclasses to initialize a non-default FormatterFactory. */
    protected FormatterFactory initFormatterFactory() { return null; }

    /**
     * Returns an instance of a TagErrorRendererFactory. Unless a subclass has picked another
     * implementation,  will return an instance of DefaultTagErrorRendererFactory.
     */
    public TagErrorRendererFactory getTagErrorRendererFactory() {
        return tagErrorRendererFactory;
    }

    /** Allows subclasses to initialize a non-default TagErrorRendererFactory instance to be used. */
    protected TagErrorRendererFactory initTagErrorRendererFactory() { return null; }

    /**
     * Returns an instance of a PopulationsStrategy.  Unless a sublcass has picked another
     * implementation, will return an instance of
     * {@link net.sourceforge.stripes.tag.DefaultPopulationStrategy}.
     */
    public PopulationStrategy getPopulationStrategy() { return this.populationStrategy; }

    /** Allows subclasses to initialize a non-default PopulationStrategy instance to be used. */
    protected PopulationStrategy initPopulationStrategy() { return null; }

}
