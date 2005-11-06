/* Copyright (C) 2005 Tim Fennell
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the license with this software. If not,
 * it can be found online at http://www.fsf.org/licensing/licenses/lgpl.html
 */
package net.sourceforge.stripes.mock;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * <p>Mock implementation of an HttpServletResponse.  Captures any output is written along with
 * any headers, status information etc. and makes it available through various getter methods.</p>
 *
 * <p>Of major note is the fact that none of the setStatus(), sendError() or sendRedirect() methods
 * have any real effect on the request processing lifecycle.  Information is recorded so it can be
 * verified what was invoked, but that is all.</p>
 *
 * @author Tim Fennell
 * @since Stripes 1.1.1
 */
public class MockHttpServletResponse implements HttpServletResponse {
    private MockServletOutputStream out = new MockServletOutputStream();
    private PrintWriter writer = new PrintWriter(out, true);
    private Locale locale = Locale.getDefault();
    private Map<String,List<Object>> headers = new HashMap<String,List<Object>>();
    private List<Cookie> cookies = new ArrayList<Cookie>();
    private int status = 200;
    private String errorMessage;
    private String characterEncoding = "UTF-8";
    private int contentLength;
    private String contentType = "text/html";
    private String redirectUrl;

    /** Adds a cookie to the set of cookies in the response. */
    public void addCookie(Cookie cookie) { this.cookies.add(cookie); }

    /** Gets the set of cookies stored in the response. */
    public Cookie[] getCookies() { return this.cookies.toArray(new Cookie[this.cookies.size()]); }

    /** Returns true if the specified header was placed in the response. */
    public boolean containsHeader(String name) { return this.headers.containsKey(name); }

    /** Returns the URL unchanged. */
    public String encodeURL(String url) { return url; }

    /** Returns the URL unchanged. */
    public String encodeRedirectURL(String url) { return url; }

    /** Returns the URL unchanged. */
    public String encodeUrl(String url) { return url; }

    /** Returns the URL unchanged. */
    public String encodeRedirectUrl(String url) { return url; }

    /** Sets the status code and saves the message so it can be retrieved later. */
    public void sendError(int status, String errorMessage) throws IOException {
        this.status = status;
        this.errorMessage = errorMessage;
    }

    /** Sets that status code to the error code provided. */
    public void sendError(int status) throws IOException { this.status = status; }

    /** Simply stores the URL that was supplied, so that it can be examined later with getRedirectUrl. */
    public void sendRedirect(String url) throws IOException { this.redirectUrl = url; }

    /**
     * If a call was made to sendRedirect() this method will return the URL that was supplied.
     * Otherwise it will return null.
     */
    public String getRedirectUrl() { return this.redirectUrl; }

    /** Stores the value in a Long and saves it as a header. */
    public void setDateHeader(String name, long value) {
        this.headers.remove(name);
        addDateHeader(name, value);
    }

    /** Adds the specified value for the named header (does not remove/replace existing values). */
    public void addDateHeader(String name, long value) {
        List<Object> values = this.headers.get(name);
        if (values == null) {
            this.headers.put(name, values = new ArrayList<Object>());
        }
        values.add(value);
    }

    /** Sets the value of the specified header to the single value provided. */
    public void setHeader(String name, String value) {
        this.headers.remove(name);
        addHeader(name, value);
    }

    /** Adds the specified value for the named header (does not remove/replace existing values). */
    public void addHeader(String name, String value) {
        List<Object> values = this.headers.get(name);
        if (values == null) {
            this.headers.put(name, values = new ArrayList<Object>());
        }
        values.add(value);
    }

    /** Stores the value in an Integer and saves it as a header. */
    public void setIntHeader(String name, int value) {
        this.headers.remove(name);
        addIntHeader(name, value);
    }

    /** Adds the specified value for the named header (does not remove/replace existing values). */
    public void addIntHeader(String name, int value) {
        List<Object> values = this.headers.get(name);
        if (values == null) {
            this.headers.put(name, values = new ArrayList<Object>());
        }
        values.add(value);
    }

    /**
     * Provides access to all headers that were set. The format is a Map which uses the header
     * name as the key, and stores a List of Objects, one per header value.  The Objects will
     * be either Strings (if setHeader() was used), Integers (if setIntHeader() was used) or
     * Longs (if setDateHeader() was used).
     */
    public Map<String,List<Object>> getHeaderMap() { return this.headers; }

    /** Sets the HTTP Status code of the response. */
    public void setStatus(int statusCode) { this.status = statusCode; }

    /** Saves the HTTP status code and the message provided. */
    public void setStatus(int status, String errorMessage) {
        this.status = status;
        this.errorMessage = errorMessage;
    }

    /** Gets the status (or error) code if one was set. Defaults to 200 (HTTP OK). */
    public int getStatus() { return this.status; }

    /** Gets the error message if one was set with setStatus() or sendError(). */
    public String getErrorMessage() { return this.errorMessage; }

    /** Sets the character encoding on the request. */
    public void setCharacterEncoding(String encoding) { this.characterEncoding = encoding; }

    /** Gets the character encoding (defaults to UTF-8). */
    public String getCharacterEncoding() { return this.characterEncoding; }

    /** Sets the content type for the response. */
    public void setContentType(String string) { this.contentType = contentType; }

    /** Gets the content type for the response. Defaults to text/html. */
    public String getContentType() { return this.contentType; }

    /**
     * Returns a reference to a ServletOutputStream to be used for output. The output is captured
     * and can be examined at the end of a test run by calling getOutputBytes() or
     * getOutputString().
     */
    public ServletOutputStream getOutputStream() throws IOException { return this.out; }

    /**
     * Returns a reference to a PrintWriter to be used for character output. The output is captured
     * and can be examined at the end of a test run by calling getOutputBytes() or
     * getOutputString().
     */
    public PrintWriter getWriter() throws IOException { return this.writer; }

    /** Gets the output that was written to the output stream, as a byte[]. */
    public byte[] getOutputBytes() { return this.out.getBytes(); }

    /** Gets the output that was written to the output stream, as a character String. */
    public String getOutputString() { return this.out.getString(); }

    /** Sets a custom content length on the response. */
    public void setContentLength(int contentLength) { this.contentLength = contentLength; }

    /** Returns the content length if one was set on the response by calling setContentLength(). */
    public int getContentLength() { return this.contentLength; }

    /** Has no effect. */
    public void setBufferSize(int i) {  }

    /** Always returns 0. */
    public int getBufferSize() { return 0; }

    /** Has no effect. */
    public void flushBuffer() throws IOException { }

    /** Always throws IllegalStateException. */
    public void resetBuffer() {
        throw new IllegalStateException("reset() is not supported");
    }

    /** Always returns true. */
    public boolean isCommitted() { return true; }

    /** Always throws an IllegalStateException. */
    public void reset() {
        throw new IllegalStateException("reset() is not supported");
    }

    /** Sets the response locale to the one specified. */
    public void setLocale(Locale locale) { this.locale = locale; }

    /** Gets the response locale. Default to the system default locale. */
    public Locale getLocale() { return this.locale; }
}
