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
package net.sourceforge.stripes.util;

import org.apache.commons.logging.LogFactory;

import java.util.Arrays;

/**
 * <p>A <em>wafer thin</em> wrapper around Commons logging that uses var-args to make it
 * much more efficient to call the logging methods in commons logging without having to
 * surround every call site with calls to Log.isXXXEnabled().  All the methods on this
 * class take a variable length list of arguments and, only if logging is enabled for
 * the level and channel being logged to, will those arguments be toString()'d and
 * appended together.</p>
 *
 * @author Tim Fennell
 */
public final class Log {
    private org.apache.commons.logging.Log realLog;

    /**
     * Get a Log instance to perform logging within the Class specified.  Returns an instance
     * of this class which wraps an instance of the commons logging Log class.
     * @param clazz the Class which is going to be doing the logging
     * @return a Log instance with which to log
     */
    public static Log getInstance(Class clazz) {
        return new Log( LogFactory.getLog(clazz) );
    }

    /**
     * Private constructor which creates a new Log instance wrapping the commons Log instance
     * provided.  Only used by the static getInstance() method on this class.
     */
    private Log(org.apache.commons.logging.Log realLog) {
        this.realLog = realLog;
    }

    /**
     * Logs a Throwable and optional message parts at level fatal.
     * @param throwable an instance of Throwable that should be logged with stack trace
     * @param messageParts zero or more objects which should be combined, by calling toString()
     *        to form the log message.
     */
    public final void fatal(Throwable throwable, Object... messageParts) {
        if (this.realLog.isFatalEnabled()) {
            this.realLog.fatal(combineParts(messageParts), throwable);
        }
    }

    /**
     * Logs a Throwable and optional message parts at level error.
     * @param throwable an instance of Throwable that should be logged with stack trace
     * @param messageParts zero or more objects which should be combined, by calling toString()
     *        to form the log message.
     */
    public final void error(Throwable throwable, Object... messageParts) {
        if (this.realLog.isErrorEnabled()) {
            this.realLog.error(combineParts(messageParts), throwable);
        }
    }

    /**
     * Logs a Throwable and optional message parts at level warn.
     * @param throwable an instance of Throwable that should be logged with stack trace
     * @param messageParts zero or more objects which should be combined, by calling toString()
     *        to form the log message.
     */
    public final void warn(Throwable throwable, Object... messageParts) {
        if (this.realLog.isWarnEnabled()) {
            this.realLog.warn(combineParts(messageParts), throwable);
        }
    }

    /**
     * Logs a Throwable and optional message parts at level info.
     * @param throwable an instance of Throwable that should be logged with stack trace
     * @param messageParts zero or more objects which should be combined, by calling toString()
     *        to form the log message.
     */
    public final void info(Throwable throwable, Object... messageParts) {
        if (this.realLog.isInfoEnabled()) {
            this.realLog.info(combineParts(messageParts), throwable);
        }
    }

    /**
     * Logs a Throwable and optional message parts at level debug.
     * @param throwable an instance of Throwable that should be logged with stack trace
     * @param messageParts zero or more objects which should be combined, by calling toString()
     *        to form the log message.
     */
    public final void debug(Throwable throwable, Object... messageParts) {
        if (this.realLog.isDebugEnabled()) {
            this.realLog.debug(combineParts(messageParts), throwable);
        }
    }

    /**
     * Logs a Throwable and optional message parts at level trace.
     * @param throwable an instance of Throwable that should be logged with stack trace
     * @param messageParts zero or more objects which should be combined, by calling toString()
     *        to form the log message.
     */
    public final void trace(Throwable throwable, Object... messageParts) {
        if (this.realLog.isTraceEnabled()) {
            this.realLog.trace(combineParts(messageParts), throwable);
        }
    }

    // Similar methods, but without Throwables, follow

    /**
     * Logs one or more message parts at level fatal.
     * @param messageParts one or more objects which should be combined, by calling toString()
     *        to form the log message.
     */
    public final void fatal(Object... messageParts) {
        if (this.realLog.isFatalEnabled()) {
            this.realLog.fatal(combineParts(messageParts));
        }
    }

    /**
     * Logs one or more message parts at level error.
     * @param messageParts one or more objects which should be combined, by calling toString()
     *        to form the log message.
     */
    public final void error(Object... messageParts) {
        if (this.realLog.isErrorEnabled()) {
            this.realLog.error(combineParts(messageParts));
        }
    }

    /**
     * Logs one or more message parts at level warn.
     * @param messageParts one or more objects which should be combined, by calling toString()
     *        to form the log message.
     */
    public final void warn(Object... messageParts) {
        if (this.realLog.isWarnEnabled()) {
            this.realLog.warn(combineParts(messageParts));
        }
    }

    /**
     * Logs one or more message parts at level info.
     * @param messageParts one or more objects which should be combined, by calling toString()
     *        to form the log message.
     */
    public final void info(Object... messageParts) {
        if (this.realLog.isInfoEnabled()) {
            this.realLog.info(combineParts(messageParts));
        }
    }

    /**
     * Logs one or more message parts at level debug.
     * @param messageParts one or more objects which should be combined, by calling toString()
     *        to form the log message.
     */
    public final void debug(Object... messageParts) {
        if (this.realLog.isDebugEnabled()) {
            this.realLog.debug(combineParts(messageParts));
        }
    }

    /**
     * Logs one or more message parts at level trace.
     * @param messageParts one or more objects which should be combined, by calling toString()
     *        to form the log message.
     */
    public final void trace(Object... messageParts) {
        if (this.realLog.isTraceEnabled()) {
            this.realLog.trace(combineParts(messageParts));
        }
    }


    /**
     * Combines all the message parts handed in to the logger in order to pass them in to
     * the commons logging interface.
     */
    private String combineParts(Object[] messageParts) {
        StringBuilder builder = new StringBuilder(128);
        for (Object part : messageParts) {
            if (part instanceof Object[]) {
                builder.append( Arrays.toString((Object[])part) );
            }
            else {
                builder.append(part);
            }
        }

        return builder.toString();
    }
}
