package net.sourceforge.stripes.tag;

import net.sourceforge.stripes.config.Configuration;

/**
 * A basic implementation of the TagErrorRendererFactory interface that always
 * constructs and returns the {@link DefaultTagErrorRenderer}.
 *
 * @author Greg Hinkle
 */
public class DefaultTagErrorRendererFactory implements TagErrorRendererFactory {
    private Configuration configuration;

    /** Just stores the configuration passed in. */
    public void init(Configuration configuration) throws Exception {
        this.configuration = configuration;
    }

    /**
     * Always returns an initialized instance of DefaultTagErrorRenderer.
     */
    public TagErrorRenderer getTagErrorRenderer(InputTagSupport tag) {
        TagErrorRenderer renderer = new DefaultTagErrorRenderer();
        renderer.init(tag);
        return renderer;
    }
}
