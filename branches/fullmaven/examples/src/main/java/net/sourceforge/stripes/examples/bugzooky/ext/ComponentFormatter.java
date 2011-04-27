/* Copyright 2009 Ben Gunter
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
package net.sourceforge.stripes.examples.bugzooky.ext;

import java.util.Locale;

import net.sourceforge.stripes.examples.bugzooky.biz.Component;
import net.sourceforge.stripes.format.Formatter;

/**
 * A simple {@link Formatter} that formats a {@link Component} object to text by returning its
 * integer ID in string form. For a more advanced formatter implementation, see
 * {@link PersonFormatter}.
 * 
 * @author Ben Gunter
 */
public class ComponentFormatter implements Formatter<Component> {
    /** Format the {@link Component} object. */
    public String format(Component component) {
        if (component == null)
            return "";
        else if (component.getId() == null)
            return "";
        else
            return String.valueOf(component.getId());
    }

    /** This method is specified by the {@link Formatter} interface, but it is not used here. */
    public void init() {
    }

    /** This method is specified by the {@link Formatter} interface, but it is not used here. */
    public void setFormatPattern(String formatPattern) {
    }

    /** This method is specified by the {@link Formatter} interface, but it is not used here. */
    public void setFormatType(String formatType) {
    }

    /** This method is specified by the {@link Formatter} interface, but it is not used here. */
    public void setLocale(Locale locale) {
    }
}