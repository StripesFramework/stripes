package org.stripesframework.web.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.jupiter.api.Test;


public class SimpleErrorTest {

   @Test
   void noParams() {
      SimpleError error = new SimpleError("error message");
      assertThat(error.getMessage(Locale.US)).isEqualTo("error message");
   }

   @Test
   void noParams_noEscapingRequired() {
      SimpleError error = new SimpleError("it's an error message");
      assertThat(error.getMessage(Locale.US)).isEqualTo("it's an error message");
   }

   @Test
   void oneParam() {
      // Params 0, 1 are reserved for field name and field value
      SimpleError error = new SimpleError("error message for {2}", "user");
      assertThat(error.getMessage(Locale.US)).isEqualTo("error message for user");
   }

   @Test
   void oneParam_noEscapingRequired() {
      // Params 0, 1 are reserved for field name and field value
      SimpleError error = new SimpleError("it's an error message for {2}", "user");
      assertThat(error.getMessage(Locale.US)).isEqualTo("it's an error message for user");
   }
}
