package net.sourceforge.stripes.examples.bugzooky;

import java.io.ByteArrayInputStream;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.examples.bugzooky.biz.Attachment;
import net.sourceforge.stripes.examples.bugzooky.biz.Bug;

/**
 * Action that responds to a user's request to download an attachment to a bug. This ActionBean
 * demonstrates the use of clean URLs.
 * 
 * @author Tim Fennell
 */
@UrlBinding("/attachment/{bug}/{attachmentIndex}")
public class DownloadAttachmentActionBean extends BugzookyActionBean {
    private Bug bug;
    private Integer attachmentIndex;

    public Bug getBug() { return bug; }
    public void setBug(Bug bugId) { this.bug = bugId; }

    public Integer getAttachmentIndex() { return attachmentIndex; }
    public void setAttachmentIndex(Integer attachmentIndex) { this.attachmentIndex = attachmentIndex; }

    @DefaultHandler
    public Resolution getAttachment() {
        Attachment attachment = getBug().getAttachments().get(this.attachmentIndex);

        // Uses a StreamingResolution to send the file contents back to the user.
        // Note the use of the chained .setFilename() method, which causes the
        // browser to [prompt to] save the "file" instead of displaying it in browser
        return new StreamingResolution(attachment.getContentType(),
                new ByteArrayInputStream(attachment.getData())).setFilename(attachment.getName());
    }
}
