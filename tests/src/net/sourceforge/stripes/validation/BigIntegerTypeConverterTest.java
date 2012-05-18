package net.sourceforge.stripes.validation;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

/**
 * Unit tests for the BigInteger class.
 */
public class BigIntegerTypeConverterTest {

    /** Returns an empty collection of validation errors. */
    public Collection<ValidationError> errors() {
        return new ArrayList<ValidationError>();
    }

    @Test(groups="fast")
    public void basicParse() throws Exception {
        TypeConverter<BigInteger> converter = new BigIntegerTypeConverter();
        converter.setLocale(Locale.US);
        BigInteger result = converter.convert("1234567", BigInteger.class, errors());
        Assert.assertEquals(result, new BigInteger("1234567"));
    }

    @Test(groups="fast")
    public void parseBigNumber() throws Exception {
        String number = String.valueOf(Long.MAX_VALUE) + "8729839871981298798234";
        TypeConverter<BigInteger> converter = new BigIntegerTypeConverter();
        converter.setLocale(Locale.US);
        BigInteger result = converter.convert(number, BigInteger.class, errors());
        Assert.assertEquals(result, new BigInteger(number));
        Assert.assertEquals(result.toString(), number);
    }

    @Test(groups="fast")
    public void parseWithGroupingCharacters() throws Exception {
        String number  = "7297029872767869231987623498756389734567876534";
        String grouped = "7,297,029,872,767,869,231,987,623,498,756,389,734,567,876,534";
        TypeConverter<BigInteger> converter = new BigIntegerTypeConverter();
        converter.setLocale(Locale.US);
        BigInteger result = converter.convert(grouped, BigInteger.class, errors());
        Assert.assertEquals(result, new BigInteger(number));
    }

    @Test(groups="fast")
    public void parseAlternateLocale() throws Exception {
        String number    = "123456789";
        String localized = "123.456.789";
        TypeConverter<BigInteger> converter = new BigIntegerTypeConverter();
        converter.setLocale(Locale.GERMANY);
        BigInteger result = converter.convert(localized, BigInteger.class, errors());
        Assert.assertEquals(result, new BigInteger(number));
    }

    @Test(groups="fast")
    public void decimalTruncation() throws Exception {
        TypeConverter<BigInteger> converter = new BigIntegerTypeConverter();
        converter.setLocale(Locale.US);
        BigInteger result = converter.convert("123456789.98765", BigInteger.class, errors());
        Assert.assertEquals(result, new BigInteger("123456789"));
        Assert.assertEquals(result.toString(), "123456789");
    }

    @Test(groups="fast")
    public void invalidInput() throws Exception {
        String number    = "a1b2vc3d4";
        TypeConverter<BigInteger> converter = new BigIntegerTypeConverter();
        converter.setLocale(Locale.US);
        Collection<ValidationError> errors = errors();
        @SuppressWarnings("unused")
        BigInteger result = converter.convert(number, BigInteger.class, errors);
        Assert.assertEquals(errors.size(), 1);
    }

}
