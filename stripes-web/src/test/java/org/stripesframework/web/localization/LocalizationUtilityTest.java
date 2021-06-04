package org.stripesframework.web.localization;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;


/**
 * Simple test cases for the LocalizationUtility.
 * @author Tim Fennell
 */
public class LocalizationUtilityTest {

   @Test
   public void testBaseCase() throws Exception {
      String input = "Hello";
      String output = LocalizationUtility.makePseudoFriendlyName(input);
      assertThat(output).isEqualTo(input);
   }

   @Test
   public void testComplexName() throws Exception {
      String input = "bug.submittedBy.firstName";
      String output = LocalizationUtility.makePseudoFriendlyName(input);
      assertThat(output).isEqualTo("Bug Submitted By First Name");
   }

   @Test
   public void testSimpleCase() throws Exception {
      String input = "hello";
      String output = LocalizationUtility.makePseudoFriendlyName(input);
      assertThat(output).isEqualTo("Hello");
   }

   @Test
   public void testSimpleClassName() throws Exception {
      String output = LocalizationUtility.getSimpleName(TestEnum.class);
      assertThat(output).isEqualTo("LocalizationUtilityTest.TestEnum");

      output = LocalizationUtility.getSimpleName(A.B.C.class);
      assertThat(output).isEqualTo("LocalizationUtilityTest.A.B.C");
   }

   @Test
   public void testWithPeriod() throws Exception {
      String input = "bug.name";
      String output = LocalizationUtility.makePseudoFriendlyName(input);
      assertThat(output).isEqualTo("Bug Name");
   }

   @Test
   public void testWithStudlyCaps() throws Exception {
      String input = "bugName";
      String output = LocalizationUtility.makePseudoFriendlyName(input);
      assertThat(output).isEqualTo("Bug Name");
   }

   public enum TestEnum {
      A,
      B,
      C
   }


   public static class A {

      public static class B {

         public static class C {}
      }
   }
}
