package org.stripesframework.web.extensions;

import java.util.Collection;

import org.stripesframework.web.validation.IntegerTypeConverter;
import org.stripesframework.web.validation.ValidationError;


/**
 * Converts the input string to an Integer and doubles the value. This type converter extends the
 * stock IntegerTypeConverter.
 *
 * @author Freddy Daoud
 */
public class MyDoubleIntegerTypeConverter extends IntegerTypeConverter {

   @Override
   public Integer convert( String input, Class<? extends Integer> targetType, Collection<ValidationError> errors ) {
      Integer result = super.convert(input, targetType, errors);

      if ( result != null ) {
         result *= 2;
      }
      return result;
   }
}
