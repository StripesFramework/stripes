package net.sourceforge.stripes.controller;

import net.sourceforge.stripes.FilterEnabledTestBase;
import net.sourceforge.stripes.StripesTestFixture;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;
import net.sourceforge.stripes.test.TestBean;
import net.sourceforge.stripes.test.TestEnum;
import org.junit.Test;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.AfterClass;


import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Tests all reasonable variations of binding involving Maps. String keys, numeric keys,
 * enum keys, date keys (!).  Maps as terminal values (foo[bar]) and maps in the middle
 * of property chains (foo[bar].splat).
 *
 * @author Tim Fennell
 */
public class MapBindingTests extends FilterEnabledTestBase implements ActionBean {
    public enum Color { red, green, blue }

    // Boilerplate ActionBean methods
    private ActionBeanContext context;
    public void setContext(ActionBeanContext context) { this.context = context; }
    public ActionBeanContext getContext() { return this.context; }
    public Resolution doNothing() { return null; }

    /** Map of String keys and Long values. */
    private Map<String,Long> mapStringLong;
    public Map<String, Long> getMapStringLong() { return mapStringLong; }
    public void setMapStringLong(Map<String, Long> mapStringLong) { this.mapStringLong = mapStringLong; }

    /** Map of Short keys to Integer values. */
    private Map<Short,Integer> mapShortInteger;
    public Map<Short, Integer> getMapShortInteger() { return mapShortInteger; }
    public void setMapShortInteger(Map<Short, Integer> mapShortInteger) { this.mapShortInteger = mapShortInteger; }

    /** Map of enum (Color) to String values. */
    private Map<Color,String> mapEnumString;
    public Map<Color, String> getMapEnumString() { return mapEnumString; }
    public void setMapEnumString(Map<Color, String> mapEnumString) { this.mapEnumString = mapEnumString; }

    /** A TestBean which contains various Maps. */
    private TestBean testBean;
    public TestBean getTestBean() { return testBean; }
    public void setTestBean(TestBean testBean) { this.testBean = testBean; }

    /** A map of Enum to TestBean, to test read-through expressions. */
    private Map<Color,TestBean> mapEnumTestBean;
    public Map<Color, TestBean> getMapEnumTestBean() { return mapEnumTestBean; }
    public void setMapEnumTestBean(Map<Color, TestBean> mapEnumTestBean) { this.mapEnumTestBean = mapEnumTestBean; }

    /** A map of Date to Date. */
    private Map<Date,Date> mapDateDate;
    public Map<Date, Date> getMapDateDate() { return mapDateDate; }
    public void setMapDateDate(Map<Date, Date> mapDateDate) { this.mapDateDate = mapDateDate; }

    /** A map completely lacking in type information!!. */
    @SuppressWarnings("rawtypes")
    private Map typelessMap;
    @SuppressWarnings("rawtypes")
	public Map getTypelessMap() { return typelessMap; }
    @SuppressWarnings("rawtypes")
	public void setTypelessMap(Map typelessMap) { this.typelessMap = typelessMap; }


    /** Helper method to create a roundtrip with the TestActionBean class. */
    protected MockRoundtrip getRoundtrip() {
        return new MockRoundtrip(getMockServletContext(), MapBindingTests.class);
    }

    @Test
    public void bindSingleQuotedStringKey() throws Exception {
        MockRoundtrip trip = getRoundtrip();
        trip.addParameter("mapStringLong['one']", "1");
        trip.addParameter("mapStringLong['two']", "2");
        trip.addParameter("mapStringLong['three']", "3");
        trip.execute();

        MapBindingTests bean = trip.getActionBean(MapBindingTests.class);
        Assert.assertEquals(bean.getMapStringLong().get("one"), Long.valueOf(1L));
        Assert.assertEquals(bean.getMapStringLong().get("two"), Long.valueOf(2L));
        Assert.assertEquals(bean.getMapStringLong().get("three"), Long.valueOf(3L));
    }

    @Test
    public void bindDoubleQuotedStringKey() throws Exception {
        MockRoundtrip trip = getRoundtrip();
        trip.addParameter("mapStringLong[\"one\"]", "1");
        trip.addParameter("mapStringLong[\"two\"]", "2");
        trip.addParameter("mapStringLong[\"three\"]", "3");
        trip.execute();

        MapBindingTests bean = trip.getActionBean(MapBindingTests.class);
        Assert.assertEquals(bean.getMapStringLong().get("one"), Long.valueOf(1L));
        Assert.assertEquals(bean.getMapStringLong().get("two"), Long.valueOf(2L));
        Assert.assertEquals(bean.getMapStringLong().get("three"), Long.valueOf(3L));
    }

    @Test
    public void bindSingleLetterStringKey() throws Exception {
        MockRoundtrip trip = getRoundtrip();
        trip.addParameter("mapStringLong['a']", "1");
        trip.addParameter("mapStringLong['b']", "2");
        trip.addParameter("mapStringLong['c']", "3");
        trip.execute();

        MapBindingTests bean = trip.getActionBean(MapBindingTests.class);
        Assert.assertEquals(bean.getMapStringLong().get("a"), Long.valueOf(1L));
        Assert.assertEquals(bean.getMapStringLong().get("b"), Long.valueOf(2L));
        Assert.assertEquals(bean.getMapStringLong().get("c"), Long.valueOf(3L));
    }

    @Test
    public void bindUnquotedShortKey() throws Exception {
        MockRoundtrip trip = getRoundtrip();
        trip.addParameter("mapShortInteger[1]",    "1");
        trip.addParameter("mapShortInteger[200]",  "2");
        trip.addParameter("mapShortInteger[3000]", "3");
        trip.execute();

        MapBindingTests bean = trip.getActionBean(MapBindingTests.class);
        Assert.assertEquals(bean.getMapShortInteger().get( (short) 1 ), Integer.valueOf(1));
        Assert.assertEquals(bean.getMapShortInteger().get( (short) 200), Integer.valueOf(2));
        Assert.assertEquals(bean.getMapShortInteger().get( (short) 3000), Integer.valueOf(3));
    }

    @Test
    public void bindSingleQuotedShortKey() throws Exception {
        MockRoundtrip trip = getRoundtrip();
        trip.addParameter("mapShortInteger['1']",    "1");
        trip.addParameter("mapShortInteger['200']",  "2");
        trip.addParameter("mapShortInteger['3000']", "3");
        trip.execute();

        MapBindingTests bean = trip.getActionBean(MapBindingTests.class);
        Assert.assertEquals(bean.getMapShortInteger().get( (short) 1 ), Integer.valueOf(1));
        Assert.assertEquals(bean.getMapShortInteger().get( (short) 200), Integer.valueOf(2));
        Assert.assertEquals(bean.getMapShortInteger().get( (short) 3000), Integer.valueOf(3));
    }

    @Test
    public void bindDoubleQuotedShortKey() throws Exception {
        MockRoundtrip trip = getRoundtrip();
        trip.addParameter("mapShortInteger[\"1\"]",    "1");
        trip.addParameter("mapShortInteger[\"200\"]",  "2");
        trip.addParameter("mapShortInteger[\"3000\"]", "3");
        trip.execute();

        MapBindingTests bean = trip.getActionBean(MapBindingTests.class);
        Assert.assertEquals(bean.getMapShortInteger().get( (short) 1 ), Integer.valueOf(1));
        Assert.assertEquals(bean.getMapShortInteger().get( (short) 200), Integer.valueOf(2));
        Assert.assertEquals(bean.getMapShortInteger().get( (short) 3000), Integer.valueOf(3));
    }

    @Test
    public void bindSingleQuotedEnumKey() throws Exception {
        MockRoundtrip trip = getRoundtrip();
        trip.addParameter("mapEnumString['red']",    "Red");
        trip.addParameter("mapEnumString['green']",  "Green");
        trip.addParameter("mapEnumString['blue']",   "Blue");
        trip.execute();

        MapBindingTests bean = trip.getActionBean(MapBindingTests.class);
        Assert.assertEquals(bean.getMapEnumString().get(Color.red), "Red");
        Assert.assertEquals(bean.getMapEnumString().get(Color.green), "Green");
        Assert.assertEquals(bean.getMapEnumString().get(Color.blue), "Blue");
    }

    @Test
    public void bindNestedMap() throws Exception {
        MockRoundtrip trip = getRoundtrip();
        trip.addParameter("testBean.longMap[1]", "1");
        trip.addParameter("testBean.longMap[2]", "2");
        trip.addParameter("testBean.longMap[3]", "3");
        trip.execute();

        MapBindingTests bean = trip.getActionBean(MapBindingTests.class);
        Assert.assertEquals(bean.getTestBean().getLongMap().get(1l), Long.valueOf(1L));
        Assert.assertEquals(bean.getTestBean().getLongMap().get(2l), Long.valueOf(2L));
        Assert.assertEquals(bean.getTestBean().getLongMap().get(3l), Long.valueOf(3L));
    }

    @Test
    public void bindKeyGreaterThanMaxInt() throws Exception {
        MockRoundtrip trip = getRoundtrip();
        trip.addParameter("testBean.longMap[9999999999l]", "1");
        trip.execute();

        MapBindingTests bean = trip.getActionBean(MapBindingTests.class);
        Assert.assertEquals(bean.getTestBean().getLongMap().get(9999999999l), Long.valueOf(1L));
    }

    @Test
    public void bindKeyGreaterThanMaxIntII() throws Exception {
        MockRoundtrip trip = getRoundtrip();
        trip.addParameter("testBean.longMap['9999999999']", "1");
        trip.execute();

        MapBindingTests bean = trip.getActionBean(MapBindingTests.class);
        Assert.assertEquals(bean.getTestBean().getLongMap().get(9999999999l), Long.valueOf(1L));
    }

    @Test
    public void writeThroughBeanInMap() throws Exception {
        MockRoundtrip trip = getRoundtrip();
        trip.addParameter("mapEnumTestBean['red'].longProperty", "1");
        trip.addParameter("mapEnumTestBean['red'].intProperty", "2");
        trip.addParameter("mapEnumTestBean['green'].longProperty", "3");
        trip.addParameter("mapEnumTestBean['green'].intProperty", "4");
        trip.execute();

        MapBindingTests bean = trip.getActionBean(MapBindingTests.class);
        Assert.assertNotNull(bean.getMapEnumTestBean().get(Color.red));
        Assert.assertNotNull(bean.getMapEnumTestBean().get(Color.green));
        Assert.assertEquals(bean.getMapEnumTestBean().get(Color.red).getLongProperty(), Long.valueOf(1L));
        Assert.assertEquals(Integer.valueOf(bean.getMapEnumTestBean().get(Color.red).getIntProperty()), Integer.valueOf(2));
        Assert.assertEquals(bean.getMapEnumTestBean().get(Color.green).getLongProperty(), Long.valueOf(3L));
        Assert.assertEquals(Integer.valueOf(bean.getMapEnumTestBean().get(Color.green).getIntProperty()), Integer.valueOf(4));
    }

    @Test
    public void bindDateKeysInMap() throws Exception {
        MockRoundtrip trip = getRoundtrip();
        trip.getRequest().addLocale(Locale.ENGLISH);
        trip.addParameter("mapDateDate['31-Dec-1999']", "01/01/2000");
        trip.execute();

        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(1999,Calendar.DECEMBER,31);
        Date key = cal.getTime();

        MapBindingTests bean = trip.getActionBean(MapBindingTests.class);
        Assert.assertNotNull(bean.getMapDateDate().get(key));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Before(stages=LifecycleStage.BindingAndValidation)
    public void populateTypelessMap() {
        this.typelessMap = new HashMap();
        this.typelessMap.put(1, new TestBean());
        this.typelessMap.put(2l, new TestBean());
        this.typelessMap.put("foo", new TestBean());
    }

    @Test
    public void bindThroughTypelessMap() throws Exception {
        MockRoundtrip trip = getRoundtrip();
        trip.addParameter("typelessMap[1].longProperty", "1234");
        trip.addParameter("typelessMap[2l].nestedBean.longProperty", "4321");
        trip.addParameter("typelessMap['foo'].enumProperty", "Sixth");
        trip.execute();

        MapBindingTests bean = trip.getActionBean(MapBindingTests.class);
        Assert.assertEquals( ((TestBean) bean.getTypelessMap().get(1)).getLongProperty(), Long.valueOf(1234L));
        Assert.assertEquals( ((TestBean) bean.getTypelessMap().get(2l)).getNestedBean().getLongProperty(), Long.valueOf(4321L));
        Assert.assertEquals( ((TestBean) bean.getTypelessMap().get("foo")).getEnumProperty(), TestEnum.Sixth);
    }
}
