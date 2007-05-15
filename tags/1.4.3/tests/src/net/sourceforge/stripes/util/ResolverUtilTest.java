package net.sourceforge.stripes.util;

import org.testng.annotations.Test;
import org.testng.Assert;
import net.sourceforge.stripes.validation.TypeConverter;
import net.sourceforge.stripes.validation.BooleanTypeConverter;
import net.sourceforge.stripes.validation.DateTypeConverter;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.LocalizableError;
import net.sourceforge.stripes.validation.ScopedLocalizableError;

import java.util.Set;
import java.util.HashSet;

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
        resolver.loadImplementationsFromContextClassloader(TypeConverter.class);
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
    public void testFindWithFilters() throws Exception {
        // Because the tests package depends on stripes, it's safe to assume that
        // there will be some TypeConverter subclasses in the classpath
        Set<String> pathFilters = new HashSet<String>(), packageFilters = new HashSet<String>();
        pathFilters.add("stripes");
        packageFilters.add("net.sourceforge.stripes.*");
        ResolverUtil<TypeConverter> resolver = new ResolverUtil<TypeConverter>();
        resolver.setLocationFilters(pathFilters);
        resolver.setPackageFilters(packageFilters);
        resolver.loadImplementationsFromContextClassloader(TypeConverter.class);
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
        Set<String> pathFilters = Literal.set("stripes"),
                    packageFilters = Literal.set("net.sourceforge.stripes.*");

        ResolverUtil<SimpleError> resolver = new ResolverUtil<SimpleError>();
        resolver.setLocationFilters(pathFilters);
        resolver.setPackageFilters(packageFilters);
        resolver.loadImplementationsFromContextClassloader(SimpleError.class);

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
        Set<String> pathFilters = Literal.set("stripes"),
                    packageFilters = Literal.set("net.sourceforge.stripes.*");

        ResolverUtil<ZeroImplementations> resolver = new ResolverUtil<ZeroImplementations>();
        resolver.setLocationFilters(pathFilters);
        resolver.setPackageFilters(packageFilters);
        resolver.loadImplementationsFromContextClassloader(ZeroImplementations.class);

        Set<Class<? extends ZeroImplementations>> impls = resolver.getClasses();

        Assert.assertTrue(impls.size() == 0,
                          "There should not have been any implementations.");
    }
}
