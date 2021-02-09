package net.sourceforge.stripes.validation;

import org.junit.jupiter.api.Test;


class CreditCardTypeConverterTest extends TypeConverterTest<CreditCardTypeConverter, String> {

   @Test
   void testInvalidNumber() {
      whenTypeIsConverted("4111111111111110");
      thenResultIs(null);
   }

   @Test
   void testStripNonNumericCharacters() {
      whenTypeIsConverted("4111-1111-1111-1111");
      thenResultIs("4111111111111111");
   }

   @Test
   void testValidNumber() {
      whenTypeIsConverted("4111111111111111");
      thenResultIs("4111111111111111");
   }
}