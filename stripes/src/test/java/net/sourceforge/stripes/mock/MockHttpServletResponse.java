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
package net.sourceforge.stripes.mock;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;


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

   private final MockServletOutputStream   _out               = new MockServletOutputStream();
   private final PrintWriter               _writer            = new PrintWriter(_out, true);
   private       Locale                    _locale            = Locale.getDefault();
   private final Map<String, List<Object>> _headers           = new HashMap<>();
   private final List<Cookie>              _cookies           = new ArrayList<>();
   private       int                       _status            = 200;
   private       String                    _errorMessage;
   private       String                    _characterEncoding = "UTF-8";
   private       int                       _contentLength;
   private       String                    _contentType;
   private       String                    _redirectUrl;

   public MockHttpServletResponse() {
      setContentType("text/html");
   }

   /** Adds a cookie to the set of cookies in the response. */
   @Override
   public void addCookie( Cookie cookie ) {
      // Remove existing cookies with the same name as the new one
      _cookies.removeIf(cookie1 -> cookie1.getName().equals(cookie.getName()));

      _cookies.add(cookie);
   }

   /** Adds the specified value for the named header (does not remove/replace existing values). */
   @Override
   public void addDateHeader( String name, long value ) {
      List<Object> values = _headers.computeIfAbsent(name, k -> new ArrayList<>());
      values.add(value);
   }

   /** Adds the specified value for the named header (does not remove/replace existing values). */
   @Override
   public void addHeader( String name, String value ) {
      List<Object> values = _headers.computeIfAbsent(name, k -> new ArrayList<>());
      values.add(value);
   }

   /** Adds the specified value for the named header (does not remove/replace existing values). */
   @Override
   public void addIntHeader( String name, int value ) {
      List<Object> values = _headers.computeIfAbsent(name, k -> new ArrayList<>());
      values.add(value);
   }

   /** Returns true if the specified header was placed in the response. */
   @Override
   public boolean containsHeader( String name ) { return _headers.containsKey(name); }

   /** Returns the URL unchanged. */
   @Override
   public String encodeRedirectURL( String url ) { return url; }

   /** Returns the URL unchanged. */
   @Override
   public String encodeRedirectUrl( String url ) { return url; }

   /** Returns the URL unchanged. */
   @Override
   public String encodeURL( String url ) { return url; }

   /** Returns the URL unchanged. */
   @Override
   public String encodeUrl( String url ) { return url; }

   /** Has no effect. */
   @Override
   public void flushBuffer() throws IOException { }

   /** Always returns 0. */
   @Override
   public int getBufferSize() { return 0; }

   /** Gets the character encoding (defaults to UTF-8). */
   @Override
   public String getCharacterEncoding() { return _characterEncoding; }

   /** Returns the content length if one was set on the response by calling setContentLength(). */
   public int getContentLength() { return _contentLength; }

   /** Gets the content type for the response. Defaults to text/html. */
   @Override
   public String getContentType() { return _contentType; }

   /** Gets the set of cookies stored in the response. */
   public Cookie[] getCookies() { return _cookies.toArray(new Cookie[0]); }

   /** Gets the error message if one was set with setStatus() or sendError(). */
   public String getErrorMessage() { return _errorMessage; }

   @Override
   public String getHeader( String name ) {
      List<Object> list = _headers.get(name);
      if ( list != null && !list.isEmpty() ) {
         return list.get(0).toString();
      }
      return null;
   }

   /**
    * Provides access to all headers that were set. The format is a Map which uses the header
    * name as the key, and stores a List of Objects, one per header value.  The Objects will
    * be either Strings (if setHeader() was used), Integers (if setIntHeader() was used) or
    * Longs (if setDateHeader() was used).
    */
   public Map<String, List<Object>> getHeaderMap() { return _headers; }

   @Override
   public Collection<String> getHeaderNames() {
      return _headers.keySet();
   }

   @Override
   public Collection<String> getHeaders( String name ) {
      List<Object> headers = _headers.get(name);
      if ( headers == null ) {
         return Collections.emptyList();
      }
      return headers.stream().map(Objects::toString).collect(Collectors.toList());
   }

   /** Gets the response locale. Default to the system default locale. */
   @Override
   public Locale getLocale() { return _locale; }

   /** Gets the output that was written to the output stream, as a byte[]. */
   public byte[] getOutputBytes() {
      _writer.flush();
      return _out.getBytes();
   }

   /**
    * Returns a reference to a ServletOutputStream to be used for output. The output is captured
    * and can be examined at the end of a test run by calling getOutputBytes() or
    * getOutputString().
    */
   @Override
   public ServletOutputStream getOutputStream() throws IOException { return _out; }

   /** Gets the output that was written to the output stream, as a character String. */
   public String getOutputString() {
      _writer.flush();
      return _out.getString();
   }

   /**
    * If a call was made to sendRedirect() this method will return the URL that was supplied.
    * Otherwise it will return null.
    */
   public String getRedirectUrl() { return _redirectUrl; }

   /** Gets the status (or error) code if one was set. Defaults to 200 (HTTP OK). */
   @Override
   public int getStatus() { return _status; }

   /**
    * Returns a reference to a PrintWriter to be used for character output. The output is captured
    * and can be examined at the end of a test run by calling getOutputBytes() or
    * getOutputString().
    */
   @Override
   public PrintWriter getWriter() throws IOException { return _writer; }

   /** Always returns true. */
   @Override
   public boolean isCommitted() { return true; }

   /** Always throws an IllegalStateException. */
   @Override
   public void reset() {
      throw new IllegalStateException("reset() is not supported");
   }

   /** Always throws IllegalStateException. */
   @Override
   public void resetBuffer() {
      throw new IllegalStateException("reset() is not supported");
   }

   /** Sets the status code and saves the message so it can be retrieved later. */
   @Override
   public void sendError( int status, String errorMessage ) throws IOException {
      _status = status;
      _errorMessage = errorMessage;
   }

   /** Sets that status code to the error code provided. */
   @Override
   public void sendError( int status ) throws IOException { _status = status; }

   /**
    * Simply sets the status code and stores the URL that was supplied, so that it can be examined
    * later with getRedirectUrl.
    */
   @Override
   public void sendRedirect( String url ) throws IOException {
      _status = HttpServletResponse.SC_MOVED_TEMPORARILY;
      _redirectUrl = url;
   }

   /** Has no effect. */
   @Override
   public void setBufferSize( int i ) { }

   /** Sets the character encoding on the request. */
   @Override
   public void setCharacterEncoding( String encoding ) { _characterEncoding = encoding; }

   /** Sets a custom content length on the response. */
   @Override
   public void setContentLength( int contentLength ) { _contentLength = contentLength; }

   @Override
   public void setContentLengthLong( long contentLength ) {
      _contentLength = (int)contentLength;
   }

   /** Sets the content type for the response. */
   @Override
   public void setContentType( String contentType ) {
      _contentType = contentType;
      getHeaderMap().put("Content-type", Collections.singletonList(contentType));
   }

   /** Stores the value in a Long and saves it as a header. */
   @Override
   public void setDateHeader( String name, long value ) {
      _headers.remove(name);
      addDateHeader(name, value);
   }

   /** Sets the value of the specified header to the single value provided. */
   @Override
   public void setHeader( String name, String value ) {
      _headers.remove(name);
      addHeader(name, value);
   }

   /** Stores the value in an Integer and saves it as a header. */
   @Override
   public void setIntHeader( String name, int value ) {
      _headers.remove(name);
      addIntHeader(name, value);
   }

   /** Sets the response locale to the one specified. */
   @Override
   public void setLocale( Locale locale ) { _locale = locale; }

   /** Sets the HTTP Status code of the response. */
   @Override
   public void setStatus( int statusCode ) { _status = statusCode; }

   /** Saves the HTTP status code and the message provided. */
   @Override
   public void setStatus( int status, String errorMessage ) {
      _status = status;
      _errorMessage = errorMessage;
   }
}
