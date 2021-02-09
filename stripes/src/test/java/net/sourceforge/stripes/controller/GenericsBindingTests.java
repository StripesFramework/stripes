package net.sourceforge.stripes.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.sourceforge.stripes.StripesTestFixture;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;
import net.sourceforge.stripes.testbeans.TestBean;


/**
 *
 * @author Tim Fennell
 */
public class GenericsBindingTests extends GenericsBindingTestsBaseClass<TestBean, Double, Boolean, Long, Date> implements ActionBean {

   private static MockServletContext ctx;

   @AfterAll
   public static void closeServletContext() {
      ctx.close();
   }

   @BeforeAll
   public static void setupServletContext() {
      ctx = StripesTestFixture.createServletContext();
   }

   // Stuff necessary to implement ActionBean!
   private ActionBeanContext context;

   ///////////////////////////////////////////////////////////////////////////
   // Test and Support Methods
   ///////////////////////////////////////////////////////////////////////////

   @DefaultHandler
   public Resolution execute() { return new RedirectResolution("/somewhere.jsp"); }

   @Override
   public ActionBeanContext getContext() { return context; }

   @Override
   public void setContext( ActionBeanContext context ) { this.context = context; }

   @Test
   public void testGenericBean() throws Exception {
      MockRoundtrip trip = getRoundtrip();
      trip.getRequest().addLocale(Locale.ENGLISH);
      trip.addParameter("genericBean.genericA", "123.4");
      trip.addParameter("genericBean.genericB", "true");
      trip.execute();

      GenericsBindingTests bean = trip.getActionBean(GenericsBindingTests.class);
      assertThat(bean.getGenericBean().getGenericA()).isEqualTo(123.4);
      assertThat(bean.getGenericBean().getGenericB()).isEqualTo(true);
   }

   @Test
   public void testSimpleTypeVariable() throws Exception {
      MockRoundtrip trip = getRoundtrip();
      trip.getRequest().addLocale(Locale.ENGLISH);
      trip.addParameter("number", "123.4");
      trip.execute();

      GenericsBindingTests bean = trip.getActionBean(GenericsBindingTests.class);
      assertThat(bean.getNumber()).isEqualTo(123.4);
   }

   @Test
   public void testTypeVariableLists() throws Exception {
      MockRoundtrip trip = getRoundtrip();
      trip.addParameter("list[0]", "true");
      trip.addParameter("list[1]", "false");
      trip.addParameter("list[2]", "yes");
      trip.execute();

      GenericsBindingTests bean = trip.getActionBean(GenericsBindingTests.class);
      assertThat(bean.getList()).hasSize(3);
      assertThat(bean.getList().get(0)).isEqualTo(true);
      assertThat(bean.getList().get(1)).isEqualTo(false);
      assertThat(bean.getList().get(2)).isEqualTo(true);
   }

   @Test
   public void testTypeVariableMaps() throws Exception {
      MockRoundtrip trip = getRoundtrip();
      trip.addParameter("map[10]", "1/1/2010");
      trip.addParameter("map[20]", "1/1/2020");
      trip.addParameter("map[30]", "1/1/2030");
      trip.execute();

      GenericsBindingTests bean = trip.getActionBean(GenericsBindingTests.class);
      assertThat(bean.getMap()).hasSize(3);
      assertThat(bean.getMap().get(10L)).isEqualTo(makeDate(2010, 1, 1));
      assertThat(bean.getMap().get(20L)).isEqualTo(makeDate(2020, 1, 1));
      assertThat(bean.getMap().get(30L)).isEqualTo(makeDate(2030, 1, 1));
   }

   @Test
   public void testTypeVariableNestedProperties() throws Exception {
      MockRoundtrip trip = getRoundtrip();
      trip.addParameter("bean.longProperty", "1234");
      trip.addParameter("bean.stringProperty", "foobar");
      trip.execute();

      GenericsBindingTests bean = trip.getActionBean(GenericsBindingTests.class);
      assertThat(bean.getBean()).isNotNull();
      assertThat(bean.getBean().getLongProperty()).isEqualTo(1234L);
      assertThat(bean.getBean().getStringProperty()).isEqualTo("foobar");
   }

   /** Makes a roundtrip using the current instances' type. */
   protected MockRoundtrip getRoundtrip() {
      return new MockRoundtrip(ctx, GenericsBindingTests.class);
   }

   /**
    * Helper method to manufacture dates without time components. Months are 1 based unlike
    * the retarded Calendar API that uses 1 based everything else and 0 based months. Sigh.
    */
   private Date makeDate( int year, int month, int day ) {
      Calendar cal = Calendar.getInstance();
      cal.clear();
      cal.set(year, month - 1, day);
      return cal.getTime();
   }
}