package net.sourceforge.stripes.localization;

import org.junit.Assert;
import org.junit.Test;

/**
 * Simple test cases for the LocalizationUtility.
 *
 * @author Tim Fennell
 */
public class LocalizationUtilityTest {

  @Test
  public void testBaseCase() throws Exception {
    String input = "Hello";
    String output = LocalizationUtility.makePseudoFriendlyName(input);
    Assert.assertEquals(output, input);
  }

  @Test
  public void testSimpleCase() throws Exception {
    String input = "hello";
    String output = LocalizationUtility.makePseudoFriendlyName(input);
    Assert.assertEquals(output, "Hello");
  }

  @Test
  public void testWithPeriod() throws Exception {
    String input = "bug.name";
    String output = LocalizationUtility.makePseudoFriendlyName(input);
    Assert.assertEquals(output, "Bug Name");
  }

  @Test
  public void testWithStudlyCaps() throws Exception {
    String input = "bugName";
    String output = LocalizationUtility.makePseudoFriendlyName(input);
    Assert.assertEquals(output, "Bug Name");
  }

  @Test
  public void testComplexName() throws Exception {
    String input = "bug.submittedBy.firstName";
    String output = LocalizationUtility.makePseudoFriendlyName(input);
    Assert.assertEquals(output, "Bug Submitted By First Name");
  }

  public static enum TestEnum {
    A,
    B,
    C;
  }

  public static class A {
    public static class B {
      public static class C {}
    }
  }

  @Test
  public void testSimpleClassName() throws Exception {
    String output = LocalizationUtility.getSimpleName(TestEnum.class);
    Assert.assertEquals(output, "LocalizationUtilityTest.TestEnum");

    output = LocalizationUtility.getSimpleName(A.B.C.class);
    Assert.assertEquals(output, "LocalizationUtilityTest.A.B.C");
  }
}
