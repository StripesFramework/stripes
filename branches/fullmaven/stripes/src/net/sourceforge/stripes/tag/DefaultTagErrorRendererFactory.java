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

import net.sourceforge.stripes.config.Configuration;
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
        setConfiguration(configuration);

        this.rendererClass = configuration.getBootstrapPropertyResolver().
                getClassProperty(RENDERER_CLASS_KEY, TagErrorRenderer.class);
        
        if (this.rendererClass == null)
            this.rendererClass = DefaultTagErrorRenderer.class;
    }

    /**
     * Returns a new instance of the configured renderer that is ready for use. By default
     * returns an instance of {@link DefaultTagErrorRenderer}. If a custom class is configured
     * and cannot be instantiated, an exception will be thrown.
     */
    public TagErrorRenderer getTagErrorRenderer(InputTagSupport tag) {
        try {
            TagErrorRenderer renderer = getConfiguration().getObjectFactory().newInstance(
                    this.rendererClass);
            renderer.init(tag);
            return renderer;
        }
        catch (Exception e) {
            throw new StripesRuntimeException("Could not create an instance of the configured " +
                "TagErrorRenderer class '" + this.rendererClass.getName() + "'. Please check " +
                "that the class is public and has a no-arg public constructor.", e);
        }
    }

	protected Configuration getConfiguration()
	{
		return configuration;
	}

	protected void setConfiguration(Configuration configuration)
	{
		this.configuration = configuration;
	}
}
