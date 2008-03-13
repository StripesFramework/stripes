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
 * <p>Provides a mechanism for creating localizable error messages for presentation to the user.
 * Uses ResourceBundles to provide the localization of the error message.  Messages may contain one
 * or more &quot;replacement parameters &quot;.  Two replacement parameters are provided by default,
 * they are the field name and field value, and are indices 0 and 1 respectively.  To use
 * replacement parameters a message must contain the replacement token {#} where # is the numeric
 * index of the replacement parameter.</p>
 *
 * <p>For example, to construct an error message with one additional replacement parameter which is
 * the action the user was trying to perform, you might have a properties file entry like:</p>
 *
 * <pre>/action/MyAction.myErrorMessage={1} is not a valid {0} when trying to {2}</pre>
 *
 * <p>At runtime this might get replaced out to result in an error message for the user that looks
 * like &quot;<em>Fixed</em> is not a valid <em>status</em> when trying to create a new
 * <em>bug</em>&quot;.</p>
 *
 * <p>First looks for a resource with the action bean FQN prepended to the supplied message key. If
 * If that cannot be found then looks with the action path as a prefix instead of the FQN. Failing
 * that, the last attempt looks for a resource with the exact message key provided. This allows
 * developers to segregate their error messages by action without having to repeat the action
 * path in the ActionBean.  For example a message constructed with
 * {@code new LocalizableError("insufficientBalance")} might look for message resources with
 * the following keys:</p>
 *
 * <ul>
 *   <li>{@code com.myco.TransferActionBean.insufficientBalance}</li>
 *   <li>{@code /account/Transfer.action.insufficientBalance}</li>
 *   <li>{@code insufficientBalance}</li>
 * </ul>
 *
 * <p>One last point of interest is where the user friendly field name comes from. Firstly an
 * attempt is made to look up the localized name in the applicable resource bundle using the
 * String <em>beanClassFQN.fieldName</em> where beanClassFQN is the fully qualified name of the
 * bean class, and fieldName is the name of the field on the form. The second attempt is made with 
 * String <em>actionPath.fieldName</em> where actionPath is the action of the form in the JSP
 * (or equally, the path given in the @UrlBinding annotation in the ActionBean class). Finally,
 * the last attempt uses fieldName by itself.</p>
 *
 * @see java.text.MessageFormat
 * @see java.util.ResourceBundle
 */
public class LocalizableError extends SimpleError {
	private static final long serialVersionUID = 1L;

    private String messageKey;

    /**
     * Creates a new LocalizableError with the message key provided, and optionally zero or more
     * replacement parameters to use in the message.  It should be noted that the replacement
     * parameters provided here can be referenced in the error message <b>starting with number
     * 2</b>.
     *
     * @param messageKey a key to lookup a message in the resource bundle
     * @param parameter one or more replacement parameters to insert into the message
     */
    public LocalizableError(String messageKey, Object... parameter) {
        super(null, parameter);
        this.messageKey = messageKey;
    }

    /**
     * Method responsible for using the information supplied to the error object to find a
     * message template. In this class this is done simply by looking up the resource
     * corresponding to the messageKey supplied in the constructor, first with the FQN
     * prepended, then with the action path prepended and finally bare.
     */
    @Override
    protected String getMessageTemplate(Locale locale) {
        String template = null;
        
        if (getBeanclass() != null) {
            template = LocalizationUtility.getErrorMessage(locale, getBeanclass().getName() + "." + messageKey);
        }
        if (template == null) {
            template = LocalizationUtility.getErrorMessage(locale, getActionPath() + "." + messageKey);
        }
        if (template == null) {
            template = LocalizationUtility.getErrorMessage(locale, messageKey);
        }

        if (template == null) {
            throw new MissingResourceException(
                    "Could not find an error message with key: " + messageKey, null, null);
        }

        return template;
    }

    /** Generated equals method that compares each field and super.equals(). */
    @Override
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

        final LocalizableError that = (LocalizableError) o;

        if (messageKey != null ? !messageKey.equals(that.messageKey) : that.messageKey != null) {
            return false;
        }

        return true;
    }

    /** Generated hashCode method. */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 29 * result + (messageKey != null ? messageKey.hashCode() : 0);
        return result;
    }

	public String getMessageKey() {
		return messageKey;
	}
}
