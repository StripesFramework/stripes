package net.sourceforge.stripes.tag.layout;

import org.testng.annotations.Test;

import java.nio.ByteBuffer;

public class MyServletOutputStreamTest {

    @Test
    public void shouldWriteSuccess() throws Exception {
        MyServletOutputStream stream = new MyServletOutputStream(new DummyJspWriter(4080, true));
        String text = "This is an example é◌";
        StringBuffer buffer = new StringBuffer();
        for (int i=0;i<4080;i=buffer.length()) {
            buffer.append(text);
        }
        byte[] bytes = buffer.toString().substring(0, 4080).getBytes();
        stream.write(bytes, 0, 4080);
    }

    @Test
    public void shouldWriteFailArraySize() throws Exception {
        MyServletOutputStream stream = new MyServletOutputStream(new DummyJspWriter(4080, true));
        String text = "é◌";
        StringBuffer buffer = new StringBuffer();
        for (int i=0;i<4080;i=buffer.length()) {
            buffer.append(text);
        }
        byte[] bytes = buffer.toString().substring(0, 4080).getBytes();
        stream.write(bytes, 0, 4080);
    }

    @Test
    public void shouldWriteFailRemainingNull() throws Exception {
        MyServletOutputStream stream = new MyServletOutputStream(new DummyJspWriter(4080, true));
        stream.bbuf = ByteBuffer.allocate(0);
        String text = "Some Text é◌";
        StringBuffer buffer = new StringBuffer();
        for (int i=0;i<4080;i=buffer.length()) {
            buffer.append(text);
        }
        byte[] bytes = buffer.toString().substring(0, 4080).getBytes();
        stream.write(bytes, 0, 4080);
    }

    @Test
    public void shouldWriteSuccessNullLength632() throws Exception {
        MyServletOutputStream stream = new MyServletOutputStream(new DummyJspWriter(4080, true));
        int max = 632;
        byte[] extend = new byte[max];
        for (int i=0;i<max;i++) {
            extend[i] = -61;
        }
        stream.write(extend, 0, max);
    }

    @Test
    public void shouldWriteFailNullLength633() throws Exception {
        MyServletOutputStream stream = new MyServletOutputStream(new DummyJspWriter(4080, true));
        int max = 633;
        byte[] extend = new byte[max];
        for (int i=0;i<max;i++) {
            extend[i] = -61;
        }
        stream.write(extend, 0, max);
    }
}
