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
public class LongTypeConverter extends NumberTypeConverterSupport implements TypeConverter<Long> {
  /**
   * @param input the String to convert into a single Long
   * @param errors the collection to which validation errors should be added
   * @return Long a Long object if one can be parsed from the input
   */
  public Long convert(
      String input, Class<? extends Long> targetType, Collection<ValidationError> errors) {

    Number number = parse(input, errors);
    Long returnValue = null;
    if (errors.isEmpty()) {
      returnValue = number.longValue();
    }

    return returnValue;
  }

  /** Overridden to return integer instances instead. */
  @Override
  protected NumberFormat[] getNumberFormats() {
    return new NumberFormat[] {NumberFormat.getIntegerInstance(this.getLocale())};
  }
}
