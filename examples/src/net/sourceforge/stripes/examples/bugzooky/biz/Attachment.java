package net.sourceforge.stripes.examples.bugzooky.biz;

/**
 * Created by IntelliJ IDEA. User: tfenne Date: Aug 21, 2005 Time: 2:45:57 PM To change this
 * template use File | Settings | File Templates.
 */
public class Attachment {
    private String name;
    private long size;
    private String data;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getPreview() {
        int endIndex = Math.min(data.length(), 30);
        return data.substring(0, endIndex);
    }
}
