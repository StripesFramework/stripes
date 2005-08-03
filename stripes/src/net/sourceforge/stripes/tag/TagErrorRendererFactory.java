package net.sourceforge.stripes.tag;

import net.sourceforge.stripes.config.ConfigurableComponent;

/**
 * Constructs and returns an instance of TagErrorRenderer to handle the
 * error output of a specific form input tag.
 *
 * @author Greg Hinkle
 */
public interface TagErrorRendererFactory extends ConfigurableComponent {


    /**
     * Returns a new instance of a TagErrorRenderer that is utilized
     * by the supplied tag.
     * @param tag The tag that needs to be error renderered
     * @return TagErrorRenderer the error renderer to render the error output
     */
    public TagErrorRenderer getTagErrorRenderer(InputTagSupport tag);

}
