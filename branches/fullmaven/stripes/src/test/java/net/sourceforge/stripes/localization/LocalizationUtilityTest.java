package net.sourceforge.stripes.localization;

import org.testng.annotations.Test;
import org.testng.Assert;
import net.sourceforge.stripes.localization.LocalizationUtility;

/**
 * Simple test cases for the LocalizationUtility.
 * @author Tim Fennell
 */
public class LocalizationUtilityTest {

    @Test(groups="fast")
    public void testBaseCase() throws Exception {
        String input = "Hello";
        String output = LocalizationUtility.makePseudoFriendlyName(input);
        Assert.assertEquals(output, input);
    }

    @Test(groups="fast")
    public void testSimpleCase() throws Exception {
        String input = "hello";
        String output = LocalizationUtility.makePseudoFriendlyName(input);
        Assert.assertEquals(output, "Hello");
    }

    @Test(groups="fast")
    public void testWithPeriod() throws Exception {
        String input = "bug.name";
        String output = LocalizationUtility.makePseudoFriendlyName(input);
        Assert.assertEquals(output, "Bug Name");
    }

    @Test(groups="fast")
    public void testWithStudlyCaps() throws Exception {
        String input = "bugName";
        String output = LocalizationUtility.makePseudoFriendlyName(input);
        Assert.assertEquals(output, "Bug Name");
    }

    @Test(groups="fast")
    public void testComplexName() throws Exception {
        String input = "bug.submittedBy.firstName";
        String output = LocalizationUtility.makePseudoFriendlyName(input);
        Assert.assertEquals(output, "Bug Submitted By First Name");
    }
}
