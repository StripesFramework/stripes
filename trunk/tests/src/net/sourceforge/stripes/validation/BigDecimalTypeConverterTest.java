package net.sourceforge.stripes.validation;

import org.testng.annotations.Test;
import org.testng.Assert;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Unit tests for the BigDecimal class.
 */
public class BigDecimalTypeConverterTest {

    /** Returns an empty collection of validation errors. */
    public Collection<ValidationError> errors() {
        return new ArrayList<ValidationError>();
    }

    @Test(groups="fast")
    public void basicParse() throws Exception {
        TypeConverter<BigDecimal> converter = new BigDecimalTypeConverter();
        converter.setLocale(Locale.US);
        BigDecimal result = converter.convert("12345.67", BigDecimal.class, errors());
        Assert.assertEquals(result, new BigDecimal("12345.67"));
    }

    @Test(groups="fast")
    public void parseBigNumber() throws Exception {
        String number = "7297029872767869231987623498756389734567893246934298765342987563489723497" +
                        ".97982730927907092387409872340987234698750987129872348970982374076283764";
        TypeConverter<BigDecimal> converter = new BigDecimalTypeConverter();
        converter.setLocale(Locale.US);
        BigDecimal result = converter.convert(number, BigDecimal.class, errors());
        Assert.assertEquals(result, new BigDecimal(number));
    }

    @Test(groups="fast")
    public void parseWithGroupingCharacters() throws Exception {
        String number  = "7297029872767869231987623498756389734567876534.2987563489723497";
        String grouped = "7,297,029,872,767,869,231,987,623,498,756,389,734,567,876,534.2987563489723497";
        TypeConverter<BigDecimal> converter = new BigDecimalTypeConverter();
        converter.setLocale(Locale.US);
        BigDecimal result = converter.convert(grouped, BigDecimal.class, errors());
        Assert.assertEquals(result, new BigDecimal(number));
    }

    @Test(groups="fast")
    public void parseAlternateLocale() throws Exception {
        String number    = "123456789.99";
        String localized = "123.456.789,99";
        TypeConverter<BigDecimal> converter = new BigDecimalTypeConverter();
        converter.setLocale(Locale.GERMANY);
        BigDecimal result = converter.convert(localized, BigDecimal.class, errors());
        Assert.assertEquals(result, new BigDecimal(number));
    }

    @Test(groups="fast")
    public void invalidInput() throws Exception {
        String number    = "a1b2vc3d4";
        TypeConverter<BigDecimal> converter = new BigDecimalTypeConverter();
        converter.setLocale(Locale.US);
        Collection<ValidationError> errors = errors();
        @SuppressWarnings("unused")
        BigDecimal result = converter.convert(number, BigDecimal.class, errors);
        Assert.assertEquals(errors.size(), 1);
    }

}
