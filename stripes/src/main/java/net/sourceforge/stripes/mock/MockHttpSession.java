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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * Simple mock implementation of HttpSession that implements most basic
 * operations.
 *
 * @author Tim Fennell
 * @since Stripes 1.1.1
 */
@SuppressWarnings("deprecation")
public class MockHttpSession implements HttpSession {

  /**
   * The log.
   */
  private static Logger log = LogManager.getLogger(MockHttpSession.class);

  /**
   * The creation time.
   */
    private long creationTime = System.currentTimeMillis();

  /**
   * The session id.
   */
    private String sessionId = String.valueOf(new Random().nextLong());

  /**
   * The context.
   */
    private ServletContext context;

  /**
   * The attributes.
   */
    private Map<String, Object> attributes = new HashMap<String, Object>();

    /**
     * Default constructor which provides the session with access to the
     * context.
     * @param context
     */
    public MockHttpSession(ServletContext context) {
        this.context = context;
    }

    /**
     * Returns the time in milliseconds when the session was created.
     * @return
     */
    @Override
    public long getCreationTime() {
        return this.creationTime;
    }

    /**
     * Returns an ID that was randomly generated when the session was created.
     * @return
     */
    @Override
    public String getId() {
        return this.sessionId;
    }

    /**
     * Always returns the current time.
     * @return
     */
    @Override
    public long getLastAccessedTime() {
        return System.currentTimeMillis();
    }

    /**
     * Provides access to the servlet context within which the session exists.
     * @return
     */
    @Override
    public ServletContext getServletContext() {
        return this.context;
    }

    /**
     * Sets the servlet context within which the session exists.
     * @param context
     */
    public void setServletContext(ServletContext context) {
        this.context = context;
    }

    /**
     * Has no effect.
     * @param i
     */
    @Override
    public void setMaxInactiveInterval(int i) {
    }

    /**
     * Always returns Integer.MAX_VALUE.
     * @return
     */
    @Override
    public int getMaxInactiveInterval() {
        return Integer.MAX_VALUE;
    }

    /**
     * Deprecated method always returns null.
     * @return
     */
    @Override
    public javax.servlet.http.HttpSessionContext getSessionContext() {
        return null;
    }

    /**
     * Returns the value of the named attribute from an internal Map.
     * @param key
     * @return
     */
    @Override
    public Object getAttribute(String key) {
        Object value = this.attributes.get(key);
        log.debug("getAttribute(" + System.identityHashCode(this) + ", " + key + ")=" + Objects.toString(value, ""));
        return value;
    }

    /**
     * Deprecated method. Use getAttribute() instead.
     * @param key
     * @return
     */
    @Override
    public Object getValue(String key) {
        return getAttribute(key);
    }

    /**
     * Returns an enumeration of all the attribute names in the session.
     * @return
     */
    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(this.attributes.keySet());
    }

    /**
     * Returns a String[] of all the attribute names in session. Deprecated.
     * @return
     */
    @Override
    public String[] getValueNames() {
        return this.attributes.keySet().toArray(new String[this.attributes.size()]);
    }

    /**
     * Stores the value in session, replacing any existing value with the same
     * key.
     * @param key
     * @param value
     */
    @Override
    public void setAttribute(String key, Object value) {
        log.debug("setAttribute(" + System.identityHashCode(this) + ", " + key + "=" + Objects.toString(value, ""));
        this.attributes.put(key, value);
    }

    /**
     * Stores the value in session, replacing any existing value with the same
     * key.
     * @param key
     * @param value
     */
    @Override
    public void putValue(String key, Object value) {
        setAttribute(key, value);
    }

    /**
     * Removes any value stored in session with the key supplied.
     * @param key
     */
    @Override
    public void removeAttribute(String key) {
        this.attributes.remove(key);
    }

    /**
     * Removes any value stored in session with the key supplied.
     * @param key
     */
    @Override
    public void removeValue(String key) {
        removeAttribute(key);
    }

    /**
     * Clears the set of attributes, but has no other effect.
     */
    @Override
    public void invalidate() {
        this.attributes.clear();
    }

    /**
     * Always returns false.
     * @return
     */
    @Override
    public boolean isNew() {
        return false;
    }
}
