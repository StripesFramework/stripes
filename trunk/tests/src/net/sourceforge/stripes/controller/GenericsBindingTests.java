package net.sourceforge.stripes.controller;

import net.sourceforge.stripes.StripesTestFixture;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.test.TestBean;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author Tim Fennell
 */
public class GenericsBindingTests
     extends GenericsBindingTestsBaseClass<TestBean,Double,Boolean,Long,Date>
  implements ActionBean {

    // Stuff necessary to implement ActionBean!
    private ActionBeanContext context;
    public ActionBeanContext getContext() { return context; }
    public void setContext(ActionBeanContext context) { this.context = context; }
    @DefaultHandler public Resolution execute() { return new RedirectResolution("/somewhere.jsp"); }

    ///////////////////////////////////////////////////////////////////////////
    // Test and Support Methods
    ///////////////////////////////////////////////////////////////////////////

    /** Makes a roundtrip using the current instances' type. */
    protected MockRoundtrip getRoundtrip() {
        return new MockRoundtrip(StripesTestFixture.getServletContext(), GenericsBindingTests.class);
    }

    @Test(groups="fast")
    public void testSimpleTypeVariable() throws Exception {
        MockRoundtrip trip = getRoundtrip();
        trip.addParameter("number", "123.4");
        trip.execute();

        GenericsBindingTests bean = trip.getActionBean(GenericsBindingTests.class);
        Assert.assertNotNull(bean.getNumber());
        Assert.assertEquals(bean.getNumber(), new Double(123.4));
    }

    @Test(groups="fast")
    public void testGenericBean() throws Exception {
        MockRoundtrip trip = getRoundtrip();
        trip.addParameter("genericBean.genericA", "123.4");
        trip.addParameter("genericBean.genericB", "true");
        trip.execute();

        GenericsBindingTests bean = trip.getActionBean(GenericsBindingTests.class);
        Assert.assertNotNull(bean.getGenericBean().getGenericA());
        Assert.assertEquals(bean.getGenericBean().getGenericA(), new Double(123.4));
        Assert.assertNotNull(bean.getGenericBean().getGenericB());
        Assert.assertEquals(bean.getGenericBean().getGenericB(), Boolean.TRUE);
    }

    @Test(groups="fast")
    public void testTypeVariableLists() throws Exception {
        MockRoundtrip trip = getRoundtrip();
        trip.addParameter("list[0]", "true");
        trip.addParameter("list[1]", "false");
        trip.addParameter("list[2]", "yes");
        trip.execute();

        GenericsBindingTests bean = trip.getActionBean(GenericsBindingTests.class);
        Assert.assertNotNull(bean.getList());
        Assert.assertEquals(bean.getList().get(0), Boolean.TRUE);
        Assert.assertEquals(bean.getList().get(1), Boolean.FALSE);
        Assert.assertEquals(bean.getList().get(2), Boolean.TRUE);
    }

    @Test(groups="fast")
    public void testTypeVariableMaps() throws Exception {
        MockRoundtrip trip = getRoundtrip();
        trip.addParameter("map[10]", "1/1/2010");
        trip.addParameter("map[20]", "1/1/2020");
        trip.addParameter("map[30]", "1/1/2030");
        trip.execute();

        GenericsBindingTests bean = trip.getActionBean(GenericsBindingTests.class);
        Assert.assertNotNull(bean.getMap());
        Assert.assertEquals(bean.getMap().get(10l), makeDate(2010,1,1));
        Assert.assertEquals(bean.getMap().get(20l), makeDate(2020,1,1));
        Assert.assertEquals(bean.getMap().get(30l), makeDate(2030,1,1));
    }

    @Test(groups="fast")
    public void testTypeVariableNestedProperties() throws Exception {
        MockRoundtrip trip = getRoundtrip();
        trip.addParameter("bean.longProperty", "1234");
        trip.addParameter("bean.stringProperty", "foobar");
        trip.execute();

        GenericsBindingTests bean = trip.getActionBean(GenericsBindingTests.class);
        Assert.assertNotNull(bean.getBean());
        Assert.assertEquals(bean.getBean().getLongProperty(), new Long(1234));
        Assert.assertEquals(bean.getBean().getStringProperty(), "foobar");
    }

    /**
     * Helper method to manufacture dates without time components. Months are 1 based unlike
     * the retarded Calendar API that uses 1 based everything else and 0 based months. Sigh.
     */
    private Date makeDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(year, month-1, day);
        return cal.getTime();
    }
}