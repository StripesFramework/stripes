package net.sourceforge.stripes.util;

import org.testng.annotations.Test;
import org.testng.Assert;

import java.util.List;

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

    @Test(groups="fast")
    public void testApplies() {
        Assert.assertTrue(CollectionUtil.applies(null, "foo"));
        Assert.assertTrue(CollectionUtil.applies(new String[] {}, "foo"));
        Assert.assertTrue(CollectionUtil.applies(new String[] {"bar", "foo"}, "foo"));
        Assert.assertFalse(CollectionUtil.applies(new String[] {"bar", "f00"}, "foo"));
        Assert.assertFalse(CollectionUtil.applies(new String[] {"!bar", "!foo"}, "foo"));
        Assert.assertTrue(CollectionUtil.applies(new String[] {"!bar", "!f00"}, "foo"));
    }

    @Test(groups="fast")
    public void testAsList() {
        List<Object> list = CollectionUtil.asList(new String[] {"foo", "bar"});
        Assert.assertEquals(list.get(0), "foo");
        Assert.assertEquals(list.get(1), "bar");

        list = CollectionUtil.asList(new String[] {});
        Assert.assertEquals(list.size(), 0);

        list = CollectionUtil.asList(new int[] {0,1,2,3});
        Assert.assertEquals(list.get(0), new Integer(0));
        Assert.assertEquals(list.get(1), new Integer(1));
        Assert.assertEquals(list.get(2), new Integer(2));
        Assert.assertEquals(list.get(3), new Integer(3));
    }
}
