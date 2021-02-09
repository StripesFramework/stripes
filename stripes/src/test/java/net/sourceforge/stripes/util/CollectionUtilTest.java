package net.sourceforge.stripes.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;


/**
 * Tests for the CollectionUtil class
 *
 * @author Tim Fennell
 */
public class CollectionUtilTest {

   @Test
   public void testApplies() {
      assertThat(CollectionUtil.applies(null, "foo")).isTrue();
      assertThat(CollectionUtil.applies(new String[] {}, "foo")).isTrue();
      assertThat(CollectionUtil.applies(new String[] { "bar", "foo" }, "foo")).isTrue();
      assertThat(CollectionUtil.applies(new String[] { "bar", "f00" }, "foo")).isFalse();
      assertThat(CollectionUtil.applies(new String[] { "!bar", "!foo" }, "foo")).isFalse();
      assertThat(CollectionUtil.applies(new String[] { "!bar", "!f00" }, "foo")).isTrue();
   }

   @Test
   public void testAsList() {
      List<Object> list = CollectionUtil.asList(new String[] { "foo", "bar" });
      assertThat(list.get(0)).isEqualTo("foo");
      assertThat(list.get(1)).isEqualTo("bar");

      list = CollectionUtil.asList(new String[] {});
      assertThat(list).isEmpty();

      list = CollectionUtil.asList(new int[] { 0, 1, 2, 3 });
      assertThat(list.get(0)).isEqualTo(0);
      assertThat(list.get(1)).isEqualTo(1);
      assertThat(list.get(2)).isEqualTo(2);
      assertThat(list.get(3)).isEqualTo(3);
   }

   @Test
   public void testEmptyOnCollectionOfEmptyStrings() {
      assertThat(CollectionUtil.empty(new String[] { "", null, "" })).isTrue();
   }

   @Test
   public void testEmptyOnCollectionOfNulls() {
      assertThat(CollectionUtil.empty(new String[] { null, null, null })).isTrue();
   }

   @Test
   public void testEmptyOnNonEmptyCollection1() {
      assertThat(CollectionUtil.empty(new String[] { "", null, "foo" })).isFalse();
   }

   @Test
   public void testEmptyOnNonEmptyCollection2() {
      assertThat(CollectionUtil.empty(new String[] { "bar" })).isFalse();
   }

   @Test
   public void testEmptyOnNonEmptyCollection3() {
      assertThat(CollectionUtil.empty(new String[] { "bar", "splat", "foo" })).isFalse();
   }

   @SuppressWarnings("ConstantConditions")
   @Test
   public void testEmptyOnNullCollection() {
      assertThat(CollectionUtil.empty(null)).isTrue();
   }

   @Test
   public void testEmptyZeroLengthCollection() {
      assertThat(CollectionUtil.empty(new String[] {})).isTrue();
   }
}
