/* Copyright 2005-2006 Tim Fennell
 * Copyright 2023 Rick Grashel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sourceforge.stripes.controller.multipart;

import static org.apache.commons.fileupload2.core.DiskFileItemFactory.DEFAULT_THRESHOLD;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Pattern;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.controller.FileUploadLimitExceededException;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.fileupload2.core.FileUploadException;
import org.apache.commons.fileupload2.core.FileUploadSizeException;
import org.apache.commons.fileupload2.jakarta.JakartaServletFileUpload;

/**
 * An implementation of MultipartWrapper that uses Jakarta Commons FileUpload (from apache) to parse
 * the request parts. This implementation requires that both commons-fileupload and commons-io be
 * present in the classpath. While this implementation does introduce additional dependencies, it's
 * licensing (ASL 2.0) is significantly less restrictive than the licensing for COS - the other
 * alternative provided by Stripes.
 *
 * @author Tim Fennell
 * @author Rick Grashel
 * @since Stripes 1.4
 * @since Stripes 1.6.1
 */
public class CommonsMultipartWrapper implements MultipartWrapper {
  private static final Pattern WINDOWS_PATH_PREFIX_PATTERN = Pattern.compile("(?i:^[A-Z]:\\\\)");

  /*
   * Ensure this class will not load unless Commons FileUpload is on the classpath.
   */
  static {
    //noinspection ResultOfMethodCallIgnored
    FileUploadException.class.getName();
  }

  @SuppressWarnings("rawtypes")
  private final Map<String, FileItem> files = new HashMap<>();

  private final Map<String, String[]> parameters = new HashMap<>();
  private String charset;

  /**
   * Pseudo-constructor that allows the class to perform any initialization necessary.
   *
   * @param request an HttpServletRequest that has a content-type of multipart.
   * @param tempDir a File representing the temporary directory that can be used to store file parts
   *     as they are uploaded if this is desirable
   * @param maxPostSize the size in bytes beyond which the request should not be read, and a
   *     FileUploadLimitExceeded exception should be thrown
   * @throws IOException if a problem occurs processing the request of storing temporary files
   * @throws FileUploadLimitExceededException if the POST content is longer than the maxPostSize
   *     supplied.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void build(HttpServletRequest request, File tempDir, long maxPostSize)
      throws IOException, FileUploadLimitExceededException {
    try {
      this.charset = request.getCharacterEncoding();
      DiskFileItemFactory factory =
          DiskFileItemFactory.builder()
              .setPath(tempDir.toPath())
              .setBufferSize(DEFAULT_THRESHOLD)
              .get();

      JakartaServletFileUpload upload = new JakartaServletFileUpload(factory);
      upload.setSizeMax(maxPostSize);
      List<FileItem> items = upload.parseRequest(request);
      Map<String, List<String>> params = new HashMap<>();

      for (FileItem item : items) {
        // If it's a form field, add the string value to the list
        if (item.isFormField()) {
          List<String> values = params.computeIfAbsent(item.getFieldName(), k -> new ArrayList<>());
          values.add(charset == null ? item.getString() : item.getString(Charset.forName(charset)));
        }
        // Else store the file param
        else {
          files.put(item.getFieldName(), item);
        }
      }

      // Now convert them down into the usual map of String->String[]
      for (Map.Entry<String, List<String>> entry : params.entrySet()) {
        List<String> values = entry.getValue();
        this.parameters.put(entry.getKey(), values.toArray(new String[0]));
      }
    } catch (FileUploadSizeException fuse) {
      throw new FileUploadLimitExceededException(maxPostSize, fuse.getActualSize());
    } catch (FileUploadException fue) {
      throw new IOException("Could not parse and cache file upload data.", fue);
    }
  }

  /**
   * Fetches the names of all non-file parameters in the request. Directly analogous to the method
   * of the same name in HttpServletRequest when the request is non-multipart.
   *
   * @return an Enumeration of all non-file parameter names in the request
   */
  public Enumeration<String> getParameterNames() {
    return new IteratorEnumeration(this.parameters.keySet().iterator());
  }

  /**
   * Fetches all values of a specific parameter in the request. To simulate the HTTP request style,
   * the array should be null for non-present parameters, and values in the array should never be
   * null - the empty String should be used when there is value.
   *
   * @param name the name of the request parameter
   * @return an array of non-null parameters or null
   */
  public String[] getParameterValues(String name) {
    return this.parameters.get(name);
  }

  /**
   * Fetches the names of all file parameters in the request. Note that these are not the file
   * names, but the names given to the form fields in which the files are specified.
   *
   * @return the names of all file parameters in the request.
   */
  public Enumeration<String> getFileParameterNames() {
    return new IteratorEnumeration(this.files.keySet().iterator());
  }

  /**
   * Responsible for constructing a FileBean object for the named file parameter. If there is no
   * file parameter with the specified name this method should return null.
   *
   * @param name the name of the file parameter
   * @return a FileBean object wrapping the uploaded file
   */
  @SuppressWarnings("rawtypes")
  public FileBean getFileParameterValue(String name) {
    final FileItem item = this.files.get(name);
    if (item == null
        || ((item.getName() == null || item.getName().isEmpty()) && item.getSize() == 0)) {
      return null;
    } else {
      // Attempt to ensure the file name is just the basename with no path included
      String filename = item.getName();
      int index;
      if (WINDOWS_PATH_PREFIX_PATTERN.matcher(filename).find()) index = filename.lastIndexOf('\\');
      else index = filename.lastIndexOf('/');
      if (index >= 0 && index + 1 < filename.length() - 1) filename = filename.substring(index + 1);

      // Use an anonymous inner subclass of FileBean that overrides all the
      // methods that rely on having a File present, to use the FileItem
      // created by commons upload instead.
      return new FileBean(null, item.getContentType(), filename, this.charset) {
        @Override
        public long getSize() {
          return item.getSize();
        }

        @Override
        public InputStream getInputStream() throws IOException {
          return item.getInputStream();
        }

        @Override
        public void save(File toFile) throws IOException {
          try {
            item.write(toFile.toPath());
            delete();
          } catch (Exception e) {
            if (e instanceof IOException) throw (IOException) e;
            else {
              throw new IOException("Problem saving uploaded file.", e);
            }
          }
        }

        @Override
        public void delete() throws IOException {
          item.delete();
        }
      };
    }
  }

  /** Little helper class to create an enumeration as per the interface. */
  private static class IteratorEnumeration implements Enumeration<String> {
    Iterator<String> iterator;

    /** Constructs an enumeration that consumes from the underlying iterator. */
    IteratorEnumeration(Iterator<String> iterator) {
      this.iterator = iterator;
    }

    /** Returns true if more elements can be consumed, false otherwise. */
    public boolean hasMoreElements() {
      return this.iterator.hasNext();
    }

    /** Gets the next element out of the iterator. */
    public String nextElement() {
      return this.iterator.next();
    }
  }
}
