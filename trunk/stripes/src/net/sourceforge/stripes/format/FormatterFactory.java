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

import net.sourceforge.stripes.config.ConfigurableComponent;

import java.util.Locale;

/**
 * Interface for creating instances of formatter classes that are capable of formatting
 * the types specified into Strings.
 *
 * @see Formatter
 * @author Tim Fennell
 */
public interface FormatterFactory extends ConfigurableComponent {

    /**
     * Returns a configured formatter that meets the criteria specified.  The formatter is ready
     * for use as soon as it is returned from this method.
     *
     * @param clazz the type of object being formatted
     * @param locale the Locale into which the object should be formatted
     * @param formatType the manner in which the object should be formatted (allows nulls)
     * @param formatPattern the named format, or format pattern to be applied (allows nulls)
     * @return Formatter an instance of a Formatter, or null if no Formatter is available for
     *         the type specified
     */
    Formatter<?> getFormatter(Class<?> clazz, Locale locale, String formatType, String formatPattern);
}
