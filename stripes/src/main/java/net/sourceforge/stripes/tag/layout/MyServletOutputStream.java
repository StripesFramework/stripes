package net.sourceforge.stripes.tag.layout;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * A servlet output stream implementation that decodes bytes to characters and writes the
 * characters to an underlying writer.
 */
class MyServletOutputStream extends ServletOutputStream {
    static final String DEFAULT_CHARSET = "UTF-8";
    static final int BUFFER_SIZE = 1024;

    Writer out;
    String charset = DEFAULT_CHARSET;
    CharsetDecoder decoder;
    ByteBuffer bbuf;
    CharBuffer cbuf;

    /** Construct a new instance that sends output to the specified writer. */
    MyServletOutputStream(Writer out) {
        this.out = out;
    }

    /** Get the character set to which bytes will be decoded. */
    String getCharset() {
        return charset;
    }

    /** Set the character set to which bytes will be decoded. */
    void setCharset(String charset) {
        if (charset == null)
            charset = DEFAULT_CHARSET;

        // Create a new decoder only if the charset has changed
        if (!charset.equals(this.charset))
            decoder = null;

        this.charset = charset;
    }

    /** Initialize the character decoder, byte buffer and character buffer. */
    void initDecoder() {
        if (decoder == null) {
            decoder = Charset.forName(getCharset()).newDecoder();

            if (bbuf == null)
                bbuf = ByteBuffer.allocate(BUFFER_SIZE);

            int size = (int) Math.ceil(BUFFER_SIZE * decoder.maxCharsPerByte());
            if (cbuf == null || cbuf.capacity() != size)
                cbuf = CharBuffer.allocate(size);
        }
    }

    /**
     * Clear the byte buffer. If the byte buffer has any data remaining to be read, then
     * those bytes are shifted to the front of the buffer and the buffer's position is
     * updated accordingly.
     */
    void resetBuffer() {
        if (bbuf.hasRemaining()) {
            ByteBuffer slice = bbuf.slice();
            bbuf.clear();
            bbuf.put(slice);
        }
        else {
            bbuf.clear();
        }
    }

    /**
     * Decode the contents of the byte buffer to the character buffer and then write the
     * contents of the character buffer to the underlying writer.
     */
    void decodeBuffer() throws IOException {
        bbuf.flip();
        cbuf.clear();
        decoder.decode(bbuf, cbuf, false);
        cbuf.flip();
        out.write(cbuf.array(), cbuf.position(), cbuf.remaining());
        resetBuffer();
    }

    @Override
    public void print(char c) throws IOException {
        out.write(c);
    }

    @Override
    public void print(String s) throws IOException {
        out.write(s);
    }

    @Override
    public void write(int b) throws IOException {
        initDecoder();
        bbuf.put((byte) b);
        decodeBuffer();
    }

    @Override
    public void write(byte[] buf, int off, int len) throws IOException {
        initDecoder();

        int copied = 0;
        while (copied<len && bbuf.remaining() > 0) {
            int toCopy = Math.min(len - copied, bbuf.remaining());
            bbuf.put(buf, copied, toCopy);
            decodeBuffer();
            copied = copied + toCopy;
        }
    }

    @Override
    public void write(byte[] buf) throws IOException {
        write(buf, 0, buf.length);
    }
}
