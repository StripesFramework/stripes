package org.stripesframework.web.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.stripesframework.web.FilterEnabledTestBase;
import org.stripesframework.web.action.ActionBean;
import org.stripesframework.web.action.ActionBeanContext;
import org.stripesframework.web.action.DefaultHandler;
import org.stripesframework.web.action.Resolution;
import org.stripesframework.web.action.UrlBinding;
import org.stripesframework.web.mock.MockRoundtrip;


/**
 * Unit tests that ensure that the OneToManyTypeConverter works in the context of the
 * regular binding and type conversion process.
 *
 * @author Tim Fennell
 */
@SuppressWarnings("unused")
@UrlBinding("/test/OneToMany.action")
public class OneToManyTypeConverterTest extends FilterEnabledTestBase implements ActionBean {

   private ActionBeanContext context;
   @Validate(converter = OneToManyTypeConverter.class)
   private List<Long>        numbers;
   @Validate(converter = OneToManyTypeConverter.class)
   private List<Date>        dates;

   // Dummy action method
   @DefaultHandler
   public Resolution doNothing() { return null; }

   // Getter/setter methods belows
   @Override
   public ActionBeanContext getContext() { return context; }

   public List<Date> getDates() { return dates; }

   public List<Long> getNumbers() { return numbers; }

   @Override
   public void setContext( ActionBeanContext context ) { this.context = context; }

   public void setDates( List<Date> dates ) { this.dates = dates; }

   public void setNumbers( List<Long> numbers ) { this.numbers = numbers; }

   @Test
   public void testListOfDate() throws Exception {
      MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
      trip.getRequest().addLocale(Locale.ENGLISH);
      trip.addParameter("dates", "12/31/2005, 1/1/2006, 6/15/2008, 7/7/2007");
      trip.execute();
      OneToManyTypeConverterTest bean = trip.getActionBean(getClass());
      List<Date> dates = bean.getDates();

      DateFormat format = new SimpleDateFormat("MM/dd/yyyy");
      assertThat(dates).containsExactly(format.parse("12/31/2005"), format.parse("1/1/2006"), format.parse("6/15/2008"), format.parse("7/7/2007"));
   }

   @Test
   public void testListOfDateWithCommasAndNoSpaces() throws Exception {
      MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
      trip.getRequest().addLocale(Locale.ENGLISH);
      trip.addParameter("dates", "12/31/2005,1/1/2006,6/15/2008,7/7/2007");
      trip.execute();
      OneToManyTypeConverterTest bean = trip.getActionBean(getClass());
      List<Date> dates = bean.getDates();

      DateFormat format = new SimpleDateFormat("MM/dd/yyyy");
      assertThat(dates).containsExactly(format.parse("12/31/2005"), format.parse("1/1/2006"), format.parse("6/15/2008"), format.parse("7/7/2007"));
   }

   @Test
   public void testListOfLong() throws Exception {
      MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
      trip.addParameter("numbers", "123 456 789");
      trip.execute();
      OneToManyTypeConverterTest bean = trip.getActionBean(getClass());
      List<Long> numbers = bean.getNumbers();
      assertThat(numbers).containsExactly(123L, 456L, 789L);
   }

   @Test
   public void testListOfLongWithCommasAndNoSpaces() throws Exception {
      MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
      trip.addParameter("numbers", "123,456,789");
      trip.execute();
      OneToManyTypeConverterTest bean = trip.getActionBean(getClass());
      List<Long> numbers = bean.getNumbers();
      assertThat(numbers).containsExactly(123L, 456L, 789L);
   }

   @Test
   public void testListOfLongWithCommasAndSpaces() throws Exception {
      MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
      trip.addParameter("numbers", "123, 456,789 999");
      trip.execute();
      OneToManyTypeConverterTest bean = trip.getActionBean(getClass());
      List<Long> numbers = bean.getNumbers();
      assertThat(numbers).containsExactly(123L, 456L, 789L, 999L);
   }

   @Test
   public void testWithErrors() throws Exception {
      MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
      trip.addParameter("numbers", "123 456 abc 789 def");
      trip.execute();

      OneToManyTypeConverterTest bean = trip.getActionBean(getClass());
      List<Long> numbers = bean.getNumbers();

      assertThat(numbers).isNull();
      Collection<ValidationError> errors = bean.getContext().getValidationErrors().get("numbers");
      assertThat(errors).describedAs("There should be two errors!").hasSize(2);
   }
}
