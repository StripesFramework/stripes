package net.sourceforge.stripes.examples.bugzooky;

import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.examples.bugzooky.BugzookyActionBean;

/**
 * Straightforward logout action that logs the user out and then sends to an exit page.
 * @author Tim Fennell
 */
public class LogoutActionBean extends BugzookyActionBean {
    public Resolution logout() throws Exception {
        getContext().logout();
        return new RedirectResolution("/bugzooky/Exit.jsp");
    }
}
