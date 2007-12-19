/* Copyright 2007 Ben Gunter
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
package net.sourceforge.stripes.tag;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.format.EncryptedValueFormatter;
import net.sourceforge.stripes.format.Formatter;

/**
 * A simple class that wraps an object that is intended to be encrypted before it is written into a
 * JSP page. This class, coupled with {@link EncryptedValueFormatter}, allows for
 * {@link ActionBean} properties marked as encrypted to be encrypted transparently to the normal
 * {@link Formatter} for the value.
 * 
 * @author Ben Gunter
 */
public class EncryptedValue {
    private Object value;

    /**
     * Create a new instance that wraps the given {@code value}.
     */
    public EncryptedValue(Object value) {
        this.value = value;
    }

    /** Get the actual value that is to be encrypted. */
    public Object getValue() {
        return value;
    }
}
