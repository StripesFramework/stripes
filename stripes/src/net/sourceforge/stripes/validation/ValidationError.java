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
package net.sourceforge.stripes.validation;

import net.sourceforge.stripes.action.Message;

import java.util.Locale;

/**
 * Interface to which all error objects in Stripes should conform.
 *
 * @author Tim Fennell
 */
public interface ValidationError extends Message {
    /**
     * Provides the message with access to the name of the field in which the error occurred. This
     * is the name the system uses for the field (e.g. cat.name) and not necessarily something that
     * the user should see!
     */
    void setFieldName(String name);

    /**
     * Provides the message with access to the value of the field in which the error occurred
     */
    void setFieldValue(String value);

    /**
     * Provides the message with access to the unique action path associated with the
     * ActionBean bound to the current request.
     */
    void setActionPath(String actionPath);

    /** Returns the name of the field in error, if one was supplied. */
    String getFieldName();

    /** Returns the value that is in error, if one was supplied. */
    String getFieldValue();

    /** Returns the action path of the form/ActionBean, if one was supplied. */
    String getActionPath();
}

