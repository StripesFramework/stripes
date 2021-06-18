package org.stripesframework.web.validation;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Locale;


/**
 * Converts a ISO-8601 formatted string (e.g. "2020-01-01") to a LocalDate.
 */
public class LocalDateTypeConverter implements TypeConverter<LocalDate> {

   @Override
   public LocalDate convert( String input, Class<? extends LocalDate> targetType, Collection<ValidationError> errors ) {
      try {
         return LocalDate.parse(input);
      }
      catch ( Exception e ) {
         errors.add(new ScopedLocalizableError("converter.localDate", "invalidDate"));
         return null;
      }
   }

   @Override
   public void setLocale( Locale locale ) {}
}
