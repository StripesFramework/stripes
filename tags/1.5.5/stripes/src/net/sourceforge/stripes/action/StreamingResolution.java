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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.util.Range;

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
    /** Boundary for use in multipart responses. */
    private static final String MULTIPART_BOUNDARY = "BOUNDARY_F7C98B76AEF711DF86D1B4FCDFD72085";
    private static final Log log = Log.getInstance(StreamingResolution.class);
    private InputStream inputStream;
    private Reader reader;
    private String filename;
    private String contentType;
    private String characterEncoding;
    private long lastModified = -1;
    private long length = -1;
    private boolean attachment;
    private boolean rangeSupport = false;
    private List<Range<Long>> byteRanges;

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
     * Indicates whether byte range serving is supported by stream method. (Defaults to false).
     * Besides setting this flag, the ActionBean also needs to set the length of the response and
     * provide an {@link InputStream}-based input. Reasons for disabling byte range serving:
     * <ul>
     * <li>The stream method is overridden and does not support byte range serving</li>
     * <li>The input to this {@link StreamingResolution} was created on-demand, and retrieving in
     * byte ranges would redo this process for every byte range.</li>
     * </ul>
     * Reasons for enabling byte range serving:
     * <ul>
     * <li>Streaming static multimedia files</li>
     * <li>Supporting resuming download managers</li>
     * </ul>
     * 
     * @param rangeSupport Whether byte range serving is supported by stream method.
     * @return StreamingResolution so that this method call can be chained to the constructor and
     *         returned.
     */
    public StreamingResolution setRangeSupport(boolean rangeSupport) {
        this.rangeSupport = rangeSupport;
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
        /*-
         * Process byte ranges only when the following three conditions are met:
         *     - Length has been defined (without length it is impossible to efficiently stream)
         *     - rangeSupport has not been set to false
         *     - Output is binary and not character based
        -*/
        if (rangeSupport && (length >= 0) && (inputStream != null))
            byteRanges = parseRangeHeader(request.getHeader("Range"));

        applyHeaders(response);
        stream(response);
    }

    /**
     * Sets the response headers, based on what is known about the file or stream being handled.
     * 
     * @param response the current HttpServletResponse
     */
    protected void applyHeaders(HttpServletResponse response) {
        if (byteRanges != null) {
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        }

        if ((byteRanges == null) || (byteRanges.size() == 1)) {
            response.setContentType(this.contentType);
        }
        else {
            response.setContentType("multipart/byteranges; boundary=" + MULTIPART_BOUNDARY);
        }

        if (this.characterEncoding != null) {
            response.setCharacterEncoding(characterEncoding);
        }

        // Set Content-Length header
        if (length >= 0) {
            if (byteRanges == null) {
                // Odd that ServletResponse.setContentLength is limited to int.
                // requires downcast from long to int e.g.
                // response.setContentLength((int)length);
                // Workaround to allow large files:
                response.addHeader("Content-Length", Long.toString(length));
            }
            else if (byteRanges.size() == 1) {
                Range<Long> byteRange;

                byteRange = byteRanges.get(0);
                response.setHeader("Content-Length",
                        Long.toString(byteRange.getEnd() - byteRange.getStart() + 1));
                response.setHeader("Content-Range", "bytes " + byteRange.getStart() + "-"
                        + byteRange.getEnd() + "/" + length);
            }
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
     * Parse the Range header according to RFC 2616 section 14.35.1. Example ranges from this
     * section:
     * <ul>
     * <li>The first 500 bytes (byte offsets 0-499, inclusive): bytes=0-499</li>
     * <li>The second 500 bytes (byte offsets 500-999, inclusive): bytes=500-999</li>
     * <li>The final 500 bytes (byte offsets 9500-9999, inclusive): bytes=-500 - Or bytes=9500-</li>
     * <li>The first and last bytes only (bytes 0 and 9999): bytes=0-0,-1</li>
     * <li>Several legal but not canonical specifications of the second 500 bytes (byte offsets
     * 500-999, inclusive): bytes=500-600,601-999 bytes=500-700,601-999</li>
     * </ul>
     * 
     * @param value the value of the Range header
     * @return List of sorted, non-overlapping ranges
     */
    protected List<Range<Long>> parseRangeHeader(String value) {
        Iterator<Range<Long>> i;
        String byteRangesSpecifier[], bytesUnit, byteRangeSet[];
        List<Range<Long>> res;
        long lastEnd = -1;

        if (value == null)
            return null;
        res = new ArrayList<Range<Long>>();
        // Parse prelude
        byteRangesSpecifier = value.split("=");
        if (byteRangesSpecifier.length != 2)
            return null;
        bytesUnit = byteRangesSpecifier[0];
        byteRangeSet = byteRangesSpecifier[1].split(",");
        if (!bytesUnit.equals("bytes"))
            return null;
        // Parse individual byte ranges
        for (String byteRangeSpec : byteRangeSet) {
            String[] bytePos;
            Long firstBytePos = null, lastBytePos = null;

            bytePos = byteRangeSpec.split("-", -1);
            try {
                if (bytePos[0].trim().length() > 0)
                    firstBytePos = Long.valueOf(bytePos[0].trim());
                if (bytePos[1].trim().length() > 0)
                    lastBytePos = Long.valueOf(bytePos[1].trim());
            }
            catch (NumberFormatException e) {
                log.warn("Unable to parse Range header", e);
            }
            if ((firstBytePos == null) && (lastBytePos == null)) {
                return null;
            }
            else if (firstBytePos == null) {
                firstBytePos = length - lastBytePos;
                lastBytePos = length - 1;
            }
            else if (lastBytePos == null) {
                lastBytePos = length - 1;
            }
            if (firstBytePos > lastBytePos)
                return null;
            if (firstBytePos < 0)
                return null;
            if (lastBytePos >= length)
                return null;
            res.add(new Range<Long>(firstBytePos, lastBytePos));
        }
        // Sort byte ranges
        Collections.sort(res);
        // Remove overlapping ranges
        i = res.listIterator();
        while (i.hasNext()) {
            Range<Long> range;

            range = i.next();
            if (lastEnd >= range.getStart()) {
                range.setStart(lastEnd + 1);
                if ((range.getStart() >= length) || (range.getStart() > range.getEnd()))
                    i.remove();
                else
                    lastEnd = range.getEnd();
            }
            else {
                lastEnd = range.getEnd();
            }
        }
        if (res.isEmpty())
            return null;
        else
            return res;
    }

    /**
     * <p>
     * Does the actual streaming of data through the response. If subclassed, this method should be
     * overridden to stream back data other than data supplied by an InputStream or a Reader
     * supplied to a constructor. If not implementing byte range serving, be sure not to set
     * rangeSupport to true.
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
            long count = 0;

            try {
                ServletOutputStream out = response.getOutputStream();

                if (byteRanges == null) {
                    while ((length = this.inputStream.read(buffer)) != -1) {
                        out.write(buffer, 0, length);
                    }
                }
                else {
                    for (Range<Long> byteRange : byteRanges) {
                        // See RFC 2616 section 14.16
                        if (byteRanges.size() > 1) {
                            out.print("--" + MULTIPART_BOUNDARY + "\r\n");
                            out.print("Content-Type: " + contentType + "\r\n");
                            out.print("Content-Range: bytes " + byteRange.getStart() + "-"
                                    + byteRange.getEnd() + "/" + this.length + "\r\n");
                            out.print("\r\n");
                        }
                        if (count < byteRange.getStart()) {
                            long skip;

                            skip = byteRange.getStart() - count;
                            this.inputStream.skip(skip);
                            count += skip;
                        }
                        while ((length = this.inputStream.read(buffer, 0, (int) Math.min(
                                (long) buffer.length, byteRange.getEnd() + 1 - count))) != -1) {
                            out.write(buffer, 0, length);
                            count += length;
                            if (byteRange.getEnd() + 1 == count)
                                break;
                        }
                        if (byteRanges.size() > 1) {
                            out.print("\r\n");
                        }
                    }
                    if (byteRanges.size() > 1)
                        out.print("--" + MULTIPART_BOUNDARY + "--\r\n");
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
