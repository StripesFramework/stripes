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

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;


/**
 * Simple mock implementation of HttpSession that implements most basic operations.
 *
 * @author Tim Fennell
 * @since Stripes 1.1.1
 */
@SuppressWarnings("deprecation")
public class MockHttpSession implements HttpSession {

   private final long                _creationTime = System.currentTimeMillis();
   private final String              _sessionId    = String.valueOf(new Random().nextLong());
   private       ServletContext      _context;
   private final Map<String, Object> _attributes   = new HashMap<>();

   /** Default constructor which provides the session with access to the context. */
   public MockHttpSession( ServletContext context ) {
      _context = context;
   }

   /** Returns the value of the named attribute from an internal Map. */
   @Override
   public Object getAttribute( String key ) { return _attributes.get(key); }

   /** Returns an enumeration of all the attribute names in the session. */
   @Override
   public Enumeration<String> getAttributeNames() {
      return Collections.enumeration(_attributes.keySet());
   }

   /** Returns the time in milliseconds when the session was created. */
   @Override
   public long getCreationTime() { return _creationTime; }

   /** Returns an ID that was randomly generated when the session was created. */
   @Override
   public String getId() { return _sessionId; }

   /** Always returns the current time. */
   @Override
   public long getLastAccessedTime() { return System.currentTimeMillis(); }

   /** Always returns Integer.MAX_VALUE. */
   @Override
   public int getMaxInactiveInterval() { return Integer.MAX_VALUE; }

   /** Provides access to the servlet context within which the session exists. */
   @Override
   public ServletContext getServletContext() { return _context; }

   /** Deprecated method always returns null. */
   @Override
   public javax.servlet.http.HttpSessionContext getSessionContext() { return null; }

   /** Deprecated method. Use getAttribute() instead. */
   @Override
   public Object getValue( String key ) { return getAttribute(key); }

   /** Returns a String[] of all the attribute names in session. Deprecated. */
   @Override
   public String[] getValueNames() {
      return _attributes.keySet().toArray(new String[_attributes.size()]);
   }

   /** Clears the set of attributes, but has no other effect. */
   @Override
   public void invalidate() { _attributes.clear(); }

   /** Always returns false. */
   @Override
   public boolean isNew() { return false; }

   /** Stores the value in session, replacing any existing value with the same key. */
   @Override
   public void putValue( String key, Object value ) {
      setAttribute(key, value);
   }

   /** Removes any value stored in session with the key supplied. */
   @Override
   public void removeAttribute( String key ) {
      _attributes.remove(key);
   }

   /** Removes any value stored in session with the key supplied. */
   @Override
   public void removeValue( String key ) {
      removeAttribute(key);
   }

   /** Stores the value in session, replacing any existing value with the same key. */
   @Override
   public void setAttribute( String key, Object value ) {
      _attributes.put(key, value);
   }

   /** Has no effect. */
   @Override
   public void setMaxInactiveInterval( int i ) { }

   /** Sets the servlet context within which the session exists. */
   public void setServletContext( ServletContext context ) { _context = context; }
}
