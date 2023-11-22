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
package net.sourceforge.stripes.action;

import java.io.Serial;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

/**
 * A simple non-error message that uses the String supplied to it as the message (i.e. it does not
 * look up the message in a resource bundle).
 *
 * <p>Messages may contain one or more &quot;replacement parameters&quot;. To use replacement
 * parameters a message must contain the replacement token {#} where # is the numeric index of the
 * replacement parameter.
 *
 * <p>For example, to construct a message with two replacement parameters you might supply a message
 * like:
 *
 * <pre>Welcome back {0}, your last login was on {1,date,short}.</pre>
 *
 * <p>At runtime this might get replaced out to result in a message for the user that looks like
 * &quot;Welcome back <em>Johnnie</em>, your last login was on <em>01/01/2006</em>&quot;.
 *
 * <p>{@link java.text.MessageFormat} is used to merge the parameters in to the message and as a
 * result the parameters can themselves receive formatting through the various java.text.*
 * formatters.
 *
 * @author Tim Fennell
 * @see java.text.MessageFormat
 */
public class SimpleMessage implements Message {
  @Serial private static final long serialVersionUID = 1L;

  private String message;

  /**
   * The set of replacement parameters that will be used to create the message from the message
   * template. Note that position 0 is reserved for the field name and position 1 is reserved for
   * the field value.
   */
  private final Object[] replacementParameters;

  /**
   * Constructs a message with the supplied message string and zero or more parameters to be merged
   * into the message. When constructing a SimpleMessage a non-null message string must be supplied
   * (though subclasses may return null if they do not rely upon it).
   *
   * @param message the String message to display to the user, optionally with placeholders for
   *     replacement parameters
   * @param parameters Message parameters to be merged into the message
   */
  public SimpleMessage(String message, Object... parameters) {
    this.replacementParameters = parameters;
    this.message = message;
  }

  /**
   * Helper constructor to allow subclasses to provide and manipulate replacement parameters without
   * having to supply a message String.
   *
   * @param parameters zero or more parameters for replacement into the message
   */
  protected SimpleMessage(Object... parameters) {
    this.replacementParameters = parameters;
  }

  /**
   * Uses the String message passed in as the message template and combines it with any replacement
   * parameters provided to construct a message for display to the user. Although SimpleMessage does
   * not localize its message string, any formatters invoked as a result of using replacement
   * parameters will be in the correct locale.
   *
   * @param locale the locale of the current request
   * @return String the message stored under the messageKey supplied
   */
  public String getMessage(Locale locale) {
    // Now get the message itself
    String messageTemplate = getMessageTemplate(locale);

    // For compatibility with JSTL, only apply formatting if there are replacement parameters
    if (this.replacementParameters != null && this.replacementParameters.length > 0) {
      MessageFormat format = new MessageFormat(messageTemplate, locale);
      return format.format(this.replacementParameters, new StringBuffer(), null).toString();
    } else {
      return messageTemplate;
    }
  }

  /**
   * Simply returns the message passed in at Construction time. Designed to be overridden by
   * subclasses to lookup messages from resource bundles.
   *
   * @param locale the Locale of the message template desired
   * @return the message (potentially with TextFormat replacement tokens).
   */
  protected String getMessageTemplate(Locale locale) {
    return this.message;
  }

  /**
   * Returns the exact message that was supplied in the constructor. This should not be called to
   * render user output, but only when direct access to the String is needed for some reason.
   *
   * @return the exact message String passed in to the constructor
   */
  public String getMessage() {
    return this.message;
  }

  /** Allows subclasses to access the replacement parameters for this message. */
  public Object[] getReplacementParameters() {
    return this.replacementParameters;
  }

  /**
   * Checks equality by ensuring that the current instance and the 'other' instance are instances of
   * the same class (though not necessarily SimpleMessage!) and that the message String and
   * replacement parameters provided are the same.
   *
   * @param o another object that is a SimpleMessage or subclass thereof
   * @return true if the two objects will generate the same user message, false otherwise
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final SimpleMessage that = (SimpleMessage) o;

    if (!Objects.equals(message, that.message)) {
      return false;
    }
    return Arrays.equals(replacementParameters, that.replacementParameters);
  }

  /** Generated hash code method. */
  @Override
  public int hashCode() {
    return (message != null ? message.hashCode() : 0);
  }
}
