package net.sourceforge.stripes.util;

import java.util.Set;
import net.sourceforge.stripes.validation.BooleanTypeConverter;
import net.sourceforge.stripes.validation.DateTypeConverter;
import net.sourceforge.stripes.validation.LocalizableError;
import net.sourceforge.stripes.validation.ScopedLocalizableError;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.TypeConverter;
import org.junit.Assert;
import org.junit.Test;

/**
 * Simple test case that tets out the basic functionality of the Resolver Util class.
 *
 * @author Tim Fennell
 */
public class ResolverUtilTest {

  @Test
  public void testSimpleFind() throws Exception {
    // Because the tests package depends on stripes, it's safe to assume that
    // there will be some TypeConverter subclasses in the classpath
    ResolverUtil<TypeConverter<?>> resolver = new ResolverUtil<TypeConverter<?>>();
    resolver.findImplementations(TypeConverter.class, "net");
    Set<Class<? extends TypeConverter<?>>> impls = resolver.getClasses();

    // Check on a few random converters
    Assert.assertTrue(
        "BooleanTypeConverter went missing.", impls.contains(BooleanTypeConverter.class));
    Assert.assertTrue("DateTypeConverter went missing.", impls.contains(DateTypeConverter.class));
    Assert.assertTrue(
        "ShortTypeConverter went missing.", impls.contains(BooleanTypeConverter.class));
    Assert.assertTrue("Did not find all the built in TypeConverters.", impls.size() >= 10);
  }

  @Test
  public void testMoreSpecificFind() throws Exception {
    // Because the tests package depends on stripes, it's safe to assume that
    // there will be some TypeConverter subclasses in the classpath
    ResolverUtil<TypeConverter<?>> resolver = new ResolverUtil<TypeConverter<?>>();
    resolver.findImplementations(TypeConverter.class, "net.sourceforge.stripes.validation");
    Set<Class<? extends TypeConverter<?>>> impls = resolver.getClasses();

    // Check on a few random converters
    Assert.assertTrue(
        "BooleanTypeConverter went missing.", impls.contains(BooleanTypeConverter.class));
    Assert.assertTrue("DateTypeConverter went missing.", impls.contains(DateTypeConverter.class));
    Assert.assertTrue(
        "ShortTypeConverter went missing.", impls.contains(BooleanTypeConverter.class));
    Assert.assertTrue("Did not find all the built in TypeConverters.", impls.size() >= 10);
  }

  @Test
  public void testFindExtensionsOfClass() throws Exception {
    ResolverUtil<SimpleError> resolver = new ResolverUtil<SimpleError>();
    resolver.findImplementations(SimpleError.class, "net.sourceforge.stripes");

    Set<Class<? extends SimpleError>> impls = resolver.getClasses();

    Assert.assertTrue(
        "LocalizableError should have been found.", impls.contains(LocalizableError.class));
    Assert.assertTrue(
        "ScopedLocalizableError should have been found.",
        impls.contains(ScopedLocalizableError.class));
    Assert.assertTrue(
        "SimpleError itself should have been found.", impls.contains(SimpleError.class));
  }

  /** Test interface used with the testFindZeroImplementations() method. */
  private static interface ZeroImplementations {}

  @Test
  public void testFindZeroImplementations() throws Exception {
    ResolverUtil<ZeroImplementations> resolver = new ResolverUtil<ZeroImplementations>();
    resolver.findImplementations(ZeroImplementations.class, "net.sourceforge.stripes");

    Set<Class<? extends ZeroImplementations>> impls = resolver.getClasses();

    Assert.assertTrue(
        "There should not have been any implementations besides the interface itself.",
        impls.size() == 1 && impls.contains(ZeroImplementations.class));
  }
}
