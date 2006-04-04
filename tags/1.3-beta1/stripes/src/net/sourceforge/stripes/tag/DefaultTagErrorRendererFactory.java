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
package net.sourceforge.stripes.tag;

import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.util.ReflectUtil;
import net.sourceforge.stripes.exception.StripesRuntimeException;

/**
 * <p>A straightforward implementation of the TagErrorRendererFactory interface that looks
 * up the name of the renderer class in config, and if one is not supplied defaults to
 * using the {@link DefaultTagErrorRenderer}.  The same TagErrorRenderer is instantiated for
 * all tags, and must be public and have a public no-arg constructor.</p>
 *
 * <p>To configure a different TagErrorRenderer use the configuration key
 * {@code TagErrorRenderer.Class} and supply a fully qualified class name.  For example, to
 * do this in web.xml you would add the following parameter to the Stripes Filter:</p>
 *
 *<pre>
 *{@literal <init-param>}
 *    {@literal <param-name>TagErrorRenderer.Class</param-name>}
 *    {@literal <param-value>com.myco.web.util.CustomTagErrorRenderer</param-value>}
 *{@literal </init-param>}
 *</pre>
 *
 * @author Greg Hinkle, Tim Fennell
 */
public class DefaultTagErrorRendererFactory implements TagErrorRendererFactory {
    public static final String RENDERER_CLASS_KEY = "TagErrorRenderer.Class";

    private Configuration configuration;
    private Class<? extends TagErrorRenderer> rendererClass;

    /**
     * Looks up the name of the configured renderer class in the configuration and
     * attempts to find the Class object for it.  If one isn't provided then the default
     * class is used.  If the configured class cannot be found an exception will be
     * thrown and the factory is deemed invalid.
     */
    public void init(Configuration configuration) throws Exception {
        this.configuration = configuration;

        String className = configuration.getBootstrapPropertyResolver().
                getProperty(RENDERER_CLASS_KEY);
        if (className == null) {
            this.rendererClass = DefaultTagErrorRenderer.class;
        }
        else {
            try {
                this.rendererClass = ReflectUtil.findClass(className);
            }
            catch (ClassNotFoundException cnfe) {
                throw new StripesRuntimeException("Could not load the specified TagErrorRenderer " +
                    "class '" + className + "'. Please check the classname for typos and make " +
                    "sure that the class is available in the classpath of the web app.", cnfe);
            }
        }
    }

    /**
     * Returns a new instance of the configured renderer that is ready for use. By default
     * returns an instance of {@link DefaultTagErrorRenderer}. If a custom class is configured
     * and cannot be instantiated, an exception will be thrown.
     */
    public TagErrorRenderer getTagErrorRenderer(InputTagSupport tag) {
        try {
            TagErrorRenderer renderer = this.rendererClass.newInstance();
            renderer.init(tag);
            return renderer;
        }
        catch (Exception e) {
            throw new StripesRuntimeException("Could not create an instance of the configured " +
                "TagErrorRenderer class '" + this.rendererClass.getName() + "'. Please check " +
                "that the class is public and has a no-arg public constructor.", e);
        }
    }
}
