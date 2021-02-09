package net.sourceforge.stripes.validation;

import java.math.BigInteger;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class BigIntegerTypeConverterTest extends TypeConverterTest<BigIntegerTypeConverter, BigInteger> {

   @BeforeEach
   void before() {
      givenLocale(Locale.US);
   }

   @Test
   void testBasicParse() {
      whenTypeIsConverted("1234567");
      thenResultIs("1234567");
   }

   @Test
   void testDecimalTruncation() {
      whenTypeIsConverted("123456789.98765");
      thenResultIs("123456789");
   }

   @Test
   void testInvalidInput() {
      whenTypeIsConverted("a1b2vc3d4");
      thenValidationFails();
   }

   @Test
   void testParseAlternateLocale() {
      givenLocale(Locale.GERMANY);
      whenTypeIsConverted("123.456.789");
      thenResultIs("123456789");
   }

   @Test
   void testParseBigNumber() {
      String number = Long.MAX_VALUE + "8729839871981298798234";
      whenTypeIsConverted(number);
      thenResultIs(number);
   }

   @Test
   void testParseWithGroupingCharacters() {
      whenTypeIsConverted("7,297,029,872,767,869,231,987,623,498,756,389,734,567,876,534");
      thenResultIs("7297029872767869231987623498756389734567876534");
   }

   void thenResultIs( String bigIntegerStringRepresentation ) {
      thenResultIs(new BigInteger(bigIntegerStringRepresentation));
   }

}
