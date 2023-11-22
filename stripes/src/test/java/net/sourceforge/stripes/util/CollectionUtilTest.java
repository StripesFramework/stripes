package net.sourceforge.stripes.util;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the CollectionUtil class
 *
 * @author Tim Fennell
 */
public class CollectionUtilTest {
  @Test
  public void testEmptyOnNullCollection() {
    Assert.assertTrue(CollectionUtil.empty(null));
  }

  @Test
  public void testEmptyOnCollectionOfNulls() {
    Assert.assertTrue(CollectionUtil.empty(new String[] {null, null, null}));
  }

  @Test
  public void testEmptyZeroLengthCollection() {
    Assert.assertTrue(CollectionUtil.empty(new String[] {}));
  }

  @Test
  public void testEmptyOnCollectionOfEmptyStrings() {
    Assert.assertTrue(CollectionUtil.empty(new String[] {"", null, ""}));
  }

  @Test
  public void testEmptyOnNonEmptyCollection1() {
    Assert.assertFalse(CollectionUtil.empty(new String[] {"", null, "foo"}));
  }

  @Test
  public void testEmptyOnNonEmptyCollection2() {
    Assert.assertFalse(CollectionUtil.empty(new String[] {"bar"}));
  }

  @Test
  public void testEmptyOnNonEmptyCollection3() {
    Assert.assertFalse(CollectionUtil.empty(new String[] {"bar", "splat", "foo"}));
  }

  @Test
  public void testApplies() {
    Assert.assertTrue(CollectionUtil.applies(null, "foo"));
    Assert.assertTrue(CollectionUtil.applies(new String[] {}, "foo"));
    Assert.assertTrue(CollectionUtil.applies(new String[] {"bar", "foo"}, "foo"));
    Assert.assertFalse(CollectionUtil.applies(new String[] {"bar", "f00"}, "foo"));
    Assert.assertFalse(CollectionUtil.applies(new String[] {"!bar", "!foo"}, "foo"));
    Assert.assertTrue(CollectionUtil.applies(new String[] {"!bar", "!f00"}, "foo"));
  }

  @Test
  public void testAsList() {
    List<Object> list = CollectionUtil.asList(new String[] {"foo", "bar"});
    Assert.assertEquals(list.get(0), "foo");
    Assert.assertEquals(list.get(1), "bar");

    list = CollectionUtil.asList(new String[] {});
    Assert.assertEquals(list.size(), 0);

    list = CollectionUtil.asList(new int[] {0, 1, 2, 3});
    Assert.assertEquals(list.get(0), 0);
    Assert.assertEquals(list.get(1), 1);
    Assert.assertEquals(list.get(2), 2);
    Assert.assertEquals(list.get(3), 3);
  }
}
