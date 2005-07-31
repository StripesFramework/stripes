package net.sourceforge.stripes.tag;

/**
 * <p>Tag that generates HTML form fields of type {@literal <input type="button" ... />} which
 * render buttons for submitting forms.  The only capability offered above and beyond a pure
 * html tag is the ability to lookup the value of the button (i.e. the text on the button that the
 * user sees) from a localized resource bundle. For more details on operation see
 * {@link InputButtonSupportTag}.
 *
 * @author Tim Fennell
 */
public class InputButtonTag extends InputButtonSupportTag {
    /** Sets the input tag type to be button. */
    public InputButtonTag() {
        super();
        getAttributes().put("type", "button");
    }
}
