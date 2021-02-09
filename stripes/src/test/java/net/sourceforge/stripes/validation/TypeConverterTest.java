package net.sourceforge.stripes.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import net.sourceforge.stripes.util.GenericType;


public abstract class TypeConverterTest<C extends TypeConverter<T>, T> {

   private final C        _converter;
   private final Class<T> _type;

   private       T                           _result;
   private final Collection<ValidationError> _errors = new ArrayList<>();

   protected TypeConverterTest() {
      _converter = createConverter();
      _type = GenericType.resolve(getClass(), 1);
   }

   @SuppressWarnings("unchecked")
   protected C createConverter() {
      Class<Object> converterClass = GenericType.resolve(getClass(), 0);
      try {
         return (C)converterClass.getConstructor().newInstance();
      }
      catch ( Exception e ) {
         throw new RuntimeException("Failed to create type converter " + converterClass, e);
      }
   }

   protected T getResult() {
      return _result;
   }

   protected void givenLocale( Locale locale ) {
      _converter.setLocale(locale);
   }

   protected void thenResultIs( T result ) {
      assertThat(_result).isEqualTo(result);
   }

   protected void thenValidationFails() {
      assertThat(_errors).isNotEmpty();
   }

   protected void thenValidationSucceeds() {
      assertThat(_errors).isEmpty();
   }

   protected void whenTypeIsConverted( String input ) {
      whenTypeIsConverted(input, _type);
   }

   protected void whenTypeIsConverted( String input, Class<? extends T> targetType ) {
      _result = _converter.convert(input, targetType, _errors);
   }
}
