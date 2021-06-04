package org.stripesframework.web.validation;

import java.util.Locale;

import org.junit.jupiter.api.Test;


public class PercentageTypeConverterTest extends TypeConverterTest<PercentageTypeConverter, Number> {

   public PercentageTypeConverterTest() {
      givenLocale(Locale.US);
   }

   @Test
   public void parseBasic() {
      whenTypeIsConverted("80%", Float.class);
      thenResultIs(0.8f);
   }

   @Test
   public void parseBasicDouble() {
      whenTypeIsConverted("0.8%", Double.class);
      thenResultIs(0.008);
   }

   @Test
   public void parseNegative() {
      whenTypeIsConverted("-80%", Float.class);
      thenResultIs(-0.8f);
   }

   @Test
   public void parseNegativeDouble() {
      whenTypeIsConverted("-0.8%", Double.class);
      thenResultIs(-0.008);
   }

   @Test
   public void parseNegativeSpaceBeforePercentSign() {
      whenTypeIsConverted("-80 %", Float.class);
      thenResultIs(-0.8f);
   }

   @Test
   public void parseNegativeSpaceBeforePercentSignDouble() {
      whenTypeIsConverted("-0.8 %", Double.class);
      thenResultIs(-0.008);
   }

   @Test
   public void parseNegativeWithoutPercentSign() {
      whenTypeIsConverted("-80", Float.class);
      thenResultIs(-0.8f);
   }

   @Test
   public void parseNegativeWithoutPercentSignDouble() {
      whenTypeIsConverted("-0.8", Double.class);
      thenResultIs(-0.008);
   }

   @Test
   public void parseParentheses() {
      whenTypeIsConverted("(80%)", Float.class);
      thenResultIs(-0.8f);
   }

   @Test
   public void parseParenthesesDouble() {
      whenTypeIsConverted("(0.8%)", Double.class);
      thenResultIs(-0.008);
   }

   @Test
   public void parseParenthesesSpaceBeforePercentSign() {
      whenTypeIsConverted("(80 %)", Float.class);
      thenResultIs(-0.8f);
   }

   @Test
   public void parseParenthesesSpaceBeforePercentSignDouble() {
      whenTypeIsConverted("(0.8 %)", Double.class);
      thenResultIs(-0.008);
   }

   @Test
   public void parseParenthesesWithoutPercentSign() {
      whenTypeIsConverted("(80)", Float.class);
      thenResultIs(-0.8f);
   }

   @Test
   public void parseParenthesesWithoutPercentSignDouble() {
      whenTypeIsConverted("(0.8)", Double.class);
      thenResultIs(-0.008);
   }

   @Test
   public void parseSpaceBeforePercentSign() {
      whenTypeIsConverted("80 %", Float.class);
      thenResultIs(0.8f);
   }

   @Test
   public void parseSpaceBeforePercentSignDouble() {
      whenTypeIsConverted("0.8 %", Double.class);
      thenResultIs(0.008);
   }

   @Test
   public void parseWithoutPercentSign() {
      whenTypeIsConverted("80", Float.class);
      thenResultIs(0.8f);
   }

   @Test
   public void parseWithoutPercentSignDouble() {
      whenTypeIsConverted("0.8", Double.class);
      thenResultIs(0.008);
   }
}
