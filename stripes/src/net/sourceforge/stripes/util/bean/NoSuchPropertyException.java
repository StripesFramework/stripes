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

/**
 * Exception indicating that an expression could not be evaluated against a bean because
 * the bean (or some sub-property of the bean) did not have a property matching the name
 * supplied in the expression.
 *
 * @author Tim Fennell
 * @since Stripes 1.4
 */
@SuppressWarnings("serial")
public class NoSuchPropertyException extends EvaluationException {
    /** Constructs an exception with the supplied message. */
    public NoSuchPropertyException(String message) {
        super(message);
    }

    /** Constructs an exception with the supplied message and causing exception. */
    public NoSuchPropertyException(String message, Throwable cause) {
        super(message, cause);
    }
}
