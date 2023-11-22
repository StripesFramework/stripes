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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A <em>wafer thin</em> wrapper around Commons logging that uses var-args to make it much more
 * efficient to call the logging methods in commons logging without having to surround every call
 * site with calls to Log.isXXXEnabled(). All the methods on this class take a variable length list
 * of arguments and, only if logging is enabled for the level and channel being logged to, will
 * those arguments be toString()'d and appended together.
 *
 * @author Tim Fennell
 */
public final class Log {
  private final java.util.logging.Logger realLog;

  public java.util.logging.Logger getRealLog() {
    return realLog;
  }

  /**
   * Get a Log instance to perform logging within the Class specified. Returns an instance of this
   * class which wraps an instance of the commons logging Log class.
   *
   * @param clazz the Class which is going to be doing the logging
   * @return a Log instance with which to log
   */
  public static Log getInstance(Class<?> clazz) {
    return new Log(Logger.getLogger(clazz.getName()));
  }

  /**
   * Forces Log to clean up any cached resources. This is called by the StripesFilter when it is
   * destroyed, but can be called from user code as well if necessary.
   */
  public static void cleanup() {}

  /**
   * Private constructor which creates a new Log instance wrapping the commons Log instance
   * provided. Only used by the static getInstance() method on this class.
   */
  private Log(java.util.logging.Logger realLog) {
    this.realLog = realLog;
  }

  /**
   * Logs a Throwable and optional message parts at level fatal.
   *
   * @param throwable an instance of Throwable that should be logged with stack trace
   * @param messageParts zero or more objects which should be combined, by calling toString() to
   *     form the log message.
   */
  public void fatal(Throwable throwable, Object... messageParts) {
    if (this.realLog.isLoggable(Level.SEVERE)) {
      this.realLog.log(Level.SEVERE, StringUtil.combineParts(messageParts), throwable);
    }
  }

  /**
   * Logs a Throwable and optional message parts at level error.
   *
   * @param throwable an instance of Throwable that should be logged with stack trace
   * @param messageParts zero or more objects which should be combined, by calling toString() to
   *     form the log message.
   */
  public void error(Throwable throwable, Object... messageParts) {
    fatal(throwable, messageParts);
  }

  /**
   * Logs a Throwable and optional message parts at level warn.
   *
   * @param throwable an instance of Throwable that should be logged with stack trace
   * @param messageParts zero or more objects which should be combined, by calling toString() to
   *     form the log message.
   */
  public void warn(Throwable throwable, Object... messageParts) {
    if (this.realLog.isLoggable(Level.WARNING)) {
      this.realLog.log(Level.WARNING, StringUtil.combineParts(messageParts), throwable);
    }
  }

  /**
   * Logs a Throwable and optional message parts at level info.
   *
   * @param throwable an instance of Throwable that should be logged with stack trace
   * @param messageParts zero or more objects which should be combined, by calling toString() to
   *     form the log message.
   */
  public void info(Throwable throwable, Object... messageParts) {
    if (this.realLog.isLoggable(Level.INFO)) {
      this.realLog.log(Level.INFO, StringUtil.combineParts(messageParts), throwable);
    }
  }

  /**
   * Logs a Throwable and optional message parts at level debug.
   *
   * @param throwable an instance of Throwable that should be logged with stack trace
   * @param messageParts zero or more objects which should be combined, by calling toString() to
   *     form the log message.
   */
  public void debug(Throwable throwable, Object... messageParts) {
    if (this.realLog.isLoggable(Level.FINE)) {
      this.realLog.log(Level.FINE, StringUtil.combineParts(messageParts), throwable);
    }
  }

  /**
   * Logs a Throwable and optional message parts at level trace.
   *
   * @param throwable an instance of Throwable that should be logged with stack trace
   * @param messageParts zero or more objects which should be combined, by calling toString() to
   *     form the log message.
   */
  public void trace(Throwable throwable, Object... messageParts) {
    if (this.realLog.isLoggable(Level.FINER)) {
      this.realLog.log(Level.FINER, StringUtil.combineParts(messageParts), throwable);
    }
  }

  // Similar methods, but without any throwable parameters follow

  /**
   * Logs one or more message parts at level fatal.
   *
   * @param messageParts one or more objects which should be combined, by calling toString() to form
   *     the log message.
   */
  public void fatal(Object... messageParts) {
    fatal(null, messageParts);
  }

  /**
   * Logs one or more message parts at level error.
   *
   * @param messageParts one or more objects which should be combined, by calling toString() to form
   *     the log message.
   */
  public void error(Object... messageParts) {
    error(null, messageParts);
  }

  /**
   * Logs one or more message parts at level warn.
   *
   * @param messageParts one or more objects which should be combined, by calling toString() to form
   *     the log message.
   */
  public void warn(Object... messageParts) {
    warn(null, messageParts);
  }

  /**
   * Logs one or more message parts at level info.
   *
   * @param messageParts one or more objects which should be combined, by calling toString() to form
   *     the log message.
   */
  public void info(Object... messageParts) {
    info(null, messageParts);
  }

  /**
   * Logs one or more message parts at level debug.
   *
   * @param messageParts one or more objects which should be combined, by calling toString() to form
   *     the log message.
   */
  public void debug(Object... messageParts) {
    debug(null, messageParts);
  }

  /**
   * Logs one or more message parts at level trace.
   *
   * @param messageParts one or more objects which should be combined, by calling toString() to form
   *     the log message.
   */
  public void trace(Object... messageParts) {
    trace(null, messageParts);
  }
}
