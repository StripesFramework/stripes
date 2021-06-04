package org.stripesframework.web.extensions;

import java.util.Collection;
import java.util.Locale;

import org.stripesframework.web.validation.TypeConverter;
import org.stripesframework.web.validation.ValidationError;


/**
 * Converts the input string to upper case. This type converter does not extend a stock type
 * converter.
 *
 * @author Freddy Daoud
 */
public class MyUppercaseStringTypeConverter implements TypeConverter<String> {

   @Override
   public String convert( String input, Class<? extends String> targetType, Collection<ValidationError> errors ) {
      return input.toUpperCase();
   }

   @Override
   public void setLocale( Locale locale ) { }
}
