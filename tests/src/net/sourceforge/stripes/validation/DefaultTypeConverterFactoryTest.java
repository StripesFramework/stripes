package net.sourceforge.stripes.validation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collection;
import java.util.Locale;

import net.sourceforge.stripes.StripesTestFixture;
import net.sourceforge.stripes.util.Log;

import org.testng.Assert;
import org.testng.annotations.Test;

public class DefaultTypeConverterFactoryTest {
	private static final Log log = Log.getInstance(DefaultTypeConverterFactoryTest.class);

    @SuppressWarnings("unchecked")
	@Test(groups="fast")
    public void testCharTypeConverter() throws Exception{
    	DefaultTypeConverterFactory factory = new DefaultTypeConverterFactory();
    	factory.init(StripesTestFixture.getDefaultConfiguration());
    	
    	TypeConverter typeConverter = factory.getTypeConverter(Character.class, Locale.getDefault());
        Assert.assertEquals(CharacterTypeConverter.class, typeConverter.getClass());

    	typeConverter = factory.getTypeConverter(Character.TYPE, Locale.getDefault());
        Assert.assertEquals(CharacterTypeConverter.class, typeConverter.getClass());
    }

    /*
     * Some tests to make sure we're getting the right type converters.
     */

	@Retention(RetentionPolicy.RUNTIME) public static @interface Ann {}
    public static interface A {}
    public static class B implements A {}
    public static class C extends B {}
    public static class D extends C {}
    @Ann public static class E extends D {}
    @Ann public static class F {}

    public static abstract class BaseTC<T> implements TypeConverter<T> {
        public T convert(String input, Class<? extends T> targetType, Collection<ValidationError> errors) { return null; }
		public void setLocale(Locale locale) {}
    }
    public static class ATC extends BaseTC<A> {}
    public static class DTC extends BaseTC<D> {}
    public static class AnnTC extends BaseTC<Ann> {}

    protected void checkTypeConverter(TypeConverterFactory factory, Class<?> targetType,
            Class<?> expect) throws Exception {
        log.debug("Checking type converter for ", targetType.getSimpleName(), " is ",
                expect == null ? "null" : ATC.class.getSimpleName());
        TypeConverter<?> tc = factory.getTypeConverter(targetType, null);
        if (expect != null) {
            Assert.assertNotNull(tc);
            Assert.assertSame(tc.getClass(), expect);
        }
    }

    @Test(groups = "fast")
    public void testTypeConverters() throws Exception {
        DefaultTypeConverterFactory factory = new DefaultTypeConverterFactory();
        factory.init(StripesTestFixture.getDefaultConfiguration());
        factory.add(A.class, ATC.class);
        factory.add(D.class, DTC.class);
        factory.add(Ann.class, AnnTC.class);

        checkTypeConverter(factory, A.class, ATC.class);
        checkTypeConverter(factory, B.class, null);
        checkTypeConverter(factory, C.class, null);
        checkTypeConverter(factory, D.class, DTC.class);
        checkTypeConverter(factory, E.class, AnnTC.class);
        checkTypeConverter(factory, F.class, AnnTC.class);
    }
}
