package org.stripesframework.web.action;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Basic set of tests for the FileBean object.
 *
 * @author Tim Fennell
 */
public class FileBeanTests {

   public static final String[] LINES = { "Hello World!", "How have you been?" };
   File from, to;

   @AfterEach
   public void cleanupFiles() {
      if ( from != null && from.exists() ) {
         from.delete();
      }
      if ( to != null && to.exists() ) {
         to.delete();
      }
   }

   @BeforeEach
   public void setupFiles() throws IOException {
      // The from file
      from = File.createTempFile("foo", "bar");
      try (PrintWriter out = new PrintWriter(from)) {
         out.println(LINES[0]);
         out.println(LINES[1]);
      }

      // A to file
      to = new File(System.getProperty("java.io.tmpdir"), "foo-" + System.currentTimeMillis());
   }

   @Test
   public void testBasicSave() throws Exception {
      FileBean bean = new FileBean(from, "text/plain", "somefile.txt");

      bean.save(to);
      assertThat(to.exists()).isTrue();
      assertThat(from.exists()).isFalse();
      assertContents(to);
   }

   @Test
   public void testIntoDirectoryThatDoesNotExistYet() throws Exception {
      FileBean bean = new FileBean(from, "text/plain", "somefile.txt");
      File realTo = to;
      to = new File(to, "somechild.txt");
      try {
         bean.save(to);
         assertThat(to.exists()).isTrue();
         assertThat(from.exists()).isFalse();
         assertContents(to);
      }
      finally {
         to.delete();
         to = realTo;
      }
   }

   @Test
   public void testReader() throws Exception {
      FileBean bean = new FileBean(from, "text/plain", "somefile.txt");

      copyReaderOut(bean.getReader());

      assertThat(to.exists()).isTrue();
      assertThat(from.exists()).isFalse();
      assertContents(to);
   }

   @Test
   public void testReaderWithCharset1() throws Exception {
      String charset = Charset.defaultCharset().name();
      FileBean bean = new FileBean(from, "text/plain", "somefile.txt", charset);

      copyReaderOut(bean.getReader());

      assertThat(to.exists()).isTrue();
      assertThat(from.exists()).isFalse();
      assertContents(to);
   }

   @Test
   public void testReaderWithCharset2() throws Exception {
      String charset = Charset.defaultCharset().name();
      FileBean bean = new FileBean(from, "text/plain", "somefile.txt");

      copyReaderOut(bean.getReader(charset));

      assertThat(to.exists()).isTrue();
      assertThat(from.exists()).isFalse();
      assertContents(to);
   }

   @Test
   public void testSaveByCopy() throws Exception {
      FileBean bean = new FileBean(from, "text/plain", "somefile.txt");

      bean.saveViaCopy(to);
      assertThat(to.exists()).isTrue();
      assertThat(from.exists()).isFalse();
      assertContents(to);
   }

   @Test
   public void testSaveOverExistingFile() throws Exception {
      FileBean bean = new FileBean(from, "text/plain", "somefile.txt");

      to.createNewFile();
      bean.save(to);
      assertThat(to.exists()).isTrue();
      assertThat(from.exists()).isFalse();
      assertContents(to);
   }

   @Test
   public void testSaveOverExistingFileWithContents() throws Exception {
      FileBean bean = new FileBean(from, "text/plain", "somefile.txt");

      to.createNewFile();
      try (PrintWriter out = new PrintWriter(to)) {
         out.println("This is not what we should read back after the save!");
         out.println("If we get this text back we're in trouble!");
      }

      bean.save(to);
      assertThat(to.exists()).isTrue();
      assertThat(from.exists()).isFalse();
      assertContents(to);
   }

   /** Helper method to assert contents of post-copy file. */
   private void assertContents( File toFile ) throws IOException {
      try (BufferedReader in = new BufferedReader(new FileReader(toFile))) {
         assertThat(in.readLine()).isEqualTo(LINES[0]);
         assertThat(in.readLine()).isEqualTo(LINES[1]);
         assertThat(in.readLine()).isNull();
      }
   }

   /** Helper method that copies a reader into a writer. */
   private void copyReaderOut( Reader in ) throws IOException {
      try (Writer out = new FileWriter(to)) {
         char[] buf = new char[1024];
         for ( int count; (count = in.read(buf)) > 0; ) {
            out.write(buf, 0, count);
         }
      }
      finally {
         try {
            if ( in != null ) {
               in.close();
            }
         }
         catch ( Exception e ) {
            // Ignored
         }
         from.delete();
      }
   }
}
