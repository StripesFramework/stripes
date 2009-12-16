/* Copyright 2005-2006 Tim Fennell
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
package net.sourceforge.stripes.action;

import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.util.Log;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <p>Resolution for streaming data back to the client (in place of forwarding the user to
 * another page). Designed to be used for streaming non-page data such as generated images/charts
 * and XML islands.</p>
 *
 * <p>Optionally supports the use of a file name which, if set, will cause a
 * Content-Disposition header to be written to the output, resulting in a "Save As" type
 * dialog box appearing in the user's browser. If you do not wish to supply a file name, but
 * wish to achieve this behaviour, simple supply a file name of "".</p>
 *
 * <p>StreamingResolution is designed to be subclassed where necessary to provide streaming
 * output where the data being streamed is not contained in an InputStream or Reader. This
 * would normally be done using an anonymous inner class as follows:</p>
 *
 *<pre>
 *return new StreamingResolution("text/xml") {
 *    public void stream(HttpServletResponse response) throws Exception {
 *        // custom output generation code
 *        response.getWriter().write(...);
 *        // or
 *        response.getOutputStream().write(...);
 *    }
 *}.setFilename("your-filename.xml");
 *</pre>
 *
 * @author Tim Fennell
 */
public class StreamingResolution implements Resolution {
    /** Date format string for RFC 822 dates. */
    private static final String RFC_822_DATE_FORMAT = "EEE, d MMM yyyy HH:mm:ss Z";
    private static final Log log = Log.getInstance(StreamingResolution.class);
    private InputStream inputStream;
    private Reader reader;
    private String filename;
    private String contentType;
    private String characterEncoding;
    private long lastModified = -1;
    private long length = -1;
    private boolean attachment;

    /**
     * Constructor only to be used when subclassing the StreamingResolution (usually using
     * an anonymous inner class. If this constructor is used, and stream() is not overridden
     * then an exception will be thrown!
     *
     * @param contentType the content type of the data in the stream (e.g. image/png)
     */
    public StreamingResolution(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Constructor that builds a StreamingResolution that will stream binary data back to the
     * client and identify the data as being of the specified content type.
     *
     * @param contentType the content type of the data in the stream (e.g. image/png)
     * @param inputStream an InputStream from which to read the data to return to the client
     */
    public StreamingResolution(String contentType, InputStream inputStream) {
        this.contentType = contentType;
        this.inputStream = inputStream;
    }

    /**
     * Constructor that builds a StreamingResolution that will stream character data back to the
     * client and identify the data as being of the specified content type.
     *
     * @param contentType the content type of the data in the stream (e.g. text/xml)
     * @param reader a Reader from which to read the character data to return to the client
     */
    public StreamingResolution(String contentType, Reader reader) {
        this.contentType = contentType;
        this.reader = reader;
    }

    /**
     * Constructor that builds a StreamingResolution that will stream character data from a String
     * back to the client and identify the data as being of the specified content type.
     *
     * @param contentType the content type of the data in the stream (e.g. text/xml)
     * @param output a String to stream back to the client
     */
    public StreamingResolution(String contentType, String output) {
        this(contentType, new StringReader(output));
    }

    /**
     * Sets the filename that will be the default name suggested when the user is prompted
     * to save the file/stream being sent back. If the stream is not for saving by the user
     * (i.e. it should be displayed or used by the browser) this value should not be set.
     *
     * @param filename the default filename the user will see
     * @return StreamingResolution so that this method call can be chained to the constructor
     *         and returned
     */
    public StreamingResolution setFilename(String filename) {
        this.filename = filename;
        setAttachment(filename != null);
        return this;
    }

    /**
     * Sets the character encoding that will be set on the request when executing this
     * resolution. If none is set, then the current character encoding (either the one
     * selected by the LocalePicker or the container default one) will be used.
     *
     * @param characterEncoding the character encoding to use instead of the default
     */
    public void setCharacterEncoding(String characterEncoding) {
        this.characterEncoding = characterEncoding;
    }

    /**
     * Sets the modification-date timestamp. If this property is set, the browser may be able to
     * apply it to the downloaded file. If this property is unset, the modification-date parameter
     * will be omitted.
     * 
     * @param lastModified The date-time (as a long) that the file was last modified. Optional.
     * @return StreamingResolution so that this method call can be chained to the constructor and
     *         returned.
     */
    public StreamingResolution setLastModified(long lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    /**
     * Sets the file length. If this property is set, the file size will be reported in the HTTP
     * header. This may help with file download progress indicators. If this property is unset, the
     * size parameter will be omitted.
     * 
     * @param length The length of the file in bytes.
     * @return StreamingResolution so that this method call can be chained to the constructor and
     *         returned.
     */
    public StreamingResolution setLength(long length) {
        this.length = length;
        return this;
    }

    /**
     * Indicates whether to use content-disposition attachment headers or not. (Defaults to true).
     * 
     * @param attachment Whether the content should be treated as an attachment, or a direct
     *            download.
     * @return StreamingResolution so that this method call can be chained to the constructor and
     *         returned.
     */
    public StreamingResolution setAttachment(boolean attachment) {
        this.attachment = attachment;
        return this;
    }

    /**
     * Streams data from the InputStream or Reader to the response's OutputStream or PrinterWriter,
     * using a moderately sized buffer to ensure that the operation is reasonable efficient.
     * Once the InputStream or Reader signaled the end of the stream, close() is called on it.
     *
     * @param request the HttpServletRequest being processed
     * @param response the paired HttpServletResponse
     * @throws IOException if there is a problem accessing one of the streams or reader/writer
     *         objects used.
     */
    final public void execute(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        applyHeaders(response);
        stream(response);
    }

    /**
     * Sets the response headers, based on what is known about the file or stream being handled.
     * 
     * @param response the current HttpServletResponse
     */
    protected void applyHeaders(HttpServletResponse response) {
        response.setContentType(this.contentType);
        if (this.characterEncoding != null) {
            response.setCharacterEncoding(characterEncoding);
        }

        // Set Content-Length header
        if (length >= 0) {
            // Odd that ServletResponse.setContentLength is limited to int.
            // requires downcast from long to int e.g.
            // response.setContentLength((int)length);
            // Workaround to allow large files:
            response.addHeader("Content-Length", Long.toString(length));
        }

        // Set Last-Modified header
        if (lastModified >= 0) {
            response.setDateHeader("Last-Modified", lastModified);
        }

        // For Content-Disposition spec, see http://www.ietf.org/rfc/rfc2183.txt
        if (attachment || filename != null) {
            // Value of filename should be RFC 2047 encoded here (see RFC 2616) but few browsers
            // support that, so just escape the quote for now
            String escaped = this.filename.replace("\"", "\\\"");
            StringBuilder header = new StringBuilder(attachment ? "attachment" : "inline").append(
                    ";filename=\"").append(escaped).append("\"");
            if (lastModified >= 0) {
                SimpleDateFormat format = new SimpleDateFormat(RFC_822_DATE_FORMAT);
                String value = format.format(new Date(lastModified));
                header.append(";modification-date=\"").append(value).append("\"");
            }
            if (length >= 0) {
                header.append(";size=").append(length);
            }
            response.setHeader("Content-Disposition", header.toString());
        }
    }

    /**
     * <p>
     * Does the actual streaming of data through the response. If subclassed, this method should be
     * overridden to stream back data other than data supplied by an InputStream or a Reader
     * supplied to a constructor.
     * </p>
     * 
     * <p>
     * If an InputStream or Reader was supplied to a constructor, this implementation uses a
     * moderately sized buffer to stream data from it to the response to make the operation
     * reasonably efficient, and closes the InputStream or the Reader. If an IOException occurs when
     * closing it, that exception will be logged as a warning, and <em>not</em> thrown to avoid
     * masking a possibly previously thrown exception.
     * </p>
     * 
     * @param response the HttpServletResponse from which either the output stream or writer can be
     *            obtained
     * @throws Exception if any problems arise when streaming data
     */
    protected void stream(HttpServletResponse response) throws Exception {
        int length = 0;
        if (this.reader != null) {
            char[] buffer = new char[512];
            try {
                PrintWriter out = response.getWriter();

                while ( (length = this.reader.read(buffer)) != -1 ) {
                    out.write(buffer, 0, length);
                }
            }
            finally {
                try {
                    this.reader.close();
                }
                catch (Exception e) {
                    log.warn("Error closing reader", e);
                }
            }
        }
        else if (this.inputStream != null) {
            byte[] buffer = new byte[512];
            try {
                ServletOutputStream out = response.getOutputStream();

                while ( (length = this.inputStream.read(buffer)) != -1) {
                    out.write(buffer, 0, length);
                }
            }
            finally {
                try {
                    this.inputStream.close();
                }
                catch (Exception e) {
                    log.warn("Error closing input stream", e);
                }
            }
        }
        else {
            throw new StripesRuntimeException("A StreamingResolution was constructed without " +
                    "supplying a Reader or InputStream, but stream() was not overridden. Please " +
                    "either supply a source of streaming data, or override the stream() method.");
        }
    }

}
