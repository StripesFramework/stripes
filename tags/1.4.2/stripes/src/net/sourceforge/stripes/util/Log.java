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
                builder.append( Arrays.toString((Object[]) CollectionUtil.asObjectArray(part) ));
            }
            else {
                builder.append(part);
            }
        }

        return builder.toString();
    }
}
