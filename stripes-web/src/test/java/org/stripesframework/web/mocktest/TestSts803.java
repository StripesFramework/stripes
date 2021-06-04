package org.stripesframework.web.mocktest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.stripesframework.web.FilterEnabledTestBase;
import org.stripesframework.web.action.ActionBean;
import org.stripesframework.web.action.ActionBeanContext;
import org.stripesframework.web.action.DefaultHandler;
import org.stripesframework.web.action.HandlesEvent;
import org.stripesframework.web.action.RedirectResolution;
import org.stripesframework.web.action.Resolution;
import org.stripesframework.web.action.UrlBinding;
import org.stripesframework.web.mock.MockRoundtrip;


public class TestSts803 extends FilterEnabledTestBase {

   @Test
   public void testActionBeanGetsResolved() throws Exception {
      MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), "/teststs803/first/_/edit");
      trip.execute();
      Sts803ActionBean bean = trip.getActionBean(Sts803ActionBean.class);
      assertThat(bean).isNotNull();
   }

   @UrlBinding("/teststs803/{param1}/{param2}/{$event}")
   public static class Sts803ActionBean implements ActionBean {

      private ActionBeanContext context;
      private String            param1;
      private String            param2;

      @HandlesEvent("edit")
      public Resolution edit() {
         return new RedirectResolution(Sts803ActionBean.class).addParameter("param1", param1).addParameter("param2", param2);
      }

      @Override
      public ActionBeanContext getContext() {
         return context;
      }

      public String getParam1() {
         return param1;
      }

      public String getParam2() {
         return param2;
      }

      @Override
      public void setContext( ActionBeanContext context ) {
         this.context = context;
      }

      public void setParam1( String param1 ) {
         this.param1 = param1;
      }

      public void setParam2( String param2 ) {
         this.param2 = param2;
      }

      @DefaultHandler
      public Resolution view() {
         return null;
      }
   }
}
