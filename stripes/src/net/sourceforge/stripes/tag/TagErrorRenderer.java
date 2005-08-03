package net.sourceforge.stripes.tag;

/**
 * <p>Implementations of this interface are used to apply formatting to form input
 * fields when there are associated errors.  TagErrorRenderers can modify attributes
 * of the tags output html before and/or after the tag renders itself.</p>
 *
 * <p>If the renderer modifies attributes of the form input tag, it is also responsible
 * for re-setting those values to their prior values in the doAfterEndTag() method. If
 * this is not done correctly and the tag is pooled by the container the results on the page
 * may be pretty unexpected!</p>
 *
 * @author Greg Hinkle
 */
public interface TagErrorRenderer {

    /**
     * Initialize this renderer for a specific tag instance
     * @param tag The InputTagSuppport subclass that will be modified
     */
    void init(InputTagSupport tag);

    /**
     * Executed before the start of rendering of the input tag.
     * The input tag attributes can be modifed here to be written
     * out with other html attributes.
     */
    void doBeforeStartTag();

    /**
     * Executed after the end of rendering of the input tag, including
     * its body and end tag.
     */
    void doAfterEndTag();
}
