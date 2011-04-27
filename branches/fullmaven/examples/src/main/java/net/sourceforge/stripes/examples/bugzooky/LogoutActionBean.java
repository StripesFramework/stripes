package net.sourceforge.stripes.examples.bugzooky;

import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.examples.bugzooky.ext.Public;

/**
 * Straightforward logout action that logs the user out and then sends to an exit page.
 * @author Tim Fennell
 */
@Public
public class LogoutActionBean extends BugzookyActionBean {
    public Resolution logout() throws Exception {
        getContext().logout();
        return new ForwardResolution("/bugzooky/Exit.jsp");
    }
}
