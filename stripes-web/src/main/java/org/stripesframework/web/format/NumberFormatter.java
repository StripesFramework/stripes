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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.stripesframework.web.exception.StripesRuntimeException;


/**
 * <p>Formats numbers into localized Strings for display.  This class relies heavily on the
 * NumberFormat and DecimalFormat classes in the java.text package, and it is suggested that you
 * become familiar with those classes before using custom formats.</p>
 *
 * <p>Accepts the following named formatTypes (not case sensitive):</p>
 * <ul>
 *   <li>number</li>
 *   <li>currency</li>
 *   <li>percentage</li>
 * </ul>
 *
 * <p>If a format type is not supplied the default value of "number" will be used. Format String
 * can be either a custom pattern as used by NumberFormat, or one of the following named formats
 * (not case sensitive):</p>
 * <ul>
 *   <li>plain - Outputs text in a manner similar to toString(), but appropriate to a locale.</li>
 *   <li>integer - Outputs text with grouping characters and no decimals.</li>
 *   <li>decimal - Outputs text with grouping characters and 2-6 decimal positions as needed.</li>
 * </ul>
 *
 * @author Tim Fennell
 */
public class NumberFormatter implements Formatter<Number> {

   /** Maintains a set of named formats that can be used instead of patterns. */
   protected static final Set<String> namedPatterns = new HashSet<>();

   private static final   ThreadLocal<HashMap<Locale, NumberFormat>> numberFormatNumberCache   = ThreadLocal.withInitial(HashMap::new);
   private static final   ThreadLocal<HashMap<Locale, NumberFormat>> numberFormatPercentCache  = ThreadLocal.withInitial(HashMap::new);
   private static final   ThreadLocal<HashMap<Locale, NumberFormat>> numberFormatCurrencyCache = ThreadLocal.withInitial(HashMap::new);

   static {
      namedPatterns.add("plain");
      namedPatterns.add("integer");
      namedPatterns.add("decimal");
   }

   private String       formatType;
   private String       formatPattern;
   private Locale       locale;
   private NumberFormat format;

   /** Formats the number supplied as a String. */
   @Override
   public String format( Number input ) {
      return format.format(input);
   }

   /** Gets the named format string or number format pattern to use to format the number. */
   public String getFormatPattern() {
      return formatPattern;
   }

   /** Gets the format type to be used to render numbers as Strings. */
   public String getFormatType() {
      return formatType;
   }

   /** Gets the locale that output String should be in. */
   public Locale getLocale() {
      return locale;
   }

   /** Instantiates the NumberFormat based on the information provided through setter methods. */
   @Override
   public void init() {
      // Set some sensible defaults if things are null
      if ( formatType == null ) {
          formatType = "number";
      }

      // Figure out which kind of number formatter to get
      if ( formatPattern == null ) {
          formatPattern = "plain";
      }

      switch (formatType.toLowerCase()) {
         case "number":
            format = numberFormatNumberCache.get().computeIfAbsent(locale, NumberFormat::getInstance);
            break;
         case "currency":
            format = numberFormatCurrencyCache.get().computeIfAbsent(locale, NumberFormat::getCurrencyInstance);
            break;
         case "percentage":
            format = numberFormatPercentCache.get().computeIfAbsent(locale, NumberFormat::getPercentInstance);
            break;
         default:
            throw new StripesRuntimeException(
                    "Invalid format type supplied for formatting a " + "number: " + formatType + ". Valid values are 'number', 'currency' " + "and 'percentage'.");
      }

      // Do any extra configuration
      if ( formatPattern.equalsIgnoreCase("plain") ) {
          format.setGroupingUsed(false);
      } else if ( formatPattern.equalsIgnoreCase("integer") ) {
          format.setMaximumFractionDigits(0);
      } else if ( formatPattern.equalsIgnoreCase(("decimal")) ) {
          format.setMinimumFractionDigits(2);
          format.setMaximumFractionDigits(6);
      } else {
         try {
            ((DecimalFormat)format).applyPattern(formatPattern);
         }
         catch ( Exception e ) {
            throw new StripesRuntimeException("Custom pattern could not be applied to " + "NumberFormat instance.  Pattern was: " + formatPattern, e);
         }
      }
   }

   /** Sets the named format string or number format pattern to use to format the number. */
   @Override
   public void setFormatPattern( String formatPattern ) {
      this.formatPattern = formatPattern;
   }

   /** Sets the format type to be used to render numbers as Strings. */
   @Override
   public void setFormatType( String formatType ) {
      this.formatType = formatType;
   }

   /** Sets the locale that output String should be in. */
   @Override
   public void setLocale( Locale locale ) {
      this.locale = locale;
   }
}
