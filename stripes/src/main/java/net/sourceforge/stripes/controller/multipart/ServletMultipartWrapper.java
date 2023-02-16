package net.sourceforge.stripes.controller.multipart;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.controller.FileUploadLimitExceededException;
import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;


public class ServletMultipartWrapper implements MultipartWrapper {

  private final Map<String, String[]> parameters = new HashMap<>();


  private final Map<String, Part> uploadParts = new HashMap<>();

  private String charset;

  @Override
  public void build(HttpServletRequest request) throws IOException {
    throw new UnsupportedOperationException("Multipart wrappers cannot use this build() method.  They must use the one in the build() interface.");
  }

  @Override
  public Enumeration<String> getParameterNames() {
    return Collections.enumeration(this.parameters.keySet());

  }

  @Override
  public String[] getParameterValues(String name) {
    return this.parameters.get(name);
  }

  @Override
  public void build(HttpServletRequest request, File tempDir, long maxPostSize) throws IOException, FileUploadLimitExceededException {


    try {
      this.charset = request.getCharacterEncoding();
      final Collection<Part> parts = request.getParts();

      // TODO: do we have to filter here the part stuff out ?
      this.parameters.putAll(request.getParameterMap());

      for (final Part part : parts) {
        final var submittedFileName = part.getSubmittedFileName();
        if(submittedFileName != null) {
          this.uploadParts.put(part.getName(), part);
        }
      }

    } catch (ServletException e) {

      throw new StripesRuntimeException(
          "An error happened while handling uploaded file.", e);
    }

  }

  @Override
  public Enumeration<String> getFileParameterNames() {
    return Collections.enumeration(this.uploadParts.keySet());
  }

  @Override
  public FileBean getFileParameterValue(final String name) {

    final Part part = this.uploadParts.get(name);

    if (part == null) {
      return null;
    }


    return new FileBean(null, part.getContentType(), part.getSubmittedFileName(),  this.charset) {
      @Override
      public long getSize() {
        return part.getSize();
      }

      @Override
      public InputStream getInputStream() throws IOException {
        return part.getInputStream();
      }

      @Override
      public void save(final File toFile) throws IOException {
        part.write(toFile.getAbsolutePath());
        delete();
      }

      @Override
      public void delete() throws IOException {
        part.delete();
      }
    };
  }

}
