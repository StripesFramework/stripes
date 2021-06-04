package org.stripesframework.web.mocktest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.stripesframework.web.FilterEnabledTestBase;
import org.stripesframework.web.action.ActionBean;
import org.stripesframework.web.action.ActionBeanContext;
import org.stripesframework.web.action.DefaultHandler;
import org.stripesframework.web.action.Resolution;
import org.stripesframework.web.action.UrlBinding;
import org.stripesframework.web.mock.MockRoundtrip;


/**
 * Submitted by Nathan Maves and Remi Vankeisbelck to test a specific failure in
 * {@link MockRoundtrip}. Unit test behavior differed when using an {@link ActionBean} class to
 * construct the {@link MockRoundtrip} and when using a string.
 *
 * @author Nathan Maves, Remi Vankeisbelck
 */
@UrlBinding("/foo/{id}/{$event}")
public class TestMockRoundtrip2 extends FilterEnabledTestBase implements ActionBean {

   private final static Integer REF_ID = 2;
   ActionBeanContext context;
   Integer           id;

   @DefaultHandler
   public Resolution bar() {
      return null;
   }

   @Override
   public ActionBeanContext getContext() {
      return context;
   }

   public Integer getId() {
      return id;
   }

   @Override
   public void setContext( ActionBeanContext context ) {
      this.context = context;
   }

   public void setId( Integer id ) {
      this.id = id;
   }

   @Test
   public void testUsingBeanClass() throws Exception {
      executeTest(new MockRoundtrip(getMockServletContext(), getClass()));
   }

   @Test
   public void testUsingUrlWithEventSpecified() throws Exception {
      executeTest(new MockRoundtrip(getMockServletContext(), "/foo/" + REF_ID + "/bar"));
   }

   @Test
   public void testUsingUrlWithoutEventSpecified() throws Exception {
      executeTest(new MockRoundtrip(getMockServletContext(), "/foo/" + REF_ID));
   }

   private void executeTest( MockRoundtrip trip ) throws Exception {
      trip.setParameter("id", REF_ID.toString());
      trip.execute();
      assertThat(trip.getActionBean(getClass()).getId()).isEqualTo(REF_ID);
   }
}