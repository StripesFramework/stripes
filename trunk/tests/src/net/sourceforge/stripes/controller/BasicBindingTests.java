package net.sourceforge.stripes.controller;

import org.testng.annotations.Test;
import org.testng.Assert;
import net.sourceforge.stripes.mock.MockServletContext;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.StripesTestFixture;
import net.sourceforge.stripes.test.TestActionBean;
import net.sourceforge.stripes.test.TestBean;
import net.sourceforge.stripes.test.TestEnum;

/**
 * Tests out a lot of basic binding functionality in Stripes.  Ensures that scalars and lists
 * and sets of various things can be properly bound.  Does not test validation errors etc.
 *
 * @author Tim Fennell
 */
public class BasicBindingTests {

    /** Helper method to create a roundtrip with the TestActionBean class. */
    protected MockRoundtrip getRoundtrip() {
        MockServletContext context = StripesTestFixture.getServletContext();
        return new MockRoundtrip(context, TestActionBean.class);
    }

    @Test(groups="fast")
    public void basicBinding() throws Exception {
        MockRoundtrip trip = getRoundtrip();
        trip.addParameter("singleString", "testValue");
        trip.addParameter("singleLong", "12345");
        trip.execute();

        TestActionBean bean = trip.getActionBean(TestActionBean.class);
        Assert.assertEquals(bean.getSingleString(), "testValue");
        Assert.assertEquals(bean.getSingleLong(), new Long(12345L));
    }

    @Test(groups="fast")
    public void bindSetsOfStrings() throws Exception {
        MockRoundtrip trip = getRoundtrip();
        trip.addParameter("setOfStrings", "testValue", "testValue", "testValue2", "testValue3");
        trip.execute();

        TestActionBean bean = trip.getActionBean(TestActionBean.class);
        Assert.assertTrue(bean.getSetOfStrings().contains("testValue"));
        Assert.assertTrue(bean.getSetOfStrings().contains("testValue2"));
        Assert.assertTrue(bean.getSetOfStrings().contains("testValue3"));
        Assert.assertEquals(bean.getSetOfStrings().size(), 3);
    }

    @Test(groups="fast")
    public void bindListOfLongs() throws Exception {
        MockRoundtrip trip = getRoundtrip();
        trip.addParameter("listOfLongs", "1", "2", "3", "456");
        trip.execute();

        TestActionBean bean = trip.getActionBean(TestActionBean.class);
        Assert.assertTrue(bean.getListOfLongs().contains(1L));
        Assert.assertTrue(bean.getListOfLongs().contains(2L));
        Assert.assertTrue(bean.getListOfLongs().contains(3L));
        Assert.assertTrue(bean.getListOfLongs().contains(456L));
        Assert.assertEquals(bean.getListOfLongs().size(), 4);
    }

    @Test(groups="fast")
    public void bindNonGenericListOfLongs() throws Exception {
        MockRoundtrip trip = getRoundtrip();
        trip.addParameter("nakedListOfLongs", "10", "20", "30", "4567");
        trip.execute();

        TestActionBean bean = trip.getActionBean(TestActionBean.class);
        Assert.assertTrue(bean.getNakedListOfLongs().contains(10L));
        Assert.assertTrue(bean.getNakedListOfLongs().contains(20L));
        Assert.assertTrue(bean.getNakedListOfLongs().contains(30L));
        Assert.assertTrue(bean.getNakedListOfLongs().contains(4567L));
        Assert.assertEquals(bean.getNakedListOfLongs().size(), 4);
    }

    @Test(groups="fast")
    public void bindNestedProperties() throws Exception {
        MockRoundtrip trip = getRoundtrip();
        trip.addParameter("testBean.intProperty", "10");
        trip.addParameter("testBean.longProperty", "20");
        trip.addParameter("testBean.booleanProperty", "true");
        trip.addParameter("testBean.enumProperty", "Third");
        trip.execute();

        TestBean bean = trip.getActionBean(TestActionBean.class).getTestBean();
        Assert.assertNotNull(bean);
        Assert.assertEquals(bean.getIntProperty(),  10);
        Assert.assertEquals(bean.getLongProperty(), new Long(20));
        Assert.assertEquals(bean.isBooleanProperty(), true);
        Assert.assertEquals(bean.getEnumProperty(), TestEnum.Third);
    }

    @Test(groups="fast")
    public void bindNestedSet() throws Exception {
        MockRoundtrip trip = getRoundtrip();
        trip.addParameter("testBean.stringSet", "foo", "bar", "splat");
        trip.execute();

        TestBean bean = trip.getActionBean(TestActionBean.class).getTestBean();
        Assert.assertNotNull(bean);
        Assert.assertNotNull(bean.getStringSet());
        Assert.assertEquals(bean.getStringSet().size(), 3);
        Assert.assertTrue(bean.getStringSet().contains("foo"));
        Assert.assertTrue(bean.getStringSet().contains("bar"));
        Assert.assertTrue(bean.getStringSet().contains("splat"));
    }

    @Test(groups="fast")
    public void bindNumericallyIndexedProperties() throws Exception {
        MockRoundtrip trip = getRoundtrip();
        trip.addParameter("listOfBeans[0].intProperty", "0");
        trip.addParameter("listOfBeans[3].intProperty", "30");
        trip.addParameter("listOfBeans[2].intProperty", "20");
        trip.addParameter("listOfBeans[1].intProperty", "10");
        trip.addParameter("listOfBeans[4].intProperty", "40");
        trip.execute();

        TestActionBean bean = trip.getActionBean(TestActionBean.class);
        Assert.assertEquals(bean.getListOfBeans().get(0).getIntProperty(), 00);
        Assert.assertEquals(bean.getListOfBeans().get(1).getIntProperty(), 10);
        Assert.assertEquals(bean.getListOfBeans().get(2).getIntProperty(), 20);
        Assert.assertEquals(bean.getListOfBeans().get(3).getIntProperty(), 30);
        Assert.assertEquals(bean.getListOfBeans().get(4).getIntProperty(), 40);
    }

    @Test(groups="fast")
    public void bindStringIndexedProperties() throws Exception {
        MockRoundtrip trip = getRoundtrip();
        trip.addParameter("mapOfLongs['one']", "1");
        trip.addParameter("mapOfLongs['twentyseven']", "27");
        trip.addParameter("mapOfLongs['nine']", "9");
        trip.execute();

        TestActionBean bean = trip.getActionBean(TestActionBean.class);
        Assert.assertEquals(bean.getMapOfLongs().get("one"), new Long(1));
        Assert.assertEquals(bean.getMapOfLongs().get("twentyseven"), new Long(27));
        Assert.assertEquals(bean.getMapOfLongs().get("nine"), new Long(9));
    }

    @Test(groups="fast")
    public void bindStringIndexedPropertiesII() throws Exception {
        MockRoundtrip trip = getRoundtrip();
        trip.addParameter("mapOfObjects['foo']", "bar");
        trip.addParameter("mapOfObjects['cat']", "meow");
        trip.addParameter("mapOfObjects['dog']", "woof");
        trip.addParameter("mapOfObjects['snake']", "ssss");
        trip.execute();

        TestActionBean bean = trip.getActionBean(TestActionBean.class);
        Assert.assertEquals(bean.getMapOfObjects().get("foo"),   "bar");
        Assert.assertEquals(bean.getMapOfObjects().get("cat"),   "meow");
        Assert.assertEquals(bean.getMapOfObjects().get("dog"),   "woof");
        Assert.assertEquals(bean.getMapOfObjects().get("snake"), "ssss");
    }
}
