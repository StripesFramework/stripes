package org.stripesframework.web.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import org.junit.jupiter.api.Test;


/**
 * Tests for the support class which helps out with number parsing and type converting.
 *
 * @author Tim Fennell
 */
public class NumberTypeConverterSupportTest {

   @Test
   public void basicPositiveTests() {
      Number number = getConverter().parse("10912", new ArrayList<>());
      assertThat(number.intValue()).isEqualTo(10912);

      number = getConverter().parse("-1,000,000", new ArrayList<>());
      assertThat(number.intValue()).isEqualTo(-1000000);
   }

   @Test
   public void testComplicatedString() {
      Number number = getConverter().parse("  ($2,154,123.66) ", new ArrayList<>());
      assertThat(number.doubleValue()).isEqualTo(-2154123.66);
   }

   @Test
   public void testCurrency() {
      Number number = getConverter().parse("$57", new ArrayList<>());
      assertThat(number.intValue()).isEqualTo(57);

      number = getConverter().parse("$1,999.95", new ArrayList<>());
      assertThat(number.doubleValue()).isEqualTo(1999.95);
   }

   @Test
   public void testCurrencyWithSpace() {
      Number number = getConverter().parse("$ 57", new ArrayList<>());
      assertThat(number).isNotNull();
      assertThat(number.intValue()).isEqualTo(57);

      number = getConverter().parse("1,999.95 $", new ArrayList<>());
      assertThat(number).isNotNull();
      assertThat(number.doubleValue()).isEqualTo(1999.95);
   }

   @Test
   public void testFloatingPointsNumbers() {
      Number number = getConverter().parse("123.456", new ArrayList<>());
      assertThat(number.doubleValue()).isEqualTo(123.456);
   }

   @Test
   public void testNegativeCurrency() {
      Number number = getConverter().parse("-$57", new ArrayList<>());
      assertThat(number.intValue()).isEqualTo(-57);

      number = getConverter().parse("$-57", new ArrayList<>());
      assertThat(number.intValue()).isEqualTo(-57);

      number = getConverter().parse("($1,999.95)", new ArrayList<>());
      assertThat(number.doubleValue()).isEqualTo(-1999.95);

      number = getConverter().parse("$(1,999.95)", new ArrayList<>());
      assertThat(number.doubleValue()).isEqualTo(-1999.95);
   }

   @Test
   public void testNumbersWithWhiteSpace() {
      Number number = getConverter().parse("   5262  ", new ArrayList<>());
      assertThat(number.intValue()).describedAs("White space should have no effect.").isEqualTo(5262);
   }

   @Test
   public void testParentheticalNumbers() {
      Number number = getConverter().parse("(891)", new ArrayList<>());
      assertThat(number.intValue()).describedAs("Brackets mean negative values.").isEqualTo(-891);
   }

   @Test
   public void testWithBogusTrailingText() {
      Collection<ValidationError> errors = new ArrayList<>();
      Number number = getConverter().parse("12345six", errors);
      assertThat(number).isNull();
      assertThat(errors).describedAs("We should have gotten a parse error.").hasSize(1);
   }

   @Test
   public void testWithMultipleDecimalPoints() {
      Collection<ValidationError> errors = new ArrayList<>();
      Number number = getConverter().parse("123.456.789", errors);
      assertThat(number).isNull();
      assertThat(errors).describedAs("We should have gotten a parse error.").hasSize(1);
   }

   @Test
   public void testWithText() {
      Collection<ValidationError> errors = new ArrayList<>();
      Number number = getConverter().parse("not-a-number", errors);
      assertThat(number).isNull();
      assertThat(errors).describedAs("We should have gotten a parse error.").hasSize(1);
   }

   /** Helper method to fetch a US locale converter. */
   protected NumberTypeConverterSupport getConverter() {
      NumberTypeConverterSupport c = new NumberTypeConverterSupport();
      c.setLocale(Locale.US);
      return c;
   }
}
