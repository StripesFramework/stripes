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
package net.sourceforge.stripes.util.bean;

import net.sourceforge.stripes.exception.StripesRuntimeException;

/**
 * Root exception type which all exceptions in this package extend. Used for generalized
 * exceptions that occur during expression parsing and evaluation. Users can catch this
 * exception instead of subclasses to be sure of catching all exceptions coming from the
 * property expression classes.
 *
 * @author Tim Fennell
 * @since Stripes 1.4
 */
@SuppressWarnings("serial")
public class ExpressionException extends StripesRuntimeException {
    /** Constructs an exception with the supplied message. */
    public ExpressionException(String message) {
        super(message);
    }

    /** Constructs an exception with the supplied message and causing exception. */
    public ExpressionException(String message, Throwable cause) {
        super(message, cause);
    }
}
