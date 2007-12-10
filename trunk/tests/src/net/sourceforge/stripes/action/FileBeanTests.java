package net.sourceforge.stripes.action;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.testng.Assert;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Basic set of tests for the FileBean object.
 *
 * @author Tim Fennell
 */
public class FileBeanTests {
    public static final String[] LINES = {"Hello World!", "How have you been?"};
    File from, to;

    @BeforeMethod(alwaysRun=true)
    public void setupFiles() throws IOException {
        // The from file
        this.from = File.createTempFile("foo", "bar");
        PrintWriter out = null;
        try {
            out = new PrintWriter(this.from);
            out.println(LINES[0]);
            out.println(LINES[1]);
        }
        finally {
            try { out.close(); } catch (Exception e) {}
        }

        // A to file
        this.to = new File(System.getProperty("java.io.tmpdir"), "foo-" + System.currentTimeMillis());
    }

    @AfterMethod(alwaysRun=true)
    public void cleanupFiles() {
        if (this.from != null && this.from.exists()) this.from.delete();
        if (this.to != null && this.to.exists()) this.to.delete();
    }

    /** Helper method to assert contents of post-copy file. */
    private void assertContents(File toFile) throws IOException {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(toFile));
            Assert.assertEquals(in.readLine(), LINES[0]);
            Assert.assertEquals(in.readLine(), LINES[1]);
            Assert.assertNull(in.readLine());
        }
        finally {
            try { in.close(); } catch (Exception e) {}
        }
    }

    /** Helper method that copies a reader into a writer. */
    private void copyReaderOut(Reader in) throws IOException {
        Writer out = null;
        try {
            out = new FileWriter(this.to);
            char[] buf = new char[1024];
            for (int count; (count = in.read(buf)) > 0;)
                out.write(buf, 0, count);
        }
        finally {
            try {
                if (in != null)
                    in.close();
            }
            catch (Exception e) {
            }
            try {
                if (out != null)
                    out.close();
            }
            catch (Exception e) {
            }
            this.from.delete();
        }
    }

    @Test(groups="fast")
    public void testBasicSave() throws Exception {
        FileBean bean = new FileBean(from, "text/plain", "somefile.txt");

        bean.save(this.to);
        Assert.assertTrue(this.to.exists());
        Assert.assertFalse(this.from.exists());
        assertContents(this.to);
    }

    @Test(groups = "fast")
    public void testReader() throws Exception {
        FileBean bean = new FileBean(from, "text/plain", "somefile.txt");

        copyReaderOut(bean.getReader());

        Assert.assertTrue(this.to.exists());
        Assert.assertFalse(this.from.exists());
        assertContents(this.to);
    }

    @Test(groups = "fast")
    public void testReaderWithCharset1() throws Exception {
        String charset = Charset.defaultCharset().name();
        FileBean bean = new FileBean(from, "text/plain", "somefile.txt", charset);

        copyReaderOut(bean.getReader());

        Assert.assertTrue(this.to.exists());
        Assert.assertFalse(this.from.exists());
        assertContents(this.to);
    }

    @Test(groups = "fast")
    public void testReaderWithCharset2() throws Exception {
        String charset = Charset.defaultCharset().name();
        FileBean bean = new FileBean(from, "text/plain", "somefile.txt");

        copyReaderOut(bean.getReader(charset));

        Assert.assertTrue(this.to.exists());
        Assert.assertFalse(this.from.exists());
        assertContents(this.to);
    }

    @Test(groups="fast")
    public void testSaveByCopy() throws Exception {
        FileBean bean = new FileBean(from, "text/plain", "somefile.txt");

        bean.saveViaCopy(this.to);
        Assert.assertTrue(this.to.exists());
        Assert.assertFalse(this.from.exists());
        assertContents(this.to);
    }

    @Test(groups="fast")
    public void testSaveOverExistingFile() throws Exception {
        FileBean bean = new FileBean(from, "text/plain", "somefile.txt");

        Assert.assertTrue(this.to.createNewFile());
        bean.save(this.to);
        Assert.assertTrue(this.to.exists());
        Assert.assertFalse(this.from.exists());
        assertContents(this.to);
    }

    @Test(groups="fast")
    public void testSaveOverExistingFileWithContents() throws Exception {
        FileBean bean = new FileBean(from, "text/plain", "somefile.txt");

        Assert.assertTrue(this.to.createNewFile());
        PrintWriter out = null;
        try {
            out = new PrintWriter(this.to);
            out.println("This is not what we should read back after the save!");
            out.println("If we get this text back we're in trouble!");
        }
        finally {
            try { out.close(); } catch (Exception e) {}
        }

        bean.save(this.to);
        Assert.assertTrue(this.to.exists());
        Assert.assertFalse(this.from.exists());
        assertContents(this.to);
    }

    @Test(groups="fast")
    public void testIntoDirectoryThatDoesNotExistYet() throws Exception {
        FileBean bean = new FileBean(from, "text/plain", "somefile.txt");
        this.to = new File(this.to, "somechild.txt");

        bean.save(this.to);
        Assert.assertTrue(this.to.exists());
        Assert.assertFalse(this.from.exists());
        assertContents(this.to);
    }
}
