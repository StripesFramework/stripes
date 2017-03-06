package net.sourceforge.stripes.tag.layout;

import javax.servlet.jsp.JspWriter;
import java.io.IOException;

public class DummyJspWriter extends JspWriter {

    private StringBuffer buffer = new StringBuffer();

    public DummyJspWriter(final int bufferSize, final boolean autoFlush) {
        super(bufferSize, autoFlush);
    }

    @Override
    public void newLine() throws IOException {
        buffer.append("<br/>");
    }

    @Override
    public void print(boolean b) throws IOException {
        buffer.append(b);
    }

    @Override
    public void print(char c) throws IOException {
        buffer.append(c);
    }

    @Override
    public void print(int i) throws IOException {
        buffer.append(i);
    }

    @Override
    public void print(long l) throws IOException {
        buffer.append(l);
    }

    @Override
    public void print(float f) throws IOException {
        buffer.append(f);
    }

    @Override
    public void print(double d) throws IOException {
        buffer.append(d);
    }

    @Override
    public void print(char[] s) throws IOException {
        buffer.append(s);
    }

    @Override
    public void print(String s) throws IOException {
        buffer.append(s);
    }

    @Override
    public void print(Object obj) throws IOException {
        buffer.append(obj);
    }

    @Override
    public void println() throws IOException {
        this.newLine();
    }

    @Override
    public void println(boolean x) throws IOException {
        this.print(x);
        this.newLine();
    }

    @Override
    public void println(char x) throws IOException {
        this.print(x);
        this.newLine();
    }

    @Override
    public void println(int x) throws IOException {
        this.print(x);
        this.newLine();
    }

    @Override
    public void println(long x) throws IOException {
        this.print(x);
        this.newLine();
    }

    @Override
    public void println(float x) throws IOException {
        this.print(x);
        this.newLine();
    }

    @Override
    public void println(double x) throws IOException {
        this.print(x);
        this.newLine();
    }

    @Override
    public void println(char[] x) throws IOException {
        this.print(x);
        this.newLine();
    }

    @Override
    public void println(String x) throws IOException {
        this.print(x);
        this.newLine();
    }

    @Override
    public void println(Object x) throws IOException {
        this.print(x);
        this.newLine();
    }

    @Override
    public void clear() throws IOException {
        this.buffer = new StringBuffer();
    }

    @Override
    public void clearBuffer() throws IOException {
        buffer.delete(0, buffer.length());
    }

    @Override
    public void write(char[] chars, int i, int i1) throws IOException {
        buffer.append(chars);
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public int getRemaining() {
        return 0;
    }

    public String getOutput() {
        return buffer.toString();
    }
}
