package net.sourceforge.stripes.examples.bugzooky.web;

import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.examples.bugzooky.biz.BugManager;
import net.sourceforge.stripes.examples.bugzooky.biz.Bug;
import net.sourceforge.stripes.examples.bugzooky.biz.Attachment;

import java.io.StringReader;

/**
 * Action that responds to a user's request to download an attachment to a bug.
 *
 * @author Tim Fennell
 */
@UrlBinding("/bugzooky/DownloadAttachment.action")
public class DownloadAttachmentActionBean extends BugzookyActionBean {
    private Integer bugId;
    private Integer attachmentIndex;

    public Integer getBugId() { return bugId; }
    public void setBugId(Integer bugId) { this.bugId = bugId; }

    public Integer getAttachmentIndex() { return attachmentIndex; }
    public void setAttachmentIndex(Integer attachmentIndex) { this.attachmentIndex = attachmentIndex; }

    @DefaultHandler
    public Resolution getAttachment() {
        BugManager bm = new BugManager();
        Bug bug = bm.getBug(this.bugId);
        Attachment attachment = bug.getAttachments().get(this.attachmentIndex);

        // Uses a StreamingResolution to send the file contents back to the user.
        // Note the use of the chained .setFilename() method, which causes the
        // browser to [prompt to] save the "file" instead of displaying it in browser
        return new StreamingResolution
                (attachment.getContentType(), new StringReader(attachment.getData()))
                    .setFilename(attachment.getName());
    }
}
