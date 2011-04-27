package net.sourceforge.stripes.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for the PercentageTypeConverter class.
 */
public class PercentageTypeConverterTest {

    /** Returns an empty collection of validation errors. */
    public Collection<ValidationError> errors() {
        return new ArrayList<ValidationError>();
    }

    /** Returns the type converter being tested. */
    public TypeConverter<Number> getConverter() {
        TypeConverter<Number> converter = new PercentageTypeConverter();
        converter.setLocale(Locale.US);
        return converter;
    }

    @Test(groups = "fast")
    public void parseBasic() throws Exception {
        Number result = getConverter().convert("80%", Float.class, errors());
        Assert.assertEquals(result, new Float(0.8f));
    }

    @Test(groups = "fast")
    public void parseSpaceBeforePercentSign() throws Exception {
        Number result = getConverter().convert("80 %", Float.class, errors());
        Assert.assertEquals(result, new Float(0.8f));
    }

    @Test(groups = "fast")
    public void parseWithoutPercentSign() throws Exception {
        Number result = getConverter().convert("80", Float.class, errors());
        Assert.assertEquals(result, new Float(0.8f));
    }

    @Test(groups = "fast")
    public void parseNegative() throws Exception {
        Number result = getConverter().convert("-80%", Float.class, errors());
        Assert.assertEquals(result, new Float(-0.8f));
    }

    @Test(groups = "fast")
    public void parseNegativeSpaceBeforePercentSign() throws Exception {
        Number result = getConverter().convert("-80 %", Float.class, errors());
        Assert.assertEquals(result, new Float(-0.8f));
    }

    @Test(groups = "fast")
    public void parseNegativeWithoutPercentSign() throws Exception {
        Number result = getConverter().convert("-80", Float.class, errors());
        Assert.assertEquals(result, new Float(-0.8f));
    }

    @Test(groups = "fast")
    public void parseParentheses() throws Exception {
        Number result = getConverter().convert("(80%)", Float.class, errors());
        Assert.assertEquals(result, new Float(-0.8f));
    }

    @Test(groups = "fast")
    public void parseParenthesesSpaceBeforePercentSign() throws Exception {
        Number result = getConverter().convert("(80 %)", Float.class, errors());
        Assert.assertEquals(result, new Float(-0.8f));
    }

    @Test(groups = "fast")
    public void parseParenthesesWithoutPercentSign() throws Exception {
        Number result = getConverter().convert("(80)", Float.class, errors());
        Assert.assertEquals(result, new Float(-0.8f));
    }

    @Test(groups = "fast")
    public void parseBasicDouble() throws Exception {
        Number result = getConverter().convert("0.8%", Double.class, errors());
        Assert.assertEquals(result, new Double(0.008));
    }

    @Test(groups = "fast")
    public void parseSpaceBeforePercentSignDouble() throws Exception {
        Number result = getConverter().convert("0.8 %", Double.class, errors());
        Assert.assertEquals(result, new Double(0.008));
    }

    @Test(groups = "fast")
    public void parseWithoutPercentSignDouble() throws Exception {
        Number result = getConverter().convert("0.8", Double.class, errors());
        Assert.assertEquals(result, new Double(0.008));
    }

    @Test(groups = "fast")
    public void parseNegativeDouble() throws Exception {
        Number result = getConverter().convert("-0.8%", Double.class, errors());
        Assert.assertEquals(result, new Double(-0.008));
    }

    @Test(groups = "fast")
    public void parseNegativeSpaceBeforePercentSignDouble() throws Exception {
        Number result = getConverter().convert("-0.8 %", Double.class, errors());
        Assert.assertEquals(result, new Double(-0.008));
    }

    @Test(groups = "fast")
    public void parseNegativeWithoutPercentSignDouble() throws Exception {
        Number result = getConverter().convert("-0.8", Double.class, errors());
        Assert.assertEquals(result, new Double(-0.008));
    }

    @Test(groups = "fast")
    public void parseParenthesesDouble() throws Exception {
        Number result = getConverter().convert("(0.8%)", Double.class, errors());
        Assert.assertEquals(result, new Double(-0.008));
    }

    @Test(groups = "fast")
    public void parseParenthesesSpaceBeforePercentSignDouble() throws Exception {
        Number result = getConverter().convert("(0.8 %)", Double.class, errors());
        Assert.assertEquals(result, new Double(-0.008));
    }

    @Test(groups = "fast")
    public void parseParenthesesWithoutPercentSignDouble() throws Exception {
        Number result = getConverter().convert("(0.8)", Double.class, errors());
        Assert.assertEquals(result, new Double(-0.008));
    }
}
