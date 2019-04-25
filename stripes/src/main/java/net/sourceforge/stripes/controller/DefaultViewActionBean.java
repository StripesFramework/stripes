package net.sourceforge.stripes.controller;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.Resolution;

/**
 * <p>
 * A special purpose ActionBean that is used by the NameBasedActionResolver when
 * a valid ActionBean cannot be found for a URL. If the URL can be successfully
 * translated into a JSP URL and a JSP exists, an instance of this ActionBean is
 * created that will forward the user to the appropriate JSP.</p>
 *
 * <p>
 * Because this ActionBean does not have a default no-arg constructor, even
 * though it gets bound to a URL, if that URL is hit the ActionBean cannot be
 * instantiated and therefore cannot be accessed directly by a user playing with
 * the URL.</p>
 *
 * @author Tim Fennell, Abdullah Jibaly
 * @since Stripes 1.3
 */
public class DefaultViewActionBean implements ActionBean {

    private ActionBeanContext context;
    private Resolution view;

    /**
     * Constructions a default view action bean given the passed view/resolution.
     * 
     * @param view - Resolution to create this default view action bean from.
     */
    public DefaultViewActionBean(Resolution view) {
        this.view = view;
    }

    @Override
    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    @Override
    public ActionBeanContext getContext() {
        return this.context;
    }

    /**
     * Returns the default view resolution.
     * 
     * @return Default view resolution
     */
    public Resolution view() {
        return view;
    }
}
