package net.sourceforge.stripes.util;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SimpleMessageFormat {

   private static final Pattern PATTERN = Pattern.compile("\\{(\\d+)}");

   public static String format( String pattern, Locale locale, Object... params ) {
      Matcher matcher = PATTERN.matcher(pattern);
      if ( matcher.find() ) {
         StringBuilder output = createStringBuilder(pattern, params);

         int startIndex = 0;
         do {
            output.append(pattern, startIndex, matcher.start());
            startIndex = matcher.end();

            int argumentIndex = Integer.parseInt(matcher.group(1));
            if ( argumentIndex < params.length ) {
               Object param = params[argumentIndex];
               appendParam(output, locale, param);
            }
         }
         while ( matcher.find() );

         output.append(pattern, startIndex, pattern.length());
         return output.toString();
      } else {
         return pattern;
      }
   }

   private static void appendParam( StringBuilder output, Locale locale, Object param ) {
      if ( param != null ) {
         if ( locale != null ) {
            if ( param instanceof Number ) {
               output.append(NumberFormat.getInstance(locale).format(param));
            } else if ( param instanceof Date ) {
               output.append(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale).format(param));
            } else {
               output.append(param);
            }
         } else {
            output.append(param);
         }
      }
   }

   private static StringBuilder createStringBuilder( String pattern, Object... args ) {
      int capacity = pattern.length();
      if ( args != null ) {
         //noinspection Convert2streamapi
         for ( Object a : args ) {
            if ( a instanceof CharSequence ) {
               capacity += ((CharSequence)a).length();
            }
         }
      }
      return new StringBuilder(capacity);
   }
}
