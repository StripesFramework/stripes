package net.sourceforge.stripes.examples.bugzooky;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.DontValidate;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.examples.bugzooky.biz.Attachment;
import net.sourceforge.stripes.examples.bugzooky.biz.Bug;
import net.sourceforge.stripes.examples.bugzooky.biz.BugManager;
import net.sourceforge.stripes.validation.PercentageTypeConverter;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;

import java.io.IOException;
import java.io.InputStream;

/**
 * ActionBean that provides method for editing a single bug in detail. Includes an
 * event for pre-populating the ActionBean on the way in to an edit screen, and a
 * single event for saving an existing or new bug.  Uses a FileBean property to
 * support the uploading of a File concurrent with other edits.
 *
 * @author Tim Fennell
 */
public class SingleBugActionBean extends BugzookyActionBean {
    @ValidateNestedProperties({
        @Validate(field="shortDescription", required=true),
        @Validate(field="longDescription", required=true),
        @Validate(field="percentComplete", minvalue=0, maxvalue=1,
                  converter=PercentageTypeConverter.class)
    })
    private Bug bug;
    private FileBean newAttachment;

    public Bug getBug() { return bug; }
    public void setBug(Bug bug) { this.bug = bug; }

    public FileBean getNewAttachment() { return newAttachment; }
    public void setNewAttachment(FileBean newAttachment) { this.newAttachment = newAttachment; }

    /** Loads a bug on to the form ready for editing. */
    @DontValidate
    public Resolution preEdit() {
        BugManager bm = new BugManager();
        this.bug = bm.getBug( this.bug.getId() );
        return new ForwardResolution("/bugzooky/AddEditBug.jsp");
    }

    /** Saves (or updates) a bug, and then returns the user to the bug list. */
    @DefaultHandler
    public Resolution save() throws IOException {
        BugManager bm = new BugManager();

        Bug newBug = populateBug(this.bug);
        if (this.newAttachment != null) {
            Attachment attachment = new Attachment();
            attachment.setName(this.newAttachment.getFileName());
            attachment.setSize(this.newAttachment.getSize());
            attachment.setContentType(this.newAttachment.getContentType());

            byte[] data = new byte[(int) this.newAttachment.getSize()];
            InputStream in = this.newAttachment.getInputStream();
            in.read(data);
            attachment.setData(data);
            newBug.addAttachment(attachment);
        }

        bm.saveOrUpdate(newBug);

        return new RedirectResolution("/bugzooky/BugList.jsp");
    }

    /** Saves or updates a bug, and then returns to the edit page to add another just like it. */
    public Resolution saveAndAgain() throws IOException {
        save();
        return new RedirectResolution("/bugzooky/AddEditBug.jsp");
    }
}
