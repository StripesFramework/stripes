package net.sourceforge.stripes.examples.bugzooky.web;

import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.RedirectResolution;

/**
 * Straightforward logout action that logs the user out and then sends to an exit page.
 * @author Tim Fennell
 */
@UrlBinding("/bugzooky/Logout.action")
public class LogoutActionBean extends BugzookyActionBean {

    @DefaultHandler @HandlesEvent("Logout")
    public Resolution logout() {
        getContext().setUser(null);
        return new RedirectResolution("/bugzooky/Exit.jsp");
    }
}
