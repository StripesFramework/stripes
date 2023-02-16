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
package net.sourceforge.stripes.exception;

import jakarta.servlet.jsp.JspException;

/**
 * Stripes' version of the JspException that is used where only JspExceptions
 * can be thrown.
 *
 * @author Tim Fennell
 */
public class StripesJspException extends JspException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs exception with the passed message.
     * 
     * @param string Message of exception
     */
    public StripesJspException(String string) {
        super(string);
    }

    /**
     * Constructs exception with passed message and cause
     * @param string Error message
     * @param throwable Cause of exception
     */
    public StripesJspException(String string, Throwable throwable) {
        super(string, throwable);
    }

    /**
     * Constructs exception with the passed cause
     * @param throwable Cause of the exception
     */
    public StripesJspException(Throwable throwable) {
        super(throwable);
    }
}
