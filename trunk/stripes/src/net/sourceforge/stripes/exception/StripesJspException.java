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

import javax.servlet.jsp.JspException;

/**
 * Stripes' version of the JspException that is used where only JspExceptions
 * can be thrown.
 *
 * @author Tim Fennell
 */
@SuppressWarnings("serial")
public class StripesJspException extends JspException {
    public StripesJspException(String string) {
        super(string);
    }

    public StripesJspException(String string, Throwable throwable) {
        super(string, throwable);
    }

    public StripesJspException(Throwable throwable) {
        super(throwable);
    }
}
