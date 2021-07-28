package org.stripesframework.web.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.stripesframework.web.FilterEnabledTestBase;
import org.stripesframework.web.action.ActionBean;
import org.stripesframework.web.action.ActionBeanContext;
import org.stripesframework.web.action.Before;
import org.stripesframework.web.action.Resolution;
import org.stripesframework.web.mock.MockRoundtrip;
import org.stripesframework.web.testbeans.TestBean;
import org.stripesframework.web.testbeans.TestEnum;
import org.stripesframework.web.validation.Validate;
import org.stripesframework.web.validation.ValidateNestedProperties;


/**
 * Tests all reasonable variations of binding involving Maps. String keys, numeric keys,
 * enum keys, date keys (!).  Maps as terminal values (foo[bar]) and maps in the middle
 * of property chains (foo[bar].splat).
 *
 * @author Tim Fennell
 */
@SuppressWarnings("unused")
public class MapBindingTests extends FilterEnabledTestBase implements ActionBean {

   // Boilerplate ActionBean methods
   private ActionBeanContext    context;
   /** Map of String keys and Long values. */
   private Map<String, Long>    mapStringLong;
   /** Map of Short keys to Integer values. */
   private Map<Short, Integer>  mapShortInteger;
   /** Map of enum (Color) to String values. */
   private Map<Color, String>   mapEnumString;
   /** A TestBean which contains various Maps. */
   private TestBean             testBean;
   /** A map of Enum to TestBean, to test read-through expressions. */
   private Map<Color, TestBean> mapEnumTestBean;
   /** A map of Date to Date. */
   private Map<Date, Date>      mapDateDate;
   /** A map completely lacking in type information!!. */
   @SuppressWarnings("rawtypes")
   private Map                  typelessMap;

   @Test
   public void bindDateKeysInMap() throws Exception {
      MockRoundtrip trip = getRoundtrip();
      trip.getRequest().addLocale(Locale.ENGLISH);
      trip.addParameter("mapDateDate['31-Dec-1999']", "01/01/2000");
      trip.execute();

      Calendar cal = Calendar.getInstance();
      cal.clear();
      cal.set(1999, Calendar.DECEMBER, 31);
      Date key = cal.getTime();

      MapBindingTests bean = trip.getActionBean(MapBindingTests.class);
      assertThat(bean.getMapDateDate()).containsKey(key);
   }

   @Test
   public void bindDoubleQuotedShortKey() throws Exception {
      MockRoundtrip trip = getRoundtrip();
      trip.addParameter("mapShortInteger[\"1\"]", "1");
      trip.addParameter("mapShortInteger[\"200\"]", "2");
      trip.addParameter("mapShortInteger[\"3000\"]", "3");
      trip.execute();

      MapBindingTests bean = trip.getActionBean(MapBindingTests.class);
      assertThat(bean.getMapShortInteger().get((short)1)).isEqualTo(1);
      assertThat(bean.getMapShortInteger().get((short)200)).isEqualTo(2);
      assertThat(bean.getMapShortInteger().get((short)3000)).isEqualTo(3);
   }

   @Test
   public void bindDoubleQuotedStringKey() throws Exception {
      MockRoundtrip trip = getRoundtrip();
      trip.addParameter("mapStringLong[\"one\"]", "1");
      trip.addParameter("mapStringLong[\"two\"]", "2");
      trip.addParameter("mapStringLong[\"three\"]", "3");
      trip.execute();

      MapBindingTests bean = trip.getActionBean(MapBindingTests.class);
      assertThat(bean.getMapStringLong().get("one")).isEqualTo(1L);
      assertThat(bean.getMapStringLong().get("two")).isEqualTo(2L);
      assertThat(bean.getMapStringLong().get("three")).isEqualTo(3L);
   }

   @Test
   public void bindKeyGreaterThanMaxInt() throws Exception {
      MockRoundtrip trip = getRoundtrip();
      trip.addParameter("testBean.longMap[9999999999l]", "1");
      trip.execute();

      MapBindingTests bean = trip.getActionBean(MapBindingTests.class);
      assertThat(bean.getTestBean().getLongMap().get(9999999999L)).isEqualTo(1L);
   }

   @Test
   public void bindKeyGreaterThanMaxIntII() throws Exception {
      MockRoundtrip trip = getRoundtrip();
      trip.addParameter("testBean.longMap['9999999999']", "1");
      trip.execute();

      MapBindingTests bean = trip.getActionBean(MapBindingTests.class);
      assertThat(bean.getTestBean().getLongMap().get(9999999999L)).isEqualTo(1L);
   }

   @Test
   public void bindNestedMap() throws Exception {
      MockRoundtrip trip = getRoundtrip();
      trip.addParameter("testBean.longMap[1]", "1");
      trip.addParameter("testBean.longMap[2]", "2");
      trip.addParameter("testBean.longMap[3]", "3");
      trip.execute();

      MapBindingTests bean = trip.getActionBean(MapBindingTests.class);
      assertThat(bean.getTestBean().getLongMap().get(1L)).isEqualTo(1L);
      assertThat(bean.getTestBean().getLongMap().get(2L)).isEqualTo(2L);
      assertThat(bean.getTestBean().getLongMap().get(3L)).isEqualTo(3L);
   }

   @Test
   public void bindSingleLetterStringKey() throws Exception {
      MockRoundtrip trip = getRoundtrip();
      trip.addParameter("mapStringLong['a']", "1");
      trip.addParameter("mapStringLong['b']", "2");
      trip.addParameter("mapStringLong['c']", "3");
      trip.execute();

      MapBindingTests bean = trip.getActionBean(MapBindingTests.class);
      assertThat(bean.getMapStringLong().get("a")).isEqualTo(1L);
      assertThat(bean.getMapStringLong().get("b")).isEqualTo(2L);
      assertThat(bean.getMapStringLong().get("c")).isEqualTo(3L);
   }

   @Test
   public void bindSingleQuotedEnumKey() throws Exception {
      MockRoundtrip trip = getRoundtrip();
      trip.addParameter("mapEnumString['red']", "Red");
      trip.addParameter("mapEnumString['green']", "Green");
      trip.addParameter("mapEnumString['blue']", "Blue");
      trip.execute();

      MapBindingTests bean = trip.getActionBean(MapBindingTests.class);
      assertThat(bean.getMapEnumString().get(Color.red)).isEqualTo("Red");
      assertThat(bean.getMapEnumString().get(Color.green)).isEqualTo("Green");
      assertThat(bean.getMapEnumString().get(Color.blue)).isEqualTo("Blue");
   }

   @Test
   public void bindSingleQuotedShortKey() throws Exception {
      MockRoundtrip trip = getRoundtrip();
      trip.addParameter("mapShortInteger['1']", "1");
      trip.addParameter("mapShortInteger['200']", "2");
      trip.addParameter("mapShortInteger['3000']", "3");
      trip.execute();

      MapBindingTests bean = trip.getActionBean(MapBindingTests.class);
      assertThat(bean.getMapShortInteger().get((short)1)).isEqualTo(1);
      assertThat(bean.getMapShortInteger().get((short)200)).isEqualTo(2);
      assertThat(bean.getMapShortInteger().get((short)3000)).isEqualTo(3);
   }

   @Test
   public void bindSingleQuotedStringKey() throws Exception {
      MockRoundtrip trip = getRoundtrip();
      trip.addParameter("mapStringLong['one']", "1");
      trip.addParameter("mapStringLong['two']", "2");
      trip.addParameter("mapStringLong['three']", "3");
      trip.execute();

      MapBindingTests bean = trip.getActionBean(MapBindingTests.class);
      assertThat(bean.getMapStringLong().get("one")).isEqualTo(1L);
      assertThat(bean.getMapStringLong().get("two")).isEqualTo(2L);
      assertThat(bean.getMapStringLong().get("three")).isEqualTo(3L);
   }

   @Test
   public void bindThroughTypelessMap() throws Exception {
      MockRoundtrip trip = getRoundtrip();
      trip.addParameter("typelessMap[1].longProperty", "1234");
      trip.addParameter("typelessMap[2l].nestedBean.longProperty", "4321");
      trip.addParameter("typelessMap['foo'].enumProperty", "Sixth");
      trip.execute();

      MapBindingTests bean = trip.getActionBean(MapBindingTests.class);
      assertThat(((TestBean)bean.getTypelessMap().get(1)).getLongProperty()).isEqualTo(1234L);
      assertThat(((TestBean)bean.getTypelessMap().get(2L)).getNestedBean().getLongProperty()).isEqualTo(4321L);
      assertThat(((TestBean)bean.getTypelessMap().get("foo")).getEnumProperty()).isEqualTo(TestEnum.Sixth);
   }

   @Test
   public void bindUnquotedShortKey() throws Exception {
      MockRoundtrip trip = getRoundtrip();
      trip.addParameter("mapShortInteger[1]", "1");
      trip.addParameter("mapShortInteger[200]", "2");
      trip.addParameter("mapShortInteger[3000]", "3");
      trip.execute();

      MapBindingTests bean = trip.getActionBean(MapBindingTests.class);
      assertThat(bean.getMapShortInteger().get((short)1)).isEqualTo(1);
      assertThat(bean.getMapShortInteger().get((short)200)).isEqualTo(2);
      assertThat(bean.getMapShortInteger().get((short)3000)).isEqualTo(3);
   }

   public Resolution doNothing() { return null; }

   @Override
   public ActionBeanContext getContext() { return context; }

   public Map<Date, Date> getMapDateDate() { return mapDateDate; }

   public Map<Color, String> getMapEnumString() { return mapEnumString; }

   public Map<Color, TestBean> getMapEnumTestBean() { return mapEnumTestBean; }

   public Map<Short, Integer> getMapShortInteger() { return mapShortInteger; }

   public Map<String, Long> getMapStringLong() { return mapStringLong; }

   public TestBean getTestBean() { return testBean; }

   @SuppressWarnings("rawtypes")
   public Map getTypelessMap() { return typelessMap; }

   @SuppressWarnings({ "unchecked", "rawtypes" })
   @Before(stages = LifecycleStage.BindingAndValidation)
   public void populateTypelessMap() {
      typelessMap = new HashMap();
      typelessMap.put(1, new TestBean());
      typelessMap.put(2L, new TestBean());
      typelessMap.put("foo", new TestBean());
   }

   @Override
   public void setContext( ActionBeanContext context ) { this.context = context; }

   @Validate
   public void setMapDateDate( Map<Date, Date> mapDateDate ) { this.mapDateDate = mapDateDate; }

   @Validate
   public void setMapEnumString( Map<Color, String> mapEnumString ) { this.mapEnumString = mapEnumString; }

   @ValidateNestedProperties({ //
                               @Validate(field = "longProperty"), //
                               @Validate(field = "intProperty"), //
                             })
   public void setMapEnumTestBean( Map<Color, TestBean> mapEnumTestBean ) { this.mapEnumTestBean = mapEnumTestBean; }

   @Validate
   public void setMapShortInteger( Map<Short, Integer> mapShortInteger ) { this.mapShortInteger = mapShortInteger; }

   @Validate
   public void setMapStringLong( Map<String, Long> mapStringLong ) { this.mapStringLong = mapStringLong; }

   @ValidateNestedProperties({ //
                               @Validate(field = "longMap"), //
                             })
   public void setTestBean( TestBean testBean ) { this.testBean = testBean; }

   @ValidateNestedProperties({ //
                               @Validate(field = "longProperty"), //
                               @Validate(field = "nestedBean.longProperty"), //
                               @Validate(field = "enumProperty"), //
                             })
   @SuppressWarnings("rawtypes")
   public void setTypelessMap( Map typelessMap ) { this.typelessMap = typelessMap; }

   @Test
   public void writeThroughBeanInMap() throws Exception {
      MockRoundtrip trip = getRoundtrip();
      trip.addParameter("mapEnumTestBean['red'].longProperty", "1");
      trip.addParameter("mapEnumTestBean['red'].intProperty", "2");
      trip.addParameter("mapEnumTestBean['green'].longProperty", "3");
      trip.addParameter("mapEnumTestBean['green'].intProperty", "4");
      trip.execute();

      MapBindingTests bean = trip.getActionBean(MapBindingTests.class);
      assertThat(bean.getMapEnumTestBean().get(Color.red)).isNotNull();
      assertThat(bean.getMapEnumTestBean().get(Color.green)).isNotNull();
      assertThat(bean.getMapEnumTestBean().get(Color.red).getLongProperty()).isEqualTo(1L);
      assertThat(bean.getMapEnumTestBean().get(Color.red).getIntProperty()).isEqualTo(2);
      assertThat(bean.getMapEnumTestBean().get(Color.green).getLongProperty()).isEqualTo(3L);
      assertThat(bean.getMapEnumTestBean().get(Color.green).getIntProperty()).isEqualTo(4);
   }

   /** Helper method to create a roundtrip with the TestActionBean class. */
   protected MockRoundtrip getRoundtrip() {
      return new MockRoundtrip(getMockServletContext(), MapBindingTests.class);
   }

   public enum Color {
      red,
      green,
      blue
   }
}
