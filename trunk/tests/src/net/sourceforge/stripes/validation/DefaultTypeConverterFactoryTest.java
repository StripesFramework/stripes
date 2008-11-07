package net.sourceforge.stripes.validation;

import java.util.Locale;

import net.sourceforge.stripes.StripesTestFixture;

import org.testng.Assert;
import org.testng.annotations.Test;

public class DefaultTypeConverterFactoryTest {

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
}
