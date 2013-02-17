package net.sourceforge.stripes.validation;

import java.util.Locale;

import net.sourceforge.stripes.FilterEnabledTestBase;
import net.sourceforge.stripes.StripesTestFixture;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.extensions.MyIntegerTypeConverter;
import net.sourceforge.stripes.extensions.MyStringTypeConverter;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;
import net.sourceforge.stripes.util.CryptoUtil;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests combinations of validation annotations.
 *
 * @author Freddy Daoud
 */
public class ValidationAnnotationsTest extends FilterEnabledTestBase implements ActionBean {
    private ActionBeanContext context;
    public ActionBeanContext getContext() { return context; }
    public void setContext(ActionBeanContext context) { this.context = context;}

    @Validate(required=true, on="validateRequiredAndIgnored", ignore=true)
    private String first;
    public String getFirst() { return first; }
    public void setFirst(String first) { this.first = first; }

    public Resolution validateRequiredAndIgnored() { return null; }

    /**
     * Tests that a required field that is also ignored, should be ignored and should not produce
     * a validation error.
     *
     * @see http://www.stripesframework.org/jira/browse/STS-600
     */
    @Test(groups="fast")
    public void testValidateRequiredAndIgnored() throws Exception {
        MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
        trip.execute("validateRequiredAndIgnored");
        ActionBean actionBean = trip.getActionBean(getClass());
        Assert.assertEquals(actionBean.getContext().getValidationErrors().size(), 0);
    }

    @Validate(required=true, on="validatePublicField")
    public String publicField;

    public Resolution validatePublicField() { return null; }

    /**
     * Tests that a validation annotation works on a public field.
     *
     * @see http://www.stripesframework.org/jira/browse/STS-604
     */
    @Test(groups="fast")
    public void testValidatePublicField() throws Exception {
        MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
        trip.execute("validatePublicField");
        ActionBean actionBean = trip.getActionBean(getClass());
        Assert.assertEquals(actionBean.getContext().getValidationErrors().size(), 1);
    }

    public Integer shouldBeDoubled;
    @Validate(converter=IntegerTypeConverter.class) public Integer shouldNotBeDoubled;

    public String shouldBeUpperCased;
    @Validate(converter=StringTypeConverter.class) public String shouldNotBeUpperCased;

    public Resolution validateTypeConverters() { return null; }

    /**
     * Tests the use of an auto-loaded type converter versus a type converter explicitly configured
     * via {@code @Validate(converter)}, where the auto-loaded type converter extends the stock
     * type converter.
     *
     * @see http://www.stripesframework.org/jira/browse/STS-610
     */
    @Test(groups="extensions")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testValidateTypeConverterExtendsStock() throws Exception {
        MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
        Locale locale = trip.getRequest().getLocale();
        TypeConverterFactory factory = StripesFilter.getConfiguration().getTypeConverterFactory();
        TypeConverter<?> tc = factory.getTypeConverter(Integer.class, locale);
        try {
            factory.add(Integer.class, MyIntegerTypeConverter.class);
            trip.addParameter("shouldBeDoubled", "42");
            trip.addParameter("shouldNotBeDoubled", "42");
            trip.execute("validateTypeConverters");
            ValidationAnnotationsTest actionBean = trip.getActionBean(getClass());
            Assert.assertEquals(actionBean.shouldBeDoubled, new Integer(84));
            Assert.assertEquals(actionBean.shouldNotBeDoubled, new Integer(42));
        }
        finally {
            Class<? extends TypeConverter> tcType = tc == null ? null : tc.getClass();
            factory.add(Integer.class, (Class<? extends TypeConverter<?>>) tcType);
        }
    }

    /**
     * Tests the use of an auto-loaded type converter versus a type converter explicitly configured
     * via {@code @Validate(converter)}, where the auto-loaded type converter does not extend the
     * stock type converter.
     *
     * @see http://www.stripesframework.org/jira/browse/STS-610
     */
    @SuppressWarnings("unchecked")
    @Test(groups="extensions")
    public void testValidateTypeConverterDoesNotExtendStock() throws Exception {
        TypeConverterFactory factory = StripesFilter.getConfiguration().getTypeConverterFactory();
        Class<? extends TypeConverter> oldtc = factory.getTypeConverter(//
                String.class, Locale.getDefault()).getClass();
        try {
            MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
            factory.add(String.class, MyStringTypeConverter.class);
            trip.addParameter("shouldBeUpperCased", "test");
            trip.addParameter("shouldNotBeUpperCased", "test");
            trip.execute("validateTypeConverters");
            ValidationAnnotationsTest actionBean = trip.getActionBean(getClass());
            Assert.assertEquals(actionBean.shouldBeUpperCased, "TEST");
            Assert.assertEquals(actionBean.shouldNotBeUpperCased, "test");
        }
        finally {
            factory.add(String.class, (Class<? extends TypeConverter<?>>) oldtc);
        }
    }

    @Validate(encrypted=true)
    public String encryptedParam;

    public Resolution validateEncrypted() { return null; }

    /**
     * Tests that an empty string encrypted value is bound as null.
     *
     * @see http://www.stripesframework.org/jira/browse/STS-521
     */
    @Test(groups="fast")
    public void testValidateEncryptedEmptyString() throws Exception {
        MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
        trip.addParameter("encryptedParam", CryptoUtil.encrypt(""));
        trip.execute("validateEncrypted");
        ValidationAnnotationsTest actionBean = trip.getActionBean(getClass());
        Assert.assertNull(actionBean.encryptedParam);
    }
}
