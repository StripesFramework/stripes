package net.sourceforge.stripes.validation;

import java.math.BigDecimal;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class BigDecimalTypeConverterTest extends TypeConverterTest<BigDecimalTypeConverter, BigDecimal> {

   @BeforeEach
   void before() {
      givenLocale(Locale.US);
   }

   @Test
   void testBasicParse() {
      whenTypeIsConverted("12345.67");
      thenResultIs("12345.67");
   }

   @Test
   void testInvalidInput() {
      whenTypeIsConverted("a1b2vc3d4");
      thenValidationFails();
   }

   @Test
   void testParseAlternateLocale() {
      givenLocale(Locale.GERMANY);
      whenTypeIsConverted("123.456.789,99");
      thenResultIs("123456789.99");
   }

   @Test
   void testParseBigNumber() {
      String number = "7297029872767869231987623498756389734567893246934298765342987563489723497"
            + ".97982730927907092387409872340987234698750987129872348970982374076283764";
      whenTypeIsConverted(number);
      thenResultIs(number);
   }

   @Test
   void testParseWithGroupingCharacters() {
      String number = "7297029872767869231987623498756389734567876534.2987563489723497";
      String grouped = "7,297,029,872,767,869,231,987,623,498,756,389,734,567,876,534.2987563489723497";
      whenTypeIsConverted(grouped);
      thenResultIs(number);
   }

   void thenResultIs( String bigDecimalStringRepresentation ) {
      thenResultIs(new BigDecimal(bigDecimalStringRepresentation));
   }
}
