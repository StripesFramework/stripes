package net.sourceforge.stripes.examples.bugzooky;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.examples.bugzooky.ext.BugzookyActionBeanContext;

/**
 * Simple ActionBean implementation that all ActionBeans in the Bugzooky example
 * will extend.
 *
 * @author Tim Fennell
 */
public abstract class BugzookyActionBean implements ActionBean {
    private BugzookyActionBeanContext context;

    public void setContext(ActionBeanContext context) {
        this.context = (BugzookyActionBeanContext) context;
    }

    /** Gets the ActionBeanContext set by Stripes during initialization. */
    public BugzookyActionBeanContext getContext() {
        return this.context;
    }
}