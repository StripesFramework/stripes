package org.stripesframework.web.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.MissingResourceException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Tests that ensure that the DateTypeConverter does the right thing given
 * an appropriate input locale.
 *
 * @author Tim Fennell
 */
class DateTypeConverterTest extends TypeConverterTest<DateTypeConverter, Date> {

   // Used to format back to dates for equality checking :)
   private final DateFormat format = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

   @Override
   protected DateTypeConverter createConverter() {
      return new DateTypeConverter() {

         @Override
         protected String getResourceString( final String key ) throws MissingResourceException {
            throw new MissingResourceException("Bundle not available to unit tests.", "", key);
         }
      };
   }

   @BeforeEach
   void setUp() {
      givenLocale(Locale.US);
   }

   @Test
   void testAlternateSeparatorsDates() {
      whenTypeIsConverted("01 31 2007");
      thenResultIs("01/31/2007");

      whenTypeIsConverted("28-Feb-06");
      thenResultIs("02/28/2006");

      whenTypeIsConverted("01-March-07");
      thenResultIs("03/01/2007");
   }

   @Test
   void testBasicUsLocaleDates() {
      whenTypeIsConverted("1/31/07");
      thenResultIs("01/31/2007");

      whenTypeIsConverted("Feb 28, 2006");
      thenResultIs("02/28/2006");

      whenTypeIsConverted("March 1, 2007");
      thenResultIs("03/01/2007");
   }

   @Test
   void testDateToStringFormat() {
      Date now = new Date();

      whenTypeIsConverted(now.toString());
      thenResultIs(format.format(now));
   }

   @Test
   void testNonStandardFormats() {
      whenTypeIsConverted("Jan 31 2007");
      thenResultIs("01/31/2007");

      whenTypeIsConverted("February 28 2006");
      thenResultIs("02/28/2006");

      whenTypeIsConverted("2007-03-01");
      thenResultIs("03/01/2007");
   }

   @Test
   void testPartialInputFormats() {
      whenTypeIsConverted("Jan 31");
      thenResultIs("01/31/" + Calendar.getInstance().get(Calendar.YEAR));

      whenTypeIsConverted("February 28");
      thenResultIs("02/28/" + Calendar.getInstance().get(Calendar.YEAR));

      whenTypeIsConverted("03/01");
      thenResultIs("03/01/" + Calendar.getInstance().get(Calendar.YEAR));
   }

   @Test
   void testUkLocaleDates() {
      givenLocale(Locale.UK);

      whenTypeIsConverted("31 01 2007");
      thenResultIs("01/31/2007");

      whenTypeIsConverted("28/02/2006");
      thenResultIs("02/28/2006");

      whenTypeIsConverted("01 March 2007");
      thenResultIs("03/01/2007");
   }

   @Test
   void testVariantUsLocaleDates() {
      whenTypeIsConverted("01/31/2007");
      thenResultIs("01/31/2007");

      whenTypeIsConverted("28 Feb 06");
      thenResultIs("02/28/2006");

      whenTypeIsConverted("1 March 07");
      thenResultIs("03/01/2007");
   }

   @Test
   void testWhackySeparators() {
      whenTypeIsConverted("01, 31, 2007");
      thenResultIs("01/31/2007");

      whenTypeIsConverted("02--28.2006");
      thenResultIs("02/28/2006");

      whenTypeIsConverted("01//March,./  2007");
      thenResultIs("03/01/2007");
   }

   private void thenResultIs( String expectedUsDateStringRepresentation ) {
      thenValidationSucceeds();
      assertThat(format.format(getResult())).isEqualTo(expectedUsDateStringRepresentation);
   }
}
