package org.stripesframework.web.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.stripesframework.web.FilterEnabledTestBase;
import org.stripesframework.web.action.ActionBean;
import org.stripesframework.web.action.ActionBeanContext;
import org.stripesframework.web.action.DefaultHandler;
import org.stripesframework.web.action.ForwardResolution;
import org.stripesframework.web.action.HandlesEvent;
import org.stripesframework.web.action.Resolution;
import org.stripesframework.web.action.UrlBinding;
import org.stripesframework.web.mock.MockRoundtrip;


/**
 * Tests that make sure the basic functions of the ActionResolver work as expected.
 *
 * @author Tim Fennell
 */
@UrlBinding("/BasicResolverTests.action")
public class BasicResolverTests extends FilterEnabledTestBase implements ActionBean {

   private ActionBeanContext context;
   private int               number;

   @Override
   public ActionBeanContext getContext() { return context; }

   public int getNumber() { return number; }

   @DefaultHandler
   @HandlesEvent("one")
   public Resolution one() {
      number = 1;
      return null;
   }

   public Resolution process() {
      return null;
   }

   @Override
   public void setContext( ActionBeanContext context ) { this.context = context; }

   public void setNumber( int number ) { this.number = number; }

   @Test
   public void testDefaultResolution() throws Exception {
      MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
      trip.execute();

      BasicResolverTests bean = trip.getActionBean(getClass());
      assertThat(bean.getNumber()).isEqualTo(1);
   }

   @Test
   public void testEventNameParameterResolution() throws Exception {
      MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
      trip.addParameter(StripesConstants.URL_KEY_EVENT_NAME, "two");
      trip.execute();

      BasicResolverTests bean = trip.getActionBean(getClass());
      assertThat(bean.getNumber()).isEqualTo(2);
      assertThat(bean.getContext().getEventName()).isEqualTo("two");
   }

   @Test
   public void testImageStyleResolution() throws Exception {
      MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
      trip.execute("two.x");

      BasicResolverTests bean = trip.getActionBean(getClass());
      assertThat(bean.getNumber()).isEqualTo(2);
   }

   @Test
   public void testImageStyleResolution2() throws Exception {
      MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
      trip.addParameter("two.x", "381");
      trip.execute();

      BasicResolverTests bean = trip.getActionBean(getClass());
      assertThat(bean.getNumber()).isEqualTo(2);
   }

   @Test
   public void testNonDefaultResolution() throws Exception {
      MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
      trip.execute("two");

      BasicResolverTests bean = trip.getActionBean(getClass());
      assertThat(bean.getNumber()).isEqualTo(2);
   }

   @Test
   public void testOverrideHandlerMethodReturnsSubtype() throws SecurityException, NoSuchMethodException {
      NameBasedActionResolver resolver = new NameBasedActionResolver();
      Map<String, Method> classMappings = new HashMap<>();
      resolver.processMethods(ExtendedBaseAction.class, classMappings);
   }

   @HandlesEvent("two")
   public Resolution two() {
      number = 2;
      return null;
   }

   public static class ExtendedBaseAction extends BasicResolverTests {

      @Override
      public ForwardResolution process() {
         return null;
      }
   }
}
