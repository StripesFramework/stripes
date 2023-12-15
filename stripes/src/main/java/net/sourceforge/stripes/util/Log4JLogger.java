/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sourceforge.stripes.util;

import java.io.Serializable;
import org.apache.commons.logging.Log;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Implementation of {@link Log} that maps directly to a
 * <strong>Logger</strong> for log4J version 2.x
 * <p>
 * Initial configuration of the corresponding Logger instances should be done in
 * the usual manner, as outlined in the Log4J documentation.
 * <p>
 *
 * @author <a href="mailto:sanders@apache.org">Scott Sanders</a>
 * @author Rod Waldhoff
 * @author Robert Burrell Donkin
 * @version $Id: Log4JLogger.java 370672 2006-01-19 23:52:23Z skitching $
 */
@SuppressWarnings({"serial", "deprecation"})
public class Log4JLogger implements Log, Serializable {

    /**
     * Log to this logger
     */
    private transient Logger logger = null;

    /**
     * Logger name
     */
    private String name = null;


    // ------------------------------------------------------------ Constructor

    /**
     *
     */
    public Log4JLogger() {
    }

    /**
     * Base constructor.
     * @param name
     */
    public Log4JLogger(String name) {
        this.name = name;
        this.logger = getLogger();
    }

    /**
     * For use with a log4j factory.
     * @param logger
     */
    public Log4JLogger(Logger logger) {
        this.name = logger.getName();
        this.logger = logger;
    }

    /**
     * Logs a message with <code>org.apache.logging.log4j.Level.TRACE</code>.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#trace(Object)
     */
    public void trace(Object message) {
        getLogger().trace(message);
    }

    /**
     * Logs a message with <code>org.apache.log4j.Priority.TRACE</code>.
     *
     * @param message to log
     * @param t log this cause
     * @see org.apache.commons.logging.Log#trace(Object, Throwable)
     */
    public void trace(Object message, Throwable t) {
        getLogger().trace(message, t);
    }

    /**
     * Logs a message with <code>org.apache.logging.log4j.Level.DEBUG</code>.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#debug(Object)
     */
    public void debug(Object message) {
        getLogger().debug(message);
    }

    /**
     * Logs a message with <code>org.apache.logging.log4j.Level.DEBUG</code>.
     *
     * @param message to log
     * @param t log this cause
     * @see org.apache.commons.logging.Log#debug(Object, Throwable)
     */
    public void debug(Object message, Throwable t) {
        getLogger().debug(message,t);
    }

    /**
     * Logs a message with <code>org.apache.logging.log4j.Level.INFO</code>.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#info(Object)
     */
    public void info(Object message) {
        getLogger().info(message);
    }

    /**
     * Logs a message with <code>org.apache.logging.log4j.Level.INFO</code>.
     *
     * @param message to log
     * @param t log this cause
     * @see org.apache.commons.logging.Log#info(Object, Throwable)
     */
    public void info(Object message, Throwable t) {
        getLogger().info(message, t);
    }

    /**
     * Logs a message with <code>org.apache.logging.log4j.Level.WARN</code>.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#warn(Object)
     */
    public void warn(Object message) {
        getLogger().warn(message);
    }

    /**
     * Logs a message with <code>org.apache.logging.log4j.Level.WARN</code>.
     *
     * @param message to log
     * @param t log this cause
     * @see org.apache.commons.logging.Log#warn(Object, Throwable)
     */
    public void warn(Object message, Throwable t) {
        getLogger().warn(message, t);
    }

    /**
     * Logs a message with <code>org.apache.logging.log4j.Level.ERROR</code>.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#error(Object)
     */
    public void error(Object message) {
        getLogger().error(message);
    }

    /**
     * Logs a message with <code>org.apache.logging.log4j.Level.ERROR</code>.
     *
     * @param message to log
     * @param t log this cause
     * @see org.apache.commons.logging.Log#error(Object, Throwable)
     */
    public void error(Object message, Throwable t) {
        getLogger().error(message, t);
    }

    /**
     * Logs a message with <code>org.apache.logging.log4j.Level.FATAL</code>.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#fatal(Object)
     */
    public void fatal(Object message) {
        getLogger().fatal(message);
    }

    /**
     * Logs a message with <code>org.apache.logging.log4j.Level.FATAL</code>.
     *
     * @param message to log
     * @param t log this cause
     * @see org.apache.commons.logging.Log#fatal(Object, Throwable)
     */
    public void fatal(Object message, Throwable t) {
        getLogger().fatal(message,t);
    }

    /**
     * Return the native Logger instance we are using.
     * @return 
     */
    public Logger getLogger() {
        if (logger == null) {
            logger = LogManager.getLogger(name);
        }
        return (this.logger);
    }

    /**
     * Check whether the Log4j Logger used is enabled for <code>DEBUG</code>
     * priority.
     * @return 
     */
    public boolean isDebugEnabled() {
        return getLogger().isDebugEnabled();
    }

    /**
     * Check whether the Log4j Logger used is enabled for <code>ERROR</code>
     * priority.
     * @return 
     */
    public boolean isErrorEnabled() {
        return getLogger().isErrorEnabled();
    }

    /**
     * Check whether the Log4j Logger used is enabled for <code>FATAL</code>
     * priority.
     * @return 
     */
    public boolean isFatalEnabled() {
        return getLogger().isFatalEnabled();
    }

    /**
     * Check whether the Log4j Logger used is enabled for <code>INFO</code>
     * priority.
     * @return 
     */
    public boolean isInfoEnabled() {
        return getLogger().isInfoEnabled();
    }

    /**
     * Check whether the Log4j Logger used is enabled for <code>TRACE</code>
     * priority. When using a log4j version that does not support the TRACE
     * level, this call will report whether <code>DEBUG</code> is enabled or
     * not.
     * @return 
     */
    public boolean isTraceEnabled() {
        return getLogger().isTraceEnabled();
    }

    /**
     * Check whether the Log4j Logger used is enabled for <code>WARN</code>
     * priority.
     * @return 
     */
    public boolean isWarnEnabled() {
        return getLogger().isWarnEnabled();
    }
}
