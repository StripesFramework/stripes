package net.sourceforge.stripes.examples.bugzooky.web;

import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.Message;
import net.sourceforge.stripes.examples.bugzooky.biz.Person;

import java.util.List;
import java.util.ArrayList;

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

    /**
     * Overrides the getMessages() method in ActionBeanContext to store the messages in
     * session scope instead of request scope.  This makes them available after a redirect.
     * Unforutnately this does introduce a minor risk that if a user makes two requests in a single
     * session at the same time, the messages may not show up in the right place!!
     */
    @Override
    public List<Message> getMessages(String key) {
        List<Message> messages = (List<Message>) getRequest().getSession().getAttribute(key);
        if (messages == null) {
            messages = new ArrayList<Message>();
            getRequest().getSession().setAttribute(key, messages);
        }

        return messages;
    }
}
