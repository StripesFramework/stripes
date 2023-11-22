package net.sourceforge.stripes.mock;

import static org.junit.Assert.assertEquals;

import net.sourceforge.stripes.FilterEnabledTestBase;
import net.sourceforge.stripes.action.*;
import org.junit.Test;

/**
 * Submitted by Nathan Maves and Remi Vankeisbelck to test a specific failure in {@link
 * MockRoundtrip}. Unit test behavior differed when using an {@link ActionBean} class to construct
 * the {@link MockRoundtrip} and when using a string.
 *
 * @author Nathan Maves, Remi Vankeisbelck
 */
@UrlBinding("/foo/{id}/{$event}")
public class TestMockRoundtrip2 extends FilterEnabledTestBase implements ActionBean {
  ActionBeanContext context;
  Integer id;

  @DefaultHandler
  public Resolution bar() {
    return null;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public void setContext(ActionBeanContext context) {
    this.context = context;
  }

  public ActionBeanContext getContext() {
    return this.context;
  }

  private static final Integer REF_ID = 2;

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

  private void executeTest(MockRoundtrip trip) throws Exception {
    trip.setParameter("id", REF_ID.toString());
    trip.execute();
    assertEquals(trip.getActionBean(getClass()).getId(), REF_ID);
  }
}
