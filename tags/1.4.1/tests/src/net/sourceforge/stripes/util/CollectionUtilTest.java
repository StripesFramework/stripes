package net.sourceforge.stripes.util;

import org.testng.annotations.Test;
import org.testng.Assert;

/**
 * Tests for the CollectionUtil class
 *
 * @author Tim Fennell
 */
public class CollectionUtilTest {
    @Test(groups="fast")
    public void testEmptyOnNullCollection() {
        Assert.assertTrue(CollectionUtil.empty(null));
    }

    @Test(groups="fast")
    public void testEmptyOnCollectionOfNulls() {
        Assert.assertTrue(CollectionUtil.empty(new String[] {null, null, null}));
    }

    @Test(groups="fast")
    public void testEmptyZeroLengthCollection() {
        Assert.assertTrue(CollectionUtil.empty(new String[] {}));
    }

    @Test(groups="fast")
    public void testEmptyOnCollectionOfEmptyStrings() {
        Assert.assertTrue(CollectionUtil.empty(new String[] {"", null, ""}));
    }

    @Test(groups="fast")
    public void testEmptyOnNonEmptyCollection1() {
        Assert.assertFalse(CollectionUtil.empty(new String[] {"", null, "foo"}));
    }

    @Test(groups="fast")
    public void testEmptyOnNonEmptyCollection2() {
        Assert.assertFalse(CollectionUtil.empty(new String[] {"bar"}));
    }

    @Test(groups="fast")
    public void testEmptyOnNonEmptyCollection3() {
        Assert.assertFalse(CollectionUtil.empty(new String[] {"bar", "splat", "foo"}));
    }
}
