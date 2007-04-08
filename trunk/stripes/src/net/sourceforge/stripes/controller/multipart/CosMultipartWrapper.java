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
package net.sourceforge.stripes.controller.multipart;

import com.oreilly.servlet.MultipartRequest;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.controller.FileUploadLimitExceededException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.File;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of MultipartWrapper that uses Jason Hunter's COS (com.oreilly.servlet)
 * multipart parser implementation. This is the default implementation in Stripes and is
 * generally preferred as it is a) free for use and b) has no other dependencies! However,
 * commercial redistribution of the COS library requires licensing from Jason Hunter, so
 * this implemenation may not be applicable for commercial products that are distributed/sold
 * (though it is fine for commercial applications that are simply developed and hosted by the
 * company developing them).
 *
 * @author Tim Fennell
 * @since Stripes 1.4
 */
public class CosMultipartWrapper implements MultipartWrapper {
    /** Pattern used to parse useful info out of the IOException cos throws. */
    private static Pattern EXCEPTION_PATTERN =
            Pattern.compile("Posted content length of (\\d*) exceeds limit of (\\d*)");

    private MultipartRequest multipart;
    private String charset;
    /**
     * Pseudo-constructor that allows the class to perform any initialization necessary.
     *
     * @param request an HttpServletRequest that has a content-type of multipart.
     * @param tempDir a File representing the temporary directory that can be used to store
     *        file parts as they are uploaded if this is desirable
     * @param maxPostSize the size in bytes beyond which the request should not be read, and a
     *                    FileUploadLimitExceeded exception should be thrown
     * @throws IOException if a problem occurrs processing the request of storing temporary
     *                     files
     * @throws FileUploadLimitExceededException if the POST content is longer than the
     *         maxPostSize supplied.
     */
    public void build(HttpServletRequest request, File tempDir, long maxPostSize)
            throws IOException, FileUploadLimitExceededException {

        try {
            this.charset = request.getCharacterEncoding();
            this.multipart = new MultipartRequest(request,
                                                  tempDir.getAbsolutePath(),
                                                  (int) maxPostSize,
                                                  this.charset);
        }
        catch (IOException ioe) {
            Matcher matcher = EXCEPTION_PATTERN.matcher(ioe.getMessage());

            if (matcher.matches()) {
                throw new FileUploadLimitExceededException(Long.parseLong(matcher.group(2)),
                                                           Long.parseLong(matcher.group(1)));
            }
            else {
                throw ioe;
            }
        }
    }

    /**
     * Fetches the names of all non-file parameters in the request. Directly analogous to the method
     * of the same name in HttpServletRequest when the request is non-multipart.
     *
     * @return an Enumeration of all non-file parameter names in the rqequest
     */
    @SuppressWarnings("unchecked")
	public Enumeration<String> getParameterNames() {
        return this.multipart.getParameterNames();
    }

    /**
     * Fetches all values of a specifical parameter in the request. To simulate the HTTP request
     * style, the array should be null for non-present parameters, and values in the array should
     * never be null - the empty String should be used when there is value.
     *
     * @param name the name of the request parameter
     * @return an array of non-null parameters or null
     */
    public String[] getParameterValues(String name) {
        String[] values = this.multipart.getParameterValues(name);
        if (values != null) {
            for (int i=0; i<values.length; ++i) {
                if (values[i] == null) {
                    values[i] = "";
                }
            }
        }

        return values;
    }

    /**
     * Fetches the names of all file parameters in the request. Note that these are not the file
     * names, but the names given to the form fields in which the files are specified.
     *
     * @return the names of all file parameters in the request.
     */
    @SuppressWarnings("unchecked")
	public Enumeration<String> getFileParameterNames() {
        return this.multipart.getFileNames();
    }

    /**
     * Responsible for contructing a FileBean object for the named file parameter. If there is no
     * file parameter with the specified name this method should return null.
     *
     * @param name the name of the file parameter
     * @return a FileBean object wrapping the uploaded file
     */
    public FileBean getFileParameterValue(String name) {
        File file = this.multipart.getFile(name);
        if (file != null) {
            return new FileBean(file,
                                this.multipart.getContentType(name),
                                this.multipart.getOriginalFileName(name),
                                this.charset);
        }
        else {
            return null;
        }
    }
}
