package org.stripesframework.web.validation;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;


class LocalDateTypeConverterTest extends TypeConverterTest<LocalDateTypeConverter, LocalDate> {

   @Test
   void testStringToLocalDate() {
      final String input = "2020-01-02";

      whenTypeIsConverted(input);

      thenResultIs(LocalDate.parse(input));
      thenValidationSucceeds();
   }

   @Test
   public void testStringToLocalDate_CannotParseString() {
      final String input = "01.01.2020";

      whenTypeIsConverted(input);

      thenResultIs(null);
      thenValidationFails();
   }
}