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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Simple mock implementation of HttpSession that implements most basic operations.
 *
 * @author Tim Fennell
 * @since Stripes 1.1.1
 */
public class MockHttpSession implements HttpSession {
    private long creationTime = System.currentTimeMillis();
    private String sessionId = String.valueOf(new Random().nextLong());
    private ServletContext context;
    private Map<String,Object> attributes = new HashMap<String,Object>();

    /** Default constructor which provides the session with access to the context. */
    public MockHttpSession(ServletContext context) {
        this.context = context;
    }

    /** Returns the time in milliseconds when the session was created. */
    public long getCreationTime() { return this.creationTime; }

    /** Returns an ID that was randomly generated when the session was created. */
    public String getId() { return this.sessionId; }

    /** Always returns the current time. */
    public long getLastAccessedTime() { return System.currentTimeMillis(); }

    /** Provides access to the servlet context within which the session exists. */
    public ServletContext getServletContext() { return this.context; }

    /** Sets the servlet context within which the session exists. */
    public void setServletContext(ServletContext context) { this.context = context; }

    /** Has no effect. */
    public void setMaxInactiveInterval(int i) { }

    /** Always returns Integer.MAX_VALUE. */
    public int getMaxInactiveInterval() { return Integer.MAX_VALUE; }

    /** Deprecated method always returns null. */
    public HttpSessionContext getSessionContext() { return null; }

    /** Returns the value of the named attribute from an internal Map. */
    public Object getAttribute(String key) { return this.attributes.get(key); }

    /** Deprecated method. Use getAttribute() instead. */
    public Object getValue(String key) { return getAttribute(key); }

    /** Returns an enumeration of all the attribute names in the session. */
    public Enumeration getAttributeNames() {
        return Collections.enumeration(this.attributes.keySet());
    }

    /** Returns a String[] of all the attribute names in session. Deprecated. */
    public String[] getValueNames() {
        return this.attributes.keySet().toArray(new String[this.attributes.size()]);
    }

    /** Stores the value in session, replacing any existing value with the same key. */
    public void setAttribute(String key, Object value) {
        this.attributes.put(key, value);
    }

    /** Stores the value in session, replacing any existing value with the same key. */
    public void putValue(String key, Object value) {
        setAttribute(key, value);
    }

    /** Removes any value stored in session with the key supplied. */
    public void removeAttribute(String key) {
        this.attributes.remove(key);
    }

    /** Removes any value stored in session with the key supplied. */
    public void removeValue(String key) {
        removeAttribute(key);
    }

    /** Clears the set of attributes, but has no other effect. */
    public void invalidate() { this.attributes.clear(); }

    /** Always returns false. */
    public boolean isNew() { return false; }
}
