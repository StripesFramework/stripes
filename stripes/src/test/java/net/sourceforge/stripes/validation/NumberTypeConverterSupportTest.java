package net.sourceforge.stripes.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the support class which helps out with number parsing and type converting.
 *
 * @author Tim Fennell
 */
public class NumberTypeConverterSupportTest {

  /** Helper method to fetch a US locale converter. */
  protected NumberTypeConverterSupport getConverter() {
    NumberTypeConverterSupport c = new NumberTypeConverterSupport();
    c.setLocale(Locale.US);
    return c;
  }

  @Test
  public void basicPositiveTests() {
    Number number = getConverter().parse("10912", new ArrayList<ValidationError>());
    Assert.assertEquals(number.intValue(), 10912);

    number = getConverter().parse("-1,000,000", new ArrayList<ValidationError>());
    Assert.assertEquals(number.intValue(), -1000000);
  }

  @Test
  public void testNumbersWithWhiteSpace() {
    Number number = getConverter().parse("   5262  ", new ArrayList<ValidationError>());
    Assert.assertEquals("White space should have no effect.", number.intValue(), 5262);
  }

  @Test
  public void testFloatingPointsNumbers() {
    Number number = getConverter().parse("123.456", new ArrayList<ValidationError>());
    Assert.assertEquals(number.doubleValue(), 123.456, 0);
  }

  @Test
  public void testParentheticalNumbers() {
    Number number = getConverter().parse("(891)", new ArrayList<ValidationError>());
    Assert.assertEquals("Brackets mean negative values.", number.intValue(), -891);
  }

  @Test
  public void testCurrency() {
    Number number = getConverter().parse("$57", new ArrayList<ValidationError>());
    Assert.assertEquals(number.intValue(), 57);

    number = getConverter().parse("$1,999.95", new ArrayList<ValidationError>());
    Assert.assertEquals(number.doubleValue(), 1999.95, 0);
  }

  @Test
  public void testCurrencyWithSpace() {
    Number number = getConverter().parse("$ 57", new ArrayList<ValidationError>());
    Assert.assertNotNull(number);
    Assert.assertEquals(number.intValue(), 57);

    number = getConverter().parse("1,999.95 $", new ArrayList<ValidationError>());
    Assert.assertNotNull(number);
    Assert.assertEquals(number.doubleValue(), 1999.95, 0);
  }

  @Test
  public void testNegativeCurrency() {
    Number number = getConverter().parse("-$57", new ArrayList<ValidationError>());
    Assert.assertEquals(number.intValue(), -57);

    number = getConverter().parse("$-57", new ArrayList<ValidationError>());
    Assert.assertEquals(number.intValue(), -57);

    number = getConverter().parse("($1,999.95)", new ArrayList<ValidationError>());
    Assert.assertEquals(number.doubleValue(), -1999.95, 0);

    number = getConverter().parse("$(1,999.95)", new ArrayList<ValidationError>());
    Assert.assertEquals(number.doubleValue(), -1999.95, 0);
  }

  @Test
  public void testComplicatedString() {
    Number number = getConverter().parse("  ($2,154,123.66) ", new ArrayList<ValidationError>());
    Assert.assertEquals(number.doubleValue(), -2154123.66, 0);
  }

  @Test
  public void testWithText() {
    Collection<ValidationError> errors = new ArrayList<ValidationError>();
    Number number = getConverter().parse("not-a-number", errors);
    Assert.assertNull(number);
    Assert.assertEquals("We should have gotten a parse error.", errors.size(), 1);
  }

  @Test
  public void testWithBogusTrailingText() {
    Collection<ValidationError> errors = new ArrayList<ValidationError>();
    Number number = getConverter().parse("12345six", errors);
    Assert.assertNull(number);
    Assert.assertEquals("We should have gotten a parse error.", errors.size(), 1);
  }

  @Test
  public void testWithMultipleDecimalPoints() {
    Collection<ValidationError> errors = new ArrayList<ValidationError>();
    Number number = getConverter().parse("123.456.789", errors);
    Assert.assertNull(number);
    Assert.assertEquals("We should have gotten a parse error.", errors.size(), 1);
  }
}
