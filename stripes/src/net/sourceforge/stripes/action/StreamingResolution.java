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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.StringReader;

/**
 * <p>Resolution for streaming data back to the client (in place of forwarding the user to
 * another page). Designed to be used for streaming non-page data such as generated images/charts
 * and XML islands.<p>
 *
 * <p>Optionally supports the use of a file name which, if set, will cause a
 * Content-Disposition header to be written to the output, resulting in a "Save As" type
 * dialog box appearing in the user's browser. If you do not wish to supply a file name, but
 * wish to acheive this behaviour, simple supply a file name of "".<p>
 *
 * @author Tim Fennell
 */
public class StreamingResolution implements Resolution {
    private InputStream inputStream;
    private Reader reader;
    private String filename;
    private String contentType;
    private String characterEncoding;

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
     * Streams data from the InputStream or Reader to the response's OutputStream or PrinterWriter,
     * using a moderately sized buffer to ensure that the operation is reasonable efficient.
     * Once the InputStream or Reader signaled the end of the stream, close() is called on it.
     *
     * @param request the HttpServletRequest being processed
     * @param response the paired HttpServletReponse
     * @throws IOException if there is a problem accessing one of the streams or reader/writer
     *         objects used.
     */
    public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType(this.contentType);
        if (this.characterEncoding != null) response.setCharacterEncoding(characterEncoding);

        // If a filename was specified, set the appropriate header
        if (this.filename != null) {
            response.setHeader("Content-disposition", "attachment; filename=" + this.filename);
        }

        int length = 0;
        if (reader != null) {
            char[] buffer = new char[512];
            PrintWriter out = response.getWriter();

            while ( (length = this.reader.read(buffer)) != -1 ) {
                out.write(buffer, 0, length);
            }
            this.reader.close();
        }
        else {
            byte[] buffer = new byte[512];
            ServletOutputStream out = response.getOutputStream();

            while ( (length = this.inputStream.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }

            this.inputStream.close();
        }
    }
}
