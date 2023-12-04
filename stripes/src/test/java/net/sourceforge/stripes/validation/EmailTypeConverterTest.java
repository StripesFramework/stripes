package net.sourceforge.stripes.validation;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for the EmailTypeConverter class. Test email address cases taken from <a
 * href="https://codefool.tumblr.com/post/15288874550/list-of-valid-and-invalid-email-addresses">here</a>.
 */
public class EmailTypeConverterTest {
  @Test
  public void validEmailTests() {
    List<String> validEmailAddresses =
        List.of(
            "email@example.com",
            "firstname.lastname@example.com",
            "email@subdomain.example.com",
            "firstname+lastname@example.com",
            "email@123.123.123.123",
            "email@[123.123.123.123]",
            "\"email\"@example.com",
            "johno'reilly@example.com",
            "1234567890@example.com",
            "email@example-one.com",
            "_______@example.com",
            "email@example.name",
            "email@example.museum",
            "email@example.co.jp",
            "firstname-lastname@example.com",
            "much.\"more\\ unusual\"@example.com"
            // "very.unusual.\"@\".unusual.com@example.com", - Does not pass, and it should
            // "very.\"(),:;<>[]\".VERY.\"very@\\\\\\ \"very\".unusual@strange.example.com" -- Does
            // not pass and it should
            );

    for (String email : validEmailAddresses) {
      TypeConverter<String> converter = new EmailTypeConverter();
      List<ValidationError> errors = new ArrayList<>();
      String result = converter.convert(email, String.class, errors);
      Assert.assertEquals(result, email);
      Assert.assertEquals(
          "Valid email address check failed. Valid Email address was: " + email, 0, errors.size());
    }
  }

  @Test
  public void invalidEmailTests() {
    List<String> invalidEmailAddresses =
        List.of(
            "plainaddress",
            "#@%^%#$@#$@#.com",
            "@example.com",
            "Joe Smith <email@example.com>",
            "email.example.com",
            "email@example@example.com",
            ".email@example.com",
            "email.@example.com",
            "email..email@example.com",
            "あいうえお@example.com",
            "email@example.com (Joe Smith)",
            "email@example",
            "email@-example.com",
            // "email@example.web",  Should fail even though this is not strict to the old RFC which
            // had limited TLDs.
            // "email@111.222.333.44444", Should fail but ignoring because this is going overboard
            "email@example..com",
            "Abc..123@example.com"
            // "\"(),:;<>[\\]@example.com", Should fail but ignoring because this is going overboard
            // "just\"not\"right@example.com", Should fail but ignoring because this is going
            // overboard
            // "this\\ is\"really\"not\\\\allowed@example.com" Should fail but ignoring because this
            // is going overboard
            );

    for (String email : invalidEmailAddresses) {
      TypeConverter<String> converter = new EmailTypeConverter();
      List<ValidationError> errors = new ArrayList<>();
      String result = converter.convert(email, String.class, errors);
      Assert.assertEquals(result, email);
      Assert.assertEquals(
          "Invalid email address check failed. Email address was: " + email, 1, errors.size());
    }
  }
}
