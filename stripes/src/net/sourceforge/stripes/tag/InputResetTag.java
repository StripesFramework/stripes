package net.sourceforge.stripes.tag;

/**
 * <p>Tag that generates HTML form fields of type {@literal <input type="reset" ... />} which
 * render buttons for submitting forms.  The only capability offered above and beyond a pure
 * html tag is the ability to lookup the value of the button (i.e. the text on the button that the
 * user sees) from a localized resource bundle. For more details on operation see
 * {@link net.sourceforge.stripes.tag.InputButtonSupportTag}.
 *
 * @author Tim Fennell
 */
public class InputResetTag extends InputButtonSupportTag {
    /** Sets the input tag type to be reset. */
    public InputResetTag() {
        super();
        getAttributes().put("type", "reset");
    }
}
