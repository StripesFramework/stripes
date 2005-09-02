package net.sourceforge.stripes.examples.bugzooky.biz;

/**
 * Very simple wrapper for file attachments uploaded for bugs.  Assumes that the attachment
 * contains some type of textual data.
 *
 * @author Tim Fennell
 */
public class Attachment {
    private String name;
    private long size;
    private String data;
    private String contentType;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getPreview() {
        int endIndex = Math.min(data.length(), 30);
        return data.substring(0, endIndex);
    }
}
