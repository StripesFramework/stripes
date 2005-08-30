package net.sourceforge.stripes.examples.bugzooky.web;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.DontValidate;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.examples.bugzooky.biz.Attachment;
import net.sourceforge.stripes.examples.bugzooky.biz.Bug;
import net.sourceforge.stripes.examples.bugzooky.biz.BugManager;
import net.sourceforge.stripes.examples.bugzooky.biz.ComponentManager;
import net.sourceforge.stripes.examples.bugzooky.biz.PersonManager;
import net.sourceforge.stripes.validation.PercentageTypeConverter;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.Validatable;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;
import net.sourceforge.stripes.validation.ValidationErrors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * ActionBean that provides method for editing a single bug in detail. Includes an
 * event for pre-populating the ActionBean on the way in to an edit screen, and a
 * single event for saving an existing or new bug.  Uses a FileBean property to
 * support the uploading of a File concurrent with other edits.
 *
 * @author Tim Fennell
 */
@UrlBinding("/bugzooky/SingleBug.action")
public class SingleBugActionBean extends BugzookyActionBean implements Validatable {
    private Bug bug;
    private FileBean newAttachment;

    /** Gets the bug for this Action. */
    @ValidateNestedProperties({
        @Validate(field="shortDescription", required=true),
        @Validate(field="longDescription", required=true),
        @Validate(field="percentComplete", converter=PercentageTypeConverter.class)
    })
    public Bug getBug() { return bug; }

    /** Sets the bug for this Action. */
    public void setBug(Bug bug) { this.bug = bug; }

    public FileBean getNewAttachment() { return newAttachment; }
    public void setNewAttachment(FileBean newAttachment) { this.newAttachment = newAttachment; }

    /** Does some very basic custom validation. */
    public void validate(ValidationErrors errors) {
        Float percentComplete = this.bug.getPercentComplete();
        if (percentComplete != null && (percentComplete > 1 || percentComplete < 0)) {
            SimpleError error =  new SimpleError("Percent complete must be in the range 0-100%.");
            errors.add("bug.percentComplete", error);
        }
    }

    /**
     * Loads a bug on to the form ready for editing.
     */
    @DontValidate
    @HandlesEvent("PreEdit")
    public Resolution loadBugForEdit() {
        BugManager bm = new BugManager();
        this.bug = bm.getBug( this.bug.getId() );
        return new ForwardResolution("/bugzooky/AddEditBug.jsp");
    }

    /** Saves (or updates) a bug, and then returns the user to the bug list. */
    @HandlesEvent("SaveOrUpdate") @DefaultHandler
    public Resolution saveOrUpdate() throws IOException {
        BugManager bm = new BugManager();
        ComponentManager cm = new ComponentManager();
        PersonManager pm = new PersonManager();

        Bug newBug = populateBug(this.bug);
        if (this.newAttachment != null) {
            Attachment attachment = new Attachment();
            attachment.setName(this.newAttachment.getFileName());
            attachment.setSize(this.newAttachment.getSize());
            attachment.setContentType(this.newAttachment.getContentType());

            BufferedReader reader = new BufferedReader
                    ( new InputStreamReader(this.newAttachment.getInputStream()) );
            StringBuilder builder = new StringBuilder();
            String line;

            while ( (line = reader.readLine()) != null ) {
                builder.append(line).append('\n');
            }

            attachment.setData(builder.toString());
            newBug.addAttachment(attachment);
        }

        bm.saveOrUpdate(newBug);

        return new ForwardResolution("/bugzooky/BugList.jsp");
    }

    /** Saves or updates a bug, and then returns to the edit page to add another just like it. */
    @HandlesEvent("SaveAndAgain")
    public Resolution saveAndAddAnother() throws IOException {
        saveOrUpdate();

        return new RedirectResolution("/bugzooky/AddEditBug.jsp");
    }
}
