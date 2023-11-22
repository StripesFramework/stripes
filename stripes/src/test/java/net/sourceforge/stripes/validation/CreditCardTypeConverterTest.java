package net.sourceforge.stripes.validation;

import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Test;

public class CreditCardTypeConverterTest {
  @Test
  public void validNumber() {
    CreditCardTypeConverter c = new CreditCardTypeConverter();
    Assert.assertEquals(
        c.convert("4111111111111111", String.class, new ArrayList<ValidationError>()),
        "4111111111111111");
  }

  @Test
  public void invalidNumber() {
    CreditCardTypeConverter c = new CreditCardTypeConverter();
    Assert.assertNull(
        c.convert("4111111111111110", String.class, new ArrayList<ValidationError>()));
  }

  @Test
  public void stripNonNumericCharacters() {
    CreditCardTypeConverter c = new CreditCardTypeConverter();
    Assert.assertEquals(
        c.convert("4111-1111-1111-1111", String.class, new ArrayList<ValidationError>()),
        "4111111111111111");
  }
}
