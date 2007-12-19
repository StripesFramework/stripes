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
package net.sourceforge.stripes.format;

import java.util.Locale;

import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.tag.EncryptedValue;
import net.sourceforge.stripes.util.CryptoUtil;

/**
 * Finds the appropriate formatter for the value of an {@link EncryptedValue}, formats it, and
 * encrypts the result.
 * 
 * @author Ben Gunter
 */
public class EncryptedValueFormatter implements Formatter<EncryptedValue> {
    private String formatPattern, formatType;
    private Locale locale;

    public void init() {
    }

    public void setFormatPattern(String formatPattern) {
        this.formatPattern = formatPattern;
    }

    public void setFormatType(String formatType) {
        this.formatType = formatType;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @SuppressWarnings("unchecked")
    public String format(EncryptedValue input) {
        // null check
        Object object;
        if (input == null || (object = input.getValue()) == null) {
            return "";
        }

        // look up the type converter
        FormatterFactory factory = StripesFilter.getConfiguration().getFormatterFactory();
        Formatter formatter = factory.getFormatter(object.getClass(), locale, formatType, formatPattern);

        // format the value to plain text
        String value;
        if (formatter != null)
            value = formatter.format(object);
        else
            value = object.toString();

        // encrypt the value
        if (value == null)
            value = "";
        else
            value = CryptoUtil.encrypt(value);

        return value;
    }
}
