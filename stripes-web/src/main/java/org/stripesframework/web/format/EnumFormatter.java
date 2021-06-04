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
package org.stripesframework.web.format;

import java.util.Locale;


/**
 * A simple formatter for Enum classes that always returns the value of Enum.name(). Intended
 * really only to enable the seamless usage of enums as values in hidden fields, radio
 * buttons, checkboxes etc. it is not intended that this will be used to format Enum
 * values into text fields where a localized value might be more appropriate.
 *
 * @author Tim Fennell
 * @since Stripes 1.4.1
 */
public class EnumFormatter implements Formatter<Enum<?>> {

   /**
    * Formats the supplied value as a String.  If the value cannot be formatted because it is
    * an inappropriate type, or because faulty pattern information was supplied, should fail
    * loudly by throwing a RuntimeException or subclass thereof.
    *
    * @param input an object of a type that the formatter knows how to format
    * @return a String version of the input, formatted for the chosen locale
    */
   @Override
   public String format( Enum<?> input ) {
       if ( input != null ) {
           return input.name();
       } else {
           return null;
       }
   }

   /** Does nothing since no initialization is needed. */
   @Override
   public void init() { }

   /** Does nothing. Format patterns are not supported for Enums. */
   @Override
   public void setFormatPattern( String formatPattern ) { }

   /** Does nothing. Format types are not supported for Enums. */
   @Override
   public void setFormatType( String formatType ) { }

   /** Does nothing. Enums values are always formatted using name() which is not localizable. */
   @Override
   public void setLocale( Locale locale ) { }
}
