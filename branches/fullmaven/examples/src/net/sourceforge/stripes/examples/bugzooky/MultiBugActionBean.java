package net.sourceforge.stripes.examples.bugzooky;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.DontValidate;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.examples.bugzooky.biz.Bug;
import net.sourceforge.stripes.examples.bugzooky.biz.BugManager;
import net.sourceforge.stripes.examples.bugzooky.ext.BugzookyActionBeanContext;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;

/**
 * ActionBean that deals with setting up and saving edits to multiple bugs at once. Can also
 * deal with adding multiple new bugs at once.
 *
 * @author Tim Fennell
 */
public class MultiBugActionBean extends BugzookyActionBean {
    /** Populated during bulk add/edit operations. */
    @ValidateNestedProperties({
        @Validate(field="shortDescription", required=true, maxlength=75),
        @Validate(field="longDescription", required=true, minlength=25),
        @Validate(field="component", required=true),
        @Validate(field="owner", required=true),
        @Validate(field="priority", required=true)
    })
    private List<Bug> bugs = new ArrayList<Bug>();

    /**
     * Simple getter that returns the List of Bugs. Note the use of generics syntax - this is
     * necessary to let Stripes know what type of object to create and insert into the list.
     */
    public List<Bug> getBugs() {
        return bugs;
    }

    /** Setter for the list of bugs. */
    public void setBugs(List<Bug> bugs) {
        this.bugs = bugs;
    }

    @DefaultHandler
    @DontValidate
    public Resolution view() {
        // Check for the "view" parameter. It will be there if we got here by a form submission.
        BugzookyActionBeanContext context = getContext();
        boolean fromForm = context.getRequest().getParameter("view") != null;
        if (fromForm && (getBugs() == null || getBugs().isEmpty())) {
            context.getValidationErrors().addGlobalError(
                    new SimpleError("You must select at least one bug to edit."));
            return context.getSourcePageResolution();
        }

        return new ForwardResolution("/bugzooky/BulkAddEditBugs.jsp");
    }

    public Resolution save() {
        BugManager bm = new BugManager();

        for (Bug bug : bugs) {
            bm.saveOrUpdate(bug);
        }

        return new RedirectResolution(BugListActionBean.class);
    }
}
