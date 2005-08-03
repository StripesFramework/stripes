package net.sourceforge.stripes.tag;

/**
 * <p>This default implementation of the TagErrorRenderer interface sets the html class
 * attribute to 'error'.  This allows applications to define a single style for all input
 * fields, and then override it for specific fields as they choose.</p>
 *
 * <p>An example of the css definition to set backgrounds to yellow by default, but
 * to red for checkboxes and radio buttons follows:</p>

 * {@code
 *   input.error { background-color: yellow; }
 *   input[type="checkbox"].error, input[type="radio"].error {background-color: red; }
 * }
 * @author Greg Hinkle, Tim Fennell
 */
public class DefaultTagErrorRenderer implements TagErrorRenderer {

    private InputTagSupport tag;
    private String oldCssClass;

    /** Simply stores the tag passed in. */
    public void init(InputTagSupport tag) {
        this.tag = tag;
    }

    /**
     * Changes the tag's class attribute to "error".
     */
    public void doBeforeStartTag() {
        this.oldCssClass = tag.getCssClass();
        tag.setCssClass("error");

    }

    /**
     * Resets the tag's class attribute to it's original value in case the tag gets pooled.
     */
    public void doAfterEndTag() {
        tag.setCssClass(oldCssClass);
    }

}
