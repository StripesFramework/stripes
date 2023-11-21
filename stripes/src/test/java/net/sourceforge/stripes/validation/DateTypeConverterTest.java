package net.sourceforge.stripes.validation;

import org.junit.Test;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.AfterClass;


import java.util.Locale;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;
import java.util.MissingResourceException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Tests that ensure that the DateTypeConverter does the right thing given
 * an appropriate input locale.
 *
 * @author Tim Fennell
 */
public class DateTypeConverterTest {
    // Used to format back to dates for equality checking :)
    private DateFormat format = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

    private DateTypeConverter getConverter(Locale locale) {
        DateTypeConverter converter = new DateTypeConverter(){
            @Override
            protected String getResourceString(final String key) throws MissingResourceException {
                throw new MissingResourceException("Bundle not available to unit tests.", "", key);
            }
        };

        converter.setLocale(locale);
        return converter;
    }

    @Test
    public void testBasicUsLocaleDates() {
        Collection<ValidationError> errors = new ArrayList<ValidationError>();
        DateTypeConverter converter = getConverter(Locale.US);
        Date date = converter.convert("1/31/07", Date.class, errors);
        Assert.assertNotNull(date);
        Assert.assertEquals(0, errors.size());
        Assert.assertEquals(format.format(date), "01/31/2007");

        date = converter.convert("Feb 28, 2006", Date.class, errors);
        Assert.assertNotNull(date);
        Assert.assertEquals(0, errors.size());
        Assert.assertEquals(format.format(date), "02/28/2006");

        date = converter.convert("March 1, 2007", Date.class, errors);
        Assert.assertNotNull(date);
        Assert.assertEquals(0, errors.size());
        Assert.assertEquals(format.format(date), "03/01/2007");
    }

    @Test
    public void testVariantUsLocaleDates() {
        Collection<ValidationError> errors = new ArrayList<ValidationError>();
        DateTypeConverter converter = getConverter(Locale.US);
        Date date = converter.convert("01/31/2007", Date.class, errors);
        Assert.assertNotNull(date);
        Assert.assertEquals(0, errors.size());
        Assert.assertEquals(format.format(date), "01/31/2007");

        date = converter.convert("28 Feb 06", Date.class, errors);
        Assert.assertNotNull(date);
        Assert.assertEquals(0, errors.size());
        Assert.assertEquals(format.format(date), "02/28/2006");

        date = converter.convert("1 March 07", Date.class, errors);
        Assert.assertNotNull(date);
        Assert.assertEquals(0, errors.size());
        Assert.assertEquals(format.format(date), "03/01/2007");
    }

    @Test
    public void testAlternateSeparatorsDates() {
        Collection<ValidationError> errors = new ArrayList<ValidationError>();
        DateTypeConverter converter = getConverter(Locale.US);
        Date date = converter.convert("01 31 2007", Date.class, errors);
        Assert.assertNotNull(date);
        Assert.assertEquals(0, errors.size());
        Assert.assertEquals(format.format(date), "01/31/2007");

        date = converter.convert("28-Feb-06", Date.class, errors);
        Assert.assertNotNull(date);
        Assert.assertEquals(0, errors.size());
        Assert.assertEquals(format.format(date), "02/28/2006");

        date = converter.convert("01-March-07", Date.class, errors);
        Assert.assertNotNull(date);
        Assert.assertEquals(0, errors.size());
        Assert.assertEquals(format.format(date), "03/01/2007");
    }

    @Test
    public void testUkLocaleDates() {
        Collection<ValidationError> errors = new ArrayList<ValidationError>();
        DateTypeConverter converter = getConverter(Locale.UK);
        Date date = converter.convert("31 01 2007", Date.class, errors);
        Assert.assertNotNull(date);
        Assert.assertEquals(0, errors.size());
        Assert.assertEquals(format.format(date), "01/31/2007");

        date = converter.convert("28/02/2006", Date.class, errors);
        Assert.assertNotNull(date);
        Assert.assertEquals(0, errors.size());
        Assert.assertEquals(format.format(date), "02/28/2006");

        date = converter.convert("01 March 2007", Date.class, errors);
        Assert.assertNotNull(date);
        Assert.assertEquals(0, errors.size());
        Assert.assertEquals(format.format(date), "03/01/2007");
    }

    @Test
    public void testWhackySeparators() {
        Collection<ValidationError> errors = new ArrayList<ValidationError>();
        DateTypeConverter converter = getConverter(Locale.US);
        Date date = converter.convert("01, 31, 2007", Date.class, errors);
        Assert.assertNotNull(date);
        Assert.assertEquals(0, errors.size());
        Assert.assertEquals(format.format(date), "01/31/2007");

        date = converter.convert("02--28.2006", Date.class, errors);
        Assert.assertNotNull(date);
        Assert.assertEquals(0, errors.size());
        Assert.assertEquals(format.format(date), "02/28/2006");

        date = converter.convert("01//March,./  2007", Date.class, errors);
        Assert.assertNotNull(date);
        Assert.assertEquals(0, errors.size());
        Assert.assertEquals(format.format(date), "03/01/2007");
    }

    @Test
    public void testNonStandardFormats() {
        Collection<ValidationError> errors = new ArrayList<ValidationError>();
        DateTypeConverter converter = getConverter(Locale.US);
        Date date = converter.convert("Jan 31 2007", Date.class, errors);
        Assert.assertNotNull(date);
        Assert.assertEquals(0, errors.size());
        Assert.assertEquals(format.format(date), "01/31/2007");

        date = converter.convert("February 28 2006", Date.class, errors);
        Assert.assertNotNull(date);
        Assert.assertEquals(0, errors.size());
        Assert.assertEquals(format.format(date), "02/28/2006");

        date = converter.convert("2007-03-01", Date.class, errors);
        Assert.assertNotNull(date);
        Assert.assertEquals(0, errors.size());
        Assert.assertEquals(format.format(date), "03/01/2007");
    }

    @Test
    public void testPartialInputFormats() {
        Collection<ValidationError> errors = new ArrayList<ValidationError>();
        DateTypeConverter converter = getConverter(Locale.US);
        Date date = converter.convert("Jan 31", Date.class, errors);
        Assert.assertNotNull(date);
        Assert.assertEquals(0, errors.size());
        Assert.assertEquals(format.format(date), "01/31/"
                + Calendar.getInstance().get( Calendar.YEAR ));

        date = converter.convert("February 28", Date.class, errors);
        Assert.assertNotNull(date);
        Assert.assertEquals(0, errors.size());
        Assert.assertEquals(format.format(date), "02/28/"
                + Calendar.getInstance().get( Calendar.YEAR ));

        date = converter.convert("03/01", Date.class, errors);
        Assert.assertNotNull(date);
        Assert.assertEquals(0, errors.size());
        Assert.assertEquals(format.format(date), "03/01/"
                + Calendar.getInstance().get( Calendar.YEAR ));
    }

    @Test
    public void testDateToStringFormat() {
        Collection<ValidationError> errors = new ArrayList<ValidationError>();
        DateTypeConverter converter = getConverter(Locale.US);
        Date now = new Date();

        Date date = converter.convert(now.toString(), Date.class, errors);
        Assert.assertNotNull(date);
        Assert.assertEquals(0, errors.size());
        Assert.assertEquals(format.format(date), format.format(now));
    }
}
