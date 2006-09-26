package net.sourceforge.stripes.action;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.testng.Assert;

import java.io.*;

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
        FileWriter out = new FileWriter(this.from);
        out.write(LINES[0]);
        out.write('\n');
        out.write(LINES[1]);
        out.write('\n');
        out.close();

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
        BufferedReader in = new BufferedReader(new FileReader(toFile));
        Assert.assertEquals(in.readLine(), LINES[0]);
        Assert.assertEquals(in.readLine(), LINES[1]);
        Assert.assertNull(in.readLine());
    }

    @Test(groups="fast")
    public void testBasicSave() throws Exception {
        FileBean bean = new FileBean(from, "text/plain", "somefile.txt");

        bean.save(this.to);
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
        BufferedWriter out = new BufferedWriter(new FileWriter(this.to));
        out.write("This is not what we should read back after the save!\n");
        out.write("If we get this text back we're in trouble!\n");
        out.close();

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
