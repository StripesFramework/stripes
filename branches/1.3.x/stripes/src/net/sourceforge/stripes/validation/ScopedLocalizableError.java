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

import net.sourceforge.stripes.localization.LocalizationUtility;

import java.util.Locale;
import java.util.MissingResourceException;

/**
 * <p>Provides a slightly more customizable approach to error messages.  Where the LocalizedError
 * class looks for an error message in a single place based on the key provided,
 * ScopedLocalizableError performs a scoped search for an error message.</p>
 *
 * <p>As an example, let's say that the IntegerConverter raises an error messsage with the values
 * defaultScope=<em>converter.integer</em> and key=<em>outOfRange</em>, for a field called
 * <em>age</em> on an ActionBean bound to <em>/cats/KittenDetail.action</em>.  Based on this information
 * an instance of ScopedLocalizableError would fetch the resource bundle and look for error message
 * templates in the following order:</p>
 *
 * <ul>
 *   <li>/cats/KittenDetail.action.age.outOfRange</li>
 *   <li>/cats/KittenDetail.action.age.errorMessage</li>
 *   <li>age.errorMessage</li>
 *   <li>/cats/KittenDetail.action.outOfRange</li>
 *   <li>converter.integer.outOfRange</li>
 * </ul>
 *
 * <p>Using ScopingLocalizedErrors provides application developers with the flexibility to provide
 * as much or as little specificity in error messages as desired.  The scope and ordering of the
 * messages is designed to allow developers to specify default messages at several levels, and
 * then override those as needed for specific circumstances.</p>
 *
 * @author Tim Fennell
 */
public class ScopedLocalizableError extends LocalizableError {
    /** Default key that is used for looking up error messages. */
    public static final String DEFAULT_NAME = "errorMessage";

    private String defaultScope;
    private String key;

    /**
     * Constructs a ScopedLocalizableError with the supplied information.
     *
     * @param defaultScope the default scope under which to look for the error message should more
     *        specificly scoped lookups fail
     * @param key the name of the message to lookup
     * @param parameters an optional number of replacement parameters to be used in the message
     */
    public ScopedLocalizableError(String defaultScope, String key, Object... parameters) {
        super(defaultScope + "." + key, parameters);
        this.defaultScope = defaultScope;
        this.key = key;
    }


    /**
     * Overrides getMessageTemplate to perform a scoped search for a message template as defined
     * in the class level javadoc.
     */
    @Override
    protected String getMessageTemplate(Locale locale) {
        String name1=null, name2=null, name3=null, name4=null, name5=null;
        name1 = getActionPath() + "." + getFieldName() + "." + key;
        String template = LocalizationUtility.getErrorMessage(locale, name1);

        if (template == null) {
            name2 = getActionPath() + "." + getFieldName() + "." + DEFAULT_NAME;
            template = LocalizationUtility.getErrorMessage(locale, name2);
        }
        if (template == null) {
            name3 = getFieldName() + "." + DEFAULT_NAME;
            template = LocalizationUtility.getErrorMessage(locale, name3);
        }
        if (template == null) {
            name4 = getActionPath() + "." + key;
            template = LocalizationUtility.getErrorMessage(locale, name4);
        }
        if (template == null) {
            name5 = defaultScope + "." + key;
            template = LocalizationUtility.getErrorMessage(locale, name5);
        }

        if (template == null) {
            throw new MissingResourceException(
                    "Could not find an error message with any of the following keys: " +
                    "'" + name1 + "', '" + name2 + "', '" + name3 + "', '" +
                    name4 + "', '" + name5 + "'.", null, null
            );
        }

        return template;
    }

    /** Generated equals method that checks all fields and super.equals(). */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final ScopedLocalizableError that = (ScopedLocalizableError) o;

        if (defaultScope != null ? !defaultScope.equals(that.defaultScope) : that.defaultScope != null) {
            return false;
        }
        if (key != null ? !key.equals(that.key) : that.key != null) {
            return false;
        }

        return true;
    }

    /** Generated hashCode() method. */
    public int hashCode() {
        int result = super.hashCode();
        result = 29 * result + (defaultScope != null ? defaultScope.hashCode() : 0);
        result = 29 * result + (key != null ? key.hashCode() : 0);
        return result;
    }
}
