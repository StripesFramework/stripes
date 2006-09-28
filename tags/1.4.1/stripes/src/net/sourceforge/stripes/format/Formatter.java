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
package net.sourceforge.stripes.format;

import java.util.Locale;

/**
 * <p>Interface that is used to provide a relatively simple formatting interface to the rest of the
 * system. Designed to wrap the complexity of the java.text format classes in an interface that
 * can take two (or three if you include Locale) parameters from tags and apply formats in an
 * intelligent way.</p>
 *
 * <p>In terms of lifecycle, a formatter will be instantiated, have setFormatType(),
 * setFormatString() and setLocale() called in rapid succession.  If no values were supplied,
 * setFormatType() and setFormatString() may not be called - and implementations should select
 * reasonable defaults.  Locale will always be provided.  After the setters have been called,
 * init() will be called, and the Formatter should use this opportunity to construct any internal
 * objects necessary to perform formatting.  The format() method will then be called one or more
 * times before the Formatter is eventually deferenced.</p>
 *
 * @author Tim Fennell
 */
public interface Formatter<T> {

    /** Sets the type of format that should be created. */
    void setFormatType(String formatType);

    /** Sets a named format, or format pattern to use in formatting objects. */
    void setFormatPattern(String formatPattern);

    /** Sets the Locale into which the object should be formatted. */
    void setLocale(Locale locale);

    /** Called once all setters have been invoked, to allow Formatter to prepare itself. */
    void init();

    /**
     * Formats the supplied value as a String.  If the value cannot be formatted because it is
     * an inappropriate type, or because faulty pattern information was supplied, should fail
     * loudly by throwing a RuntimeException or subclass thereof.

     * @param input an object of a type that the formatter knows how to format
     * @return a String version of the input, formatted for the chosen locale
     */
    String format(T input);
}
