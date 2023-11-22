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
 * Basic type converter for converting strings to short integers.
 *
 * @author Tim Fennell
 */
public class ShortTypeConverter extends NumberTypeConverterSupport implements TypeConverter<Short> {
  /**
   * Converts the input to an object of type Short.
   *
   * @param input the String to convert into a single Short
   * @param errors the collection to which validation errors should be added
   * @return Short a Short object if one can be parsed from the input
   */
  public Short convert(
      String input, Class<? extends Short> targetType, Collection<ValidationError> errors) {

    Number number = parse(input, errors);
    Short returnValue = null;

    if (errors.isEmpty()) {
      long output = number.longValue();

      if (output < Short.MIN_VALUE || output > Short.MAX_VALUE) {
        errors.add(
            new ScopedLocalizableError(
                "converter.short", "outOfRange", Short.MIN_VALUE, Short.MAX_VALUE));
      } else {
        returnValue = (short) output;
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
