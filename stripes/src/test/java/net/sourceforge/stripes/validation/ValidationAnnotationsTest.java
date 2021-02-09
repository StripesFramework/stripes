package net.sourceforge.stripes.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.jupiter.api.Test;

import net.sourceforge.stripes.FilterEnabledTestBase;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.extensions.MyDoubleIntegerTypeConverter;
import net.sourceforge.stripes.extensions.MyUppercaseStringTypeConverter;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.util.CryptoUtil;


/**
 * Tests combinations of validation annotations.
 *
 * @author Freddy Daoud
 */
@SuppressWarnings("unused")
public class ValidationAnnotationsTest extends FilterEnabledTestBase implements ActionBean {

   private ActionBeanContext context;
   @Validate(required = true, on = "validateRequiredAndIgnored", ignore = true)
   private String            first;
   @Validate(required = true, on = "validatePublicField")
   public  String            publicField;
   public  Integer           shouldBeDoubled;
   @Validate(converter = IntegerTypeConverter.class)
   public  Integer           shouldNotBeDoubled;
   public  String            shouldBeUpperCased;
   @Validate(converter = StringTypeConverter.class)
   public  String            shouldNotBeUpperCased;
   @Validate(encrypted = true)
   public  String            encryptedParam;

   @Override
   public ActionBeanContext getContext() { return context; }

   public String getFirst() { return first; }

   @Override
   public void setContext( ActionBeanContext context ) { this.context = context;}

   public void setFirst( String first ) { this.first = first; }

   /**
    * Tests that an empty string encrypted value is bound as null.
    *
    * @see http://www.stripesframework.org/jira/browse/STS-521
    */
   @Test
   public void testValidateEncryptedEmptyString() throws Exception {
      MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
      trip.addParameter("encryptedParam", CryptoUtil.encrypt(""));
      trip.execute("validateEncrypted");
      ValidationAnnotationsTest actionBean = trip.getActionBean(getClass());
      assertThat(actionBean.encryptedParam).isNull();
   }

   /**
    * Tests that a validation annotation works on a public field.
    *
    * @see http://www.stripesframework.org/jira/browse/STS-604
    */
   @Test
   public void testValidatePublicField() throws Exception {
      MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
      trip.execute("validatePublicField");
      ActionBean actionBean = trip.getActionBean(getClass());
      assertThat(actionBean.getContext().getValidationErrors()).hasSize(1);
   }

   /**
    * Tests that a required field that is also ignored, should be ignored and should not produce
    * a validation error.
    *
    * @see http://www.stripesframework.org/jira/browse/STS-600
    */
   @Test
   public void testValidateRequiredAndIgnored() throws Exception {
      MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
      trip.execute("validateRequiredAndIgnored");
      ActionBean actionBean = trip.getActionBean(getClass());
      assertThat(actionBean.getContext().getValidationErrors()).isEmpty();
   }

   /**
    * Tests the use of an auto-loaded type converter versus a type converter explicitly configured
    * via {@code @Validate(converter)}, where the auto-loaded type converter does not extend the
    * stock type converter.
    *
    * @see http://www.stripesframework.org/jira/browse/STS-610
    */
   @SuppressWarnings({ "unchecked", "rawtypes" })
   @Test
   public void testValidateTypeConverterDoesNotExtendStock() throws Exception {
      TypeConverterFactory factory = StripesFilter.getConfiguration().getTypeConverterFactory();
      Class<? extends TypeConverter> oldtc = factory.getTypeConverter(//
            String.class, Locale.getDefault()).getClass();
      try {
         MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
         factory.add(String.class, MyUppercaseStringTypeConverter.class);
         trip.addParameter("shouldBeUpperCased", "test");
         trip.addParameter("shouldNotBeUpperCased", "test");
         trip.execute("validateTypeConverters");
         ValidationAnnotationsTest actionBean = trip.getActionBean(getClass());
         assertThat(actionBean.shouldBeUpperCased).isEqualTo("TEST");
         assertThat(actionBean.shouldNotBeUpperCased).isEqualTo("test");
      }
      finally {
         factory.add(String.class, (Class<? extends TypeConverter<?>>)oldtc);
      }
   }

   /**
    * Tests the use of an auto-loaded type converter versus a type converter explicitly configured
    * via {@code @Validate(converter)}, where the auto-loaded type converter extends the stock
    * type converter.
    *
    * @see http://www.stripesframework.org/jira/browse/STS-610
    */
   @Test
   @SuppressWarnings({ "unchecked", "rawtypes" })
   public void testValidateTypeConverterExtendsStock() throws Exception {
      MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), getClass());
      Locale locale = trip.getRequest().getLocale();
      TypeConverterFactory factory = StripesFilter.getConfiguration().getTypeConverterFactory();
      TypeConverter<?> tc = factory.getTypeConverter(Integer.class, locale);
      try {
         factory.add(Integer.class, MyDoubleIntegerTypeConverter.class);
         trip.addParameter("shouldBeDoubled", "42");
         trip.addParameter("shouldNotBeDoubled", "42");
         trip.execute("validateTypeConverters");
         ValidationAnnotationsTest actionBean = trip.getActionBean(getClass());
         assertThat(actionBean.shouldBeDoubled).isEqualTo(84);
         assertThat(actionBean.shouldNotBeDoubled).isEqualTo(42);
      }
      finally {
         Class<? extends TypeConverter> tcType = tc == null ? null : tc.getClass();
         factory.add(Integer.class, (Class<? extends TypeConverter<?>>)tcType);
      }
   }

   public Resolution validateEncrypted() { return null; }

   public Resolution validatePublicField() { return null; }

   public Resolution validateRequiredAndIgnored() { return null; }

   public Resolution validateTypeConverters() { return null; }
}
