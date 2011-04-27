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
    private byte[] data;
    private String contentType;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public byte[] getData() { return data; }
    public void setData(byte[] data) { this.data = data; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getPreview() {
        if (contentType.startsWith("text")) {
            int amount = Math.min(data.length, 30);
            return new String(data, 0, amount);
        }
        else {
            return "[Binary File]";
        }
    }
}
