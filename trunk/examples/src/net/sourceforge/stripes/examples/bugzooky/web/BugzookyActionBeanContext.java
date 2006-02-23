package net.sourceforge.stripes.examples.bugzooky.web;

import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.examples.bugzooky.biz.Person;

/**
 * ActionBeanContext subclass for the Bugzooky application that manages where values
 * like the logged in user are stored.
 *
 * @author Tim Fennell
 */
public class BugzookyActionBeanContext extends ActionBeanContext {

    /** Gets the currently logged in user, or null if no-one is logged in. */
    public Person getUser() {
        return (Person) getRequest().getSession().getAttribute("user");
    }

    /** Sets the currently logged in user. */
    public void setUser(Person currentUser) {
        getRequest().getSession().setAttribute("user", currentUser);
    }

    /** Logs the user out by invalidating the session. */
    public void logout() {
        getRequest().getSession().invalidate();
    }
}
