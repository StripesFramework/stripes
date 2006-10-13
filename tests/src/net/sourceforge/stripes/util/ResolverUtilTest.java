package net.sourceforge.stripes.util;

import net.sourceforge.stripes.validation.BooleanTypeConverter;
import net.sourceforge.stripes.validation.DateTypeConverter;
import net.sourceforge.stripes.validation.LocalizableError;
import net.sourceforge.stripes.validation.ScopedLocalizableError;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.TypeConverter;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Set;

/**
 * Simple test case that tets out the basic functionality of the Resolver Util class.
 *
 * @author Tim Fennell
 */
public class ResolverUtilTest {

    @Test(groups="slow")
    public void testSimpleFind() throws Exception {
        // Because the tests package depends on stripes, it's safe to assume that
        // there will be some TypeConverter subclasses in the classpath
        ResolverUtil<TypeConverter> resolver = new ResolverUtil<TypeConverter>();
        resolver.findImplementations(TypeConverter.class, "net");
        Set<Class<? extends TypeConverter>> impls = resolver.getClasses();

        // Check on a few random converters
        Assert.assertTrue(impls.contains(BooleanTypeConverter.class),
                          "BooleanTypeConverter went missing.");
        Assert.assertTrue(impls.contains(DateTypeConverter.class),
                          "DateTypeConverter went missing.");
        Assert.assertTrue(impls.contains(BooleanTypeConverter.class),
                          "ShortTypeConverter went missing.");

        Assert.assertTrue(impls.size() >= 10,
                          "Did not find all the built in TypeConverters.");
    }

    @Test(groups="fast")
    public void testMoreSpecificFind() throws Exception {
        // Because the tests package depends on stripes, it's safe to assume that
        // there will be some TypeConverter subclasses in the classpath
        ResolverUtil<TypeConverter> resolver = new ResolverUtil<TypeConverter>();
        resolver.findImplementations(TypeConverter.class, "net.sourceforge.stripes.validation");
        Set<Class<? extends TypeConverter>> impls = resolver.getClasses();

        // Check on a few random converters
        Assert.assertTrue(impls.contains(BooleanTypeConverter.class),
                          "BooleanTypeConverter went missing.");
        Assert.assertTrue(impls.contains(DateTypeConverter.class),
                          "DateTypeConverter went missing.");
        Assert.assertTrue(impls.contains(BooleanTypeConverter.class),
                          "ShortTypeConverter went missing.");

        Assert.assertTrue(impls.size() >= 10,
                          "Did not find all the built in TypeConverters.");
    }

    @Test(groups="fast")
    public void testFindExtensionsOfClass() throws Exception {
        ResolverUtil<SimpleError> resolver = new ResolverUtil<SimpleError>();
        resolver.findImplementations(SimpleError.class, "net.sourceforge.stripes");

        Set<Class<? extends SimpleError>> impls = resolver.getClasses();

        Assert.assertTrue(impls.contains(LocalizableError.class),
                          "LocalizableError should have been found.");
        Assert.assertTrue(impls.contains(ScopedLocalizableError.class),
                          "ScopedLocalizableError should have been found.");
        Assert.assertTrue(impls.contains(SimpleError.class),
                          "SimpleError itself should have been found.");
    }

    /** Test interface used with the testFindZeroImplementatios() method. */
    private static interface ZeroImplementations {}

    @Test(groups="fast")
    public void testFindZeroImplementations() throws Exception {
        ResolverUtil<ZeroImplementations> resolver = new ResolverUtil<ZeroImplementations>();
        resolver.findImplementations(ZeroImplementations.class, "net.sourceforge.stripes");

        Set<Class<? extends ZeroImplementations>> impls = resolver.getClasses();

        Assert.assertTrue(impls.size() == 1 && impls.contains(ZeroImplementations.class),
                          "There should not have been any implementations besides the interface itself.");
    }
}
