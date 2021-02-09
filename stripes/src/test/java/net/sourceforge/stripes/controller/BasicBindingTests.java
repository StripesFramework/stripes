package net.sourceforge.stripes.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import net.sourceforge.stripes.FilterEnabledTestBase;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.testbeans.TestActionBean;
import net.sourceforge.stripes.testbeans.TestBean;
import net.sourceforge.stripes.testbeans.TestEnum;


/**
 * Tests out a lot of basic binding functionality in Stripes.  Ensures that scalars and lists
 * and sets of various things can be properly bound.  Does not test validation errors etc.
 *
 * @author Tim Fennell
 */
public class BasicBindingTests extends FilterEnabledTestBase {

   @Test
   public void attemptToBindIntoActionBeanContext() throws Exception {
      // Should be able to set it just fine
      MockRoundtrip trip = getRoundtrip();
      trip.addParameter("context.eventName", "woohaa!");
      trip.addParameter(" context.eventName", "woohaa!");
      trip.execute();

      TestActionBean bean = trip.getActionBean(TestActionBean.class);
      ActionBeanContext context = bean.getContext();
      assertThat(context.getEventName()).isNotEqualTo("woohaa!");
   }

   @Test
   public void attemptToBindIntoActionBeanContextII() throws Exception {
      // Should be able to set it just fine
      MockRoundtrip trip = getRoundtrip();
      trip.addParameter("Context.eventName", "woohaa!");
      trip.addParameter(" Context.eventName", "woohaa!");
      trip.execute();

      TestActionBean bean = trip.getActionBean(TestActionBean.class);
      ActionBeanContext context = bean.getContext();
      assertThat(context.getEventName()).isNotEqualTo("woohaa!");
   }

   @Test
   public void basicBinding() throws Exception {
      MockRoundtrip trip = getRoundtrip();
      trip.addParameter("singleString", "testValue");
      trip.addParameter("singleLong", "12345");
      trip.execute();

      TestActionBean bean = trip.getActionBean(TestActionBean.class);
      assertThat(bean.getSingleString()).isEqualTo("testValue");
      assertThat(bean.getSingleLong()).isEqualTo(12345L);
   }

   @Test
   public void bindArrayOfEnums() throws Exception {
      // Should be able to set it just fine
      MockRoundtrip trip = getRoundtrip();
      trip.addParameter("colors", "Red", "Green", "Blue");
      trip.execute();

      TestActionBean bean = trip.getActionBean(TestActionBean.class);
      TestActionBean.Color[] colors = bean.getColors();
      assertThat(colors).isNotNull();
      assertThat(colors).hasSize(3);
      assertThat(colors[0]).isEqualTo(TestActionBean.Color.Red);
      assertThat(colors[1]).isEqualTo(TestActionBean.Color.Green);
      assertThat(colors[2]).isEqualTo(TestActionBean.Color.Blue);
   }

   @Test
   public void bindIntArray() throws Exception {
      MockRoundtrip trip = getRoundtrip();
      trip.addParameter("intArray", "100", "200", "30017");
      trip.execute();

      TestActionBean bean = trip.getActionBean(TestActionBean.class);
      assertThat(bean.getIntArray()).containsExactly(100, 200, 30017);
   }

   @Test
   public void bindListOfLongs() throws Exception {
      MockRoundtrip trip = getRoundtrip();
      trip.addParameter("listOfLongs", "1", "2", "3", "456");
      trip.execute();

      TestActionBean bean = trip.getActionBean(TestActionBean.class);
      assertThat(bean.getListOfLongs()).containsExactly(1L, 2L, 3L, 456L);
   }

   @Test
   public void bindNestedProperties() throws Exception {
      MockRoundtrip trip = getRoundtrip();
      trip.addParameter("testBean.intProperty", "10");
      trip.addParameter("testBean.longProperty", "20");
      trip.addParameter("testBean.booleanProperty", "true");
      trip.addParameter("testBean.enumProperty", "Third");
      trip.execute();

      TestBean bean = trip.getActionBean(TestActionBean.class).getTestBean();
      assertThat(bean).isNotNull();
      assertThat(bean.getIntProperty()).isEqualTo(10);
      assertThat(bean.getLongProperty()).isEqualTo(20L);
      assertThat(bean.isBooleanProperty()).isTrue();
      assertThat(bean.getEnumProperty()).isEqualTo(TestEnum.Third);
   }

   @Test
   public void bindNestedSet() throws Exception {
      MockRoundtrip trip = getRoundtrip();
      trip.addParameter("testBean.stringSet", "foo", "bar", "splat");
      trip.execute();

      TestBean bean = trip.getActionBean(TestActionBean.class).getTestBean();
      assertThat(bean).isNotNull();
      assertThat(bean.getStringSet()).isNotNull();
      assertThat(bean.getStringSet()).containsExactlyInAnyOrder("foo", "bar", "splat");
   }

   @Test
   public void bindNonExistentProperty() throws Exception {
      // Should get logged but otherwise ignored...not blow up
      MockRoundtrip trip = getRoundtrip();
      trip.addParameter("foobarsplatNotProperty", "100");
      trip.execute();

      TestActionBean bean = trip.getActionBean(TestActionBean.class);
      assertThat(bean).isNotNull();
   }

   @SuppressWarnings("unchecked")
   @Test
   public void bindNonGenericListOfLongs() throws Exception {
      MockRoundtrip trip = getRoundtrip();
      trip.addParameter("nakedListOfLongs", "10", "20", "30", "4567");
      trip.execute();

      TestActionBean bean = trip.getActionBean(TestActionBean.class);
      assertThat(bean.getNakedListOfLongs()).containsExactly(10L, 20L, 30L, 4567L);
   }

   @Test
   public void bindNumericallyIndexedProperties() throws Exception {
      MockRoundtrip trip = getRoundtrip();
      trip.addParameter("listOfBeans[0].intProperty", "0");
      trip.addParameter("listOfBeans[3].intProperty", "30");
      trip.addParameter("listOfBeans[2].intProperty", "20");
      trip.addParameter("listOfBeans[1].intProperty", "10");
      trip.addParameter("listOfBeans[4].intProperty", "40");
      trip.execute();

      TestActionBean bean = trip.getActionBean(TestActionBean.class);
      assertThat(bean.getListOfBeans().get(0).getIntProperty()).isEqualTo(0);
      assertThat(bean.getListOfBeans().get(1).getIntProperty()).isEqualTo(10);
      assertThat(bean.getListOfBeans().get(2).getIntProperty()).isEqualTo(20);
      assertThat(bean.getListOfBeans().get(3).getIntProperty()).isEqualTo(30);
      assertThat(bean.getListOfBeans().get(4).getIntProperty()).isEqualTo(40);
   }

   @Test
   public void bindPropertyWithoutGetterMethod() throws Exception {
      // Should be able to set it just fine
      MockRoundtrip trip = getRoundtrip();
      trip.addParameter("setOnlyString", "whee");
      trip.execute();

      TestActionBean bean = trip.getActionBean(TestActionBean.class);
      assertThat(bean.setOnlyStringIsNotNull()).isTrue();
   }

   @Test
   public void bindPublicPropertyWithoutMethods() throws Exception {
      // Should be able to set it just fine
      MockRoundtrip trip = getRoundtrip();
      trip.addParameter("publicLong", "12345");
      trip.execute();

      TestActionBean bean = trip.getActionBean(TestActionBean.class);
      assertThat(bean.publicLong).isEqualTo(12345L);
   }

   @Test
   public void bindSetsOfStrings() throws Exception {
      MockRoundtrip trip = getRoundtrip();
      trip.addParameter("setOfStrings", "testValue", "testValue", "testValue2", "testValue3");
      trip.execute();

      TestActionBean bean = trip.getActionBean(TestActionBean.class);
      assertThat(bean.getSetOfStrings()).containsExactlyInAnyOrder("testValue", "testValue2", "testValue3");
   }

   @Test
   public void bindStringIndexedProperties() throws Exception {
      MockRoundtrip trip = getRoundtrip();
      trip.addParameter("mapOfLongs['one']", "1");
      trip.addParameter("mapOfLongs['twentyseven']", "27");
      trip.addParameter("mapOfLongs['nine']", "9");
      trip.execute();

      TestActionBean bean = trip.getActionBean(TestActionBean.class);
      assertThat(bean.getMapOfLongs().get("one")).isEqualTo(1L);
      assertThat(bean.getMapOfLongs().get("twentyseven")).isEqualTo(27L);
      assertThat(bean.getMapOfLongs().get("nine")).isEqualTo(9L);
   }

   @Test
   public void bindStringIndexedPropertiesII() throws Exception {
      MockRoundtrip trip = getRoundtrip();
      trip.addParameter("mapOfObjects['foo']", "bar");
      trip.addParameter("mapOfObjects['cat']", "meow");
      trip.addParameter("mapOfObjects['dog']", "woof");
      trip.addParameter("mapOfObjects['snake']", "ssss");
      trip.execute();

      TestActionBean bean = trip.getActionBean(TestActionBean.class);
      assertThat(bean.getMapOfObjects().get("foo")).isEqualTo("bar");
      assertThat(bean.getMapOfObjects().get("cat")).isEqualTo("meow");
      assertThat(bean.getMapOfObjects().get("dog")).isEqualTo("woof");
      assertThat(bean.getMapOfObjects().get("snake")).isEqualTo("ssss");
   }

   @Test
   public void testBindingToSubclassOfDeclaredType() throws Exception {
      MockRoundtrip trip = getRoundtrip();
      trip.addParameter("item.id", "1000000");
      trip.execute();

      TestActionBean bean = trip.getActionBean(TestActionBean.class);
      TestActionBean.PropertyLess item = bean.getItem();
      assertThat(item.getClass()).isEqualTo(TestActionBean.Item.class);
      assertThat(((TestActionBean.Item)item).getId()).isEqualTo(1000000L);
   }

   /** Helper method to create a roundtrip with the TestActionBean class. */
   protected MockRoundtrip getRoundtrip() {
      return new MockRoundtrip(getMockServletContext(), TestActionBean.class);
   }
}
