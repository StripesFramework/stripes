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

import java.text.NumberFormat;
import java.util.Collection;

/**
 * Basic type converter for converting strings to integers.
 *
 * @author Tim Fennell
 */
public class IntegerTypeConverter extends NumberTypeConverterSupport
    implements TypeConverter<Integer> {
  /**
   * @param input the String to convert into a single Integer
   * @param errors the collection to which validation errors should be added
   * @return Integer an Integer object if one can be parsed from the input
   */
  public Integer convert(
      String input, Class<? extends Integer> targetType, Collection<ValidationError> errors) {

    Number number = parse(input, errors);
    Integer returnValue = null;

    if (errors.isEmpty()) {
      long output = number.longValue();

      if (output < Integer.MIN_VALUE || output > Integer.MAX_VALUE) {
        errors.add(
            new ScopedLocalizableError(
                "converter.integer", "outOfRange", Integer.MIN_VALUE, Integer.MAX_VALUE));
      } else {
        returnValue = (int) output;
      }
    }

    return returnValue;
  }

  /** Overridden to return integer instances instead. */
  @Override
  protected NumberFormat[] getNumberFormats() {
    return new NumberFormat[] {NumberFormat.getIntegerInstance(this.getLocale())};
  }
}
