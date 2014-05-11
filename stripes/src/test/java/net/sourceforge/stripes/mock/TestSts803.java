package net.sourceforge.stripes.mock;


import net.sourceforge.stripes.FilterEnabledTestBase;
import net.sourceforge.stripes.action.*;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;

public class TestSts803 extends FilterEnabledTestBase {

  @UrlBinding("/teststs803/{param1}/{param2}/{$event}")
  public static class Sts803ActionBean implements ActionBean {

    private ActionBeanContext context;
    private String param1;
    private String param2;

    @DefaultHandler
    public Resolution view() {
      return null;
    }

    @HandlesEvent("edit")
    public Resolution edit() {
      return new RedirectResolution(Sts803ActionBean.class)
          .addParameter("param1", param1)
          .addParameter("param2", param2);
    }

    public void setContext(ActionBeanContext context) {
      this.context = context;
    }

    public ActionBeanContext getContext() {
      return context;
    }

    public String getParam1() {
      return param1;
    }

    public void setParam1(String param1) {
      this.param1 = param1;
    }

    public String getParam2() {
      return param2;
    }

    public void setParam2(String param2) {
      this.param2 = param2;
    }
  }

  @Test(groups = "fast")
  public void testActionBeanGetsResolved() throws Exception {
    MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), "/teststs803/first/_/edit");
    trip.execute();
    Sts803ActionBean bean = trip.getActionBean(Sts803ActionBean.class);
    assertNotNull(bean);
  }
}
